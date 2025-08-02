package com.googol.rmi;
import java.io.*;
import java.nio.file.*;

public class ProcessosClient {
    private static final String ID_FILE = "client_id.txt"; // Ficheiro para armazenar o último ID

    public static void main(String[] args) {
        int clientId = getNextClientId(); // Obtém o próximo ID único

        try {
            // Executa o Cliente com um ID único
            //ProcessBuilder pb = new ProcessBuilder("java", "Client", String.valueOf(clientId));
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "com.googol.rmi.Client",String.valueOf(clientId));
            pb.inheritIO(); // Permite mostrar a saída do cliente no terminal principal

            Process processo = pb.start(); // Inicia o processo do cliente
            System.out.println("Cliente " + clientId + " iniciado!");

            // Aguarda o término do processo do cliente antes de continuar
            processo.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Lê o último ID do ficheiro e incrementa
    private static int getNextClientId() {
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
