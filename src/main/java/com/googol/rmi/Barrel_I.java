package com.googol.rmi;
import java.rmi.Remote;

public interface Barrel_I extends Remote {
    public void call() throws java.rmi.RemoteException;
}
