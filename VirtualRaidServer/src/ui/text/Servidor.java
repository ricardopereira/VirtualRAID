package ui.text;

import classes.FilesList;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor extends Thread {
    
    final private int port;
    private ServerSocket servSocket;
    private FilesList files;

    public Servidor(int port, FilesList files) {
        this.port = port;
        this.files = files;
    } 
    
    @Override
    public void run() {
        // Socket do Servidor
        try {
            servSocket = new ServerSocket(port);  
        } catch (IOException e) {
            System.err.println("<Servidor> Ocorreu um erro ao criar o serverSocket" + e);
            return;
        }

        try {
            Socket socket;
            System.out.println("O servidor encontra-se à escuta de clientes...");
            while(true) {
                socket = servSocket.accept();
                System.out.println("Foi estabelecida ligação a "+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " no porto " + socket.getLocalPort() );
                new AtendeCliente(this, socket, files).start();
                System.out.println("A aguardar novo cliente");
            }
        } catch (IOException ex) {
            System.err.println("<Servidor> Ocorreu um erro ao atender clientes " + ex);
        } finally {
            // Fecha o socket do servidor
            try {
                servSocket.close();
            } catch (IOException e) {/*Silencio*/}
        }
    }
}
