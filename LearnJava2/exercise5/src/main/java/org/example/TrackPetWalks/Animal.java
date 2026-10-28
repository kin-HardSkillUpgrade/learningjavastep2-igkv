package org.example.TrackPetWalks;

abstract class Animal {
    private String name;
    private Integer age;

    public Animal(String name, Integer age){
        this.name = name;
        this.age = age;
    }

    abstract Double goToWalk();

    public String getName(){
        return name;
    }
    public Integer getAge(){
        return age;
    }

}
