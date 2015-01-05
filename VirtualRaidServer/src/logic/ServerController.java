package logic;

import classes.RepositoriesList;
import classes.Client;
import classes.Common;
import classes.FilesList;
import classes.RMIApplicationInterface;
import classes.RMIServiceInterface;
import classes.Repository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * ServerController classe.
 * Gere todos os pedidos ao servidor.
 * 
 * @author Team
 */
public class ServerController extends UnicastRemoteObject implements RMIServiceInterface, Runnable {
    
    public static final String FILE_CREDENTIALS = "credenciais.txt";
    
    // Constantes
    public static final int TIMEOUT_AUTH = 60; //1 minuto
    public static final int TIMEOUT_CLIENT = 1800; //30 minutos
    
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
    // Lista de Monitores
    private ArrayList<RMIApplicationInterface> activeMonitors;
    
    public ServerController(int listenPort) throws RemoteException {
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
                    // RMI
                    notifyObserversListUsers();
                }

                @Override
                public void onClosingClient() {
                    getActiveClients().remove(clientThread);
                    // RMI
                    notifyObserversListUsers();
                }
                
                @Override
                public FilesList getFilesList() {
                    return getAllFiles();
                }

                @Override
                public RepositoriesList getRepositoriesList() {
                    return getActiveRepositories();
                }
                
                @Override
                public void notifyFileChanged() {
                    notifyObserversListFiles();
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
        
        // RMI
        notifyObserversListRepo();
        
        // Actualizar os clientes com os ficheiros novos
        updateClients();
    }
    
    public void removeActiveRepository() {
        // RMI
        notifyObserversListRepo();
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
    
    public ArrayList<Client> getActiveUsers() {
        ArrayList<Client> clientList = new ArrayList<>();
        for (ClientThread clientThread : getActiveClients())
            clientList.add(clientThread.getClient());
        return clientList;
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

    @Override
    public RepositoriesList getRMIActiveRepositories() throws RemoteException {
        return getActiveRepositories();
    }

    @Override
    public FilesList getRMIAllFiles() throws RemoteException {
        return getAllFiles();
    }

    @Override
    public ArrayList<Client> getRMIActiveUsers() throws RemoteException {
        return getActiveUsers();
    }
    
    @Override
    public void addObserver(RMIApplicationInterface app) throws RemoteException {
        getActiveMonitors().add(app);
    }

    public ArrayList<RMIApplicationInterface> getActiveMonitors() {
        if (activeMonitors == null)
            activeMonitors = new ArrayList<>();
        return activeMonitors;
    }
    
    public void notifyObserversListFiles() {
        for (int i=0; i<getActiveMonitors().size(); i++)
            try {
                getActiveMonitors().get(i).updateListFiles();
            } catch (RemoteException e) {
                //System.out.println("Não foi possível fazer notifyObserversListFiles: "+e);
            }
    }
    
    public void notifyObserversListRepo() {
        for (int i=0; i<getActiveMonitors().size(); i++)
            try {
                getActiveMonitors().get(i).updateListRepo();
            } catch (RemoteException e) {
                //System.out.println("Não foi possível fazer notifyObserversListRepo: "+e);
            }
    }
     
    public void notifyObserversListUsers() {
        for (int i=0; i<getActiveMonitors().size(); i++)
            try {
                getActiveMonitors().get(i).updateListUsers();
            } catch (RemoteException e) {
                //System.out.println("Não foi possível fazer notifyObserversListUsers: "+e);
            }
    }
    
    public void startRMI() {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            } catch (ExportException e) {
                System.out.println("Registry já é usado no porto " + Registry.REGISTRY_PORT);
                registry = LocateRegistry.getRegistry();
            }
            try {
                Naming.bind(Common.RMIService, this);
            } catch (RemoteException | AlreadyBoundException | MalformedURLException e) {
                System.err.println("Ocorreu um erro ao registar o serviço: " + e);
            }
        } catch (RemoteException re) {
            System.err.println("Remote Error - " + re);
        } catch (Exception e) {
            System.err.println("Error - " + e);
        }
    }
    
}
