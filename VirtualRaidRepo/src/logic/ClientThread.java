package logic;

import classes.Common;
import classes.FileManager;
import classes.Request;
import classes.RepositoryFile;
import classes.Response;
import classes.VirtualFile;
import enums.ResponseType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

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
        OutputStream out;
        ObjectInputStream oin;
        ObjectOutputStream oout;
        
        performConnectedClient();
        try {
            out = socket.getOutputStream();
            oout = new ObjectOutputStream(out);
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
                    sendFile(req.getFile(), out);
                    break;
                case REQ_UPLOAD:
                    // Aceita o pedido
                    oout.writeObject(new Response(ResponseType.RES_OK, null, req));
                    oout.flush();
                    
                    // ToDo: verificar se o ficheiro já existe
                    
                    receiveFile(req.getFile(), (InputStream) oin);
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
    
    public void sendFile(VirtualFile file, OutputStream out)
    {
        if (socket == null || file == null)
            return;
        
        // ToDo: Verificar a situação da lista de ficheiros e o 
        //VirtualFile vs RepositoryFile
        
        // ToDo: Obter ficheiro pelo nome e data modificação
        
        byte []fileChunck = new byte[Common.FILECHUNK_MAX_SIZE];
        int nbytes;
        
        FileInputStream requestedFileInputStream;
        try {
            requestedFileInputStream = new FileInputStream(filesManager.getCurrentDirectoryPath() + file.getName());
            while ((nbytes = requestedFileInputStream.read(fileChunck)) > 0) {
                out.write(fileChunck, 0, nbytes);
                out.flush();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro " + filesManager.getCurrentDirectoryPath() + file.getName() + " aberto para leitura.");
        } catch(IOException e) {
            System.out.println("Ocorreu a excepcao de E/S:\n\t" + e);
        }
        System.out.println("Transferencia concluida");
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
                    return;
                }
                
                System.out.println("A receber ficheiro "+file.getName());
                // First chunk
                localFileOutputStream.write(fileChunck, 0, nbytes); 
                // Receber o ficheiro
                while ((nbytes = in.read(fileChunck)) > 0) {
                    localFileOutputStream.write(fileChunck, 0, nbytes);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro de E/S:\n\t"+e);
        } finally {
            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
        
        // ToDo: tamanho do ficheiro
        performNewFile(new RepositoryFile(file.getName(), 0, new Date()));
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
