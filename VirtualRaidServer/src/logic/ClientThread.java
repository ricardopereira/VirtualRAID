
package logic;

import classes.FilesList;
import classes.Login;
import classes.Request;
import classes.Response;
import enums.RequestType;
import enums.ResponseType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import tests.SimulateFileChangeThread;

/**
 * ClientThread classe.
 * Thread do Servidor responsável pela interacção com um cliente ligado.
 * 
 * @author Team
 */
public class ClientThread extends Thread {
    
    public static final String FILE_CREDENTIALS = "credenciais.txt";
    
    private Socket socket;
    private boolean authenticated = false;
    
    private FilesList files;
    
    public ClientThread(Socket socket, FilesList startFiles) {
        this.socket = socket;
        this.files = startFiles;
    }
    
    public FilesList getFilesList() {
        return files;
    }
    
    public void setFilesList(FilesList newFiles) {
        if (files != null)
            return;
        this.files = newFiles;
    }
    
    @Override
    public void run() {
        Login login;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        
        try {
            // Verificar autenticacao
            while (!authenticated) {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());

                // Obtem autenticacao do utilizador
                login = (Login) ois.readObject();
                
                if (login == null) { //EOF
                    // Para terminar a thread
                    break;
                }
                
                // Verificar login
                if (isValid(login)) {
                    authenticated = true;
                    oos.writeObject(authenticated);
                    oos.flush();
                }
                else {
                    oos.writeObject(authenticated);
                    oos.flush();
                }
            }
            
            // Enviar lista de ficheiros inicial
            fileChangedEvent();
            
            // Teste: Simulador de alterações de ficheiros
            new SimulateFileChangeThread(this).start();
            
            // ToDo
            socket.setSoTimeout(0);
            
            if (authenticated) {
                while (true) {
                    ois = new ObjectInputStream(socket.getInputStream());
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    // Obtem pedido do utilizador
                    Request req = (Request) ois.readObject();
                    
                    if (req == null) { //EOF
                        // Para terminar a thread
                        break;
                    }
                                        
                    // Devolver resposta
                    switch (req.getOption()) {
                        case REQ_DOWNLOAD:
                            // ToDo: Verificar o repositório que tem o ficheiro 
                            //e que está mais livre
                            oos.writeObject(new Response("127.0.0.1",9001,req));
                            oos.flush();
                            break;
                        case REQ_UPLOAD:
                            
                            break;
                        case REQ_DELETE:
                            
                            break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("<Server:ClientThread> Ocorreu um erro a validar as credenciais: " + e);        
        } catch (SocketTimeoutException e) {
            System.err.println("<Server:ClientThread> Ligação terminou: " + e);
        } catch(IOException e){
            System.err.println("<Server:ClientThread> Ocorreu um erro de ligação: " + e);
        }
        
        authenticated = false;
        try {
            socket.close();
        } catch (IOException s) {/*Silencio*/}
        
        // Debug
        System.out.println("Cliente desligou...");
    }
    
    private static boolean isValid(Login login) {
        try {
            // RP: Talvez fazer a verificação se o ficheiro existe 
            //logo no arranque do servidor será porreiro para no caso de falhar
            Scanner sc = new Scanner(new File(FILE_CREDENTIALS));
            while (sc.hasNext()) {
                // Verifica linha a linha
                if (sc.nextLine().equals(login.getUsername()+" "+login.getPassword())) {
                    // Valid
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("Ficheiro "+FILE_CREDENTIALS+" não existe:\n\t"+e.getMessage());
        }
        return false;
    }
    
    public void fileChangedEvent() {
        ObjectOutputStream oos;

        if (!authenticated)
            return;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            // Enviar lista de ficheiros
            oos.writeObject(files);
            oos.flush();
        } catch (SocketTimeoutException e) {
            System.err.println("<Server:ClientThread> Ligação terminou: " + e);
        } catch(IOException e){
            System.err.println("<Server:ClientThread> Ocorreu um erro de ligação: " + e);
        }
    }
}
