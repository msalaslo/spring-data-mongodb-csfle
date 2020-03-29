package com.msl.mongodb.csfle.repository;

import org.bson.BsonBinary;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.msl.mongodb.csfle.model.EncryptedPerson;

public interface EncryptedPersonRepository extends MongoRepository<EncryptedPerson, String> {
	public EncryptedPerson findByName(String name);
	public EncryptedPerson findByDni(BsonBinary dni);
}
