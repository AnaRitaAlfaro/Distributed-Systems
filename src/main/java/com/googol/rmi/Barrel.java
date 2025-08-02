package com.googol.rmi;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Semaphore;

public class Barrel extends UnicastRemoteObject implements Barrel_callback, Serializable{
    private MulticastSocket multicastSocket;
    private InetAddress multicastGroup;
    private int multicastPort = 1098; // Porta multicast escolhida
    private static HashMap <String, List<PageInfo>> invertedIndex;
    private static final String FILENAME = "word_urls.txt";
    private static Set<String> stopwords;

    private final Semaphore semaphore = new Semaphore(1);
    static {
        stopwords = loadStopwords("stopwords.txt");
    }
    private HeartBeat heartBeat;
    private int barrelId;
    public static Map<String, Set<String>> linksReferenciados = new HashMap<>();  // Map de páginas com os links que as referenciam
    public Barrel(int barrelId) throws RemoteException {
        super();
        this.barrelId = barrelId;
        invertedIndex = new HashMap<>();
        this.heartBeat = new HeartBeat(barrelId, 10000);
        this.heartBeat.start();
    }
    /**
     * lê e coloca num Hashset palavras - stopwords
     * @param filename
     * */
    /*private static Set<String> loadStopwords(String filename) {
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                stopwords.add(linha.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopwords;
    }*/
    private static Set<String> loadStopwords(String filename) {
    Set<String> stopwords = new HashSet<>();
    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
        if (inputStream == null) {
            throw new FileNotFoundException(filename + " not found in classpath");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                stopwords.add(linha.trim().toLowerCase());
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return stopwords;
    }


    /**
     * obtem id do barrel
     * */
    public int getId() throws RemoteException {
        return this.barrelId;
    }
    /**
     *  Inicia servidor multicast e junta se a um grupo multicast
     * @param multicastAddress endereço ip ao qual o socket se vai ligar
     * */
    public void startMulticast(String multicastAddress) {
        try {
            multicastGroup = InetAddress.getByName(multicastAddress); // Endereço multicast desejado
            multicastSocket = new MulticastSocket(multicastPort);// é criado e associado a um porta especifica
            multicastSocket.joinGroup(multicastGroup);//junta se ao grupo multicast especificado pela ip
            System.out.println("Multicast server started for address: " + multicastAddress);
            receiveMulticastMessages(); // Inicia para receber mensagens multicast
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Recebe mensagens via multicast. As mensagens reccebidas são processadas pelo metodo processMessage,
     * e converte a mensagem num indice invertido. Salva este indice num ficheiro de texto.
     * É um metodo syncronizer para garantir acesso seguro ao recursos partilhados.
     * */
    public synchronized void receiveMulticastMessages() {
        try {
            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                multicastSocket.receive(packet);
                String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                // Processa a mensagem e atualiza o índice invertido
                Map<String, List<PageInfo>> wordUrlMap = processMessage(receivedMessage);
                saveToFile(wordUrlMap, FILENAME);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Divide a mansagem em 3 partes:url,titulo,texto para constroir indice invertido.
     * Faz uso de um hashSet para evitar palavras repetidas e remove stopwords
     *
     * @param message
     * @return um hashMap onde as chaves são as palavras extraídas do texto e os valores são listas de classes do tipo PageInfo assosciadas a essa chave
     * */

    public static Map<String, List<PageInfo>> processMessage(String message) {
        Map<String, List<PageInfo>> wordInfoMap = new HashMap<>();
        String[] parts = message.split(",", 3);
        if (parts.length < 3) {
            System.err.println("Erro ao processar mensagem: formato inválido!");
            return wordInfoMap;
        }

        String url = parts[0].trim();
        String title = parts[1].trim();
        String text = parts[2].trim();
        System.out.println("Barrel a processar url: "+ url);

        String citation = text.length() > 100 ? text.substring(0, 100) + "..." : text;

        String[] words = text.toLowerCase().split("\\W+");
        Set<String> palavrasProcessadas = new HashSet<>(); // Evita duplicação de palavras


        for (String word : words) {
            if (!word.isEmpty() && !stopwords.contains(word)) {
                if (!palavrasProcessadas.contains(word)) {
                    wordInfoMap.computeIfAbsent(word, k -> new ArrayList<>()).add(new PageInfo(url, title, citation, 0));
                    palavrasProcessadas.add(word);
                }
            }
        }

        return wordInfoMap;
    }

    /**
     * Carrega ficheiro com o indice invertido.
     * O ficheiro deve estar no seguinte formato:
     * palavra:
     * URL:
     * Titulo:
     * Citação:
     * Importância:
     * ---
     * As informações são separadas por "---"
     * @param filename com indice invertido
     * @return o HashMap com o indice invertido
     * */

    public static HashMap<String, List<PageInfo>> loadIndexFromFile(String filename) {
        HashMap<String, List<PageInfo>> invertedIndex = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String linha;
            String currentWord = null;
            String url = null, titulo = null, citacao = null;
            int importancia = 0;

            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.endsWith(":")) {
                    currentWord = linha.substring(0, linha.length() - 1);
                } else if (linha.startsWith("URL: ")) {
                    url = linha.substring(5);
                } else if (linha.startsWith("Título: ")) {
                    titulo = linha.substring(8);
                } else if (linha.startsWith("Citação: ")) {
                    citacao = linha.substring(9);
                } else if (linha.startsWith("Importância: ")) {
                    try {
                        importancia = Integer.parseInt(linha.substring(12).trim());
                    } catch (NumberFormatException e) {
                        importancia = 0;
                    }
                } else if (linha.equals("---")) {
                    if (currentWord != null && url != null && titulo != null && citacao != null) {
                        PageInfo pageInfo = new PageInfo(url, titulo, citacao, importancia);
                        invertedIndex.computeIfAbsent(currentWord, k -> new ArrayList<>()).add(pageInfo);
                    }
                    url = titulo = citacao = null;
                    importancia = 0;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler ficheiro: " + e.getMessage());
        }

        return invertedIndex;
    }

    /**
     * Salva indice invertido num ficheiro e garante que não há urls repetidos para cada palavra
     * Carrega o ficheiro com o indice já existente e adiciona novas palavra com as correspondente informação
     * @param filename ficheiro com informação
     * @param newWordInfoMap HashMap que contem a informação a ser guardada
     * */
    public static void saveToFile(Map<String, List<PageInfo>> newWordInfoMap, String filename) {
        HashMap<String, List<PageInfo>> currentIndex = loadIndexFromFile(filename);

        // Para cada palavra no novo índice
        for (Map.Entry<String, List<PageInfo>> entry : newWordInfoMap.entrySet()) {
            String word = entry.getKey();
            List<PageInfo> newPages = entry.getValue();

            // Criar um conjunto de URLs já existentes para evitar repetições
            Set<String> urlsSalvas = new HashSet<>();
            if (currentIndex.containsKey(word)) {
                for (PageInfo p : currentIndex.get(word)) {
                    urlsSalvas.add(p.getUrl());
                }
            }

            // Adiciona os novos PageInfo sem repetir URLs
            for (PageInfo page : newPages) {
                if (!urlsSalvas.contains(page.getUrl())) {
                    currentIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(page);
                }
            }

        }


        // Salva o índice atualizado no arquivo
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, List<PageInfo>> entry : currentIndex.entrySet()) {
                String word = entry.getKey();
                List<PageInfo> pages = entry.getValue();

                writer.println(word + ":");
                for (PageInfo page : pages) {
                    writer.println("  URL: " + page.getUrl());
                    writer.println("  Título: " + page.getTitulo());
                    writer.println("  Citação: " + page.getTexto());
                    writer.println("  Importância: " + page.getImportancia());
                    writer.println("  ---");
                }
            }
            System.out.println("Índice invertido salvo em " + filename);
        } catch (IOException e) {
            System.err.println("Erro ao salvar índice invertido: " + e.getMessage());
        }
    }

    /**
     * Realiza um pesquisa no indice invertido pelo termo fornecido
     * O indice invertido é carregado a partir do ficheiro "word_url.txt" e é utilizado
     * para pesquisar páginas associadas ao termo.
     * @param termos a ser pesquisados
     * @return Uma lista do tipo PageInfo:URL,Titulo,citaçao
     * @throws RemoteException se ocorrer um erro de comunicação
     * */
    public List<PageInfo> pesquisar(List <String> termos) throws RemoteException {
        System.out.println("Barrel com id: "+ barrelId+ "resolveu pedido de pesquisa.");
        // Atualiza o índice carregando os dados mais recentes do ficheiro
        this.invertedIndex = loadIndexFromFile("word_urls.txt");

        Map <String,PageInfo> resultadosMap = new HashMap<>();
        // Obtém os resultados correspondentes ao termo pesquisado
        //List<PageInfo> resultados = new ArrayList<>(invertedIndex.getOrDefault(termo.toLowerCase(), new ArrayList<>()));

        for (String termo : termos) {
            List<PageInfo> paginas = invertedIndex.getOrDefault(termo.toLowerCase(), List.of());
            for (PageInfo pagina : paginas) {
                resultadosMap.put(pagina.getUrl(), pagina); // evita duplicatas
            }
        }
        List<PageInfo> resultados = new ArrayList<>(resultadosMap.values());



        return resultados;

    }

    public int getTamIndice(){
        this.invertedIndex = loadIndexFromFile("word_urls.txt");
        return invertedIndex.size();
    }

    /**
     * Inicia a execução do Barrel
     * Id do Barrel é lido a partir do ficheiro barrels_id.txt
     * Regista se no gateway e inicia comunicação multicast
     *
     * */
    public static void main(String args[]) {



        try{
            int barrelId = Integer.parseInt(args[0]);
            Barrel barrel = new Barrel(barrelId);
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Gateway_callback gatewayCallback = (Gateway_callback) registry.lookup("Gateway_callback");
            gatewayCallback.registaBarrel(barrel);
            System.out.println("Barrel registado com sucesso!");
            //barrel.startMulticast("224.0.0.1");
            Properties confs = new Properties();
            //confs.load(new FileInputStream("props.properties"));
            try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("props.properties")) {
                if (input == null) {
                    throw new FileNotFoundException("props.properties not found in classpath");
                }
                confs.load(input);
                barrel.startMulticast(confs.getProperty("multicast"));
            }
            barrel.startMulticast(confs.getProperty("multicast"));
        }catch (Exception re) {
            System.out.println("Exception in Gateway-Barrel.main: " + re);
        }
    }
}
