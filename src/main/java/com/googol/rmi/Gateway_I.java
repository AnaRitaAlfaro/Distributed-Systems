package com.googol.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public interface Gateway_I extends Remote {
    public void addURlQueue(String url) throws java.rmi.RemoteException;
    public Queue<String> getAllURL () throws RemoteException;
    public String getNextURL() throws RemoteException;
    public void putURLBack(String url) throws RemoteException;
    public List<Barrel_callback> listarBarrelsAtivos() throws RemoteException;
    public  Barrel_callback getNextBarrel() throws RemoteException;
    void receiveHeartBeat(int barrelId) throws RemoteException;
    public List<PageInfo> pesquisaBarrel(String termo) throws RemoteException;
    public List<Integer> listarBarrelsIds() throws RemoteException;
    List<BarrelsInfo> getBarrelsInfo() throws RemoteException;
    public void registarTermo(String termo) throws RemoteException;
    public List<Pesquisas> getTop10Pesquisas() throws RemoteException;

}
