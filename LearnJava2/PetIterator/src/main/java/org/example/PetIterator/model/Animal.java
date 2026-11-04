package org.example.PetIterator.model;

public abstract class Animal {
    private final String name;
    private final Integer age;

    public Animal(String name, Integer age){
        this.name = name;
        this.age = age;
    }

    public String getName(){
        return name;
    }
    public Integer getAge(){
        return age;
    }

}
