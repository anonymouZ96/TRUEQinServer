package com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String nombre, apes, email, fecha, contras1, contras2, telefono;

    public Usuario(String nombre, String apes, String email, String fecha, String contras1, String contras2, String telefono) {
        this.nombre = nombre;
        this.apes = apes;
        this.email = email;
        this.fecha = fecha;
        this.contras1 = contras1;
        this.contras2 = contras2;
        this.telefono = telefono;
    }

    public String getNombre() {
        return this.nombre;
    }

    public String getApes() {
        return this.apes;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFecha() {
        return this.fecha;
    }

    public String getContras1() {
        return this.contras1;
    }

    public String getContras2() {
        return this.contras2;
    }

    public String getTelefono() {
        return this.telefono;
    }
}
