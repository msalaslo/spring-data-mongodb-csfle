package com.msl.mongodb.csfle.template;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.BsonBinary;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.msl.mongodb.csfle.converter.BinaryToBsonBinaryConverter;
import com.msl.mongodb.csfle.converter.BsonBinaryToBinaryConverter;
import com.msl.mongodb.csfle.model.Person;

public class CSFLEExplicitWithMongoTemplate {

	private static final Log log = LogFactory.getLog(CSFLEExplicitWithMongoTemplate.class);

	private static String keyVaultNamespaceAsString = "encryption.testKeyVault";

	// This would have to be the same master key as was used to create the
	// encryption key
	private static final byte[] localMasterKey = new byte[96];

	public static void main(String[] args) throws Exception {

		String conn = args[0];
		System.out.println("Connection:" + conn);
		ConnectionString connectionString = new ConnectionString(conn);
		String dataBaseName = "CSFLE-TEST";

		MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
//                .autoEncryptionSettings(getAutoEncryptionSettings())
				.build();
		MongoClient mongoClient = MongoClients.create(clientSettings);
		MongoOperations mongoOps = new MongoTemplate(mongoClient, dataBaseName);
	    MappingMongoConverter mongoMapping = (MappingMongoConverter) mongoOps.getConverter();
	    CustomConversions customConversions = new MongoCustomConversions(
				Arrays.asList(new BinaryToBsonBinaryConverter(),
						new BsonBinaryToBinaryConverter()));
		
	    mongoMapping.setCustomConversions(customConversions); // tell mongodb to use the custom converters
	    mongoMapping.afterPropertiesSet();

		// Initialize KeyVaultNameSpace
		MongoNamespace keyVaultNamespace = new MongoNamespace(keyVaultNamespaceAsString);
		initKeyVaultNamespace(mongoClient, keyVaultNamespace);

		// Get Client Encryption
		ClientEncryption clientEncryption = getClientEncryption(connectionString, keyVaultNamespace,
				getKMSProviders(localMasterKey));

		// Create Local dataKey
		BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());

		// Explicitly encrypt a field
		String dni = "55555555X";
		BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonString(dni),
				new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

		String name = "JoeEncBsonBinary";
		Person person = new Person(name, 34);
		person.setDni(encryptedFieldValue);
		mongoOps.insert(person);

		Person personFound = mongoOps.findOne(new Query(where("dni").is(encryptedFieldValue)), Person.class);
		log.info("Person foundBy DNI:" + personFound);
		
		personFound = mongoOps.findOne(new Query(where("name").is(name)), Person.class);
		log.info("Person foundBy name:" + personFound);
	}

//	private static AutoEncryptionSettings getAutoEncryptionSettings() {
//	    AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
//	            .keyVaultNamespace(keyVaultNamespaceAsString)
//	            .kmsProviders(getKMSProviders(localMasterKey))
//	            .build();
//	    return autoEncryptionSettings;
//	}

	private static void initKeyVaultNamespace(MongoClient mongoClient, MongoNamespace keyVaultNamespace) {
		MongoCollection<Document> keyVaultCollection = mongoClient.getDatabase(keyVaultNamespace.getDatabaseName())
				.getCollection(keyVaultNamespace.getCollectionName());
		keyVaultCollection.drop();

		// Ensure that two data keys cannot share the same keyAltName.
		keyVaultCollection.createIndex(Indexes.ascending("keyAltNames"),
				new IndexOptions().unique(true).partialFilterExpression(Filters.exists("keyAltNames")));
	}

	private static Map<String, Map<String, Object>> getKMSProviders(byte[] localMasterKey) {
		new SecureRandom().nextBytes(localMasterKey);
		Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>() {
			{
				put("local", new HashMap<String, Object>() {
					{
						put("key", localMasterKey);
					}
				});
			}
		};
		return kmsProviders;
	}

	private static ClientEncryption getClientEncryption(ConnectionString connectionString,
			MongoNamespace keyVaultNamespace, Map<String, Map<String, Object>> kmsProviders) {
		// Create the ClientEncryption instance
		ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
				.keyVaultMongoClientSettings(
						MongoClientSettings.builder().applyConnectionString(connectionString).build())
				.keyVaultNamespace(keyVaultNamespace.getFullName()).kmsProviders(kmsProviders).build();

		ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

		return clientEncryption;
	}

}
