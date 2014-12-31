package logic;

import classes.Common;
import classes.FileManager;
import classes.FilesList;
import classes.Login;
import classes.RepositoryFile;
import classes.Request;
import classes.Response;
import classes.VirtualFile;
import enums.RequestType;
import enums.ResponseType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * ClientController classe.
 * Instância responsável pela ligação ao servidor e ao repositório.
 * 
 * @author Team
 */
public class ClientController {
    // Constantes
    public static final int TIMEOUT_AUTH = 30; //Segundos
    public static final int TIMEOUT = 15; //Segundos
        
    // Servidor principal
    private Socket mainSocket;
    
    private boolean isAuthenticated = false;
    private volatile FilesList remoteFilesList = new FilesList(); //Remote files list
    private ArrayList<RepositoryFile> localFilesList; //Local files list
    private ResponsesManager responsesManagerThread;
    private ClientListener clientListener;
    
    // Ficheiros locais
    private String localFilesDirectory;
    private FileManager localFilesManager = null;
    
    public ClientController() {

    }
    
    public boolean connectToServer(String host, int port) {
        // Se já está activo, não faz nada
        if (getIsConnected())
            return true;
        
        if (localFilesDirectory == null || localFilesDirectory.trim().equals("")) {
            System.err.println("Diretório de ficheiros não foi definido.");
            return false;
        }
        
        // Ficheiros locais
        refreshLocalFilesList();
        
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
        remoteFilesList = null;        
    }
    
    public synchronized void refreshLocalFilesList() {
        localFilesList = new ArrayList<>();
        getLocalFilesManager().loadFiles(localFilesList);
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
    
    public ResponseType requestUploadFile(VirtualFile file) {
        if (!getIsAuthenticated())
            return ResponseType.RES_FAILED;
        
        if (file == null)
            return ResponseType.RES_FAILED;
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(mainSocket.getOutputStream());
            // Envia pedido de upload
            oos.writeObject(new Request(file, RequestType.REQ_UPLOAD));
            oos.flush();
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor:\n\t" + e);
            return ResponseType.RES_FAILED;
        }
        return ResponseType.RES_OK;
    }
    
