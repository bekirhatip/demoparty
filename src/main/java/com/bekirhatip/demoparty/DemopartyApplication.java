// This is the main application class for the DemoParty Spring Boot application. It serves as the entry point for the application and is responsible for bootstrapping the Spring context.

package com.bekirhatip.demoparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemopartyApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemopartyApplication.class, args);
	}

}
