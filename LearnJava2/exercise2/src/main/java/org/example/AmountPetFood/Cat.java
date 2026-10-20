package org.example.AmountPetFood;

public class Cat extends Animal {

    public Cat(String name, Integer age, Double mass) {
        super(name, age, mass);
    }
    @Override

    Double getFeedInfoKg(){
        return getMass() * 0.1;
    }

    @Override
    public String toString(){
        return "Cat name = " + getName() + ", age = " + getAge() + ", mass = " + getMass() + ", feed = " + getFeedInfoKg();
    }
}
