package ui.text;

import classes.Login;
import java.util.Scanner;
import logic.ClientController;

public class UIText {
    
    private ClientController ctrl;
    
    public static enum MenuOptions {
        OPT_NONE, OPT_DOWNLOAD, OPT_UPLOAD, OPT_DELETE;
    }
    private MenuOptions currentMenuOption = MenuOptions.OPT_NONE;
    
    public UIText(ClientController ctrl) {
        this.ctrl = ctrl;
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
        if (!ctrl.getIsAuthenticated()) {
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
    
    private int getOptionNumber(int from, int to) {
        Scanner s = new Scanner(System.in);
        int nr = from;
        while (!s.hasNextInt() && (nr < from || nr > to)) {
            nr = s.nextInt();
        }
        return s.nextInt();
    }
    
    private int menu() {
        // Imprimir lista de ficheiros
        // ToDo
        
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
            
            int opt;
            do {
               opt = getOptionNumber(0,3);
                        
            } while (opt > 0);
        }
        
        // Teste
        return 0;
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
        sc.close();

        //ctrl.login();
    }

}
