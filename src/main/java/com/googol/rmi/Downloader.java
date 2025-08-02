package com.googol.rmi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class Downloader extends UnicastRemoteObject {

    private InetAddress multicastGroup;

    private int multicastPort = 1098; // Porta multicast escolhida
    public Downloader(InetAddress multicastGroup) throws RemoteException {
        super();
        this.multicastGroup = multicastGroup;
    }

    /**
     * Extrai informaçao de um url e envia por multicast para os barrels ativos
     * O metodo usa Jsoup para extrair informaçao do url: texto, titulos outros links
     * Esses links são colocados na fila de url no Gateway
     *
     * @param url a ser processado
     * @param gateway a interface remota Gateway_I utilizada para comunicação
     * */

    private void processURL(String url, Gateway_I gateway) {
        try {
            Document doc = Jsoup.connect(url).get();
            String text = doc.text();
            String title = doc.title();
            Elements links = doc.select("a[href]");


            //Thread.sleep(5000);
            //coloca url extraidos na queue da gateway
            for (Element link : links) {
                String linkURL = link.absUrl("href");
                gateway.addURlQueue(linkURL);
            }

            //Thread.sleep(5000);

            for (Barrel_callback barrel : gateway.listarBarrelsAtivos()){
                sendMulticastMessage(url,title,text);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Envia uma mensagem via multicast que contém o url, titulo e texto
     * Cria um DataSocket para enviar um DatagramPacket que contem as informações da página
     * para o grupo multicast
     *
     * @param texto
     * @param title
     * @param url
     * @throws Exception Caso ocorra um erro ao enviar pacote multicast
     * */
    public void sendMulticastMessage(String url,String title,String texto) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            StringBuilder message = new StringBuilder();
            message.append(url).append(",").append(title).append(",").append(texto);
            byte[] buffer = message.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastGroup, multicastPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Metodo main responsável por se ligar por rmi ao gateway. Obtem urls da fila da Gateway
     * e processa-os.
     *
     * */
    public static void main(String args[]) {
        try {
            Gateway_I gateway = (Gateway_I) Naming.lookup("Gateway_I");

            // Cria duas instâncias de Downloader com diferentes endereços multicast
            Properties confs = new Properties();
            // Carregar o arquivo props.properties do classpath
            InputStream input = Downloader.class.getClassLoader().getResourceAsStream("props.properties");

            if (input == null) {
                System.out.println("Arquivo props.properties não encontrado no classpath.");
                return;
            }

            confs.load(input); // Carrega as propriedades do arquivo
            //confs.load(new FileInputStream("props.properties"));
            Downloader downloader1 = new Downloader(InetAddress.getByName(confs.getProperty("multicast")));

            Scanner scanner = new Scanner(System.in);
            while (true) {
                //obtem proximo url da queue
                String url = gateway.getNextURL();
                if (url != null && !url.isEmpty()) {
                    System.out.println("Working on url: " + url);
                    downloader1.processURL(url, gateway);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
