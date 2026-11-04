package org.example.PetWalkRegistry;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PetWalkRegistry {
    private static final long PROGRAM_START = System.nanoTime();
    private static final double CONVERT_DOUBLE_CORRECT = 1_000_000_000.0;

    public static final Set<String> TYPES =
            Set.of("dog", "cat");

    public static void main(String[] args) {
        List<Animal> pets = readPets();
        List<WalkInfo> petsResult = petWalk(pets);
        printListPets(petsResult);
    }

    private static List<WalkInfo> petWalk(List<Animal> pets){
        List<WalkInfo> petsResultCopy = new CopyOnWriteArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for(Animal pet : pets){
            Thread t = new Thread(() -> {
            double startWalk = (System.nanoTime() - PROGRAM_START) / CONVERT_DOUBLE_CORRECT;
            pet.goToWalk();
            double endWalk = (System.nanoTime() - PROGRAM_START) / CONVERT_DOUBLE_CORRECT;
            petsResultCopy.add(new WalkInfo(pet, startWalk, endWalk));
        });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads){
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("BAD");
            }
        }
        return petsResultCopy;
    }

    private static void printListPets(List<WalkInfo> petsResult){
        petsResult.stream().sorted(Comparator.comparingDouble(WalkInfo::getEndWalk)).forEach(System.out::println);
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
