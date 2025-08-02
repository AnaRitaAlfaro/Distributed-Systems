package com.googol.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Barrel_callback extends Remote {
    public List<PageInfo> pesquisar(List<String> termo) throws RemoteException;
    public int getId() throws RemoteException;

    public int getTamIndice() throws RemoteException;


}
