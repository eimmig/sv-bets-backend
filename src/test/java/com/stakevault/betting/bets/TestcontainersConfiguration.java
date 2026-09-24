package com.stakevault.betting.bets;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

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
