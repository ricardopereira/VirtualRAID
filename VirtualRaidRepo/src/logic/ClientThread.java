package logic;

import classes.Common;
import classes.FileManager;
import classes.Request;
import classes.VirtualFile;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final FileManager fileManager;
    private ClientListener clientListener;
    
    public ClientThread(Socket socket, FileManager fileManager) {
        this.socket = socket;
        this.fileManager = fileManager;
    }
    
    @Override
    public void run() {
        Request req;
        OutputStream out;
        ObjectInputStream ois;
        
        performConnectedClient();
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            out = socket.getOutputStream();

            // Obtem pedido do utilizador
            req = (Request) ois.readObject();
            
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
                    receiveFile(req.getFile());
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
        
        byte []fileChunck = new byte[Common.FILECHUNK_MAX_SIZE];
        int nbytes;
        FileInputStream requestedFileInputStream;
        try {
            requestedFileInputStream = new FileInputStream(fileManager.getCurrentDirectoryPath() + file.getName());
            while ((nbytes = requestedFileInputStream.read(fileChunck)) > 0) {
                out.write(fileChunck, 0, nbytes);
                out.flush();
                try {
                    // Teste
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro " + fileManager.getCurrentDirectoryPath() + file.getName() + " aberto para leitura.");
        } catch(IOException e) {
            System.out.println("Ocorreu a excepcao de E/S:\n\t" + e);
        }
        System.out.println("Transferencia concluida");
    }
    
    public void receiveFile(VirtualFile file)
    {
        if (socket == null || file == null)
            return;
        
        
    }
    
    private void performConnectedClient() {
        if (clientListener != null)
            clientListener.onConnectedClient();
    }
    
    private void performClosingClient() {
        if (clientListener != null)
            clientListener.onClosingClient();
    }
    
    public void setClientListener(ClientListener listener) {
        clientListener = listener;
    }
    
}
