package org.example.FoodDose;

public abstract class Animal {
    private String name;
    private Integer age;
    private Double mass;

    public Animal (String name, Integer age, Double mass){
        this.name = name;
        this.age = age;
        this.mass = mass;
    }

    abstract Double getFeedInfoKg();

    public String getName(){
        return name;
    }
    public Integer getAge(){
        return age;
    }
    public Double getMass(){
        return mass;
    }

}
