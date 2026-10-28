package org.example.TrackPetWalks;

import java.util.concurrent.TimeUnit;

public class Cat extends Animal {

    public Cat(String name, Integer age) {
        super(name, age);
    }

    @Override
    public Double goToWalk()  {
        double timeWalk = getAge() * 0.25;
        try {
            TimeUnit.MILLISECONDS.sleep((long) (timeWalk * 1000));
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return timeWalk;
    }

    @Override
    public String toString(){
        return "Cat name = " + getName() + ", age = " + getAge();
    }
}