package org.example.IterPets.model;

public class Dog extends Animal{

    public Dog(String name, Integer age) {
        super(name, age);
    }

    @Override
    public String toString(){
        return "Dog name = " + getName() + ", age = " + getAge();
    }
}
