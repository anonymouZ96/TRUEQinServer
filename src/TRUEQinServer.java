import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.sql.SQLException;
import java.util.TreeMap;

public class TRUEQinServer {

    private static TreeMap<String, Byte> mapIPsBloqueadas;

    public static void main(String arg[]) {
        ConexionDB conexion;
        try {
            mapIPsBloqueadas = new TreeMap<>();
            ServerSocket server = new ServerSocket(6000);
            System.out.println("Servidor encendido y escuchando en el puerto 6000");
            System.out.println("Servidor a la espera de clientes");
            while (true) {
                Socket cliente = server.accept();
                conexion = new ConexionDB();
                new ServerThread(cliente, conexion).start();
                //new ServerThread(cliente).start();
            }
        } catch (SQLException sqle) {
            System.err.println(sqle.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    protected static void aniadeIP(String ip) {
        mapIPsBloqueadas.put(ip, (byte) 1);
    }

    protected static void aniadeIntento(String ip) {
        if (getNumIntentos(ip) < 5) {
            mapIPsBloqueadas.put(ip, (byte) (mapIPsBloqueadas.get(ip) + 1));
        }
    }

    protected static void bloqueaIP(String ip) throws InterruptedException {
        new threadIpBloqueada(ip).start();
    }

    protected static byte getNumIntentos(String ip) {
        byte intentos;
        intentos = -1;
        if (mapIPsBloqueadas.get(ip) != null) {
            intentos = mapIPsBloqueadas.get(ip);
        }
        return intentos;
    }

    protected static TreeMap getTreeMap() {
        return mapIPsBloqueadas;
    }

}

class threadIpBloqueada extends Thread {
    private String ip;

    threadIpBloqueada(String ip) throws InterruptedException {
        this.ip = ip;
    }

    public void run() {
        try {
            Thread.sleep(300000);
            TRUEQinServer.getTreeMap().remove(this.ip);
        } catch (InterruptedException ie) {
            System.err.println(ie.getMessage());
        }
    }
}