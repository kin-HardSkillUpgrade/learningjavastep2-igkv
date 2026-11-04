package org.example.PetWalkRegistry;

import java.util.concurrent.TimeUnit;

public class Dog extends Animal{

    public Dog(String name, Integer age) {
        super(name, age);
    }

    @Override
    public Double goToWalk() {
        double timeWalk = getAge() * 0.5;
        try {
            TimeUnit.MILLISECONDS.sleep((long) (timeWalk * 1000));
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return timeWalk;
    }

    @Override
    public String toString(){
        return "Dog name = " + getName() + ", age = " + getAge();
    }
}