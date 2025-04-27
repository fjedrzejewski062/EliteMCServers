package com.example.elitemcservers;

import org.springframework.boot.SpringApplication;

public class TestElitemcserversApplication {

	public static void main(String[] args) {
		SpringApplication.from(ElitemcserversApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
