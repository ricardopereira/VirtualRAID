package logic;

import classes.FilesList;
import classes.Repository;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * ServerController classe.
 * Gere todos os pedidos ao servidor.
 * 
 * @author Team
 */
public class ServerController implements Runnable {
    
    public static final String FILE_CREDENTIALS = "credenciais.txt";
    
    // Constantes
    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT_CLIENT = 300; //5 minutos
    
    // Ligação dos Clientes
    private ServerSocket mainSocket;
    // HeartBeat e lista de ficheiros
    private RepositoriesThread repositoriesThread;
    // Pesquisar o servidor
    private MulticastThread multicastThread;
    
    private int port;
    // Lista de repositórios activos
    private ArrayList<Repository> activeRepositories;
    // Lista de clientes activos
    private ArrayList<ClientThread> activeClients;
    
    public ServerController(int listenPort) {
        this.port = listenPort;
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
        repositoriesThread = new RepositoriesThread(this);
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
                clientSocket.setSoTimeout(TIMEOUT_CLIENT*1000);

                System.out.println("Foi estabelecida ligação a "+ clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " no porto " + clientSocket.getLocalPort());
                // Cria thread para o cliente
                ClientThread clientThread = new ClientThread(clientSocket);
                clientThread.setServerListener(new ServerListener() {

                    @Override
                    public void onConnectedClient() {
                        
                    }

                    @Override
                    public void onClosingClient() {
                        getActiveClients().remove(clientThread);
                        // Debug
                        System.out.println("Cliente foi desligado");
                    }

                    @Override
                    public FilesList getFilesList() {
                        return getAllFiles();
                    }
                    
                });
                // Adiciona o cliente que se ligou
                getActiveClients().add(clientThread);
                // Inicia a thread do cliente
                clientThread.start();
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
    
    public void addActiveRepository(Repository repo) {
        if (getActiveRepositories().contains(repo)) {
            getActiveRepositories().remove(repo);
        }
        getActiveRepositories().add(repo);
        
        //client.filesChangedEvent();
    }
    
    public ArrayList<Repository> getActiveRepositories() {
        if (activeRepositories == null)
            activeRepositories = new ArrayList<>();
        return activeRepositories;
    }
    
    public synchronized ArrayList<ClientThread> getActiveClients() {
        if (activeClients == null)
            activeClients = new ArrayList<>();
        return activeClients;
    }
        
    public FilesList getAllFiles() {
        FilesList allFiles = new FilesList();
        for (Repository item : getActiveRepositories()) {
            allFiles.addAll(item.getFilesList());
            // ToDo: número de replicas
        }    
        return allFiles;
    }
    
}
