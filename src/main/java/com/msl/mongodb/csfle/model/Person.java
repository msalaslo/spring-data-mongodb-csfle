package com.msl.mongodb.csfle.model;

import org.springframework.data.annotation.Id;

public class Person {

	@Id
	private String id;
	private String name;
	private int age;
	private String dni;

	public Person(String name, int age, String dni) {
		this.name = name;
		this.age = age;
		this.dni = dni;
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

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", age=" + age + ", dni=" + dni + "]";
	}
}