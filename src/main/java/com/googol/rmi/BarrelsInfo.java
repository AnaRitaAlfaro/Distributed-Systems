package com.googol.rmi;

import java.io.Serializable;
public class BarrelsInfo implements Serializable{
    private int barrelId;
    private int tamIndice;
    private long tempoResp;
    public BarrelsInfo(int barrelId,int tamIndice,long tempoResp){
        this.barrelId=barrelId;
        this.tamIndice=tamIndice;
        this.tempoResp=tempoResp;
    }
    public int getBarrelId(){
        return barrelId;
    }

    public int getTamIndice(){
        return tamIndice;
    }
    public long getTempResp(){
        return tempoResp;
    }
}
