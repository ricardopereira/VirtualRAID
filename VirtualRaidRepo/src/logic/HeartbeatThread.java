package logic;

import classes.Common;
import classes.Heartbeat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HeartbeatThread classe.
 * Thread responsável por enviar periodicamente o estado do repositório 
 * ao servidor.
 * 
 * @author Team
 */
public class HeartbeatThread extends Thread {
    
    // Constantes
    private static final int HEARTBEAT_RECUR = 3; //Segundos
    
    private final RepoController ctrl;
    private boolean isCanceled = false;
    
    public HeartbeatThread(RepoController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public void run() {  
        if (isCanceled)
            return;
        
        DatagramPacket packet;
        ObjectInputStream in;
        ObjectOutputStream out;
        Object obj;
        ByteArrayOutputStream buff;
        
        try {
            while (!isCanceled) {
                packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
                packet.setAddress(InetAddress.getByName(ctrl.getServerAddress()));
                packet.setPort(ctrl.getServerPort());
                
                buff = new ByteArrayOutputStream();
                out = new ObjectOutputStream(buff);
                // Teste
                out.writeObject(new Heartbeat(7));
                out.flush();
                out.close();

                packet.setData(buff.toByteArray());
                packet.setLength(buff.size());
                
                // Envia para o servidor
                ctrl.getHeartbeatSocket().send(packet);
                
                if (isCanceled) {
                    disconnect();
                    return;
                }
                
                try {
                    Thread.sleep(HEARTBEAT_RECUR*1000);
                } catch (InterruptedException e) {
                    disconnect();
                    return;
                }
            }
        } catch (IOException e) {
            disconnect();
        }
    }
    
    private void disconnect() {
        if (!ctrl.getHeartbeatSocket().isClosed()) {
            ctrl.getHeartbeatSocket().close();
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }
    
}
