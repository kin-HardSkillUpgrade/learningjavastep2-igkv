package org.example.ListPets;

import java.util.List;
import java.util.Scanner;

//TIP Чтобы <b>запустить</b> код, нажмите <shortcut actionId="Run"/> или
// нажмите на значок <icon src="AllIcons.Actions.Execute"/> на полях.
public class ListPets {
    public static void main(String[] args) {
        List<Animal> pets = readPets();
    }

    private List<Animal> readPets(){
        List<Animal> pets;
        try(Scanner read = new Scanner(System.in)){

        }catch (NumberFormatException e){
            System.out.println("Could not parse a number. Please, try again");
            readInt(read);
        }
        return pets;
    }

    private static int readInt(Scanner read) throws NumberFormatException{
        String line = read.nextLine();
        return Integer.parseInt(line);
    }

}