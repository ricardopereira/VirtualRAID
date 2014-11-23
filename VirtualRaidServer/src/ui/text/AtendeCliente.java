
package ui.text;

import classes.FilesList;
import classes.Login;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class AtendeCliente extends Thread {
    private Servidor myServidor;
    private Socket mySocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    PrintWriter pout;
    
    private FilesList files;
    
    public AtendeCliente(Servidor servidor, Socket socket, FilesList files){
        myServidor = servidor;
        mySocket = socket;
        this.files = files;
    }
    
    @Override
    public void run() {
        Login login;
        boolean autenticacao = false;
        
        try {
            oos = new ObjectOutputStream(mySocket.getOutputStream());
            ois = new ObjectInputStream(mySocket.getInputStream());
            pout = new PrintWriter(mySocket.getOutputStream());
            
            while(!autenticacao) {
                pout.println("Introduza username e password: <username password> ");
                pout.flush();
                login = (Login) ois.readObject();
                if (isValid(login)) {
                    pout.println("Login efetuado com sucesso!");
                    pout.flush();
                    
                    autenticacao = true;
                    // Teste
                    // Enviar lista de ficheiros
                    oos.writeObject(files);                    
                }
                else {
                    pout.println("Credenciais inválidas!");
                    pout.flush();
                }
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("<Servidor:AtendeCliente> Ocorreu um erro ao ler: " + ex);
        } catch(IOException e){
            System.err.println("<Servidor:AtendeCliente> Ocorreu um erro ao criar os streams: " + e);
        }
    }
    
    private static boolean isValid(Login login) {
        boolean valid = false;
        try {
            // RP: Talvez fazer a verificação se o ficheiro exsite 
            //logo no arranque do servidor será porreiro para no caso de falhar
            Scanner sc = new Scanner(new File("credenciais.txt"));
            while(sc.hasNext()) {
                // Verifica linha a linha
                if (sc.nextLine().equals(login.getNome()+" "+login.getPassword())) {
                    valid = true;
                    break;
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("O ficheiro não existe!");
        }
        return valid;
    }
}
