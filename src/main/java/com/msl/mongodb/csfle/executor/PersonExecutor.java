package com.msl.mongodb.csfle.executor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.msl.mongodb.csfle.model.EncryptedPerson;
import com.msl.mongodb.csfle.model.Person;
import com.msl.mongodb.csfle.model.PersonEncryptionMapper;
import com.msl.mongodb.csfle.repository.EncryptedPersonRepository;

@Component
public class PersonExecutor {

	private static final Logger logger = LoggerFactory.getLogger(PersonExecutor.class);
	
	@Autowired
	private EncryptedPersonRepository encryptedPersonRepository;

	@Autowired
	private PersonEncryptionMapper personEntityHelper;

	private void clean() {
		encryptedPersonRepository.deleteAll();
	}

	public void runApplication() {
		clean();
		// Create a couple of non encrypted persons
		Person p1 = new Person("Miguel", 34, "11343122X");
		Person p2 = new Person("Pepe", 21, "11377122Y");

		// Encrypt the Person and save to EncryptedPerson
		EncryptedPerson ep1 = personEntityHelper.getEncrypedPerson(p1);
		EncryptedPerson ep2 = personEntityHelper.getEncrypedPerson(p2);
		// Save persons..
		encryptedPersonRepository.saveAll(Arrays.asList(new EncryptedPerson[] { ep1, ep2 }));

		// fetch all persons
		logger.debug("Persons found with findAll():");
		logger.debug("-------------------------------");

		List<Person> decryptedPersons = encryptedPersonRepository.findAll().stream()
				.map(ep -> personEntityHelper.getPerson(ep)).collect(Collectors.toList());

		for (Person person : decryptedPersons) {
			logger.debug(person.toString());
		}

		// fetch an individual customer
		logger.debug("Person found with findByFirstName('Miguel'):");
		logger.debug("--------------------------------");

		EncryptedPerson findByNamePerson = encryptedPersonRepository.findByName("Miguel");
		logger.info("findByNamePerson Equals Miguel Success: {}",
				findByNamePerson.getName().equals("Miguel"));

		// For Find by DNI we have to first get the binary version of DNI
		EncryptedPerson findByDni = encryptedPersonRepository.findByDni(personEntityHelper.getEncryptedDni("11343122X"));
		logger.info("findByDni equals Miguel Success: {}",
				personEntityHelper.getPerson(findByDni).getName().equals("Miguel"));

	}
}
