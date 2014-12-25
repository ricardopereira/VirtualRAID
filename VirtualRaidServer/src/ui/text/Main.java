package ui.text;

import classes.Repository;
import logic.ServerController;

public class Main {
            
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // ToDo: receber argumentos
        ServerController ctrl = new ServerController(9000);        
        System.out.println("Servidor de ficheiros iniciado...");
        // Iniciar recepção de clientes
        //(Bloqueante)
        ctrl.startListeningClients();
        System.out.println("Servidor vai terminar...");
    }
        
}
