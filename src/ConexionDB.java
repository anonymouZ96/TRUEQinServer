
import com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils.Anuncio;
import com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils.Trueque;
import com.nuevasprofesiones.dam2.pi.trueqin.modelo.utils.Usuario;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConexionDB {

    private String host;
    private String port;
    private String user;
    private String pass;
    private String bd;
    private String classfn;
    private String[] categorias;
    private Connection con;
    private BasicDataSource basicDataSource;
    private DataSource dataSource;

    public ConexionDB() throws SQLException, NullPointerException, FileNotFoundException, IOException {
        this.confConexion();
        this.creaConexion();
        this.buscarCateg();
    }

    private void confConexion() throws FileNotFoundException, IOException {
        Properties prop;
        prop = new Properties();
        prop.load(new FileReader("DB/conexion.properties".replace('/', File.separatorChar)));
        this.host = prop.getProperty("HOST");
        this.port = prop.getProperty("PORT");
        this.user = prop.getProperty("USER");
        this.pass = prop.getProperty("PASS");
        this.bd = prop.getProperty("BD");
        this.classfn = prop.getProperty("CLASSFN");
    }

    public void creaConexion() throws SQLException, NullPointerException {
        String url;
        url = "jdbc:mysql://".concat(host).concat(":").concat(port).concat("/").concat(bd).concat("?autoReconnect=true&useSSL=false");
        this.basicDataSource = new BasicDataSource();
        this.basicDataSource.setDriverClassName(classfn);
        this.basicDataSource.setUsername(user);
        this.basicDataSource.setPassword(pass);
        this.basicDataSource.setUrl(url);
        this.basicDataSource.setValidationQuery("SELECT 1");
        this.dataSource = basicDataSource;
        this.con = this.dataSource.getConnection();
    }

    public void cierraConexion() throws SQLException {
        this.con.close();
    }

    public int exitoInicioSes(String email, String contras) throws SQLException {
        Statement s;
        String llave;
        int idUs;
        ResultSet rs;
        s = con.createStatement();
        idUs = -1;
        llave = "np1920";
        rs = s.executeQuery("SELECT ID_US FROM CONTRAS "
                + "WHERE VALOR = AES_ENCRYPT('" + contras + "', '" + llave + "') "
                + "AND ID_US = (SELECT ID FROM USUARIOS "
                + "WHERE EMAIL = '" + email + "')");
        if (rs.next()) {
            idUs = rs.getInt(1);
            //this.setPuntosActuales(idUs);
        }
        return idUs;
    }

    public int registraUser(Usuario user) throws SQLException {
        String llave;
        ResultSet rs;
        Statement s;
        int idUs;
        this.con.prepareStatement("INSERT INTO USUARIOS"
                + " (NOMBRE, APES, FEC_NAC, PUNTOS, EMAIL, TELEFONO)"
                + " VALUES "
                + "('"
                + user.getNombre() + "','"
                + user.getApes() + "','"
                + user.getFecha() + "',"
                + 300 + ",'"
                + user.getEmail() + "',"
                + Integer.parseInt(user.getTelefono())
                + ")").execute();
        s = this.con.createStatement();
        rs = s.executeQuery("SELECT MAX(ID) FROM USUARIOS");
        idUs = -1;
        if (rs.next()) {
            llave = "np1920";
            idUs = rs.getInt(1);
            con.prepareStatement("INSERT INTO CONTRAS"
                    + " (ID_US, VALOR)"
                    + " VALUES "
                    + "("
                    + idUs + ","
                    + "AES_ENCRYPT('" + user.getContras1() + "','" + llave + "'))").execute();
        }
        rs.close();
        return idUs;
    }

    public String[] obtieneAutorAnuncio(int idAnunc) throws SQLException {
        String[] datos;
        ResultSet rs;
        Statement s;
        datos = null;
        s = con.createStatement();
        rs = s.executeQuery("SELECT ID, NOMBRE, APES, EMAIL, TELEFONO, FEC_NAC FROM USUARIOS"
                + " WHERE ID = (SELECT ID_US FROM ANUNCIOS WHERE ID = " + idAnunc + ")");
        if (rs.next()) {
            datos = new String[5];
            datos[0] = Integer.toString(rs.getInt(1));
            datos[1] = rs.getString(2).concat(" ").concat(rs.getString(3));
            datos[2] = rs.getString(4);
            datos[3] = Integer.toString(rs.getInt(5));
            datos[4] = rs.getString(6);
        }
        rs.close();
        return datos;
    }

    public String[] obtieneDatosUsuarios(int idUs) throws SQLException {
        ResultSet rs;
        Statement s;
        String[] datosUsuarios;
        s = con.createStatement();
        rs = s.executeQuery("SELECT NOMBRE, APES, FEC_NAC, EMAIL, TELEFONO FROM USUARIOS WHERE ID = " + idUs);
        datosUsuarios = null;
        if (rs.next()) {
            datosUsuarios = new String[4];
            datosUsuarios[0] = rs.getString(1).concat(" ").concat(rs.getString(2));
            datosUsuarios[1] = rs.getString(3);
            datosUsuarios[2] = rs.getString(4);
            datosUsuarios[3] = Integer.toString(rs.getInt(5));
        }
        return datosUsuarios;
    }

//    public boolean setPuntosActuales(int idUs) throws SQLException {
//        Statement s;
//        ResultSet rs;
//        int puntosPos, puntosNeg;
//        boolean exito;
//        exito = false;
//        s = con.createStatement();
//        rs = s.executeQuery("SELECT SUM(PUNTOS) FROM ANUNCIOS"
//                + " WHERE ID IN (SELECT ID_ANUNCIO FROM SOLICITUDES"
//                + " WHERE ID_US = " + idUs
//                + " AND"
//                + " ESTADO = " + 1 + ")");
//        if (rs.next()) {
//            puntosNeg = rs.getInt(1);
//            rs = s.executeQuery("SELECT SUM(PUNTOS) FROM ANUNCIOS"
//                    + " WHERE ID_US = " + idUs
//                    + " AND "
//                    + "ID IN (SELECT ID_ANUNCIO FROM SOLICITUDES"
//                    + " WHERE ESTADO = " + 1 + ")");
//            if (rs.next()) {
//                puntosPos = rs.getInt(1);
//                if (puntosNeg == 0) {
//                    con.prepareStatement("UPDATE USUARIOS"
//                            + " SET PUNTOS = " + (300 + puntosPos)).execute();
//                    exito = true;
//                } else {
//                    con.prepareStatement("UPDATE USUARIOS"
//                            + " SET PUNTOS = " + (puntosPos - puntosNeg)).execute();
//                    exito = true;
//                }
//            }
//        }
//        return exito;
//    }

    public int getPuntos(int idUs) throws SQLException {
        int puntos;
        Statement s;
        ResultSet rs;
        s = con.createStatement();
        puntos = -1;
        rs = s.executeQuery("SELECT PUNTOS FROM USUARIOS WHERE ID = " + idUs);
        if (rs.next()) {
            puntos = rs.getInt(1);
        }
        return puntos;
    }

    public void insertaAnuncio(Anuncio anunc) throws SQLException {
        con.prepareStatement("INSERT INTO ANUNCIOS"
                + " (ID_US, TITULO, DESCRIPCION, PUNTOS, CATEGORIA, UBICACION)"
                + " VALUES "
                + "("
                + anunc.getIdUs() + ",'"
                + anunc.getTitulo() + "','"
                + anunc.getDescrip() + "',"
                + anunc.getPuntos() + ","
                + anunc.getCategoria() + ",'"
                + anunc.getUbicacion() + "')").execute();
    }

    public Anuncio[] buscarAnuncios(String texto, int idUs) throws SQLException {
        Anuncio[] vecAnuncio;
        ResultSet rs;
        Statement s;
        short i;
        s = con.createStatement();
        rs = s.executeQuery("SELECT ID, TITULO, UBICACION, PUNTOS FROM ANUNCIOS "
                + "WHERE (TITULO LIKE '%" + texto + "%' OR DESCRIPCION LIKE '%" + texto + "%')"
                + " AND ID_US != " + idUs);
        rs.last();
        vecAnuncio = new Anuncio[rs.getRow()];
        rs.beforeFirst();
        i = 0;
        while (rs.next()) {
            vecAnuncio[i] = new Anuncio(rs.getInt(1), rs.getString(2), rs.getString(3), Short.toString(rs.getShort(4)));
            i++;
        }
        rs.close();
        return vecAnuncio;
    }

    public Anuncio[] buscarAnuncios(String texto, String ubicacion, int idUs) throws SQLException {
        Anuncio[] vecAnuncio;
        ResultSet rs;
        Statement s;
        short i;
        s = con.createStatement();
        rs = s.executeQuery("SELECT ID, TITULO, UBICACION, PUNTOS FROM ANUNCIOS "
                + "WHERE (TITULO LIKE '%" + texto + "%' OR DESCRIPCION LIKE '%" + texto + "%') AND UBICACION = '" + ubicacion + "'"
                + " AND ID_US != " + idUs);
        rs.last();
        vecAnuncio = new Anuncio[rs.getRow()];
        rs.beforeFirst();
        i = 0;
        while (rs.next()) {
            vecAnuncio[i] = new Anuncio(rs.getInt(1), rs.getString(2), rs.getString(3), Short.toString(rs.getShort(4)));
            i++;
        }
        rs.close();
        return vecAnuncio;
    }

    public Anuncio[] buscarAnuncios(byte idCateg, int idUs) throws SQLException {
        Anuncio[] vecAnuncio;
        ResultSet rs;
        Statement s;
        short i;
        s = con.createStatement();
        rs = s.executeQuery("SELECT ID, TITULO, UBICACION, PUNTOS FROM ANUNCIOS WHERE CATEGORIA = " + idCateg
                + " AND ID_US != " + idUs);
        rs.last();
        vecAnuncio = new Anuncio[rs.getRow()];
        rs.beforeFirst();
        i = 0;
        while (rs.next()) {
            vecAnuncio[i] = new Anuncio(rs.getInt(1), rs.getString(2), rs.getString(3), Short.toString(rs.getShort(4)));
            i++;
        }
        rs.close();
        return vecAnuncio;
    }

    public Anuncio[] buscarAnuncios(int idUs) throws SQLException {
        Anuncio[] vecAnuncio;
        ResultSet rs;
        Statement s;
        short i;
        s = con.createStatement();
        rs = s.executeQuery("SELECT ID, TITULO, UBICACION, PUNTOS FROM ANUNCIOS WHERE ID_US = " + idUs);
        rs.last();
        vecAnuncio = new Anuncio[rs.getRow()];
        rs.beforeFirst();
        i = 0;
        while (rs.next()) {
            vecAnuncio[i] = new Anuncio(rs.getInt(1), rs.getString(2), rs.getString(3), Short.toString(rs.getShort(4)));
            i++;
        }
        rs.close();
        return vecAnuncio;
    }

    public Anuncio verAnuncio(int idAnunc) throws SQLException {
        Anuncio anuncio;
        ResultSet rs;
        Statement s;
        anuncio = null;
        s = con.createStatement();
        rs = s.executeQuery("SELECT TITULO, DESCRIPCION, UBICACION, PUNTOS, CATEGORIA FROM ANUNCIOS WHERE ID = " + idAnunc);
        if (rs.next()) {
            anuncio = new Anuncio(rs.getString(1), rs.getString(2), rs.getString(3), Short.toString(rs.getShort(4)), rs.getByte(5));
        }
        rs.close();
        return anuncio;
    }

    public boolean solicitar(int idUs, int idAnunc) throws SQLException {
        boolean exito;
        exito = false;
        this.con.prepareStatement("INSERT INTO SOLICITUDES"
                + " (ID_ANUNCIO, ID_US, ESTADO)"
                + " VALUES "
                + "("
                + idAnunc + ","
                + idUs + ", 3)").execute();
        this.con.prepareStatement("UPDATE USUARIOS " +
                "SET PUNTOS_BLOQUEADOS = PUNTOS_BLOQUEADOS + " + getCosteAnuncio(idAnunc) +
                ", PUNTOS = PUNTOS - " + getCosteAnuncio(idAnunc) +
                "  WHERE ID = " + idUs).execute();
        exito = true;
        return exito;
    }

    public boolean cancelar(int idUs, int idAnunc) throws SQLException {
        boolean exito;
        exito = false;
        this.con.prepareStatement("UPDATE USUARIOS " +
                "SET PUNTOS_BLOQUEADOS = PUNTOS_BLOQUEADOS - " + getCosteAnuncio(idAnunc) +
                ", PUNTOS = PUNTOS + " + getCosteAnuncio(idAnunc) +
                "  WHERE ID = " + idUs).execute();
        this.con.prepareStatement("DELETE FROM SOLICITUDES " +
                "WHERE ID_ANUNCIO = " + idAnunc +
                " AND ID_US = " + idUs).execute();
        exito = true;
        return exito;
    }

    public Trueque[] getTrueqes(boolean op, int idUs) throws SQLException {  // True solicitudes / False solicitantes
        Trueque[] vecTrueques;
        String[] autor;
        ResultSet rs;
        Statement s;
        short i;
        s = con.createStatement();
        if (op) {
            rs = s.executeQuery("SELECT ANUNCIOS.ID, TITULO, ESTADO FROM ANUNCIOS, SOLICITUDES "
                    + "WHERE ANUNCIOS.ID = ID_ANUNCIO "
                    + "AND "
                    + "ANUNCIOS.ID IN (SELECT ID_ANUNCIO FROM SOLICITUDES "
                    + "WHERE ID_US = " + idUs + ")");
        } else {
            rs = s.executeQuery("SELECT ANUNCIOS.ID, TITULO, ESTADO, SOLICITUDES.ID_US FROM ANUNCIOS, SOLICITUDES "
                    + "WHERE ANUNCIOS.ID = ID_ANUNCIO "
                    + "AND "
                    + "ANUNCIOS.ID_US = " + idUs
                    + " AND "
                    + "ANUNCIOS.ID IN (SELECT ID_ANUNCIO FROM SOLICITUDES)");
        }
        rs.last();
        vecTrueques = new Trueque[rs.getRow()];
        rs.beforeFirst();
        i = 0;
        if (op) {
            while (rs.next()) {
                vecTrueques[i] = new Trueque(rs.getInt(1), rs.getString(2), rs.getByte(3), Integer.parseInt(obtieneAutorAnuncio(rs.getInt(1))[0]));
                i++;
            }
        } else {
            while (rs.next()) {
                vecTrueques[i] = new Trueque(rs.getInt(1), rs.getString(2), rs.getByte(3), rs.getInt(4));
                i++;
            }
        }
        rs.close();
        for (i = 0; i <= vecTrueques.length - 1; i++) {
            autor = obtieneDatosUsuarios(vecTrueques[i].getIdUs());
            vecTrueques[i].setNombre(autor[0]);
            vecTrueques[i].setEmail(autor[2]);
            vecTrueques[i].setTelefono(autor[3]);
        }
        return vecTrueques;
    }

    private short getCosteAnuncio(int idAnunc) throws SQLException {
        ResultSet rs;
        Statement s;
        short puntos;
        puntos = -1;
        s = con.createStatement();
        rs = s.executeQuery("SELECT PUNTOS FROM ANUNCIOS WHERE ID = " + idAnunc);
        if (rs.next()) {
            puntos = rs.getShort(1);
        }
        return puntos;
    }

    public boolean confirmarTrueque(int idAnunc, int idUs, byte opcion) throws SQLException {
        boolean exito;
        con.prepareStatement("UPDATE SOLICITUDES"
                + " SET ESTADO = " + opcion
                + " WHERE ID_US = " + idUs
                + " AND "
                + "ID_ANUNCIO = " + idAnunc).execute();
        if (opcion == 1) {
            //Comprador
            con.prepareStatement("UPDATE USUARIOS SET PUNTOS_BLOQUEADOS = PUNTOS_BLOQUEADOS - " + getCosteAnuncio(idAnunc) +
                    " WHERE ID = " + idUs).execute();

            //Vendedor
            con.prepareStatement("UPDATE USUARIOS SET PUNTOS = " + (getPuntos(Short.parseShort(obtieneAutorAnuncio(idAnunc)[0])) + getCosteAnuncio(idAnunc)) + " WHERE ID = " + Short.parseShort(obtieneAutorAnuncio(idAnunc)[0])).execute();
        } else {
            if (opcion == 2) {
                //Comprador
                this.con.prepareStatement("UPDATE USUARIOS " +
                        "SET PUNTOS_BLOQUEADOS = PUNTOS_BLOQUEADOS -   " + getCosteAnuncio(idAnunc) +
                        ", PUNTOS = PUNTOS + " + getCosteAnuncio(idAnunc) +
                        "  WHERE ID = " + idUs).execute();
            }
        }
        exito = true;
        return exito;
    }

    public String[] getCategorias() {
        return this.categorias;
    }

    public void buscarCateg() throws SQLException {
        Statement s;
        ResultSet rs;
        s = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs = s.executeQuery("SELECT VALOR FROM CATEGORIAS");
        rs.last();
        this.categorias = new String[rs.getRow()];
        rs.beforeFirst();
        while (rs.next()) {
            this.categorias[rs.getRow() - 1] = (String) rs.getObject(1);
        }
        rs.close();
    }

    public void editar(String nuevo, int id, byte op) throws SQLException {
        String sql;
        sql = "";
        if (op == 1) {
            sql = "UPDATE ANUNCIOS"
                    + " SET TITULO = '" + nuevo + "' "
                    + "WHERE ID = " + id;
        } else {
            if (op == 2) {
                sql = "UPDATE ANUNCIOS"
                        + " SET DESCRIPCION = '" + nuevo + "' "
                        + "WHERE ID = " + id;
            } else {
                if (op == 3) {
                    sql = "UPDATE ANUNCIOS"
                            + " SET UBICACION = '" + nuevo + "' "
                            + "WHERE ID = " + id;
                } else {
                    if (op == 4) {
                        sql = "UPDATE ANUNCIOS"
                                + " SET PUNTOS = " + Short.parseShort(nuevo)
                                + " WHERE ID = " + id;
                    } else {
                        if (op == 5) {
                            sql = "UPDATE ANUNCIOS"
                                    + " SET CATEGORIA = " + (Byte.parseByte(nuevo) + 2)
                                    + " WHERE ID = " + id;
                        } else {
                            if (op == 6) {
                                sql = "UPDATE USUARIOS"
                                        + " SET EMAIL = '" + nuevo + "' "
                                        + "WHERE ID = " + id;
                            } else {
                                if (op == 7) {
                                    sql = "UPDATE USUARIOS"
                                            + " SET TELEFONO = " + Integer.parseInt(nuevo) + " "
                                            + "WHERE ID = " + id;
                                } else {
                                    if (op == 8) {
                                        String llave;
                                        llave = "np1920";
                                        sql = "UPDATE CONTRAS"
                                                + " SET VALOR = AES_ENCRYPT('" + nuevo + "','" + llave + "')"
                                                + " WHERE ID_US = " + id;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        con.prepareStatement(sql).execute();
    }

    public boolean check(int idUs, String cad, byte op) throws SQLException {
        boolean exito;
        Statement s;
        String sql;
        ResultSet rs;
        exito = false;
        s = con.createStatement();
        rs = null;
        if (op == 1) {                  //E-mail existente
            sql = "SELECT * FROM USUARIOS "
                    + "WHERE EMAIL = '" + cad + "'";
            exito = true;
            rs = s.executeQuery(sql);
            if (rs.next()) {
                exito = false;
            }
        } else {
            if (op == 2) {              //Contraseña antigua correcta
                String llave;
                llave = "np1920";
                sql = "SELECT AES_DECRYPT(VALOR,'" + llave + "') FROM CONTRAS WHERE ID_US = " + idUs;
                rs = s.executeQuery(sql);
                if (rs.next()) {
                    if (rs.getString(1).equals(cad)) {
                        exito = true;
                    }
                }
            } else {
                if (op == 3) {           //Puntos suficientes
                    sql = "SELECT PUNTOS FROM ANUNCIOS WHERE ID = " + Integer.parseInt(cad);
                    rs = s.executeQuery(sql);
                    if (rs.next() && getPuntos(idUs) >= rs.getInt(1)) {
                        exito = true;
                    }
                } else {
                    if (op == 4) {       //Ya solicitado
                        sql = "SELECT ESTADO FROM SOLICITUDES WHERE ID_US = " + idUs
                                + " AND "
                                + "ID_ANUNCIO = " + Integer.parseInt(cad);
                        rs = s.executeQuery(sql);
                        if (rs.next()) {
                            exito = rs.getByte(1) != 3;
                        } else {
                            exito = true;
                        }
                        //exito = !rs.next();
                    }
                }
            }
        }
        if (rs != null) {
            rs.close();
        }
        return exito;
    }
}