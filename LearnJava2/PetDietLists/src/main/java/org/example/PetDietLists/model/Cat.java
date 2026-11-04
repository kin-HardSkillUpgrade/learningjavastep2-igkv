package org.example.PetDietLists.model;

import org.example.PetDietLists.interfaces.Omnivore;

public class Cat extends Animal implements Omnivore {

    public Cat(String name, Integer age) {
        super(name, age);
    }

    @Override
    public String hunt(){
        return "I can hunt for mice";
    }

    @Override
    public String toString(){
        return "Cat name = " + getName() + ", age = " + getAge() + ". " + hunt();
    }
}
