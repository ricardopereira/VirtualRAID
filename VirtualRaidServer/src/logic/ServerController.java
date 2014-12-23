package logic;

import classes.FilesList;
import classes.Repository;
import classes.RepositoryFile;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

/**
 * ServerController classe.
 * Gere todos os pedidos ao servidor.
 * 
 * @author Team
 */
public class ServerController implements Runnable {
    
    // Constantes
    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 30; //segundos
    
    // Ligação dos Clientes
    private ServerSocket mainSocket;
    // HeartBeat e lista de ficheiros
    private RepositoriesThread repositoriesThread;
    // Pesquisar o servidor
    private MulticastThread multicastThread;
    
    private int port;
    // Lista de repositórios activos
    private ArrayList<Repository> repositories;
    
    public ServerController(int listenPort) {
        this.port = listenPort;
        // Teste: para efeitos de teste
        repositories = exemploDeRepositoriosActivos();
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
        
        // Thread para enviar a resposta ao broadcast (IP:Porto do servidor)
        multicastThread = new MulticastThread(mainSocket);
        multicastThread.start();
        
        // Iniciar recepção de Heartbeats e ficheiros de repositórios
        repositoriesThread = new RepositoriesThread();
        repositoriesThread.start();

        // Escuta os clientes
        try {
            processClientRequests();
        } finally {
            // Termina as várias threads
            multicastThread.interrupt();
            repositoriesThread.interrupt();
            
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
                new ClientThread(clientSocket, getAllFiles()).start();
            } catch (IOException e) {
                System.out.println("Ocorreu um erro na ligação com o cliente: \n\t" + e);
                try {
                    clientSocket.close();
                } catch (IOException s) {/*Silencio*/}
            }
            
            System.out.println("A aguardar novo cliente");
        }
    }

    @Override
    public void run() {
        startListeningClients();
    }
    
    public ArrayList<Repository> getRepositories() {
        return repositories;
    }
    
    private ArrayList<Repository> exemploDeRepositoriosActivos() {
        // Teste
        ArrayList<Repository> teste = new ArrayList<>();
        
        // Repositorio 001
        Repository r001 = new Repository("192.0.0.1",82640);
        // Repositorio 002
        Repository r002 = new Repository("192.0.0.2",27330);
        
        // Repositório 001 se conectou!
        teste.add(r001);
        r001.getFiles().add(new RepositoryFile("Screen Shot 2014-11-28 at 02.42.19.png",1024,new Date()));
        r001.getFiles().add(new RepositoryFile("HannaMontana.avi",1024,new Date()));
        r001.getFiles().add(new RepositoryFile("PowerRangers.avi",3024,new Date()));
        r001.getFiles().add(new RepositoryFile("DragonBall.avi",1024,new Date()));
        // Repositório 002 se conectou!
        teste.add(r002);
        r002.getFiles().add(new RepositoryFile("HelloKitty.avi",2024,new Date()));
        
        return teste;
    }
    
    public FilesList getAllFiles() {
        FilesList allFiles = new FilesList();
        for (Repository item : repositories) {
            allFiles.addAll(item.getFilesList());
            // ToDo: número de replicas
        }    
        return allFiles;
    }
    
}
