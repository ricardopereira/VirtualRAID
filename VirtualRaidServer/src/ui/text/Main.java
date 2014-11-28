package ui.text;

import classes.Repository;
import logic.ServerController;

public class Main {
            
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // ToDo: recever argumentos
        ServerController ctrl = new ServerController(9000);

        // Teste: mostra todos os ficheiros de repositórios no ecrã
        for (Repository item : ctrl.getRepositories()) {
            System.out.println(item.toString());
        }
        
        // Iniciar recepção de clientes
        //(Bloqueante)
        System.out.println("Servidor de ficheiros iniciado...");
        ctrl.startListeningClients();
        
        System.out.println("Servidor vai terminar...");
    }
        
}
