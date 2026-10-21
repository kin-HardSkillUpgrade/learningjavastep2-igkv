package org.example.ListsHerbiAndOmniPets.model;

import org.example.ListsHerbiAndOmniPets.interfaces.Herbivore;

public class Hamster extends Animal implements Herbivore {

    public Hamster (String name, Integer age) {
        super(name, age);
    }

    @Override
    public String chill() {
        return "I can chill for 8 hours";
    }

    @Override
    public String toString(){
        return "Hamster name = " + getName() + ", age = " + getAge() + ". " + chill();
    }
}
