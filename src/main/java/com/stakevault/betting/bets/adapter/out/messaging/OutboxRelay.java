package com.stakevault.betting.bets.adapter.out.messaging;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class OutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

	private static final String EXCHANGE = "bets.events";
	private static final int BATCH_SIZE = 500;
	private static final int MAX_BATCHES_PER_RUN = 20;
	private static final long CONFIRM_TIMEOUT_MILLIS = 10_000;
	private static final long BACKOFF_NANOS = TimeUnit.SECONDS.toNanos(5);

	private final OutboxEventSpringDataRepository repository;
	private final RabbitTemplate rabbitTemplate;
	private final TransactionTemplate transactionTemplate;
	private final Counter published;
	private final Counter failures;
	private volatile long backoffUntilNanos = System.nanoTime();

	public OutboxRelay(OutboxEventSpringDataRepository repository, RabbitTemplate rabbitTemplate,
			TransactionTemplate transactionTemplate, MeterRegistry meterRegistry) {
		this.repository = repository;
		this.rabbitTemplate = rabbitTemplate;
		this.transactionTemplate = transactionTemplate;
		this.published = meterRegistry.counter("bets.outbox.published");
		this.failures = meterRegistry.counter("bets.outbox.failures");
	}

	public int drainUnlessBackingOff() {
		if (System.nanoTime() - backoffUntilNanos < 0) {
			return 0;
		}
		return drain();
	}

	public int drain() {
		int total = 0;
		try {
			for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {
				int count = Objects.requireNonNull(transactionTemplate.execute(status -> publishBatch()));
				total += count;
				if (count < BATCH_SIZE) {
					break;
				}
			}
		} catch (RuntimeException exception) {
			failures.increment();
			backoffUntilNanos = System.nanoTime() + BACKOFF_NANOS;
			log.warn("outbox relay stopped, the remaining rows stay for the next run: {}", exception.getMessage());
		}
		return total;
	}

	private int publishBatch() {
		List<OutboxEventJpaEntity> batch = repository.lockNextBatch(BATCH_SIZE);
		if (batch.isEmpty()) {
			return 0;
		}
		List<CorrelationData> correlations = new ArrayList<>(batch.size());
		rabbitTemplate.invoke(operations -> {
			for (OutboxEventJpaEntity row : batch) {
				CorrelationData correlation = new CorrelationData(String.valueOf(row.getId()));
				correlations.add(correlation);
				operations.send(EXCHANGE, row.getRoutingKey(), toMessage(row), correlation);
			}
			operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MILLIS);
			return null;
		});
		for (CorrelationData correlation : correlations) {
			if (correlation.getReturned() != null) {
				throw new AmqpException("outbox row " + correlation.getId() + " was returned as unroutable");
			}
		}
		repository.deleteAllByIdInBatch(batch.stream().map(OutboxEventJpaEntity::getId).toList());
		published.increment(batch.size());
		return batch.size();
	}

	private Message toMessage(OutboxEventJpaEntity row) {
		return MessageBuilder.withBody(row.getPayload().getBytes(StandardCharsets.UTF_8))
				.setContentType("application/json")
				.setDeliveryMode(MessageDeliveryMode.PERSISTENT)
				.build();
	}
}
