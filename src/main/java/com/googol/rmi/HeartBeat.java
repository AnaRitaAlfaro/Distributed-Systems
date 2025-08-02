package com.googol.rmi;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HeartBeat extends Thread {
    private final int barrelId;
    private final int interval;
    private volatile boolean running = true;

    public HeartBeat(int barrelId, int interval) {
        this.barrelId = barrelId;
        this.interval = interval;
    }

    @Override
    public void run() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Gateway_I gateway = (Gateway_I) registry.lookup("Gateway_I");

            while (running) {
                gateway.receiveHeartBeat(barrelId);
                System.out.println("Heartbeat enviado para o Gateway pelo Barrel " + barrelId);
                Thread.sleep(interval);
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("Erro no heartbeat: " + e.getMessage());
            }
        }
    }

}
