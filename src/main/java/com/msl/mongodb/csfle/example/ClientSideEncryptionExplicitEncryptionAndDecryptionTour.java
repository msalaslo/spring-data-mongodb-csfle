package com.msl.mongodb.csfle.example;

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
import org.bson.BsonBinary;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.Binary;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * ClientSideEncryption explicit encryption and decryption tour
 */
public class ClientSideEncryptionExplicitEncryptionAndDecryptionTour {

    /**
     * Run this main method to see the output of this quick example.
     *
     * @param args ignored args
     */
    public static void main(final String[] args) {
    	String conn = args[0];
    	System.out.println("Connection:" + conn);
    	String dataBaseName = "CSFLE-TEST";
    	String collectionName= "CSFLE-EXPLICIT";
    	ConnectionString connectionString = new ConnectionString(conn);
    	
        // This would have to be the same master key as was used to create the encryption key
        final byte[] localMasterKey = new byte[96];
        new SecureRandom().nextBytes(localMasterKey);

        Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>() {{
            put("local", new HashMap<String, Object>() {{
                put("key", localMasterKey);
            }});
        }};

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(clientSettings);

        // Set up the key vault for this example
        MongoNamespace keyVaultNamespace = new MongoNamespace("encryption.testKeyVault");

        MongoCollection<Document> keyVaultCollection = mongoClient.getDatabase(keyVaultNamespace.getDatabaseName())
                .getCollection(keyVaultNamespace.getCollectionName());
        keyVaultCollection.drop();

        // Ensure that two data keys cannot share the same keyAltName.
        keyVaultCollection.createIndex(Indexes.ascending("keyAltNames"),
                new IndexOptions().unique(true)
                        .partialFilterExpression(Filters.exists("keyAltNames")));

        MongoCollection<Document> collection = mongoClient.getDatabase(dataBaseName).getCollection(collectionName);
        collection.drop(); // Clear old data

        // Create the ClientEncryption instance
        ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(connectionString)
                        .build())
                .keyVaultNamespace(keyVaultNamespace.getFullName())
                .kmsProviders(kmsProviders)
                .build();

        ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);

        BsonBinary dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());

        // Explicitly encrypt a field
        BsonBinary encryptedFieldValue = clientEncryption.encrypt(new BsonString("42342"),
                new EncryptOptions("AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic").keyId(dataKeyId));

        collection.insertOne(new Document("encryptedField", encryptedFieldValue));

        Document doc = collection.find().first();
        System.out.println(doc.toJson());

        // Explicitly decrypt the field
        System.out.println(clientEncryption.decrypt(new BsonBinary(doc.get("encryptedField", Binary.class).getData())));

        // release resources
        mongoClient.close();
    }
}
