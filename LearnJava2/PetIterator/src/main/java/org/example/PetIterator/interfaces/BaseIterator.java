package org.example.PetIterator.interfaces;

public interface BaseIterator<T> {
    T next();
    boolean hasNext();
    void reset();
}
