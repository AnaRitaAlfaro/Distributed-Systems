package com.googol.rmi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessosBarrel {

    private static final String ID_FILE = "barrels_id.txt"; // Ficheiro para armazenar o último ID

    public static void main(String[] args) {
        int barrelId = getNextBarrelId(); // Obtém o próximo ID único

        try {
            // Executa o Barrel com um ID único
            //ProcessBuilder pb = new ProcessBuilder("java", "Barrel", String.valueOf(barrelId));
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", "target/classes", "com.googol.rmi.Barrel", String.valueOf(barrelId));

            //ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "com.googol.rmi.Barrel", String.valueOf(barrelId));
            pb.inheritIO(); // Permite mostrar a saída do cliente no terminal principal

            Process processo = pb.start(); // Inicia o processo do cliente
            System.out.println("Barrel " + barrelId + " iniciado!");

            // Aguarda o término do processo do cliente antes de continuar
            processo.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Lê o último ID do ficheiro e incrementa
    private static int getNextBarrelId() {
        int lastId = 0;

        try {
            Path path = Paths.get(ID_FILE);
            if (Files.exists(path)) {
                lastId = Integer.parseInt(Files.readString(path).trim());
            }

            lastId++; // Incrementa o ID
            Files.writeString(path, String.valueOf(lastId)); // Guarda o novo ID no ficheiro

        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao ler/escrever ID. Definindo como 1.");
            lastId = 1; // Se houver erro, começa do ID 1
        }

        return lastId;
    }
}
