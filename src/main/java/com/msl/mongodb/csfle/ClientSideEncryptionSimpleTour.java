package com.msl.mongodb.csfle;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

public class ClientSideEncryptionSimpleTour {

    public static void main(final String[] args) {
    	
    	String conn = args[0];
    	System.out.println("Connection:" + conn);
    	
    	String dataBaseName = "CSFLE-TEST";
    	String collectionName= "CSFLE-AUTO-SIMPLE";

    	ConnectionString connectionString = new ConnectionString(conn);
    	
        // This would have to be the same master key as was used to create the encryption key
        final byte[] localMasterKey = new byte[96];
        new SecureRandom().nextBytes(localMasterKey);

        Map<String, Map<String, Object>> kmsProviders = new HashMap<String, Map<String, Object>>() {{
           put("local", new HashMap<String, Object>() {{
               put("key", localMasterKey);
           }});
        }};

        String keyVaultNamespace = "admin.datakeys";

        AutoEncryptionSettings autoEncryptionSettings = AutoEncryptionSettings.builder()
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviders)
                .build();

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .autoEncryptionSettings(autoEncryptionSettings)
                .build();

        MongoClient mongoClient = MongoClients.create(clientSettings);
        MongoCollection<Document> collection = mongoClient.getDatabase(dataBaseName).getCollection(collectionName);
        collection.drop(); // Clear old data

        collection.insertOne(new Document("encryptedField", "12313"));

        System.out.println(collection.find().first().toJson());
    }
}