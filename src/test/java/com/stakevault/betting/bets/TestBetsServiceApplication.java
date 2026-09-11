package com.stakevault.betting.bets;

import org.springframework.boot.SpringApplication;

public class TestBetsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(BetsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
