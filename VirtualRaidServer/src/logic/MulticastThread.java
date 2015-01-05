package logic;

import classes.Common;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;

/**
 * MulticastThread classe.
 * Thread responsável por aceitar pedidos à procura do servidor.
 * 
 * @author Team
 */
public class MulticastThread extends Thread {
    
    private final ServerSocket serverSocket;
    private MulticastSocket msocket;
    private boolean isCanceled = false;
    
    public MulticastThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        
        InetAddress group;
        try {
            group = InetAddress.getByName(Common.MULTICAST_ADDRESS);
            msocket = new MulticastSocket(Common.MULTICAST_PORT);
            msocket.joinGroup(group);
        } catch (IOException e) {
            System.err.println("Nao foi possivel iniciar MulticastThread:\n\t"+e.getMessage());
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
        String msg;

        try {
            while (!isCanceled) {
                packet = new DatagramPacket(new byte[Common.MULTICAST_SECRETKEY_UDP.getBytes().length], Common.MULTICAST_SECRETKEY_UDP.getBytes().length);
                msocket.receive(packet);
                
                if (isCanceled)
                    return;
                
                msg = new String(packet.getData(), 0, packet.getLength());
                                
                if (msg.equals(Common.MULTICAST_SECRETKEY_UDP)) {
                    String hostAddress = InetAddress.getLocalHost().getHostAddress();
                    packet.setData(hostAddress.getBytes());
                    packet.setLength(hostAddress.getBytes().length);
                    msocket.send(packet);
                    
                    String hostPort = Integer.toString(RepositoriesThread.UDP_PORT);
                    packet.setData(hostPort.getBytes());
                    packet.setLength(hostPort.getBytes().length);
                    msocket.send(packet);
                }
                else if (msg.equals(Common.MULTICAST_SECRETKEY_TCP)) {
                    String hostAddress = InetAddress.getLocalHost().getHostAddress();
                    packet.setData(hostAddress.getBytes());
                    packet.setLength(hostAddress.getBytes().length);
                    msocket.send(packet);
                    
                    String hostPort = Integer.toString(serverSocket.getLocalPort());
                    packet.setData(hostPort.getBytes());
                    packet.setLength(hostPort.getBytes().length);
                    msocket.send(packet);
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
