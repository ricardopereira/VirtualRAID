
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Carine
 */
public class Servidor extends Thread{
    
    private int porto;
    private ServerSocket servSocket;

    public Servidor(int porto){
        this.porto=porto;
    
    } 
    
    @Override
    public void run() {
       
        try {
            servSocket = new ServerSocket(porto);  
        } catch (IOException e) {
            System.err.println("<Servidor> Ocorreu um erro ao criar o serverSocket" + e);
            return;
        }

        try {
            Socket socket;
            System.out.println("O servidor encontra-se à escuta de clientes...");
            while(true){
                socket = servSocket.accept();
                System.out.println("Foi estabelecida ligação a "+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " no porto " + socket.getLocalPort() );
                new AtendeCliente(this, socket).start();
                System.out.println("A aguardar novo cliente");
            }
        } catch (IOException ex) {
           
                System.err.println("<Servidor> Ocorreu um erro ao atender clientes " + ex);
            
        }
            //fecha o socket do servidor
            try {
                servSocket.close();
            } catch (IOException e) {
                System.err.println("<Servidor> Ocorreu um erro ao fechar o serverSocket do servidor.");
            }
        }
    }
    
