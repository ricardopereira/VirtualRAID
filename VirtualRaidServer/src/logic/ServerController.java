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
    public static final int TIMEOUT_AUTH = 60; //1 minuto
    public static final int TIMEOUT_CLIENT = 300; //5 minutos
    
    // Ligação dos Clientes
    private ServerSocket mainSocket;
    // HeartBeat e lista de ficheiros
    private RepositoriesThread repositoriesThread;
    // Timer para verificar os repositórios activos
    private CheckRepositoriesTimer checkRepositoriesTimer;
    // Pesquisar o servidor
    private MulticastThread multicastThread;
    
    private int port;
    // Lista de repositórios activos
    private RepositoriesList activeRepositories;
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
        
        // Timer para verificar repositórios activos
        checkRepositoriesTimer = new CheckRepositoriesTimer(this);
        checkRepositoriesTimer.start();

        // Escuta os clientes
        try {
            processClientRequests();
        } finally {
            // Termina as várias threads
            multicastThread.interrupt();
            repositoriesThread.interrupt();
            checkRepositoriesTimer.interrupt();
            
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
            System.out.println("Aguarda ligação de um cliente...\n");
            
            try {
                clientSocket = mainSocket.accept();
            } catch (IOException e) {
                System.err.println("Ocorreu um erro enquanto aguardava por um pedido de ligação:\n\t" + e);
                return; //Termina o servidor
            }

            System.out.println(clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort() + " no porto " + clientSocket.getLocalPort() + " - Cliente estabeleceu ligação");

            // Cria thread para o cliente
            ClientThread clientThread = new ClientThread(clientSocket);
            clientThread.setServerListener(new ServerListener() {

                @Override
                public void onConnectedClient() {

                }

                @Override
                public void onClosingClient() {
                    getActiveClients().remove(clientThread);
                }
                
                @Override
                public FilesList getFilesList() {
                    return getAllFiles();
                }

                @Override
                public RepositoriesList getRepositoriesList() {
                    return getActiveRepositories();
                }

            });
            // Adiciona o cliente que se ligou
            getActiveClients().add(clientThread);
            // Inicia a thread do cliente
            clientThread.start();
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
        repo.setLastUpdate();
        getActiveRepositories().add(repo);
        
        // Actualizar os clientes com os ficheiros novos        
        updateClients();
    }
    
    public void setRepositoryActiveConnections(String address, int port, int nrConnections) {
        int index = getActiveRepositories().indexOf(new Repository(address, port));
        // Repositório existe?
        if (index >= 0) {
            Repository activeRepository = getActiveRepositories().get(index);
            activeRepository.setNrConnections(nrConnections);
            activeRepository.setLastUpdate();
        }
    }
    
    public void updateClients() {
        for (ClientThread client : getActiveClients())
            client.filesChangedEvent();
    }
    
    public synchronized RepositoriesList getActiveRepositories() {
        if (activeRepositories == null)
            activeRepositories = new RepositoriesList();
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
            allFiles.addRepositoryFiles(item);
        }    
        return allFiles;
    }
    
}
