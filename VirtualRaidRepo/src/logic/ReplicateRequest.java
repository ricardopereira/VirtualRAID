package logic;

import classes.BaseFile;
import classes.Common;
import classes.Request;
import classes.VirtualFile;
import enums.RequestType;
import enums.ResponseType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ReplicateRequest implements Runnable {
    
    private final RepoController ctrl;
    private final BaseFile file;
    private DatagramSocket socket = null;
    
    private boolean isCanceled = false;
    
    public ReplicateRequest(RepoController ctrl, BaseFile file) {
        this.ctrl = ctrl;
        this.file = file;
    }
    
    @Override
    public void run() {
        if (isCanceled)
            return;
        
        try {
            socket = new DatagramSocket();
        } catch(SocketException e) {
            System.out.println("Não foi possível replicar o ficheiro "+file.getName()+":\n\t" + e);
        }
        
        if (socket == null)
            return;

        System.out.println("Enviar pedido de replicação ao servidor: "+file.getName());
        try {
            requestUploadFile(file);
        } finally {
            disconnect();
        }
    }
    
    private ResponseType requestUploadFile(BaseFile file) {
        if (file == null)
            return ResponseType.RES_FAILED;
        
        if (isCanceled) {
            disconnect();
            return ResponseType.RES_CANCELED;
        }
        
        DatagramPacket packet;
        ObjectInputStream in;
        ObjectOutputStream out;
        Object obj;
        ByteArrayOutputStream buff;
        
        try {
            packet = new DatagramPacket(new byte[Common.UDPOBJECT_MAX_SIZE], Common.UDPOBJECT_MAX_SIZE);
            packet.setAddress(InetAddress.getByName(ctrl.getServerAddress()));
            packet.setPort(ctrl.getServerPort());

            buff = new ByteArrayOutputStream();
            out = new ObjectOutputStream(buff);

            out.writeObject(new Request(new VirtualFile(file), RequestType.REQ_UPLOAD));
            out.flush();
            out.close();

            packet.setData(buff.toByteArray());
            packet.setLength(buff.size());
            
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Não foi possível replicar o ficheiro "+file.getName()+":\n\t" + e);
            return ResponseType.RES_FAILED;
        }
        return ResponseType.RES_OK;
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
