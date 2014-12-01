package logic;

import classes.FilesList;
import classes.Login;
import classes.Request;
import classes.Response;
import classes.VirtualFile;
import enums.RequestType;
import enums.ResponseType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * ClientController classe.
 * Instância responsável pela ligação ao servidor e ao repositório.
 * 
 * @author Team
 */
public class ClientController {
    // Constantes
    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 30; //Segundos
        
    // Servidor principal
    private Socket mainSocket;
    
    private boolean isAuthenticated = false;
    private String localFilesDirectory;
    private volatile FilesList filesList = new FilesList(); //Remote files list
    private ResponsesManager responsesManagerThread;
    private ClientListener clientListener;
    
    public ClientController(String dir) {
        this.localFilesDirectory = dir;
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
        
        // Cancelar a thread que gere as respostas do Servidor
        if (responsesManagerThread != null) {
            responsesManagerThread.cancel();
        }
        
        try {
            // Fecha a ligação com o Servidor
            mainSocket.close();
        } catch (IOException e) {/*Silencio*/}
        
        if (responsesManagerThread != null) {
            try {
                // Esperar que ele cancele a última operação
                responsesManagerThread.join();
            } catch (InterruptedException e) {/*Silencio*/}
        }

        responsesManagerThread = null;
        filesList = null;        
    }
    
    public Socket getMainSocket() {
        return mainSocket;
    }
    
    public boolean getIsConnected() {
        return mainSocket != null;
    }
    
    public boolean getIsAuthenticated() {
        return getIsConnected() && isAuthenticated;
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
                // Neste ponto irá receber lista de ficheiros inicial
                responsesManagerThread = new ResponsesManager(this);
                responsesManagerThread.start();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Ocorreu um erro a obter o resultado da autenticação:\n\t" + e);
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor:\n\t" + e);
        }
        return isAuthenticated;
    }
    
    public ResponseType requestDownloadFile(VirtualFile file) {
        if (!getIsAuthenticated())
            return ResponseType.RES_FAILED;
        
        if (file == null)
            return ResponseType.RES_FAILED;
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(mainSocket.getOutputStream());
            // Envia pedido de download
            oos.writeObject(new Request(file, RequestType.REQ_DOWNLOAD));
            oos.flush();
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor:\n\t" + e);
            return ResponseType.RES_FAILED;
        }
        return ResponseType.RES_OK;
    }
    
    private void downloadFile(String repositoryAddress, int port, VirtualFile file) {
        if (file == null)
            return;

        // Repositórios
        Socket tempSocket;
        byte []fileChunck = new byte[MAX_SIZE];
        int nbytes;                
        int index = 0;
        
        performDownloadStarted("Downloading a file...");
        
        // Recebe ficheiro por blocos
        //while ((nbytes = in.read(fileChunck)) > 0) {
            //System.out.println("Recebido o bloco n. " + ++index + " com " + nbytes + " bytes.");
            //localFileOutputStream.write(fileChunck, 0, nbytes);                 
        //}
        
        performDownloadFinished();
    }
    
    private void uploadFile(String repositoryAddress, int port, VirtualFile file) {
        if (file == null)
            return;
        
        // ToDo
        
        return;
    }
    
    private int deleteFile(VirtualFile file) {
        if (file == null)
            return 0;
        
        // ToDo
        
        return 0;
    }
    
    public void interpretResponse(Response res) {
        if (res == null)
            return;
        
        // Só inicia o download ou upload se a resposta do servidor for positivo
        if (res.getStatus() != ResponseType.RES_OK) {
            performResponseError(res.getStatus());
        }
        else if (res.getRequested() == null) {
            performResponseError(ResponseType.RES_FAILED);
        }
        else {
            switch (res.getRequested().getOption()) {
                case REQ_DOWNLOAD:
                    downloadFile(res.getRepositoryAddress(), res.getRepositoryPort(), 
                        res.getRequested().getFile());
                    break;
                case REQ_UPLOAD:
                    // ToDo
                    break;
            }
        }
    }
    
    public boolean canUseFilesList() {
        return filesList != null && !filesList.isEmpty();
    }
    
    public FilesList getFilesList() {
        return filesList;
    }
    
    public void setFilesList(FilesList newFilesList) {
        this.filesList = newFilesList;
        performFilesListChanged(filesList);
    }
    
    private void performFilesListChanged(FilesList filesList) {
        if (clientListener != null)
            clientListener.onFilesListChanged(filesList);
    }

    private void performResponseError(ResponseType status) {
        if (clientListener != null)
            clientListener.onResponseError(status);
    }

    private void performDownloadStarted(String fileName) {
        if (clientListener != null)
            clientListener.onDownloadStarted(fileName);
    }

    private void performDownloadProgress(int nbytes) {
        if (clientListener != null)
            clientListener.onDownloadProgress(nbytes);
    }
    
    private void performDownloadFinished() {
        if (clientListener != null)
            clientListener.onDownloadFinished();
    }
    
    public void setClientListener(ClientListener listener) {
        clientListener = listener;
    }
    
}
