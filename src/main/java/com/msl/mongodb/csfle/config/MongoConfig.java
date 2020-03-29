package com.msl.mongodb.csfle.config;

import static java.lang.String.format;
import static java.lang.System.getProperty;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.internal.build.MongoDriverVersion;
import com.msl.mongodb.csfle.converter.BinaryToBsonBinaryConverter;
import com.msl.mongodb.csfle.converter.BsonBinaryToBinaryConverter;
import com.msl.mongodb.csfle.kms.KMSManager;

@Configuration
@EnableMongoRepositories(basePackages = "com.msl.mongodb.csfle.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

	@Value(value = "${spring.data.mongodb.database}")
	private String DB_DATABASE;
	@Value(value = "${spring.data.mongodb.uri}")
	private String DB_CONNECTION;
	@Autowired
	private KMSManager kmsManager;

	private MongoDriverInformation  getMongoDriverInfo(){
		return MongoDriverInformation.builder()
				.driverName(MongoDriverVersion.NAME)
				.driverVersion(MongoDriverVersion.VERSION)
				.driverPlatform(format("Java/%s/%s", getProperty("java.vendor", "unknown-vendor"),
						getProperty("java.runtime.version", "unknown-version")))
				.build();
	}

	private MongoClientSettings getMongoClientSettings(){
		kmsManager.buildOrValidateVault();
		return MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(DB_CONNECTION))
				.build();
	}

	/**
	 * Returns the list of custom converters that will be used by the MongoDB template
	 * These conversions avoid exceptions caused by Spring Data MongoDB retrieving Binary types
	 * from the repository an converting them to BsonBinary
	 *
	 **/
	public CustomConversions customConversions() {
		CustomConversions customConversions = new MongoCustomConversions(
				Arrays.asList(new BinaryToBsonBinaryConverter(),
						new BsonBinaryToBinaryConverter()));
		return customConversions;
	}


	@Override
	public MongoClient mongoClient() {
		kmsManager.buildOrValidateVault();
		MongoClient mongoClient = new MongoClientImpl(getMongoClientSettings(),getMongoDriverInfo());
		return mongoClient;
	}

	@Override
	protected String getDatabaseName() {
		return DB_DATABASE;
	}
}