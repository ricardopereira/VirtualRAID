package logic;

import classes.FilesList;
import classes.Login;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;

public class ClientController {
    // Constantes
    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 30; //Segundos
    
    // Servidor principal
    private Socket mainSocket;
    // Repositórios
    private Socket tempSocket;
    
    private boolean isAuthenticated = false;
    private volatile FilesList filesList = new FilesList();
    private RefreshFilesThread refreshFilesThread;
    private IFilesListListener filesListChangedListener;
    
    public ClientController() {
        
    }
    
    public boolean connectToServer(String host, int port) {
        // Se já está activo, não faz nada
        if (getIsConnected())
            return true;
        
        try {
            // Estabelecer ligação com o servidor
            mainSocket = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.err.println("Destino desconhecido:\n\t" + e);
            mainSocket = null;
            return false;
        } catch (NumberFormatException e) {
            System.err.println("O porto do servidor deve ser um inteiro positivo:\n\t" + e);
            mainSocket = null;
            return false;
        } catch (SocketTimeoutException e) {
            System.err.println("Não foi recebida qualquer bloco adicional, podendo a transferencia estar incompleta:\n\t" + e);
            mainSocket = null;
            return false;
        } catch (SocketException e) {
            System.err.println("Ocorreu um erro ao nível do socket TCP:\n\t" + e);
            mainSocket = null;
            return false;
        } catch (IOException e) {
            System.err.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
            mainSocket = null;
            return false;
        }
        return true;
    }
    
    public void disconnectToServer() {
        // Tem que estar activo
        if (!getIsConnected())
            return;
        
        if (refreshFilesThread != null) {
            refreshFilesThread.cancel();
            refreshFilesThread = null;
        }
        filesList = null;
        
        try {
            mainSocket.close();
        } catch (IOException ex) {/*Silencio*/}
    }
    
    public boolean getIsConnected() {
        return mainSocket != null;
    }
    
    public boolean getIsAuthenticated() {
        return isAuthenticated;
    }
    
    public void authenticate(String username, String password) {
        Login login = new Login(username);
        login.setPassword(password);
        authenticate(login);
    }
    
    public boolean authenticate(Login login) {
        if (!getIsConnected() || getIsAuthenticated())
            return false;
        
        if (login == null)
            return false;
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(mainSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(mainSocket.getInputStream());
            
            // Envia dados para autenticação
            oos.writeObject(login);
            oos.flush();
            
            // Recebe a resposta do servidor
            isAuthenticated = (Boolean) ois.readObject();
            
            if (isAuthenticated) {                
                // Receber lista de ficheiros inicial
                refreshFilesThread = new RefreshFilesThread(mainSocket);
                // Callback quando a lista de ficheiros é alterada
                refreshFilesThread.setFilesListChangedListener(refreshFilesList);
                refreshFilesThread.start();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Ocorreu um erro a obter o resultado da autenticação:\n\t" + e);
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor:\n\t" + e);
        }
        return isAuthenticated;
    }
    
    public boolean canUseFilesList() {
        return filesList != null && !filesList.isEmpty();
    }
    
    public FilesList getFilesList() {
        return filesList;
    }
    
    private IFilesListListener refreshFilesList = new IFilesListListener() {

        @Override
        public void onFilesListChanged(FilesList newFilesList) {
            filesList = newFilesList;
            // Callback para a interface
            performFilesListChanged(filesList);
        }
        
    };
    
    public void performFilesListChanged(FilesList filesList) {
        if (filesListChangedListener != null)
            filesListChangedListener.onFilesListChanged(filesList);
    }
    
    public void setFilesListChangedListener(IFilesListListener listener) {
        filesListChangedListener = listener;
    }
    
}
