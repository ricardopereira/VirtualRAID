package ui.text;

import java.rmi.RemoteException;
import logic.ServerController;

public class Main {
            
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Servidor de ficheiros não foi iniciado:\n\t- Argumentos <porto_escuta>");
            return;
        }
        int listenPort = Integer.parseInt(args[0]);
        ServerController ctrl;
        try {
            ctrl = new ServerController(listenPort);
        } catch (RemoteException e) {
            System.out.println("Servidor vai terminar: " + e);
            return;
        }
        
        ctrl.startRMI();
        
        System.out.println("Servidor de ficheiros iniciado no porto "+listenPort);
        // Iniciar recepção de clientes
        //(Bloqueante)
        ctrl.startListeningClients();
        System.out.println("Servidor vai terminar...");
    }
        
}
