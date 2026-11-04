package org.example.FoodDose;

import java.util.*;

public class FoodDose {
    private static final Set<String> TYPES =
            Set.of("dog", "cat");

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
            int size = readInt(read);
            for (int i = 0; i < size; i++){
                readPet(read).ifPresent(pets::add);
            }
            return pets;
        }
    }

    private static Optional<Animal> readPet(Scanner read){
        String type = read.nextLine().trim().toLowerCase();
        if (!TYPES.contains(type)){
            System.out.println("Incorrect input. Unsupported pet type");
            return Optional.empty();
        }
        String name = read.nextLine();
        int age = readInt(read);
        if (age <= 0) {
            System.out.println("Incorrect input. Age <= 0");
            return Optional.empty();
        }
        double mass = readDouble(read);
        if (mass <= 0) {
            System.out.println("Incorrect input. Mass <= 0");
            return Optional.empty();
        }
        return Optional.of(createPet(type, name, age, mass));
    }

    private static Animal createPet(String type, String name, Integer age, Double mass){
        return switch (type) {
            case "dog" -> new Dog(name, age, mass);
            case "cat" -> new Cat(name, age, mass);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
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