package org.example.AmountPetFood;

public class Dog extends Animal{

    public Dog(String name, Integer age, Double mass) {
        super(name, age, mass);
    }

    @Override
    Double getFeedInfoKg(){
      return getMass() * 0.3;
    }

    @Override
    public String toString(){
        return "Dog name = " + getName() + ", age = " + getAge() + ", mass = " + getMass() + ", feed = " + getFeedInfoKg();
    }
}
