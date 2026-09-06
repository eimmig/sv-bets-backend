package com.stakevault.betting.bets.adapter.out.messaging;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.stakevault.betting.bets.config.TenantContextHolder;
import com.stakevault.betting.bets.domain.model.Bet;
import com.stakevault.betting.bets.domain.port.out.BetEventPublisher;

import tools.jackson.databind.ObjectMapper;

@Component
public class RabbitBetEventPublisher implements BetEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(RabbitBetEventPublisher.class);

	// Topologia (exchange/routing keys) ja provisionada por infra/rabbitmq/definitions.json -
	// nunca redeclarada aqui (ver docs/API-CONTRACTS.md).
	private static final String EXCHANGE = "bets.events";
	private static final String ROUTING_KEY_BET_CREATED = "bet.created";

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public RabbitBetEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public void publishCreated(Bet bet) {
		BetEventEnvelope<BetCreatedPayload> envelope = new BetEventEnvelope<>(UUID.randomUUID(), "BetCreated", 1,
				Instant.now(), TenantContextHolder.current().slug(), bet.createdByUserId(),
				BetCreatedPayload.from(bet));
		try {
			byte[] body = objectMapper.writeValueAsBytes(envelope);
			Message message = MessageBuilder.withBody(body).setContentType("application/json").build();
			rabbitTemplate.send(EXCHANGE, ROUTING_KEY_BET_CREATED, message);
		} catch (Exception exception) {
			// Consistencia eventual e intencional (ver CLAUDE.md raiz) - durabilidade do registro
			// da aposta pesa mais que o sinal assincrono; sem outbox/retry nesta fase do projeto.
			log.error("failed to publish BetCreated for bet {}", bet.id(), exception);
		}
	}
}
