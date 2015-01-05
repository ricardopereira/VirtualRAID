package logic;

import classes.Common;
import classes.Heartbeat;
import classes.Repository;
import classes.Request;
import classes.Response;
import classes.VirtualFile;
import enums.RequestType;
import enums.ResponseType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * RepositoriesThread classe.
 * Thread responsável pela recepção de Heartbeats e ficheiros de repositórios.
 * 
 * @author Team
 */
public class RepositoriesThread extends Thread {
    
    // Constantes
    public static final int UDP_PORT = 4999;
    
    private final ServerController ctrl;
    private DatagramSocket repoSocket;
    private boolean isCanceled = false;
    
    public RepositoriesThread(ServerController ctrl) {
        this.ctrl = ctrl;
        try {
            repoSocket = new DatagramSocket(UDP_PORT);
        } catch (SocketException e) {
            System.err.println("Nao foi possivel iniciar RepositoriesThread:\n\t"+e.getMessage());
            repoSocket = null;
        }
    }

    @Override
    public void run() {
        if (repoSocket == null)
            return;
        
        if (isCanceled)
            return;
        
        DatagramPacket packet;
        ObjectInputStream in;
        Object obj;

        try {
            while (!isCanceled) {
                packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
                repoSocket.receive(packet);
                
                if (isCanceled)
                    return;
                                
                try {
                    // Recebe objecto por UDP
                    in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                    obj = in.readObject();
                    in.close();
                    
                    if (isCanceled)
                        return;
                    
                    if (obj != null && obj instanceof Heartbeat) {
                        // Número de ligações
                        ctrl.setRepositoryActiveConnections(packet.getAddress().getHostAddress(), packet.getPort(), ((Heartbeat) obj).getCurrentConnections());
                        // Debug - Heartbeat
                        //System.out.println(packet.getAddress().getHostAddress()+":"+packet.getPort()+" - ligações: "+((Heartbeat) obj).getCurrentConnections());
                    }
                    else if (obj != null && obj instanceof Repository) {
                        // Adiciona ou actualiza
                        ctrl.addActiveRepository((Repository) obj);
                        // Mostra ficheiros do repositório ligado
                        System.out.println(packet.getAddress().getHostAddress()+":"+packet.getPort()+" - "+((Repository) obj).toString());
                    }
                    else if (obj != null && obj instanceof Request) {
                        // Pedido de UP_LOAD do repositório (REPLICAÇÃO)
                        Request req = (Request) obj;
                        if (req.getOption() == RequestType.REQ_UPLOAD) {
                            // Procurar um repositório
                            Repository repo = ctrl.getActiveRepositories().getItemWithMinorConnections(req.getFile());
                            if (repo == null) {
                                // Não existe mais nenhum repositório
                                sendResponse(packet, new Response(ResponseType.RES_CANCELED, "Replicação", req));
                            }
                            else {
                                sendResponse(packet, new Response(repo.getAddress(),repo.getPort(),req));
                            }
                        }
                        else {
                            sendResponse(packet, new Response(ResponseType.RES_INVALID, "Replicação", req));
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {
                    System.out.println("<RepositoriesThread> Impossibilidade de aceder ao conteudo da mensagem recebida:\n\t"+e);
                }
            }
        } catch (IOException e) {
            if (!repoSocket.isClosed()) {
                repoSocket.close();
            }
        }
    }
    
    private void sendResponse(DatagramPacket packet, Response res) {
        if (repoSocket == null)
            return;
        
        ObjectOutputStream out;
        ByteArrayOutputStream buff;
        
        try {
            packet.setData(new byte[Common.UDPOBJECT_MAX_SIZE]);
            packet.setLength(Common.UDPOBJECT_MAX_SIZE);

            buff = new ByteArrayOutputStream();
            out = new ObjectOutputStream(buff);

            out.writeObject(res);
            out.flush();
            out.close();

            packet.setData(buff.toByteArray());
            packet.setLength(buff.size());
            
            // Envia resposta
            repoSocket.send(packet);
        } catch (IOException e) {
            System.out.println("<RepositoriesThread> Nao foi possivel respsota:\n\t" + e);
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }
    
}
