package com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils;

import java.io.Serializable;

public class Anuncio implements Serializable {
    private static final long serialVersionUID = 1L;
    private String titulo, descrip, ubicacion, puntos;
    private int idUs, id;
    private byte categoria;

    public Anuncio(int id, String titulo, String ubicacion, String puntos) {
        this.id = id;
        this.titulo = titulo;
        this.ubicacion = ubicacion;
        this.puntos = puntos;
    }

    public Anuncio(String titulo, String descrip, String ubicacion, String puntos, byte categoria) {
        this.titulo = titulo;
        this.descrip = descrip;
        this.ubicacion = ubicacion;
        this.puntos = puntos;
        this.categoria = categoria;
    }

    public Anuncio(String titulo, String descrip, String ubicacion, int id, String puntos, byte categoria) {
        this.titulo = titulo;
        this.descrip = descrip;
        this.ubicacion = ubicacion;
        this.id = id;
        this.puntos = puntos;
        this.categoria = categoria;
    }


    public String getTitulo() {
        return titulo;
    }

    public String getDescrip() {
        return descrip;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public int getIdUs() {
        return idUs;
    }

    public void setIdUs(int idUs) {
        this.idUs = idUs;
    }

    public String getPuntos() {
        return puntos;
    }

    public byte getCategoria() {
        return categoria;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.titulo.concat("\n").concat("\nPuntos necesarios: ").concat(this.puntos);
    }
}
