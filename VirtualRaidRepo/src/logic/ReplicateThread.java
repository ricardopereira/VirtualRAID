package logic;

import classes.Common;
import classes.Response;
import enums.ResponseType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReplicateThread extends Thread {
    
    private final RepoController ctrl;
    private final DatagramSocket socket;
    
    private boolean isCanceled = false;

    public ReplicateThread(RepoController ctrl, DatagramSocket socket) {
        this.ctrl = ctrl;
        this.socket = socket;
    }
    
    @Override
    public void run() {        
        if (socket == null)
            return;
        
        if (isCanceled) {
            disconnect();
            return;
        }
        
        DatagramPacket packet;
        ObjectInputStream in;
        Object obj;
        Response res = null;

        try {
            packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
            socket.receive(packet);
            
            // Recebe objecto por UDP
            in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
            obj = in.readObject();
            in.close();

            if (isCanceled || obj == null)
                return;

            if (obj instanceof Response) {
                res = (Response) obj;
            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("<RepositoriesThread> Impossibilidade de aceder ao conteudo da mensagem recebida:\n\t"+e);
        } finally {
            disconnect();
        }
        
        if (res == null)
            return;
        
        if (res.getStatus() == ResponseType.RES_OK) {
            // Replicar para o outro repositório
            ctrl.replicateFile(res.getRepositoryAddress(), res.getRepositoryPort(), res.getRequested().getFile());
            // Repete o processo para replicar para outro repositório
            ctrl.requestReplicateFile(res.getRequested().getFile());
            // ToDo: poderá entrar em ciclo se enviar sempre o mesmo repositório
        }
    }
    
    private void disconnect() {
        if (socket == null)
            return;
        if (!socket.isClosed()) {
            socket.close();
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }

}
