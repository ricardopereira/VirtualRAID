package ui.text;

import classes.FilesList;
import classes.Login;
import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import logic.ClientController;
import logic.IFilesListListener;

public class UIText {
    
    private ClientController ctrl;
    
    public static enum MenuOptions {
        OPT_NONE, OPT_DOWNLOAD, OPT_UPLOAD, OPT_DELETE;
    }
    
    private MenuOptions currentMenuOption = MenuOptions.OPT_NONE;
    
    public UIText(ClientController ctrl) {
        this.ctrl = ctrl;
        ctrl.setFilesListChangedListener(refreshFilesList);
    }
    
    public void startInterface() {
        // Verificar se está ligado ao servidor
        if (!ctrl.getIsConnected()) {
            // Teste: falta os argumentos para o endereço e porta do servidor
            if (ctrl.connectToServer("127.0.0.1",9000)) {
                System.out.println("Ligado ao servidor principal...");
            }
            else {
                System.out.println("Sem ligação ao servidor.");
                return;
            }
        }
        
        // Verificar se está autenticado
        while (!ctrl.getIsAuthenticated()) {
            menuAuthenticate();
        }
        
        while (true) {
            if (menu() == 0)
                break;
        }
        
        // Desligar
        ctrl.disconnectToServer();
    }
    
    private String getOptionString() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    private int getOptionNumber() {
        Scanner s = new Scanner(System.in);
        while (!s.hasNextInt()) {
            s.next();
        }
        return s.nextInt();
    }
    
    private int menu() {
        int opt = 0;
        // Imprimir lista de ficheiros
        printFilesList();
        
        // Menu do Cliente
        if (currentMenuOption != MenuOptions.OPT_NONE) {
            System.out.println("MENU:");
            System.out.println(" 0. Exit");
            System.out.println(" Option: "+currentMenuOption);
            
            // ToDo
            Scanner sc = new Scanner(System.in);
            if (sc.nextInt() == 0)
                return 0;
        }
        else {
            System.out.println("MENU:");
            System.out.println(" 0. Exit");
            System.out.println(" 1. Download file");
            System.out.println(" 2. Upload file");
            System.out.println(" 3. Delete file");
            
            do {
                System.out.println(" Option: ");
                opt = getOptionNumber();
            } while (opt < 0 || opt > 3);

            // Guarda a opção escolhida
            currentMenuOption = MenuOptions.values()[opt];
                    
            switch (currentMenuOption) {
                case OPT_DOWNLOAD:
                    System.out.println("Choose file to download: ");
                    // ToDo
                    break;
                case OPT_UPLOAD:
                    System.out.println("Choose file to upload: ");
                    // ToDo
                    break;
                case OPT_DELETE:
                    System.out.println("Choose file to delete: ");
                    // ToDo
                    break;
                default:
                    System.out.println("Closing...");
                    break;
            }            
        }
        return opt;
    }
    
    private void menuAuthenticate() {
        // Autenticar
        Login login = null;
        String word;

        System.out.println("\nAUTENTICAÇÃO:");
        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        while ((word = sc.nextLine()) != null) {
            if (login == null) {
                login = new Login(word);
                System.out.print("Password: ");
            } else {
                login.setPassword(word);
                break;
            }
        }

        if (ctrl.authenticate(login))
            System.out.println("Login com sucesso.\n");
        else
            System.out.println("Credenciais inválidas.\n");
    }
    
    public void printFilesList() {
        if (ctrl.canUseFilesList()) {
            System.out.println("\nFILES:");
            System.out.println(ctrl.getFilesList().toString());
        }
        else {
            System.out.println("Não existem ficheiros.");
        }        
    }
    
    private IFilesListListener refreshFilesList = new IFilesListListener() {

        @Override
        public void onFilesListChanged(FilesList newFilesList) {
            printFilesList();
        }
        
    };

}
