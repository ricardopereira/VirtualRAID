package logic;

import classes.BaseFile;
import classes.Common;
import classes.FileManager;
import classes.Repository;
import classes.RepositoryFile;
import classes.Request;
import classes.VirtualFile;
import enums.RequestType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * RepoController classe.
 * Instância responsável pelo fluxo de comunicação dos ficheiros.
 * 
 * @author Team
 */
public class RepoController {
    
    public static final int TIMEOUT = 15; //Segundos
    
    // Ligação dos Clientes
    private ServerSocket mainSocket;
    // Multicast para receber um pedido de remoção de uma ficheiro
    private DeleteThread deleteThread;
    // HeartBeat e lista de ficheiros
    private HeartbeatThread heartbeatThread;
    
    private DatagramSocket heartbeatSocket;
    private String serverAddress;
    private int serverPort;

    private final Repository self;
    private final FileManager fileManager;
    private int currentConnections;
    
    public RepoController(String host, int listenPort, FileManager fm) {
        // Criação do repositorio
        self = new Repository(host, listenPort /*Client*/);
        // Carregar a lista de ficheiros
        this.fileManager = fm;
        this.fileManager.loadFiles(self.getFiles());
    }
    
    public Repository getRepository() {
        return self;
    }
    
    public ArrayList<RepositoryFile> getFiles() {
        return self.getFiles();
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
            packet.setData(new byte[Common.UDPOBJECT_MAX_SIZE]);
            packet.setLength(Common.UDPOBJECT_MAX_SIZE);
            socket.receive(packet);            
            serverAddress = new String(packet.getData(), 0, packet.getLength());
            
            // Recebe a resposta: Porto
            packet.setData(new byte[Common.UDPOBJECT_MAX_SIZE]);
            packet.setLength(Common.UDPOBJECT_MAX_SIZE);
            socket.receive(packet);
            serverPort = Integer.parseInt(new String(packet.getData(), 0, packet.getLength()));
            
            heartbeatSocket = new DatagramSocket(self.getPort());
            return true;
        } catch (IOException e) {
            heartbeatSocket = null;
            return false;
        }
    }
    
    public void startListeningClients() {
        // Socket do Servidor
        try {
            mainSocket = new ServerSocket(self.getPort());
        } catch (SocketException e) {
            System.err.println("Ocorreu um erro ao nível do socket TCP:\n\t" + e);
            return;
        } catch (IOException e) {
            System.err.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            return;
        }
        
        // Enviar a primeira lista de ficheiros ao servidor
        filesChangedEvent();
        
        // Thread para receber um pedido DeleteFile
        deleteThread = new DeleteThread(this);
        deleteThread.start();

        // Thread para enviar heartbeats
        heartbeatThread = new HeartbeatThread(this);
        heartbeatThread.start();

        // Escuta os clientes
        try {
            processClientRequests();
        } finally {
            deleteThread.interrupt();
            heartbeatThread.interrupt();
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
            System.out.println("A aguardar pedido...\n");

            try {
                clientSocket = mainSocket.accept();
            } catch (IOException e) {
                System.err.println("Ocorreu um erro enquanto aguardava por um pedido de ligação:\n\t" + e);
                return; //Termina o servidor
            }
            
            System.out.println(clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort() + " - Cliente ligado");

            try {
                clientSocket.setSoTimeout(TIMEOUT * 1000);
                
                System.out.println("Foi estabelecida ligação a "+ clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " no porto " + clientSocket.getLocalPort());
                // Cria thread para o cliente
                ClientThread clientThread = new ClientThread(clientSocket, fileManager);                
                clientThread.setClientListener(new RepoListener() {

                    @Override
                    public void onConnectedClient() {
                        incrementCurrentConnections();
                    }

                    @Override
                    public void onClosingClient() {
                        decrementCurrentConnections();
                    }
                                        
                    @Override
                    public void onRemoveFile(RepositoryFile file) {
                        getFiles().remove(file);
                    }
                    
                    @Override
                    public boolean onCheckFile(RepositoryFile file) {
                        return checkFile(file);
                    }
                    
                    @Override
                    public void onNewFile(RepositoryFile file) {
                        // Informa do novo ficheiro
                        filesChangedEvent();
                        // REPLICAR
                        requestReplicateFile(file);
                    }
                
                });
                // Inicia thread
                clientThread.start();
            } catch (IOException e) {
                System.out.println("Ocorreu um erro na ligação com o cliente: \n\t" + e);
                // Fecha socket do client
                try {
                    clientSocket.close();
                } catch (IOException s) {/*Silencio*/}
            }            
        }
    }
    
    private synchronized boolean checkFile(RepositoryFile file) {
        boolean result = getFiles().contains(file);
        if (!result) {
            getFiles().add(file);
        }
        return result;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public synchronized DatagramSocket getHeartbeatSocket() {
        return heartbeatSocket;
    }

    public synchronized int getCurrentConnections() {
        return currentConnections;
    }
    
    private synchronized void incrementCurrentConnections() {
        currentConnections++;
    }
    
    private synchronized void decrementCurrentConnections() {
        currentConnections--;
    }
    
    public void filesChangedEvent() {
        DatagramPacket packet;
        ObjectOutputStream out;
        ByteArrayOutputStream buff;
        
        // Utiliza o socket do Heartbeat para enviar a lista de ficheiros
        try {
            packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
            packet.setAddress(InetAddress.getByName(getServerAddress()));
            packet.setPort(getServerPort());

            buff = new ByteArrayOutputStream();
            out = new ObjectOutputStream(buff);

            out.writeObject(getRepository());
            out.flush();
            out.close();

            packet.setData(buff.toByteArray());
            packet.setLength(buff.size());

            // Envia para o servidor
            getHeartbeatSocket().send(packet);
        } catch (IOException e) {
            // ToDo: melhorar o tratamento de erros
            if (!getHeartbeatSocket().isClosed()) {
                getHeartbeatSocket().close();
            }
        }
    }
    
    public void deleteFile(BaseFile file) {
        if (fileManager == null)
            return;
        
        File f = new File(fileManager.getCurrentDirectoryPath() + file.getName());

        if (f.delete()) {
            getFiles().remove(file);
            // Debug
            System.out.println(file.getName() + " foi eliminado.");
            // Informa que a lista foi modificada
            filesChangedEvent();
        }        
    }
    
    public void requestReplicateFile(BaseFile file) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch(SocketException e) {
            System.out.println("Não foi possível replicar o ficheiro "+file.getName()+":\n\t" + e);
        }
        
        if (socket == null)
            return;
        
        new Thread(new ReplicateRequest(this, file, socket)).start();
    }
    
    public void replicateFile(String repositoryAddress, int port, VirtualFile file) {
        if (file == null || fileManager == null)
            return;
                
        // Repositórios
        Socket tempSocket = null;
        InputStream in;
        ObjectInputStream oin;
        ObjectOutputStream oout;
        byte []fileChunck = new byte[Common.FILECHUNK_MAX_SIZE];
        int nbytes;
        
        try {            
            // Ligar ao repositório
            try {
                tempSocket = new Socket(InetAddress.getByName(repositoryAddress), port);
                tempSocket.setSoTimeout(TIMEOUT * 1000);

                in = tempSocket.getInputStream();
                oout = new ObjectOutputStream(tempSocket.getOutputStream());

                oout.writeObject(new Request(file, RequestType.REQ_UPLOAD));
                oout.flush();

                // Iniciar envio do ficheiro
                FileInputStream requestedFileInputStream = null;
                try {
                    requestedFileInputStream = new FileInputStream(fileManager.getCurrentDirectoryPath() + file.getName());
                    
                    // Debug
                    //System.out.println("Replicação de "+file.getName());
                    
                    while ((nbytes = requestedFileInputStream.read(fileChunck)) > 0) {
                        oout.write(fileChunck, 0, nbytes);
                        oout.flush();
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Ficheiro " + fileManager.getCurrentDirectoryPath() + file.getName() + " aberto para leitura.");
                } finally {
                    if (requestedFileInputStream != null)
                        requestedFileInputStream.close();
                }
            } catch (UnknownHostException e) {
                System.out.println("Destino desconhecido:\n\t" + e);
            } catch (NumberFormatException e) {
                System.out.println("O porto do servidor deve ser um inteiro positivo:\n\t" + e);
            } catch (SocketTimeoutException e) {
                System.out.println("Não foi recebida qualquer bloco adicional, podendo a transferencia estar incompleta:\n\t" + e);
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nível do socket TCP:\n\t" + e);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket do repositório "+repositoryAddress+port+":\n\t" + e);
            }
            // Debug: Terminou a transferência
            //System.out.println("Concluído");
        } finally {
            if (tempSocket != null) {
                try {
                    tempSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }

}
