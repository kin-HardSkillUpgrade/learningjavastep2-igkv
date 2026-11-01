package model;

import interfaces.BaseIterator;

import java.util.List;

public class AnimalIterator implements BaseIterator {
    private List<Animal> pets;
    private Integer index = 0;

    AnimalIterator(List<Animal> pets) {
        this.pets = pets;
    }

    @Override
    public Animal next() {
        if(hasNext()){
            return this.pets.get(index++);
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        if(this.index < this.pets.size()){
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        this.index = 0;
    }
}
