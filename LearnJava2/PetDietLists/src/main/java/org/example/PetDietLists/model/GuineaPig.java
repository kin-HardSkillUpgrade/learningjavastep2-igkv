package org.example.PetDietLists.model;

import org.example.PetDietLists.interfaces.Herbivore;

public class GuineaPig extends Animal implements Herbivore {

    public GuineaPig (String name, Integer age) {
        super(name, age);
    }

    @Override
    public String chill() {
        return "I can chill for 12 hours";
    }

    @Override
    public String toString(){
        return "GuineaPig name = " + getName() + ", age = " + getAge() + ". " + chill();
    }
}
