import com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils.Anuncio;
import com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils.Usuario;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ServerThread extends Thread {

    private ConexionDB conexion;
    private Socket cliente;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private DataOutputStream dos;
    private DataInputStream dis;
    private int idUs;
    private String ip;

    public ServerThread(Socket cliente, ConexionDB conexion) {
        this.cliente = cliente;
        this.ip = this.cliente.getRemoteSocketAddress().toString().substring(1, this.cliente.getRemoteSocketAddress().toString().indexOf(":"));
        this.conexion = conexion;
    }

    public void run() {
        byte idActivity;
        try {
            this.oos = new ObjectOutputStream(this.cliente.getOutputStream());
            this.ois = new ObjectInputStream(this.cliente.getInputStream());
            this.dos = new DataOutputStream(this.cliente.getOutputStream());
            this.dis = new DataInputStream(this.cliente.getInputStream());
//            while (this.cliente.isConnected()) {
            while (!this.cliente.isClosed()) {
                try {
                    if (this.dis.available() > 0) {
                        idActivity = this.dis.readByte();
                        if (idActivity == 1) {

                        } else {
                            if (idActivity == 2) {
                                this.iniciaSesion();
                            } else {
                                if (idActivity == 3) {
                                    this.registro();
                                } else {
                                    if (idActivity == 4) {

                                    } else {
                                        if (idActivity == 5) {
                                            this.listaAnuncios(this.dis.readByte());
                                        } else {
                                            if (idActivity == 6) {
                                                this.anuncio(this.dis.readByte());
                                            } else {
                                                if (idActivity == 7) {
                                                    this.categorias();
                                                } else {
                                                    if (idActivity == 8) {
                                                        this.trueques(this.dis.readByte());
                                                    } else {
                                                        if (idActivity == 9) {
                                                            this.getMisDatos();
                                                        } else {
                                                            if (idActivity == 10) {
                                                                this.nuevoAnuncio();
                                                            } else {
                                                                if (idActivity == 11) {
                                                                    this.check(this.dis.readByte());
                                                                } else {
                                                                    if (idActivity == 12) {
                                                                        this.editarMisDatos();
                                                                    } else {
                                                                        if (idActivity == 13) {
                                                                            this.editarAnuncio();
                                                                        } else {
                                                                            if (idActivity == 14) {
                                                                                this.cierraConexion();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (EOFException eofe) {
                    System.err.println(eofe.getMessage());
                } catch (InterruptedException ie) {
                    System.err.println(ie.getMessage());
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe.getMessage());
        }
    }

    private boolean compruebaFecha(String cadFecha) {
        boolean correcto = false;
        GregorianCalendar fechAct;
        byte dia, mes;
        short anio;
        GregorianCalendar fechIntro;
        dia = Byte.parseByte(cadFecha.substring(cadFecha.lastIndexOf("/") + 1));
        mes = Byte.parseByte(cadFecha.substring(cadFecha.indexOf("/") + 1, cadFecha.lastIndexOf("/")));
        anio = Short.parseShort(cadFecha.substring(0, cadFecha.indexOf("/")));
        fechAct = new GregorianCalendar();
        fechIntro = new GregorianCalendar(anio, mes - 1, dia);
        if (anio > 1582) {
            if (mes > 0 && mes <= 12) {
                if (mes == 1 || mes == 3 || mes == 5 || mes == 7 || mes == 8 || mes == 10 || mes == 12) {
                    correcto = dia <= 31;
                } else if (mes == 2) {
                    if (fechIntro.isLeapYear(anio)) {
                        correcto = dia <= 29;
                    } else {
                        correcto = dia <= 28;
                    }
                } else {
                    correcto = dia <= 30;
                }
            }
        }
        if (correcto) {
            if (fechIntro.get(Calendar.YEAR) == fechAct.get(Calendar.YEAR)) {
                if (fechIntro.get(Calendar.MONTH) == fechAct.get(Calendar.MONTH)) {
                    if (fechIntro.get(Calendar.DAY_OF_MONTH) > fechAct.get(Calendar.DAY_OF_MONTH)) {
                        correcto = false;
                    }
                } else if (fechIntro.get(Calendar.MONTH) > fechAct.get(Calendar.MONTH)) {
                    correcto = false;
                }
            } else if (fechIntro.get(Calendar.YEAR) > fechAct.get(Calendar.YEAR)) {
                correcto = false;
            }
        }
        return correcto;
    }

    private void iniciaSesion() throws IOException, ClassNotFoundException, SQLException, InterruptedException {
        boolean[] resFormIniSes;
        byte i;
        String[] valores;
        valores = (String[]) this.ois.readObject();
        resFormIniSes = new boolean[4];
        if (TRUEQinServer.getNumIntentos(this.ip) < 5) {
            resFormIniSes[0] = true;
            i = 2;  //Email
            if (!valores[0].isEmpty() &&
                    valores[0].toUpperCase().matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$") &&
                    valores[0].length() <= 60) {
                resFormIniSes[i] = true;
            }
            i++;    //Contraseña
            if (valores[1].length() >= 6 &&
                    valores[1].toUpperCase().matches("[A-Z0-9]+") &&
                    valores[1].length() <= 30) {
                resFormIniSes[i] = true;
            }
            i = 2;
            while (i <= resFormIniSes.length - 1 && resFormIniSes[i]) {
                i++;
            }
            if (i == resFormIniSes.length) {
                this.idUs = this.conexion.exitoInicioSes(valores[0], valores[1]);
                valores[0] = Integer.toString(this.idUs);
                if (Integer.parseInt(valores[0]) > 0) {
                    valores[1] = Integer.toString(this.conexion.getPuntos(this.idUs));
                } else {
                    if (TRUEQinServer.getNumIntentos(this.ip) < 1) {
                        TRUEQinServer.aniadeIP(this.ip);
                    } else {
                        TRUEQinServer.aniadeIntento(this.ip);
                    }
                }
            } else {
                valores[0] = "-1";
            }
            resFormIniSes[1] = true;
        } else {
            TRUEQinServer.bloqueaIP(this.ip);
        }
        this.oos.writeObject(valores);
        this.oos.flush();
        this.oos.writeObject(resFormIniSes);
        this.oos.flush();
    }

    private void registro() throws IOException, ClassNotFoundException, SQLException {
        Usuario usuario;
        byte i;
        int idUs;
        boolean[] resRegistro;
        usuario = (Usuario) this.ois.readObject();
        resRegistro = new boolean[9];
        i = 1;  //Email
        if (!usuario.getEmail().isEmpty() &&
                usuario.getEmail().toUpperCase().matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$") &&
                usuario.getEmail().length() <= 60) {
            resRegistro[i] = true;
            if (this.conexion.check(this.idUs, usuario.getEmail(), (byte) 1)) {
                resRegistro[i + 1] = true;
            }
        }
        i++;    //(Email existente)
        i++;    //Contraseña
        if (usuario.getContras1().length() >= 6 &&
                usuario.getContras1().toUpperCase().matches("[A-Z0-9]+") &&
                usuario.getContras1().length() <= 30) {
            resRegistro[i] = true;
            if (usuario.getContras1().equals(usuario.getContras2())) {
                resRegistro[i + 1] = true;
            }
        }
        i++;    //Contraseñas no coinciden
        i++;    //Nombre
        if (!usuario.getNombre().isEmpty() &&
                usuario.getNombre().toUpperCase().matches("[A-ZÁÉÍÓÚÑ ]+") &&
                usuario.getNombre().length() <= 20) {
            resRegistro[i] = true;
        }
        i++;    //Apellidos
        if (!usuario.getNombre().isEmpty() &&
                usuario.getNombre().toUpperCase().matches("[A-ZÁÉÍÓÚÑ ]+") &&
                usuario.getNombre().length() <= 20) {
            resRegistro[i] = true;
        }
        i++;    //Teléfono
        if (!usuario.getTelefono().isEmpty() &&
                usuario.getTelefono().matches("\\d{9}")) {
            resRegistro[i] = true;
        }
        i++;    //Fecha nacimiento
        if (usuario.getFecha().length() >= 8 &&
                usuario.getFecha().matches("[\\d]{4}/[\\d]{1,2}/[\\d]{1,2}") &&
                usuario.getFecha().length() <= 10 &&
                compruebaFecha(usuario.getFecha())) {
            resRegistro[i] = true;
        }
        i = 1;
        while (i <= resRegistro.length - 1 && resRegistro[i]) {
            i++;
        }
        idUs = -1;
        if (i == resRegistro.length) {
            idUs = this.conexion.registraUser(usuario);
        }
        resRegistro[0] = true;
        this.dos.writeInt(idUs);
        this.dos.flush();
        this.oos.writeObject(resRegistro);
        this.oos.flush();
    }

    private void listaAnuncios(byte op) throws IOException, SQLException, ClassNotFoundException {
        if (op == 1) {          //Mis anuncios
            this.oos.writeObject(this.conexion.buscarAnuncios(this.idUs));
            this.oos.flush();
        } else {
            if (op == 2) {      //Buscar por parámetros
                Anuncio[] listaAnuncios;
                String[] valores;
                byte i;
                boolean[] resultados;
                valores = (String[]) this.ois.readObject();
                resultados = new boolean[3];
                i = 0;
                if (!valores[i].isEmpty() &&        //Título
                        valores[i].toUpperCase().matches("[A-ZÁÉÍÓÚÑ \\d]+") &&
                        valores[i].length() <= 30) {
                    resultados[i + 1] = true;
                }
                i++;
                if (!valores[i].isEmpty()) {        //Ubicación
                    if (valores[i].toUpperCase().matches("[A-ZÁÉÍÓÚÑ ]+") &&
                            valores[i].length() <= 20) {
                        resultados[i + 1] = true;
                    }
                } else {
                    resultados[i + 1] = true;
                }
                i = 1;
                while (i <= resultados.length - 1 && resultados[i]) {
                    i++;
                }
                listaAnuncios = null;
                if (i == resultados.length) {
                    if (!valores[1].isEmpty()) {
                        listaAnuncios = this.conexion.buscarAnuncios(valores[0], valores[1], this.idUs);
                    } else {
                        listaAnuncios = this.conexion.buscarAnuncios(valores[0], this.idUs);
                    }
                }
                resultados[0] = true;
                this.oos.writeObject(listaAnuncios);
                this.oos.flush();
                this.oos.writeObject(resultados);
                this.oos.flush();
            } else {
                if (op == 3) {  //Buscar por categoría

                    this.oos.writeObject(this.conexion.buscarAnuncios(this.dis.readByte(), this.dis.readInt()));
                    this.oos.flush();
                }
            }
        }
    }

    private void anuncio(byte op) throws IOException, SQLException {
        if (op == 1) {
            this.oos.writeObject(this.conexion.verAnuncio(this.dis.readInt()));
            this.oos.flush();
        } else {
            if (op == 2) {
                this.oos.writeObject(this.conexion.obtieneAutorAnuncio(this.dis.readInt()));
                this.oos.flush();
            } else {
                if (op == 3) {
                    this.dos.writeBoolean(this.conexion.check(this.dis.readInt(), Integer.toString(this.dis.readInt()), (byte) 3));
                    this.dos.flush();
                } else {
                    if (op == 4) {
                        this.dos.writeBoolean(this.conexion.check(this.dis.readInt(), Integer.toString(this.dis.readInt()), (byte) 4));
                        this.dos.flush();
                    } else {
                        if (op == 5) {
                            this.dos.writeBoolean(this.conexion.solicitar(this.dis.readInt(), this.dis.readInt()));
                            this.dos.flush();
                        }
                    }
                }
            }
        }
    }

    private void categorias() throws IOException {
        this.oos.writeObject(this.conexion.getCategorias());
        this.oos.flush();
    }

    private void trueques(byte op) throws IOException, SQLException, ClassNotFoundException {
        if (op == 1) {
            int[] valores;
            valores = (int[]) this.ois.readObject();
            this.dos.writeBoolean(this.conexion.confirmarTrueque(valores[0], valores[1], (byte) valores[2]));
            this.dos.flush();
        } else {
            if (op == 2) {
                this.oos.writeObject(this.conexion.getTrueqes(this.dis.readBoolean(), this.idUs));
                this.oos.flush();
            }
        }
    }

    private void getMisDatos() throws IOException, SQLException {
        this.oos.writeObject(this.conexion.obtieneDatosUsuarios(this.dis.readInt()));
    }

    private void nuevoAnuncio() throws IOException, ClassNotFoundException, SQLException {
        boolean[] resultados;
        Anuncio anuncio;
        byte i;
        anuncio = (Anuncio) this.ois.readObject();
        resultados = new boolean[5];
        i = 1;
        if (!anuncio.getTitulo().isEmpty() &&
                anuncio.getTitulo().toUpperCase().matches("[A-ZÁÉÍÓÚÑ \\d]+") &&
                anuncio.getTitulo().length() <= 60) {
            resultados[i] = true;
        }
        i++;
        if (!anuncio.getDescrip().isEmpty() &&
                anuncio.getDescrip().toUpperCase().matches("[A-ZÁÉÍÓÚÑ 0-9.,¡!¿?_:()]*") &&
                anuncio.getDescrip().length() <= 300) {
            resultados[i] = true;
        }
        i++;
        if (!anuncio.getUbicacion().isEmpty() &&
                anuncio.getUbicacion().toUpperCase().matches("[A-ZÁÉÍÓÚÑ]+") &&
                anuncio.getUbicacion().length() <= 30) {
            resultados[i] = true;
        }
        i++;
        if (!anuncio.getPuntos().isEmpty() &&
                anuncio.getPuntos().matches("\\d+") &&
                Short.parseShort(anuncio.getPuntos()) <= 9999) {
            resultados[i] = true;
        }
        i = 1;
        while (i <= resultados.length - 1 && resultados[i]) {
            i++;
        }
        if (i == resultados.length) {
            this.conexion.insertaAnuncio(anuncio);
        }
        resultados[0] = true;
        this.oos.writeObject(resultados);
        this.oos.flush();
    }

    private void check(byte op) throws IOException, SQLException {
        String cad;
        cad = this.dis.readUTF();
        this.dos.writeBoolean(this.conexion.check(this.idUs, cad, op));
    }

    private void editarMisDatos() throws IOException, ClassNotFoundException, SQLException {
        String[] datos;
        boolean[] resultados;
        resultados = new boolean[7];
        datos = (String[]) this.ois.readObject();
        if (!datos[0].isEmpty()) {       //Email
            if (datos[0].toUpperCase().matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$") &&
                    datos[0].length() <= 60) {
                resultados[1] = true;
                if (this.conexion.check(this.idUs, datos[0], (byte) 1)) {   //Email no está registrado por otro usuario
                    resultados[2] = true;
                    this.conexion.editar(datos[0], this.idUs, (byte) 5);
                }
            }
        } else {
            resultados[1] = true;
            resultados[2] = true;
        }
        if (!datos[1].isEmpty()) {       //Teléfono
            if (datos[1].matches("\\d{9}")) {
                resultados[3] = true;
                this.conexion.editar(datos[1], this.idUs, (byte) 6);
            }
        } else {
            resultados[3] = true;
        }
        if (!datos[2].isEmpty()) {       //Contraseña
            if (datos[2].length() >= 6 &&
                    datos[2].toUpperCase().matches("[A-Z0-9]+") &&
                    datos[2].length() <= 30) {
                resultados[4] = true;
                if (datos[2].equals(datos[3])) {
                    resultados[5] = true;
                    if (this.conexion.check(this.idUs, datos[4], (byte) 2)) {
                        resultados[6] = true;
                        this.conexion.editar(datos[2], this.idUs, (byte) 7);
                    }
                }
            }
        } else {
            resultados[4] = true;
            resultados[5] = true;
            resultados[6] = true;
        }
        resultados[0] = true;
        this.oos.writeObject(resultados);

    }

    private void editarAnuncio() throws IOException, SQLException, ClassNotFoundException {
        String[] valores;
        boolean[] resultados;
        valores = (String[]) this.ois.readObject();
        resultados = new boolean[2];
        if ((Byte.parseByte(valores[2]) == 1 &&         //Título
                !valores[0].isEmpty() &&
                valores[0].toUpperCase().matches("[A-ZÁÉÍÓÚÑ \\d]+") &&
                valores[0].length() <= 30) ||
                (Byte.parseByte(valores[2]) == 2 &&     //Descripción
                        !valores[0].isEmpty() &&
                        valores[0].toUpperCase().matches("[A-ZÁÉÍÓÚÑ 0-9.,¡!¿?_:()]*") &&
                        valores[0].length() <= 300) ||
                (Byte.parseByte(valores[2]) == 3 &&     //Ubicación
                        !valores[0].isEmpty() &&
                        valores[0].toUpperCase().matches("[A-ZÁÉÍÓÚÑ ]+") &&
                        valores[0].length() <= 20) ||
                (Byte.parseByte(valores[2]) == 4 &&     //Puntos
                        !valores[0].isEmpty() &&
                        valores[0].matches("\\d{1,4}") &&
                        Short.parseShort(valores[0]) <= 9999 &&
                        Short.parseShort(valores[0]) >= 1)) {
            resultados[1] = true;
            this.conexion.editar(valores[0], Integer.parseInt(valores[1]), Byte.parseByte(valores[2]));
        }
        resultados[0] = true;
        this.oos.writeObject(resultados);
    }

    private void cierraConexion() throws SQLException, IOException {
        this.dos.close();
        this.oos.close();
        this.dis.close();
        this.ois.close();
        this.conexion.cierraConexion();
    }

}