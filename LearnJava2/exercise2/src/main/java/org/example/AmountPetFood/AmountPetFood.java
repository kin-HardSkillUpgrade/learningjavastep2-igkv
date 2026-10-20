package org.example.AmountPetFood;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AmountPetFood {
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
            Integer age;
            Double mass;
            int size = readInt(read);
            while (size > 0) {
                type = read.nextLine();
                if (type.equalsIgnoreCase("dog") || type.equalsIgnoreCase("cat")) {
                    name = read.nextLine();
                    age = readInt(read);
                    if (age > 0) {
                        mass = readDouble(read);
                        if (mass > 0) {
                            if (type.equalsIgnoreCase("dog")) {
                                pets.add(new Dog(name, age, mass));
                            } else {
                                pets.add(new Cat(name, age, mass));
                            }
                        }else {
                            System.out.println("Incorrect input. Mass <= 0");
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

    private static Double readDouble(Scanner read) {
        while (true){
            String line = read.nextLine();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Could not parse a number. Please, try again");
            }
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