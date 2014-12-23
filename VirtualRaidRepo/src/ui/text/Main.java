package ui.text;

import classes.FileManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import logic.RepoController;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // ToDo: receber por argumento
        String dir = "/Users/ricardopereira/Desktop/Repo";
        int port = 9001;
        
        FileManager fm = null;
        try {
            fm = new FileManager(dir);
        } catch (FileManager.DirectoryNotFound e) {
            System.out.println(e.getMessage());
        } catch (FileManager.DirectoryInvalid e) {
            System.out.println(e.getMessage());
        } catch (FileManager.DirectoryNoPermissions e) {
            System.out.println(e.getMessage());
        }
        
        if (fm == null) {
            System.out.println("Não foi possível obter controlo dos ficheiros.");
            return;
        }
        
        RepoController ctrl = null;
        try {
            ctrl = new RepoController(InetAddress.getLocalHost().getHostAddress(), port, fm);
        } catch (UnknownHostException e) {
            System.err.println("Não foi possível obter o endereço local.");
        }

        System.out.println("Ficheiros:\n"+ctrl.getRepository().toString());
        
        if (ctrl == null)
            return;
                
        System.out.println("À procura do servidor\n...");
        // Procura o servidor
        if (ctrl.findServer()) {
            System.out.println("Iniciar ligação com: "+ctrl.getServerAddress()+":"+ctrl.getServerPort());
        }
        else {
            System.out.println("Nenhum servidor encontrado.");
            return;
        }
        
        // Iniciar recepção de clientes
        //(Bloqueante)
        ctrl.startListeningClients();
        System.out.println("Repositório vai terminar...");
    }

}
