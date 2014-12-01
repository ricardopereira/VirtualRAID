package logic;

import classes.Common;
import classes.FileManager;
import classes.Repository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * RepoController classe.
 * Instância responsável pelo fluxo de comunicação dos ficheiros.
 * 
 * @author Team
 */
public class RepoController {
    
    public static final int MAX_SIZE = 1000;
    public static final int TIMEOUT = 5; //Segundos
    
    // Ligação dos Clientes
    private ServerSocket mainSocket;
    // Multicast para receber um pedido de remoção de uma ficheiro
    private MulticastThread multicastThread;
    // HeartBeat e lista de ficheiros
    private HeartbeatThread heartbeatThread;
    private DatagramSocket heartbeatSocket;
    private String serverAddress;
    private int serverPort;
    
    private final Repository self;
    private final int port;
    private final FileManager fileManager;
    
    public RepoController(String host, int listenPort, FileManager fm) {
        this.port = listenPort;
        
        // Criação do repositorio
        self = new Repository(host, port);
        // Carregar a lista de ficheiros
        this.fileManager = fm;
        this.fileManager.loadFiles(self.getFiles());
    }
    
    public Repository getRepository() {
        return self;
    }
    
    public boolean findServer() {
        ObjectInputStream in;
        Object result;
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(Common.MULTICAST_TIME_OUT*1000);
            
            // Envia broadcast para descobrir servidor
            DatagramPacket packet = new DatagramPacket(Common.MULTICAST_SECRETKEY_UDP.getBytes(), Common.MULTICAST_SECRETKEY_UDP.getBytes().length,
                    InetAddress.getByName(Common.MULTICAST_ADDRESS), Common.MULTICAST_PORT);
            socket.send(packet);
            
            // Recebe a resposta: IP
            packet.setData(new byte[MAX_SIZE]);
            packet.setLength(MAX_SIZE);
            socket.receive(packet);            
            serverAddress = new String(packet.getData(), 0, packet.getLength());
            
            // Recebe a resposta: Porto
            packet.setData(new byte[MAX_SIZE]);
            packet.setLength(MAX_SIZE);
            socket.receive(packet);
            serverPort = Integer.parseInt(new String(packet.getData(), 0, packet.getLength()));
            
            heartbeatSocket = new DatagramSocket();
            return true;
        } catch (IOException e) {
            heartbeatSocket = null;
            return false;
        }
    }
    
    public void startListeningClients() {
        // Socket do Servidor
        try {
            mainSocket = new ServerSocket(port);
        } catch (SocketException e) {
            System.err.println("Ocorreu um erro ao nível do socket TCP:\n\t" + e);
            return;
        } catch (IOException e) {
            System.err.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            return;
        }
        
        // Thread para receber um pedido DeleteFile
        multicastThread = new MulticastThread();
        multicastThread.start();

        // Thread para enviar heartbeats
        heartbeatThread = new HeartbeatThread(this);
        heartbeatThread.start();

        // Escuta os clientes
        try {
            processClientRequests();
        } finally {
            multicastThread.interrupt();
            // Fecha o socket do servidor
            try {
                mainSocket.close();
            } 
            catch (IOException e) {/*Silencio*/}
            finally { mainSocket = null; }
        }
    }
    
    private void processClientRequests() {
        Socket clientSocket;
        
        while (true) {
            // À espera de pedidos de ligação...
            try {
                clientSocket = mainSocket.accept();
            } catch (IOException e) {
                System.err.println("Ocorreu um erro enquanto aguardava por um pedido de ligação:\n\t" + e);
                return; //Termina o servidor
            }

            try {
                clientSocket.setSoTimeout(TIMEOUT*1000);
                System.out.println("Foi estabelecida ligação a "+ clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " no porto " + clientSocket.getLocalPort());
                // Inicia thread para o cliente
                
                //new ClientThread(clientSocket, getAllFiles()).start();
            } catch (IOException e) {
                System.out.println("Ocorreu um erro na ligação com o cliente: \n\t" + e);
                try {
                    clientSocket.close();
                } catch (IOException s) {/*Silencio*/}
            }
            
            System.out.println("A aguardar novo cliente");
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public DatagramSocket getHeartbeatSocket() {
        return heartbeatSocket;
    }
}
