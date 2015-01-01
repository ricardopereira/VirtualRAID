package logic;

import classes.BaseFile;
import classes.Common;
import classes.FileManager;
import classes.Request;
import classes.RepositoryFile;
import classes.VirtualFile;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * ClientThread classe.
 * Thread do Repositório responsável pela interacção com um cliente ligado.
 * 
 * @author Team
 */
public class ClientThread extends Thread {
    
    private final Socket socket;
    private final FileManager filesManager;
    private RepoListener clientListener;
    
    public ClientThread(Socket socket, FileManager filesManager) {
        this.socket = socket;
        this.filesManager = filesManager;
    }
    
    @Override
    public void run() {
        Request req;
        ObjectInputStream oin;
        OutputStream out;
        
        performConnectedClient();
        try {
            oin = new ObjectInputStream(socket.getInputStream());

            // Obtem pedido do utilizador
            req = (Request) oin.readObject();
            
            if (req == null) { //EOF
                // Para terminar a thread
                return;
            }

            // Verificar pedido
            switch (req.getOption()) {
                case REQ_DOWNLOAD:
                    out = socket.getOutputStream();
                    System.out.println(socket.getInetAddress().getHostAddress()+":"+socket.getPort() + " - Enviar ficheiro "+req.getFile().getName());
                    sendFile(req.getFile(), out);
                    out.close();
                    break;
                case REQ_UPLOAD:
                    out = socket.getOutputStream();
                    
                    BaseFile file = req.getFile();
                    
                    // Já existe o mesmo ficheiro?
                    if (performFileExists(file)) {
                        out.close();
                        break;
                    }

                    performAddFile(new RepositoryFile(file.getName(), file.getSizeBytes(), file.getDateModified()));

                    System.out.println(socket.getInetAddress().getHostAddress()+":"+socket.getPort() + " - Receber ficheiro "+file.getName());
                    receiveFile(req.getFile(), (InputStream) oin);
                    out.close();
                    break;
            }
        } catch (ClassNotFoundException e) {            
            System.out.println("Erro ao tentar receber pedido de um cliente:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu a excepcao de E/S:\n\t" + e);
        } finally {
            performClosingClient();
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }
    
    public void sendFile(VirtualFile file, OutputStream out) {
        if (socket == null || file == null)
            return;
        
        byte []fileChunck = new byte[Common.FILECHUNK_MAX_SIZE];
        int nbytes;
        
        FileInputStream requestedFileInputStream;
        try {
            requestedFileInputStream = new FileInputStream(filesManager.getCurrentDirectoryPath() + file.getName());
            while ((nbytes = requestedFileInputStream.read(fileChunck)) > 0) {
                // Debug
                System.out.println("Readed "+nbytes);
                // Work
                out.write(fileChunck, 0, nbytes);
                out.flush();
            }
            System.out.println("Done");
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro " + filesManager.getCurrentDirectoryPath() + file.getName() + " aberto para leitura.");
        } catch(IOException e) {
            System.out.println("Ocorreu a excepcao de E/S:\n\t" + e);
        }
    }
    
    public void receiveFile(VirtualFile file, InputStream in)
    {
        if (socket == null || file == null)
            return;
                
        FileOutputStream localFileOutputStream = null;
        byte []fileChunck = new byte[Common.FILECHUNK_MAX_SIZE];
        int nbytes;

        try {
            if ((nbytes = in.read(fileChunck)) > 0) {
                // Criar FileStream para receber o ficheiro
                try {
                    localFileOutputStream = new FileOutputStream(filesManager.getCurrentDirectoryPath() + file.getName());
                } catch (IOException e) {
                    System.out.println("Não foi possível criar o ficheiro "+filesManager.getCurrentDirectoryPath() + file.getName());
                    performRemoveFile(new RepositoryFile(file.getName(), file.getSizeBytes(), file.getDateModified()));
                    return;
                }
                
                System.out.println("A receber "+file.getName());
                // First chunk
                localFileOutputStream.write(fileChunck, 0, nbytes); 
                // Receber o ficheiro
                while ((nbytes = in.read(fileChunck)) > 0) {
                    // Debug
                    System.out.println("Writed "+nbytes);
                    // Work
                    localFileOutputStream.write(fileChunck, 0, nbytes);
                }
                System.out.println("Done");
            }
        } catch (IOException e) {
            System.out.println("Erro de E/S:\n\t"+e);
            performRemoveFile(new RepositoryFile(file.getName(), file.getSizeBytes(), file.getDateModified()));
        } finally {
            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
        
        // Refresh files
        performNewFile(new RepositoryFile(file.getName(), file.getSizeBytes(), file.getDateModified()));
    }
    
    private boolean performFileExists(BaseFile file) {
        if (clientListener != null)
            return clientListener.onFileExists(file);
        return false;
    }
    
    private void performAddFile(RepositoryFile file) {
        if (clientListener != null)
            clientListener.onAddFile(file);
    }
    
    private void performRemoveFile(RepositoryFile file) {
        if (clientListener != null)
            clientListener.onRemoveFile(file);
    }
    
    private void performNewFile(RepositoryFile file) {
        if (clientListener != null)
            clientListener.onNewFile(file);
    }
    
    private void performConnectedClient() {
        if (clientListener != null)
            clientListener.onConnectedClient();
    }
    
    private void performClosingClient() {
        if (clientListener != null)
            clientListener.onClosingClient();
    }
    
    public void setClientListener(RepoListener listener) {
        clientListener = listener;
    }
    
}
