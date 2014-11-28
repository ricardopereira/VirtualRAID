
package logic;

import classes.FilesList;
import classes.Login;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ClientThread extends Thread {
    
    public static final String FILE_CREDENTIALS = "credenciais.txt";
    
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    PrintWriter pout;
    
    private FilesList files;
    
    public ClientThread(Socket socket, FilesList startFiles) {
        this.socket = socket;
        this.files = startFiles;
    }
    
    public void setFilesList(FilesList newFiles) {
        if (files != null)
            return;
        this.files = newFiles;
    }
    
    @Override
    public void run() {
        Login login;
        boolean authenticated = false;
        
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            pout = new PrintWriter(socket.getOutputStream());
            
            while(!authenticated) {
                pout.println("Introduza username e password: <username password> ");
                pout.flush();
                
                login = (Login) ois.readObject();
                // Verificar login
                if (isValid(login)) {
                    pout.println("Login efetuado com sucesso.");
                    pout.flush();
                    
                    authenticated = true;
                    // Enviar lista de ficheiros inicial
                    oos.writeObject(files);
                }
                else {
                    pout.println("Credenciais inválidas.");
                    pout.flush();
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("<Server:ClientThread> Ocorreu um erro ao ler: " + e);        
        } catch (SocketTimeoutException e) {
            System.err.println("<Server:ClientThread> Ligação terminou: " + e);
        } catch(IOException e){
            System.err.println("<Server:ClientThread> Ocorreu um erro de ligação: " + e);
        }

        try {
            socket.close();
        } catch (IOException s) {/*Silencio*/}
    }
    
    private static boolean isValid(Login login) {
        try {
            // RP: Talvez fazer a verificação se o ficheiro existe 
            //logo no arranque do servidor será porreiro para no caso de falhar
            Scanner sc = new Scanner(new File(FILE_CREDENTIALS));
            while (sc.hasNext()) {
                // Verifica linha a linha
                if (sc.nextLine().equals(login.getNome()+" "+login.getPassword())) {
                    // Valid
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("Ficheiro "+FILE_CREDENTIALS+" não existe:\n\t"+e.getMessage());
        }
        return false;
    }
}
