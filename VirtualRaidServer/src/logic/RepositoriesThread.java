package logic;

import classes.Common;
import classes.Heartbeat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * RepositoriesThread classe.
 * Thread responsável pela recepção de Heartbeats e ficheiros de repositórios.
 * 
 * @author Team
 */
public class RepositoriesThread extends Thread {
    
    // Constantes
    public static final int UDP_PORT = 4999;
    
    private DatagramSocket repoSocket;
    private boolean isCanceled = false;
    
    public RepositoriesThread() {
        try {
            repoSocket = new DatagramSocket(UDP_PORT);
        } catch (SocketException e) {
            System.err.println("Não foi possível iniciar RepositoriesThread:\n\t"+e.getMessage());
            repoSocket = null;
        }
    }

    @Override
    public void run() {
        if (repoSocket == null)
            return;
        
        if (isCanceled)
            return;
        
        DatagramPacket packet;
        ObjectInputStream in;
        Object obj;

        try {
            while (!isCanceled) {
                packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
                repoSocket.receive(packet);
                
                if (isCanceled)
                    return;
                                
                try {
                    // Recebe objecto por UDP
                    in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    obj = in.readObject();
                    in.close();
                    
                    if (isCanceled)
                        return;
                    
                    if (obj != null && obj instanceof Heartbeat) {
                        
                        // Teste
                        //System.out.println(packet.getAddress().getHostAddress()+" - total conexões: "+((Heartbeat) obj).getCurrentConnections());
                    }
                    
                    // ToDo: lista de ficheiros
                    
                } catch (ClassNotFoundException | IOException e) {
                    System.out.println("Impossibilidade de aceder ao conteudo da mensagem recebida!");
                }
            }
        } catch (IOException e) {
            if (!repoSocket.isClosed()) {
                repoSocket.close();
            }
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }
    
}
