
package logic;

import classes.Login;
import classes.Request;
import classes.Response;
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

    private Socket socket;
    private boolean authenticated = false;
    
    private ServerListener serverListener = null;
    
    public ClientThread(Socket socket) {
        this.socket = socket;
    }
        
    @Override
    public void run() {
        if (serverListener == null)
            return;
        
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
            
            serverListener.onConnectedClient();
            // Enviar lista de ficheiros inicial
            filesChangedEvent();
            
            // Teste: Simulador de alterações de ficheiros
            //new SimulateFileChangeThread(this).start();
                        
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
                            // ToDo: Verificar o repositório que tem o ficheiro 
                            //e que está mais livre
                            oos.writeObject(new Response("127.0.0.1",9001,req));
                            oos.flush();
                            break;
                        case REQ_DELETE:
                            
                            // ToDo
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
        } finally {
            serverListener.onClosingClient();
            authenticated = false;
            
            try {
                socket.close();
            } catch (IOException s) {/*Silencio*/}
        }
        
        // Debug
        System.out.println("<Server:ClientThread> Cliente desligou...");
    }
    
    private static boolean isValid(Login login) {
        try {
            // RP: Talvez fazer a verificação se o ficheiro existe 
            //logo no arranque do servidor será porreiro para no caso de falhar
            Scanner sc = new Scanner(new File(ServerController.FILE_CREDENTIALS));
            while (sc.hasNext()) {
                // Verifica linha a linha
                if (sc.nextLine().equals(login.getUsername()+" "+login.getPassword())) {
                    // Valid
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("Ficheiro "+ServerController.FILE_CREDENTIALS+" não existe:\n\t"+e.getMessage());
        }
        return false;
    }
    
    public void setServerListener(ServerListener listener) {
        serverListener = listener;
    }
    
    public void filesChangedEvent() {
        if (serverListener == null)
            return;
        
        ObjectOutputStream oos;

        if (!authenticated)
            return;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            // Enviar lista de ficheiros
            oos.writeObject(serverListener.getFilesList());
            oos.flush();
        } catch (SocketTimeoutException e) {
            System.err.println("<Server:ClientThread> Ligação terminou: " + e);
        } catch(IOException e){
            System.err.println("<Server:ClientThread> Ocorreu um erro de ligação: " + e);
        }
    }
}
