package com.hospital.hms;

import org.springframework.boot.SpringApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class HmsApplication {
	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(HmsApplication.class, args);
	}

	private static void loadDotEnv() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.filename(".env")
					.ignoreIfMissing()
					.ignoreIfMalformed()
					.load();

			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue())
			);

			System.out.println(
					"✅ .env loaded — " + dotenv.entries().size() + " variables set"
			);

		} catch (Exception e) {
			System.err.println("⚠️ Failed to load .env file: " + e.getMessage());
		}
	}
}