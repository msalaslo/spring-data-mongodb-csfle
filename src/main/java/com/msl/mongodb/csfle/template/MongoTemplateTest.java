package com.msl.mongodb.csfle.template;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.msl.mongodb.csfle.model.EncryptedPerson;
import com.msl.mongodb.csfle.model.Person;

public class MongoTemplateTest {

	private static final Log log = LogFactory.getLog(MongoTemplateTest.class);

	public static void main(String[] args) throws Exception {

    	String conn = args[0];
    	System.out.println("Connection:" + conn);
    	ConnectionString connectionString = new ConnectionString(conn);
    	String dataBaseName = "CSFLE-TEST";
    	
		MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString)
				.build();
		MongoClient mongoClient = MongoClients.create(clientSettings);
		MongoOperations mongoOps = new MongoTemplate(mongoClient, dataBaseName);
		EncryptedPerson encryptedPerson = new EncryptedPerson("MongoTemplateTest", 34);
//		encryptedPerson.setDni(new BsonBinary());
		mongoOps.insert(encryptedPerson);

		log.info(mongoOps.findOne(new Query(where("name").is("Joe")), Person.class));
	}
}
