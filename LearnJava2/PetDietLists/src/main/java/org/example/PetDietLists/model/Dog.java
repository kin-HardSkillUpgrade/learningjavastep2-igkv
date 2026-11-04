package org.example.PetDietLists.model;

import org.example.PetDietLists.interfaces.Omnivore;

public class Dog extends Animal implements Omnivore {

    public Dog(String name, Integer age) {
        super(name, age);
    }

    @Override
    public String hunt() {
        return "I can hunt for robbers";
    }

    @Override
    public String toString(){
        return "Dog name = " + getName() + ", age = " + getAge() + ". " + hunt();
    }
}
