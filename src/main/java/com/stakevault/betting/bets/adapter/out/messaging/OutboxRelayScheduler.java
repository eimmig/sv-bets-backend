package com.stakevault.betting.bets.adapter.out.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
class OutboxRelayScheduler {

	private final OutboxRelay relay;

	OutboxRelayScheduler(OutboxRelay relay) {
		this.relay = relay;
	}

	@Scheduled(fixedDelayString = "${outbox.relay.delay-ms:200}")
	void run() {
		relay.drainUnlessBackingOff();
	}
}
