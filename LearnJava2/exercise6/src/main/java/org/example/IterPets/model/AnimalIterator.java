package org.example.IterPets.model;

import org.example.IterPets.interfaces.BaseIterator;

import java.util.List;

public class AnimalIterator implements BaseIterator<Animal> {
    private final List<Animal> pets;
    private int index = 0;

    public AnimalIterator(List<Animal> pets) {
        this.pets = pets;
    }

    @Override
    public Animal next() {
        return pets.get(index++);
    }

    @Override
    public boolean hasNext() {
        return index < pets.size();
    }

    @Override
    public void reset() {
        index = 0;
    }
}
