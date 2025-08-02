package com.googol.rmi;

import java.io.Serializable;

public class Pesquisas implements Serializable {
    private String termo;
    private int frequencia;

    public Pesquisas (String termo, int frequencia) {
        this.termo = termo;
        this.frequencia = frequencia;
    }

    public String getTermo() {
        return termo;
    }

    public int getFrequencia() {
        return frequencia;
    }
}
