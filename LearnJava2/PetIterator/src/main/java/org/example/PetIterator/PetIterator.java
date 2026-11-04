package org.example.PetIterator;

import java.util.*;

import org.example.PetIterator.model.Animal;
import org.example.PetIterator.model.AnimalIterator;
import org.example.PetIterator.model.Cat;
import org.example.PetIterator.model.Dog;

public class PetIterator {

    private static final Set<String> TYPES = Set.of("dog", "cat");
    public static void main(String[] args) {
        List<Animal> pets = readPets();
        printListPets(pets);
    }

    private static void printListPets(List<Animal> pets){
        AnimalIterator iteratorpets = new AnimalIterator(pets);
        while (iteratorpets.hasNext()){
            System.out.println(iteratorpets.next());
        }
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
        while (true) {
            String line = read.nextLine();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
    }
}