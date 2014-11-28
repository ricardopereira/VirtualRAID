package ui.text;

import classes.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Criação do repositorio
        Repository self = null;
        try {
            self = new Repository(InetAddress.getLocalHost().getHostAddress(), 0);
        } catch (UnknownHostException e) {
            System.out.println("Não foi possível obter o endereço local.");
        }
        
        if (self == null) {
            System.out.println("Não foi possível iniciar o repositório.");
            return;
        }
        
        FileManager fm = null;
        try {
            fm = new FileManager("/Users/ricardopereira/Desktop");
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
        
        fm.loadFiles(self.getFiles());
        
        System.out.println(self.toString());
    }
    
}