    public ResponseType requestDeleteFile(VirtualFile file) {
        if (!getIsAuthenticated())
            return ResponseType.RES_FAILED;
        
        if (file == null)
            return ResponseType.RES_FAILED;
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(mainSocket.getOutputStream());
            // Envia pedido de delete
            oos.writeObject(new Request(file, RequestType.REQ_DELETE));
            oos.flush();
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor:\n\t" + e);
            return ResponseType.RES_FAILED;
        }
        return ResponseType.RES_OK;
    }
    
    private void downloadFile(String repositoryAddress, int port, VirtualFile file) {
        if (file == null) {
            return;
        }
        
        if (localFilesManager == null) {
            performFilesError("Não foi especificado uma directoria de ficheiros");
            return;
        }

        // Repositórios
        Socket tempSocket = null;
        FileOutputStream localFileOutputStream = null;
        ObjectOutputStream oout;
        InputStream in;
        byte []fileChunck = new byte[Common.FILECHUNK_MAX_SIZE];
        int nbytes;
        
        try {
            // Ligar ao repositório
            try {
                tempSocket = new Socket(InetAddress.getByName(repositoryAddress), port);
                tempSocket.setSoTimeout(TIMEOUT * 1000);

                in = tempSocket.getInputStream();
                oout = new ObjectOutputStream(tempSocket.getOutputStream());

                oout.writeObject(new Request(file, RequestType.REQ_DOWNLOAD));
                oout.flush();
                
                if ((nbytes = in.read(fileChunck)) > 0) {
                    // Criar FileStream para receber o ficheiro
                    try {
                        localFileOutputStream = new FileOutputStream(localFilesManager.getCurrentDirectoryPath() + file.getName());
                        performOperationStarted("Iniciar transferência de "+file.getName());
                    } catch (IOException e) {
                        performFilesError("Não foi possível criar o ficheiro "+localFilesManager.getCurrentDirectoryPath() + file.getName());
                        return;
                    }
                    
                    // First chunk
                    localFileOutputStream.write(fileChunck, 0, nbytes);
                    performOperationProgress(nbytes);
                        
                    // Receber o ficheiro
                    while ((nbytes = in.read(fileChunck)) > 0) {
                        localFileOutputStream.write(fileChunck, 0, nbytes); 
                        performOperationProgress(nbytes);
                    }
                }
                else {
                    performOperationFinished("Ficheiro não está disponível.");
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
            // Terminou a transferência
            performOperationFinished("Concluido");
        } finally {
            if (tempSocket != null) {
                try {
                    tempSocket.close();
                } catch (IOException e) {
                }
            }

            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    private void uploadFile(String repositoryAddress, int port, VirtualFile file) {
        if (file == null)
            return;
        
        if (localFilesManager == null) {
            performFilesError("Não foi especificado uma directoria de ficheiros");
            return;
        }
        
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
                    requestedFileInputStream = new FileInputStream(localFilesManager.getCurrentDirectoryPath() + file.getName());
                    performOperationStarted("Iniciar transferência de "+file.getName());
                    while ((nbytes = requestedFileInputStream.read(fileChunck)) > 0) {
                        oout.write(fileChunck, 0, nbytes);
                        oout.flush();
                        performOperationProgress(nbytes);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Ficheiro " + localFilesManager.getCurrentDirectoryPath() + file.getName() + " aberto para leitura.");
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
            // Terminou a transferência
            performOperationFinished("Concluído");
        } finally {
            if (tempSocket != null) {
                try {
                    tempSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }
        
    public void interpretResponse(Response res) {
        if (res == null)
            return;
        
        // Só inicia o download ou upload se a resposta do servidor for positiva
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
                    uploadFile(res.getRepositoryAddress(), res.getRepositoryPort(), 
                        res.getRequested().getFile());
                    break;
            }
        }
    }
    
    public boolean canUseFilesList() {
        return remoteFilesList != null && !remoteFilesList.isEmpty();
    }
    
    public FilesList getFilesList() {
        return remoteFilesList;
    }
    
    public void setFilesList(FilesList newFilesList) {
        this.remoteFilesList = newFilesList;
        performFilesListChanged(remoteFilesList);
    }
    
    public ArrayList<RepositoryFile> getLocalFilesList() {
        return localFilesList;
    }
    
    private void performFilesListChanged(FilesList filesList) {
        if (clientListener != null)
            clientListener.onFilesListChanged(filesList);
    }
    
    private void performFilesError(String message) {
        if (clientListener != null)
            clientListener.onFilesError(message);
    }

    private void performResponseError(ResponseType status) {
        if (clientListener != null)
            clientListener.onResponseError(status);
    }

    private void performOperationStarted(String fileName) {
        if (clientListener != null)
            clientListener.onOperationStarted(fileName);
    }

    private void performOperationProgress(int nbytes) {
        if (clientListener != null)
            clientListener.onOperationProgress(nbytes);
    }
    
    private void performOperationFinished(String message) {
        if (clientListener != null)
            clientListener.onOperationFinished(message);
    }
    
    public void setClientListener(ClientListener listener) {
        clientListener = listener;
    }

    public String getLocalFilesDirectory() {
        return localFilesDirectory;
    }

    public void setLocalFilesDirectory(String localFilesDirectory) {
        this.localFilesDirectory = localFilesDirectory;
        try {
            localFilesManager = new FileManager(localFilesDirectory);
        } catch (FileManager.DirectoryNotFound e) {
            performFilesError(e.getMessage());
        } catch (FileManager.DirectoryInvalid e) {
            performFilesError(e.getMessage());
        } catch (FileManager.DirectoryNoPermissions e) {
            performFilesError(e.getMessage());
        }
    }
    
    public FileManager getLocalFilesManager() {
        if (localFilesManager == null)
            System.err.println("Gestor de ficheiros não foi definido");
        return localFilesManager;
    }
 
}