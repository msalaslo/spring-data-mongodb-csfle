package com.msl.mongodb.csfle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.msl.mongodb.csfle.executor.PersonExecutor;

@SpringBootApplication
public class MongodbCsfleApplication implements CommandLineRunner {

	@Autowired
	private PersonExecutor executor;

	Logger logger = LoggerFactory.getLogger(MongodbCsfleApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(MongodbCsfleApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		executor.runApplication();
	}

}
