package com.msl.mongodb.csfle.model;

import org.bson.BsonBinary;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "CSFLE-PERSON")
public class Person {

	private String id;
	private String name;
	private int age;
	private BsonBinary dni;

	public Person(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public BsonBinary getDni() {
		return dni;
	}

	public void setDni(BsonBinary dni) {
		this.dni = dni;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", age=" + age + ", dni=" + dni + "]";
	}
}