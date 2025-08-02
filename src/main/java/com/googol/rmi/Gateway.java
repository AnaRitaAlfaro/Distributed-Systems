package com.googol.rmi;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Gateway extends UnicastRemoteObject implements Gateway_I, Gateway_callback, Serializable {
    private Barrel_callback barrel;
    private ConcurrentLinkedQueue<String> urlQueue;
    private List <Barrel_callback> barrelsAtivos;
    private final Map<Integer, Long> lastHeartbeats = new ConcurrentHashMap<>();
    private static final Map<String,Integer> TopPesquisas = new ConcurrentHashMap<>();
    private static Map<Integer, Long> temposPorBarrel = new ConcurrentHashMap<>();


    private final long HEARTBEAT_TIMEOUT = 10000;
    private int roundRobinIndex = 0;

    protected Gateway() throws RemoteException {
        super();
        urlQueue = new ConcurrentLinkedQueue<>();
        barrelsAtivos = new ArrayList<>();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::removerBarrelsInativos,10,10, TimeUnit.SECONDS);
    }
    /**
     * Permite receber confirmação de barrels ativos usando classe HeartBeat
     * Atualiza tempo do ultimo heartbeat
     * */
    public synchronized void receiveHeartBeat(int barrelId) throws RemoteException {
        System.out.println("Recebido Heartbeat do Barrel " + barrelId);
        lastHeartbeats.put(barrelId, System.currentTimeMillis()); // Atualiza o tempo do último heartbeat
    }

    /**
     * Remove os barrels inativos da lista de barrels ativos caso haja falha de comunicação ou
     * o ultimo heartbeat recebido tenha excedido o limite.
     * O metodo é synchronized para evitar problemas de concorrencia ao aceder a lista de barrels ativos
     * */
    private synchronized void removerBarrelsInativos() {
        long agora = System.currentTimeMillis();
        Iterator<Barrel_callback> iterator = barrelsAtivos.iterator();

        while (iterator.hasNext()) {
            Barrel_callback barrel = iterator.next();
            int id;
            try {
                id = barrel.getId();
            } catch (RemoteException e) {
                iterator.remove(); // Remove diretamente se der erro de comunicação
                System.out.println("Barrel removido por falha de comunicação.");
                continue;
            }

            if (lastHeartbeats.containsKey(id) && (agora - lastHeartbeats.get(id) > HEARTBEAT_TIMEOUT)) {
                iterator.remove();
                lastHeartbeats.remove(id);
                System.out.println("Barrel " + id + " removido por timeout (sem heartbeat).");
            }
            System.out.println("lista de barrel ativos:" + barrelsAtivos.size() );
            for (int i=0;i<barrelsAtivos.size();i++){
                try {
                    System.out.println("ids dos barrel:" + barrel.getId());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }

    /**
     * Regista barrel na lista de barrelsAtivos
     * */
    public void registaBarrel(Barrel_callback barrel) throws RemoteException{
        barrelsAtivos.add(barrel);
        System.out.println("Barrel registado! Total de barrels ativos: " + barrelsAtivos.size());

    }
    /**
     *  Percorre a lista de barrels ativos para aceder ao seu id e caso haja falha de comunicação
     *  esse barrel é removido da lista
     *
     * @return Lista de ids dos barrels ativos
     * @throws RemoteException Se houver um erro na comunicação remota
     * */
    public List<Integer> listarBarrelsIds() throws RemoteException {
        List<Integer> ids = new ArrayList<>();
        Iterator<Barrel_callback> iterator = listarBarrelsAtivos().iterator();

        while (iterator.hasNext()) {
            try {
                Barrel_callback barrel = iterator.next();
                ids.add(barrel.getId());
            } catch (RemoteException e) {
                iterator.remove();
            }
        }

        return ids;
    }
    /**
     * Lista barrels ativos
     *
     * @return lista barrelsAtivos
     * */
    public List<Barrel_callback> listarBarrelsAtivos() throws RemoteException {
        return new ArrayList<>(barrelsAtivos);
    }

    /**
     * Adiciona url à fila de Urls
     *
     * @param url adicionado à fila
     * */
    public void addURlQueue(String url) throws RemoteException{
        urlQueue.add(url);
        //System.out.println("URl was added to queue: " +url);
    }
    /**
     * Obtem todos os urls na fila
     *
     * @return fila
     * */
    public Queue<String> getAllURL () throws RemoteException{
        return urlQueue;
    }
    /**
     * Retira url da fila
     *
     * @return url retirado da fila
     * */
    public String getNextURL() throws RemoteException{
        return urlQueue.poll();

    }
    public void putURLBack(String url) throws RemoteException{
        urlQueue.offer(url);
        System.out.println("Putting url: "+url+" back in the queue!");
    }
    /**
     * Usa round robin para escolher proximo barrel
     *
     * @return o barrel selecionado
     * */
    public  Barrel_callback getNextBarrel() {
        if (barrelsAtivos.isEmpty()) {
            return null;
        }
        Barrel_callback selected = barrelsAtivos.get(roundRobinIndex);
        roundRobinIndex = (roundRobinIndex + 1) % barrelsAtivos.size();
        return selected;
    }
    /**
     * Realiza a pesquisa de um termo em um dos barrels ativos
     *
     * @param termo a ser pesquisado
     * @return Uma lista do tipo PageInfo que contem os resultados da pesquisa
     * @throws RemoteException se houver erro de comunicaçao remota com o barrel
     * */
    public List<PageInfo> pesquisaBarrel(String termo) throws RemoteException {
        if (barrelsAtivos.isEmpty()) {
            System.out.println("Gateway: Nenhum barrel ativo!");
            return List.of();  // Retorna uma lista vazia
        }
        System.out.println("Barrels Ativos: " + barrelsAtivos.size());  // Verifica o número de barrels ativos
        Barrel_callback barrelSelecionado = getNextBarrel();
        if (barrelSelecionado == null) {
            System.out.println("Nenhum barrel selecionado para pesquisa.");
            return List.of();  // Retorna uma lista vazia
        }

        int barrelId = barrelSelecionado.getId(); // Obtém o ID do Barrel
        System.out.println("Realizando pesquisa no Barrel ID: " + barrelId);

        List<String> tokens = divide(termo);  // Divide o termo de pesquisa em tokens
        System.out.println("Tokens para pesquisa: " + tokens);
        //comaça a medir tempo
        long start = System.currentTimeMillis();
        List<PageInfo> resultados = barrelSelecionado.pesquisar(tokens);  // Chama o método de pesquisa do barrel
        long end = System.currentTimeMillis();
        long tempoDecimas = (end - start) / 100;
        //fim da mediçao
        temposPorBarrel.put(barrelId, tempoDecimas);
        System.out.println("Pesquisa realizada pelo Barrel ID: " + barrelId);
        if (resultados.isEmpty()) {
            System.out.println("Nenhum resultado encontrado.");
        }

        return resultados;  // Retorna os resultados
    }


    /**
     * Divide a string de input em palavras, usando caracteres que nao sejam letras ou digitos.
     * Converte o texto para minusculas, retira espaços em brancos e retona uma lista de palavras validas
     * @param input a string a ser dividida
     * @return uma lista de palavras (tokens) extraídas do input
     *
     */
    private List<String> divide (String input){
        return Arrays.stream(input.toLowerCase().split("\\W+"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Override
    public List<BarrelsInfo> getBarrelsInfo() throws RemoteException{
        List<BarrelsInfo> infoList = new ArrayList<>();

        for(Barrel_callback barrel :barrelsAtivos) {
            try {

                int tamanho = barrel.getTamIndice();
                int barrelId = barrel.getId();
                long tempoResposta = temposPorBarrel.getOrDefault(barrelId,-1L);
                infoList.add(new BarrelsInfo(barrelId, tamanho, tempoResposta));

            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        return infoList;
    }
    public void registarTermo(String termo) throws RemoteException {
        TopPesquisas.merge(termo.toLowerCase(), 1, Integer::sum);
        System.out.println("Termo registado: " + termo);
    }

    public List<Pesquisas> getTop10Pesquisas() throws RemoteException {
        TopPesquisas.forEach((k, v) -> System.out.println(k + ": " + v));
        return TopPesquisas.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(e -> new Pesquisas(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Inicia um metodo registry RMI na porta 1099 e faz o registo para duas interfaces
     * */
    public static void main(String args[]) {
        try {
            Gateway gateway = new Gateway();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("Gateway_I", gateway);
            registry.rebind("Gateway_callback", gateway);
            System.out.println("Gateway ready!");



        } catch (RemoteException re) {
            System.out.println("Exception in Gateway-Downloader.main: " + re);
        }
    }
}
