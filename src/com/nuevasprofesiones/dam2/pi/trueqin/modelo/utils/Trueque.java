package com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils;

import java.io.Serializable;

public class Trueque implements Serializable {

    private String titulo;
    private int idAnuncio;

    private int idUs;
    private byte estado;
    private String nombre, email, telefono;

    public Trueque(int idAnuncio, String titulo, byte estado, int idUs) {
        this.idAnuncio = idAnuncio;
        this.titulo = titulo;
        this.estado = estado;
        this.idUs = idUs;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getIdUs() {
        return idUs;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public int getIdAnuncio() {
        return this.idAnuncio;
    }

    public byte getEstado() {
        return this.estado;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getEmail() {
        return this.email;
    }

    public String getTelefono() {
        return this.telefono;
    }
}
