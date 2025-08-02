package com.googol.rmi;
import java.io.Serializable;

public class PageInfo implements Serializable {
    private String url;
    private String titulo;
    private String texto;
    private int importancia; //numero de links para a pagina

    public PageInfo(String url,String titulo, String texto, int importancia){
        this.url = url;
        this.titulo = titulo;
        this.texto = texto;
        this.importancia = importancia;
    }

    public String getUrl() {return url;}
    public String getTitulo() {return titulo;}
    public String getTexto() {return texto;}
    public int getImportancia() {return importancia;}

    public void setImportancia(int importancia){
        this.importancia = importancia;
    }

    public String toString(){
        return "Título: " + titulo+ "\nUrl: "+url+"\ncitaçao de texto: "+texto+"\n";
    }


}
