package logic;

import classes.Common;
import classes.Request;
import classes.VirtualFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * DeleteThread classe.
 * Thread responsável por aceitar pedidos para eliminar ficheiros.
 * 
 * @author Team
 */
public class DeleteThread extends Thread {
    
    // Constantes
    
    private final RepoController ctrl;
    private MulticastSocket msocket;
    private boolean isCanceled = false;
    
    public DeleteThread(RepoController ctrl) {
        this.ctrl = ctrl;
        InetAddress group;
        try {
            group = InetAddress.getByName(Common.DELETE_ADDRESS);
            msocket = new MulticastSocket(Common.DELETE_PORT);
            msocket.joinGroup(group);
        } catch (IOException e) {
            System.err.println("Nao foi possivel iniciar DeleteThread:\n\t"+e.getMessage());
            msocket = null;
        }
    }

    @Override
    public void run() {
        if (msocket == null)
            return;
        
        if (isCanceled)
            return;

        DatagramPacket packet;
        ObjectInputStream in;
        Object obj = null;

        try {
            while (!isCanceled) {
                packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
                msocket.receive(packet);
                
                // Recebe objecto por UDP
                in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                try {
                    obj = in.readObject();
                } catch (ClassNotFoundException e) {
                    continue;
                }
                in.close();
                
                if (isCanceled)
                    return;
                
                if (obj != null && obj instanceof Request) {
                    // Receber ficheiro para eliminar
                    Request req = (Request) obj;
                    ctrl.deleteFile(req.getFile());
                }
            }
        } catch (IOException e) {
            if (!msocket.isClosed()) {
                msocket.close();
            }
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }
    
}
