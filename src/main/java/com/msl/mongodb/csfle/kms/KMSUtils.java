package com.msl.mongodb.csfle.kms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KMSUtils implements CommandLineRunner {

	@Autowired
	private KMSManager manager;

	Logger logger = LoggerFactory.getLogger(KMSUtils.class);
	
	public static void main(String[] args) {
		SpringApplication.run(KMSUtils.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
//		logger.info("Deleting KeyVault Collection");
//		manager.deleteKeyVaulCollection();
//		logger.info("KeyVault Collection Deleted");
	}
}
