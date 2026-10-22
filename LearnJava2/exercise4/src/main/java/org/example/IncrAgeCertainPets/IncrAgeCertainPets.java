package org.example.IncrAgeCertainPets;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class IncrAgeCertainPets {

    public static final Set<String> TYPES =
            Set.of("dog", "cat");

    public static void main(String[] args) {
        List<Animal> pets = readPets();
        List<Animal> resultPets = updatePets(pets);
        printListPets(resultPets);
    }

    private static void printListPets(List<Animal> resultPets){
        resultPets.forEach(System.out::println);
    }

    private static List<Animal> updatePets(List<Animal> pets) {
        return pets.stream().map(p -> p.getAge() > 10 ? incrementAge(p) : p ).collect(toList());
    }


    private static Animal incrementAge(Animal pet) {
        if( pet instanceof Dog){
            return new Dog(pet.getName(), pet.getAge() + 1);
        } else {
            return new Cat(pet.getName(), pet.getAge() + 1);
        }
    }

    private static List<Animal> readPets(){
        try(Scanner read = new Scanner(System.in)){
            int size = readInt(read);
            return readPets(read, size);
        }
    }

    private static List<Animal> readPets(Scanner read, int size) {
        if (size <= 0){
            return new ArrayList<>();
        }
        List<Animal> pets = readPets(read, size - 1);
        readPet(read).ifPresent(pets::add);
        return pets;
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
        return Optional.of(createPet(type, name, age));
    }


    private static Animal createPet(String type, String name, Integer age){
        return switch (type) {
            case "dog" -> new Dog(name, age);
            case "cat" -> new Cat(name, age);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static int readInt(Scanner read) {
            String line = read.nextLine();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Could not parse a number. Please, try again");
                return readInt(read);
            }
    }
}
