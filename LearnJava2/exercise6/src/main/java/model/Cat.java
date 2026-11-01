package model;

public class Cat extends Animal {

    public Cat(String name, Integer age) {
        super(name, age);
    }

    @Override
    public String toString(){
        return "model.Cat name = " + getName() + ", age = " + getAge();
    }
}
