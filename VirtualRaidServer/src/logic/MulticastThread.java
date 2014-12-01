package logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;

public class MulticastThread extends Thread {
    // Constantes
    public static final String MULTICAST_ADDRESS = "230.30.30.30";
    public static final int MULTICAST_PORT = 9999;
    public static final String MULTICAST_SECRETKEY = "What'sYourIP?";
    public static final int MAX_SIZE = 1000;
    
    private final ServerSocket serverSocket;
    private MulticastSocket msocket;
    private boolean isCanceled = false;
    
    public MulticastThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        
        InetAddress group;
        try {
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            msocket = new MulticastSocket(MULTICAST_PORT);
            msocket.joinGroup(group);
        } catch (IOException e) {
            System.err.println("Não foi possível iniciar MulticastThread:\n\t"+e.getMessage());
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
                packet = new DatagramPacket(MULTICAST_SECRETKEY.getBytes(), MULTICAST_SECRETKEY.getBytes().length);
                msocket.receive(packet);
                
                if (isCanceled)
                    return;
                
                msg = new String(packet.getData(), 0, packet.getLength());
                                
                if (msg.equals(MULTICAST_SECRETKEY)) {
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
