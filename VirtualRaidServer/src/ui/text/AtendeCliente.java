
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Carine
 */
public class AtendeCliente extends Thread{
    private Servidor myServidor;
    private Socket mySocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    PrintWriter pout;
    
    public AtendeCliente(Servidor servidor, Socket socket){
        myServidor = servidor;
        mySocket = socket;
        
    }
    
    @Override
    public void run(){
        Login login;
        boolean autenticacao = false;
         try{
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());
            pout = new PrintWriter(mySocket.getOutputStream());
            
                    
            while(!autenticacao){
                pout.println("Introduza username e password: <username password> ");
                pout.flush();
                login = (Login) ois.readObject();
                if(isValid(login)){
                    pout.println("Login efetuado com sucesso!");
                    pout.flush();
                    //TODO: enviar lista de ficheiros ao cliente
                    
                    autenticacao = true;
                }
                else{
                    pout.println("Credenciais inválidas!");
                    pout.flush();
                }
            }
        } catch(IOException e){
            System.err.println("<Servidor:AtendeCliente> Ocorreu um erro ao criar os streams: " + e);
        } catch (ClassNotFoundException ex) {
            System.err.println("<Servidor:AtendeCliente> Ocorreu um erro ao ler: " + ex);
        }
    
    }
    
    private static boolean isValid(Login login){
        boolean valid = false;
        try{
            Scanner sc = new Scanner(new File("credenciais.txt"));
            while(sc.hasNext()){
                if(sc.nextLine().equals(login.getNome()+" "+login.getPassword())){
                    valid = true;
                    break;
                }
            }
        } catch(FileNotFoundException e){
            System.err.println("O ficheiro não existe!");
        }
        return valid;
    }
}
