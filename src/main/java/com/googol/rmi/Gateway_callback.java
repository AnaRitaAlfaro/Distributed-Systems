package com.googol.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Gateway_callback extends Remote {

    public void registaBarrel(Barrel_callback barrel) throws RemoteException;
    public List<Barrel_callback> listarBarrelsAtivos() throws RemoteException;
}
