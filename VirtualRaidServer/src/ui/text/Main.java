package ui.text;

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
        ServerController ctrl = new ServerController(listenPort);
        System.out.println("Servidor de ficheiros iniciado no porto "+listenPort);
        // Iniciar recepção de clientes
        //(Bloqueante)
        ctrl.startListeningClients();
        System.out.println("Servidor vai terminar...");
    }
        
}
