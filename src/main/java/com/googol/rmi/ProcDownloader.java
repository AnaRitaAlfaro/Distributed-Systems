package com.googol.rmi;
import java.io.IOException;
import java.io.*;
import java.nio.file.*;

public class ProcDownloader {
    private static final String ID_FILE = "downloader_id.txt"; // Ficheiro para armazenar o último ID

    public static void main(String[] args) {
        int DownloaderId = getNextDownloaderId(); // Obtém o próximo ID único


        try {
            // Executa o Cliente com um ID único
            //ProcessBuilder pb = new ProcessBuilder("java", "Downloader");
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", "target/classes", "com.googol.rmi.Downloader");

            //ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "com.googol.rmi.Downloader");
            pb.inheritIO(); // Permite mostrar a saída do Downloader no terminal principal

            Process processo = pb.start(); // Inicia o processo Downloader
            System.out.println("Downloader " +DownloaderId + " iniciado!");

            // Aguarda o término do processo do cliente antes de continuar
            processo.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static int getNextDownloaderId() {
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
