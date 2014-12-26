
package logic;

import classes.Common;
import classes.Login;
import classes.Request;
import classes.Response;
import classes.VirtualFile;
import enums.RequestType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
                            // ToDo: Verificar o repositório que tem o ficheiro 
                            //e que está mais livre e se é o dono
                            
                            deleteFile(req.getFile());
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
    
    private void deleteFile(VirtualFile file) {
        ObjectInputStream in;
        ObjectOutputStream out;
        ByteArrayOutputStream buff;
        
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(Common.MULTICAST_TIME_OUT*1000);
            
            // Envia broadcast para eliminar ficheiro
            DatagramPacket packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
            packet.setAddress(InetAddress.getByName(Common.DELETE_ADDRESS));
            packet.setPort(Common.DELETE_PORT);
            
            buff = new ByteArrayOutputStream();
            out = new ObjectOutputStream(buff);

            out.writeObject(new Request(file, RequestType.REQ_DELETE));
            out.flush();
            out.close();

            packet.setData(buff.toByteArray());
            packet.setLength(buff.size());
            
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("<Server:ClientThread> Ocorreu um erro ao enviar pedido de eliminação de ficheiro:\n\t" + e);
        }
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
            System.err.println("<Server:ClientThread> Ficheiro "+ServerController.FILE_CREDENTIALS+" não existe:\n\t"+e.getMessage());
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
            System.out.println("<Server:ClientThread> Ligação terminou:\n\t" + e);
        } catch(IOException e){
            System.out.println("<Server:ClientThread> Ocorreu um erro de ligação:\n\t" + e);
        }
    }
}
