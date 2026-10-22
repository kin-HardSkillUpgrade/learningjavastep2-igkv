package org.example.IncrAgeCertainPets;

public class Cat extends Animal {

    public Cat(String name, Integer age) {
        super(name, age);
    }

    @Override
    public String toString(){
        return "Cat name = " + getName() + ", age = " + getAge();
    }
}
