package logic;

import classes.FilesList;
import classes.Login;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientController {
    // Constantes
    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 30; //Segundos
    
    // Servidor principal
    private Socket mainSocket;
    // Repositórios
    private Socket tempSocket;
    
    private boolean isAuthenticated = false;
    
    public ClientController() {
        
    }
    
    public boolean connectToServer(String host, int port) {
        // Se já está activo, não faz nada
        if (getIsConnected())
            return true;
        
        try {
            // Estabelecer ligação com o servidor
            mainSocket = new Socket(host, port);
            mainSocket.setSoTimeout(TIMEOUT * 1000);
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
    
    public void login() {
        if (!getIsConnected())
            return;
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(mainSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(mainSocket.getInputStream());
            BufferedReader pin = new BufferedReader(new InputStreamReader(mainSocket.getInputStream()));
            System.out.println(pin.readLine());
            
            // Autenticação
            Login login = null;
            
            oos.writeObject(login);
            
            // Teste
            System.out.println(pin.readLine());
            // Receber lista de ficheiros
            FilesList allFiles;
            try {
                allFiles = (FilesList) ois.readObject();
                System.out.println(allFiles.toString());
            } catch (ClassNotFoundException e) {
                System.err.println("Não foi possível receber a lista de ficheiros:\n\t" + e);
            }
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor:\n\t" + e);
        } finally {
            disconnectToServer();
        }
    }
}
