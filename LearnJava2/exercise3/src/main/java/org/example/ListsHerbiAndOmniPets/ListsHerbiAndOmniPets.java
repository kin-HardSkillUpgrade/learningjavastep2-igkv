package org.example.ListsHerbiAndOmniPets;

import org.example.ListsHerbiAndOmniPets.interfaces.Herbivore;
import org.example.ListsHerbiAndOmniPets.interfaces.Omnivore;
import org.example.ListsHerbiAndOmniPets.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ListsHerbiAndOmniPets {
    public static void main(String[] args) {
        List<Animal> pets = readPets();
        printListPets(pets);
    }

    private static void printListPets(List<Animal> pets){
        pets.stream().filter(p -> p instanceof Herbivore).forEach(System.out::println);
        pets.stream().filter(p -> p instanceof Omnivore).forEach(System.out::println);
    }

    private static List<Animal> readPets(){
        try(Scanner read = new Scanner(System.in)){
            List<Animal> pets = new ArrayList<>();
            String name, type;
            Integer age;
            int size = readInt(read);
            while (size > 0) {
                type = read.nextLine().toLowerCase();

                if(!type.equals("dog") && !type.equals("cat") &&!type.equals("guinea") && !type.equals("hamster")) {
                    size--;
                    System.out.println("Incorrect input. Unsupported pet type");
                    continue;
                }

                name = read.nextLine();
                age = readInt(read);
                if (age  <= 0) {
                    size--;
                    System.out.println("Incorrect input. Age <= 0");
                    continue;
                }

                Animal pet = switch (type) {
                    case "dog" -> new Dog(name, age);
                    case "cat" -> new Cat(name, age);
                    case "hamster" -> new Hamster(name, age);
                    case "guinea" -> new GuineaPig(name, age);
                    default -> throw new IllegalStateException("Unexpected value: " + type);
                };

                pets.add(pet);
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