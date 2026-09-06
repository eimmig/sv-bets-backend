package com.stakevault.betting.bets;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	// Nome de fila/binding so para prova de teste - a topologia real de producao (fila
	// stats.bet-events com DLQ) e contrato do consumidor (stats-service), fora de escopo aqui.
	public static final String TEST_QUEUE = "test.bet-events";

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitMQContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));
	}

	// Beans Declarable sao auto-declarados pelo RabbitAdmin quando o contexto sobe - mesmo
	// exchange usado em producao (bets.events, ver docs/API-CONTRACTS.md), so pra publicar sem
	// erro nos testes; nao substitui infra/rabbitmq/definitions.json (nao versionado aqui).
	@Bean
	TopicExchange betsEventsExchange() {
		return new TopicExchange("bets.events", true, false);
	}

	@Bean
	Queue testBetEventsQueue() {
		return new Queue(TEST_QUEUE, true);
	}

	@Bean
	Binding testBetEventsBinding() {
		return BindingBuilder.bind(testBetEventsQueue()).to(betsEventsExchange()).with("bet.*");
	}

}
