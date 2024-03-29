
package logic;

import classes.Client;
import classes.Common;
import classes.Login;
import classes.Repository;
import classes.Request;
import classes.Response;
import classes.VirtualFile;
import enums.RequestType;
import enums.ResponseType;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
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
import java.net.URISyntaxException;
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
    private Client client;
    
    private ServerListener serverListener = null;
    
    public ClientThread(Socket socket) {
        this.socket = socket;
    }
        
    @Override
    public void run() {
        if (serverListener == null)
            return;
        
        Login login;
        Repository repo;
        ObjectOutputStream oos;
        ObjectInputStream ois;
        
        try {
            socket.setSoTimeout(ServerController.TIMEOUT_AUTH*1000);
            
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
                        
                        client = new Client();
                        client.setUsername(login.getUsername());
                        
                        oos.writeObject(authenticated);
                        oos.flush();
                        // Debug
                        System.out.println(socket.getInetAddress().getHostAddress()+":"+socket.getPort()+" - Cliente autenticado");
                    }
                    else {
                        oos.writeObject(authenticated);
                        oos.flush();
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println("<Server:ClientThread> Ocorreu um erro a validar as credenciais: " + e);
            }
                                    
            if (authenticated) {
                // TimeOut após autenticação
                socket.setSoTimeout(ServerController.TIMEOUT_CLIENT*1000);
                // Autenticado
                serverListener.onConnectedClient();
                
                // Enviar lista de ficheiros inicial
                filesChangedEvent();

                // Teste: Simulador de alterações de ficheiros
                //new SimulateFileChangeThread(this).start();
                
                try {
                    while (true) {
                        ois = new ObjectInputStream(socket.getInputStream());

                        // Obtem pedido do utilizador
                        Request req = (Request) ois.readObject();

                        if (req == null) { //EOF
                            // Para terminar a thread
                            break;
                        }

                        // Devolver resposta
                        switch (req.getOption()) {
                            case REQ_DOWNLOAD:
                                // Debug
                                System.out.println(getClient().getUsername() + ": pediu download de "+req.getFile().getName()+"\n");
                                
                                oos = new ObjectOutputStream(socket.getOutputStream());
                                repo = serverListener.getRepositoriesList().getItemWithFileAndMinorConnections(req.getFile());
                                if (repo == null) {
                                    // Ficheiro já não existe na lista
                                    oos.writeObject(new Response(ResponseType.RES_NOFILE,"Sem repositorio com o ficheiro "+req.getFile().getName(),req));
                                }
                                else {
                                    oos.writeObject(new Response(repo.getAddress(),repo.getPort(),req));
                                }                                
                                oos.flush();                                    
                                break;
                            case REQ_UPLOAD:
                                // Debug
                                System.out.println(getClient().getUsername() + ": pediu upload de "+req.getFile().getName()+"\n");
                                
                                oos = new ObjectOutputStream(socket.getOutputStream());
                                repo = serverListener.getRepositoriesList().getItemWithMinorConnections(req.getFile());
                                if (repo == null) {
                                    // Ficheiro já existe nos repositórios
                                    oos.writeObject(new Response(ResponseType.RES_ALREADYEXIST,"Sem repositorio para enviar o ficheiro "+req.getFile().getName(),req));
                                }
                                else {
                                    oos.writeObject(new Response(repo.getAddress(),repo.getPort(),req));
                                }
                                oos.flush();
                                break;
                            case REQ_DELETE:
                                // Debug
                                System.out.println(getClient().getUsername() + ": pediu delete de "+req.getFile().getName()+"\n");
                                
                                deleteFile(req.getFile());
                                break;
                            default:
                                System.out.println("VirtualRaidLibrary imcompativel");
                                break;
                        }
                    }
                } catch(ClassNotFoundException e) {
                    System.err.println("<Server:ClientThread> Ocorreu um erro a receber pedido: " + e);
                }
            }
        } catch (EOFException e) {
            // Cliente fechou a ligação
        } catch (SocketTimeoutException e) {
            System.out.println("<Server:ClientThread> Ligacao terminou: " + e);
        } catch(IOException e){
            System.err.println("<Server:ClientThread> Ocorreu um erro de ligacao: " + e);
        } finally {
            // Debug
            System.out.println(socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " - Cliente foi desligado");

            try {
                socket.close();
            } catch (IOException s) {/*Silencio*/}
            
            serverListener.onClosingClient();
            authenticated = false;
        }
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
            System.out.println("<Server:ClientThread> Ocorreu um erro ao enviar pedido de eliminacao de ficheiro:\n\t" + e);
        }
    }
    
    private boolean isValid(Login login) {
        if (serverListener == null || login == null)
            return false;
        
        if (serverListener.isClientOnline(login.getUsername()))
            return false;
        
        String credentialsPath = ServerController.FILE_CREDENTIALS;
        try {
            File runnablePath = new File(ClientThread.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (runnablePath.isDirectory() && runnablePath.canRead())
                credentialsPath = runnablePath.getPath() + File.separator + credentialsPath;
            else
                credentialsPath = runnablePath.getParentFile().getPath() + File.separator + credentialsPath;
            // RP: Talvez fazer a verificação se o ficheiro existe 
            //logo no arranque do servidor será porreiro para no caso de falhar
            Scanner sc = new Scanner(new File(credentialsPath));
            while (sc.hasNext()) {
                // Verifica linha a linha
                if (sc.nextLine().equals(login.getUsername()+" "+login.getPassword())) {
                    // Valid
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("<Server:ClientThread> Ficheiro "+ServerController.FILE_CREDENTIALS+" nao existe:\n\t"+e.getMessage());
        } catch (URISyntaxException ex) {

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
        
        // Debug
        //System.out.println("filesChangedEvent called");
        serverListener.notifyFileChanged();
        
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            // Enviar lista de ficheiros
            oos.writeObject(serverListener.getFilesList());
            oos.flush();
        } catch (SocketTimeoutException e) {
            System.out.println("<Server:ClientThread> Ligacao terminou:\n\t" + e);
        } catch(IOException e){
            System.out.println("<Server:ClientThread> Ocorreu um erro de ligacao:\n\t" + e);
        }
    }

    public Client getClient() {
        return client;
    }
    
    public boolean isAuthenticated() {
        return client != null;
    }
    
}
