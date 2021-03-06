package com.msl.mongodb.csfle.model;

import org.bson.BsonBinary;
import org.bson.BsonString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.vault.EncryptOptions;
import com.msl.mongodb.csfle.kms.KMSManager;

@Component
public class PersonEncryptionMapper {

	@Autowired
	protected KMSManager kmsManager;

	//Apply deterministic encryption for searchable attributes
	public static final String DETERMINISTIC_ENCRYPTION_TYPE = "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic";
	
	//Apply radom encryption for non searchable attributes and add more security
	public static final String RANDOM_ENCRYPTION_TYPE = "AEAD_AES_256_CBC_HMAC_SHA_512-Random";

	public EncryptedPerson getEncrypedPerson(Person p) {
		EncryptedPerson ep = new EncryptedPerson(p.getName(), p.getAge());
		ep.setDni(kmsManager.getClientEncryption().encrypt(new BsonString(p.getDni()),
				getEncryptOptions(DETERMINISTIC_ENCRYPTION_TYPE)));
		return ep;
	}

	public Person getPerson(EncryptedPerson ep) {
		String dni = kmsManager.getClientEncryption().decrypt(ep.getDni()).asString().getValue();
		Person p = new Person(ep.getName(), ep.getAge(), dni);
		return p;
	}

	public BsonBinary getEncryptedDni(String dni) {
		return kmsManager.getClientEncryption().encrypt(new BsonString(dni),
				getEncryptOptions(DETERMINISTIC_ENCRYPTION_TYPE));
	}

	private EncryptOptions getEncryptOptions(String algorithm) {
		EncryptOptions encryptOptions = new EncryptOptions(algorithm);
		encryptOptions.keyId(new BsonBinary(kmsManager.getEncryptionKeyUUID()));
		return encryptOptions;
	}
}
