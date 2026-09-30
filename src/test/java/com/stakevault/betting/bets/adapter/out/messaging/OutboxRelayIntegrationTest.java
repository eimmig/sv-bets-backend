package com.stakevault.betting.bets.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import com.stakevault.betting.bets.TestcontainersConfiguration;
import com.stakevault.betting.bets.domain.port.in.ProvisionTenantSchemaUseCase;
import com.stakevault.betting.bets.support.TenantSchemaIntegrationSupport;

class OutboxRelayIntegrationTest extends TenantSchemaIntegrationSupport {

	private final OutboxRelay relay;
	private final OutboxEventSpringDataRepository repository;
	private final RabbitTemplate rabbitTemplate;

	OutboxRelayIntegrationTest(ProvisionTenantSchemaUseCase provisionTenantSchema, JdbcTemplate jdbcTemplate,
			OutboxRelay relay, OutboxEventSpringDataRepository repository, RabbitTemplate rabbitTemplate) {
		super(provisionTenantSchema, jdbcTemplate);
		this.relay = relay;
		this.repository = repository;
		this.rabbitTemplate = rabbitTemplate;
	}

	@BeforeEach
	void emptyOutboxAndQueue() {
		jdbcTemplate.update("DELETE FROM public.outbox_event");
		rabbitTemplate.execute(channel -> channel.queuePurge(TestcontainersConfiguration.TEST_QUEUE));
	}

	@AfterEach
	void clearOutbox() {
		jdbcTemplate.update("DELETE FROM public.outbox_event");
	}

	private void store(String routingKey, String payload) {
		repository.save(new OutboxEventJpaEntity(routingKey, payload, tenantSlug, UUID.randomUUID(), Instant.now()));
	}

	private int pendingRows() {
		Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM public.outbox_event", Integer.class);
		return count == null ? 0 : count;
	}

	private List<Message> receiveAll() {
		List<Message> messages = new ArrayList<>();
		Message message;
		while ((message = rabbitTemplate.receive(TestcontainersConfiguration.TEST_QUEUE, 1500)) != null) {
			messages.add(message);
		}
		return messages;
	}

	@Test
	void shouldDoNothingWhenTheOutboxIsEmpty() {
		assertThat(relay.drain()).isZero();
	}

	@Test
	void shouldDeliverPendingRowsInOrderAndDeleteThemAfterTheBrokerConfirms() {
		store("bet.created", "{\"n\":1}");
		store("bet.settled", "{\"n\":2}");
		store("bet.created", "{\"n\":3}");

		int sent = relay.drain();

		assertThat(sent).isEqualTo(3);
		assertThat(pendingRows()).isZero();
		List<Message> messages = receiveAll();
		assertThat(messages).extracting(m -> new String(m.getBody()))
				.containsExactly("{\"n\":1}", "{\"n\":2}", "{\"n\":3}");
		assertThat(messages).allSatisfy(m -> {
			assertThat(m.getMessageProperties().getContentType()).isEqualTo("application/json");
			assertThat(m.getMessageProperties().getReceivedDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
		});
	}

	@Test
	void shouldKeepTheRowWhenTheBrokerReturnsTheMessageAsUnroutable() {
		store("no.binding.matches", "{\"n\":1}");

		int sent = relay.drain();

		assertThat(sent).isZero();
		assertThat(pendingRows()).isEqualTo(1);
		assertThat(receiveAll()).isEmpty();
	}

	@Test
	void shouldDeliverAgainOnTheNextRunOnceTheRowBecomesRoutable() {
		store("no.binding.matches", "{\"n\":1}");
		relay.drain();
		jdbcTemplate.update("UPDATE public.outbox_event SET routing_key = 'bet.created'");

		int sent = relay.drain();

		assertThat(sent).isEqualTo(1);
		assertThat(pendingRows()).isZero();
		assertThat(receiveAll()).hasSize(1);
	}

	@Test
	void shouldNotDuplicateOrLoseRowsWhenTwoRelaysDrainConcurrently() throws Exception {
		int rows = 1200;
		for (int i = 0; i < rows; i++) {
			store("bet.created", "{\"n\":" + i + "}");
		}
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Integer>> futures = new ArrayList<>();
			for (int i = 0; i < 2; i++) {
				futures.add(executor.submit(() -> {
					start.await();
					return relay.drain();
				}));
			}
			start.countDown();
			int sent = 0;
			for (Future<Integer> future : futures) {
				sent += future.get();
			}

			assertThat(sent).isEqualTo(rows);
		} finally {
			executor.shutdownNow();
		}

		assertThat(pendingRows()).isZero();
		List<String> bodies = receiveAll().stream().map(message -> new String(message.getBody())).toList();
		assertThat(bodies).hasSize(rows);
		assertThat(new HashSet<>(bodies)).hasSize(rows);
	}
}
