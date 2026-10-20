package org.example.ListPets;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ListPets {
    public static void main(String[] args) {
        List<Animal> pets = readPets();
        printListPets(pets);
    }

    private static void printListPets(List<Animal> pets){
        for (Animal a : pets ) System.out.println(a);
    }

    private static List<Animal> readPets(){
        try(Scanner read = new Scanner(System.in)){
            List<Animal> pets = new ArrayList<>();
            String name, type;
            int age;
            int size = readInt(read);
            while (size > 0) {
                type = read.nextLine();
                if (type.equalsIgnoreCase("dog") || type.equalsIgnoreCase("cat")) {
                    name = read.nextLine();
                    age = readInt(read);
                    if (age > 0) {
                        if (type.equalsIgnoreCase("dog")) {
                            pets.add(new Dog(name, age));
                        } else {
                            pets.add(new Cat(name, age));
                        }
                    }else {
                        System.out.println("Incorrect input. Age <= 0");
                    }
                } else {
                    System.out.println("Incorrect input. Unsupported pet type");
                }
                size--;
            }
            return pets;
        }
    }

    private static int readInt(Scanner read) {
        while (true){
            String line = read.nextLine();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
    }
}