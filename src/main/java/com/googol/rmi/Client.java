package com.googol.rmi;
import java.awt.*;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Client extends UnicastRemoteObject {
    Client() throws RemoteException {
        super();
    }

    /**
     * Main do cliente. Consiste num menu interativo em que permite o cliente
     * indexar URLs,pesquisar por termos no indice invertido ,
     * Aceder à página de administração para visualizar barrels ativos, sair do programa.
     * A comunicação é realizada por RMI com Gateway
     * */
    public static void main(String args[]) {
        try  {

            Gateway_I gateway = (Gateway_I) Naming.lookup("Gateway_I");
            Scanner scanner = new Scanner(System.in);
            int opcao;
            do{
                System.out.println("--Menu--");
                System.out.println("1. Indexar URL");
                System.out.println("2. Pesquisar");
                System.out.println("3. Acessar pagina de Administracao");
                System.out.println("4. Sair");
                System.out.println("Escolha uma opcao: ");
                opcao = scanner.nextInt();
                switch (opcao) {
                    case 1:
                        System.out.println("URl:");
                        String url = scanner.next();
                        gateway.addURlQueue(url);
                        break;
                    case 2:
                        System.out.println("Pesquise no Googol:");
                        scanner.nextLine();
                        String termo = scanner.nextLine();
                        List<PageInfo> resultados = gateway.pesquisaBarrel(termo);
                        if (resultados.isEmpty()) {
                            System.out.println("Nenhum resultado encontrado.");
                            break;
                        }
                        if (resultados.size() > 10) {
                            int inicio = 0;
                            int tam = 10;
                            do {
                                int fim = Math.min(inicio + tam, resultados.size());

                                for (int i = inicio; i < fim; i++) {
                                    System.out.println(resultados.get(i));
                                }
                                inicio = fim;
                                if (inicio < resultados.size()) {
                                    System.out.print("\nVer mais resultados? (s/n): ");
                                    String resposta = scanner.nextLine().toLowerCase();
                                    if (!resposta.equals("s")) {
                                        break;
                                    }
                                }

                            } while (inicio< resultados.size());
                        } else {

                            for (PageInfo info : resultados) {
                                System.out.println(info);
                            }
                        }
                    break;
                    case 3:
                        System.out.println("Aceder à página de admin...");
                        List<Integer> ids = gateway.listarBarrelsIds();
                        if (ids.isEmpty()){
                            System.out.println("Nenhuma Barrel ativo.");
                        }else {
                            for (int id : ids) {
                                System.out.println("Barrel com id: " + id + " está ativo.");
                            }
                        }
                        break;

                    case 4:
                        System.out.println("Bye Bye..");
                        break;
                    default:
                        System.out.println("Opcao invalida!");
                }
            }while (opcao != 4);
            scanner.close();
        } catch (Exception e) {
            System.out.println("Exception in  Client: " + e);
        }

    }
}