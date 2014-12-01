package ui.text;

import classes.FilesList;
import classes.Login;
import classes.VirtualFile;
import enums.ResponseType;
import java.util.Scanner;
import logic.ClientController;
import logic.ClientListener;

public class UIText {
    
    private ClientController ctrl;
    
    public static final int APP_EXIT = 0;
    public static final int APP_DONE = 9;
    
    // Várias opções possíveis no menu
    public static enum MenuOptions {
        OPT_NONE, 
        OPT_DOWNLOAD, 
        OPT_UPLOAD, 
        OPT_DELETE;
    }
    
    // Opção escolhida fica em memória
    private MenuOptions currentMenuOption = MenuOptions.OPT_NONE;
    private boolean hasPendingRequest = false;
    
    public UIText(ClientController ctrl) {
        this.ctrl = ctrl;
        ctrl.setClientListener(clientListener);
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
        
        // Menu
        while (true) {
            if (menu() == APP_EXIT)
                break;
        }
        
        // Desligar
        ctrl.disconnectToServer();
    }
    
    // Obter opção da linha de comandos
    private int getOptionNumber() {
        Scanner s = new Scanner(System.in);
        while (!s.hasNextInt()) {
            s.next();
        }
        return s.nextInt();
    }
    
    private int menu() {
        int opt = APP_EXIT;
        // Imprime a lista de opções, mais a lista de ficheiros
        printCurrentInterface(false);
        
        if (hasPendingRequest) {
            // Tem acção pendente, ou seja, está a fazer download ou upload
            //e dar possibilidade de cancelar...
            do {
                System.out.println("Enter key "+APP_DONE+" to cancel: ");
                opt = getOptionNumber();
            } while (opt != APP_DONE && hasPendingRequest);
            
            // ToDo: Cancel...
            return APP_DONE;
        }
        else {
            // Escolha da opção do Menu...
            do {
                System.out.println(" Option: ");
                opt = getOptionNumber();
            } while (opt < APP_EXIT || opt > 3);
        }
        
        // Verificar se é para terminar a aplicação
        if (opt == APP_EXIT || opt == APP_DONE)
            return opt; // Exit

        // Guarda a opção escolhida
        currentMenuOption = MenuOptions.values()[opt];

        switch (currentMenuOption) {
            case OPT_DOWNLOAD:
                System.out.println("Choose file to download: ");
                // Obter o índice do ficheiro
                opt = getOptionNumber();
                
                // ToDo: o índice pode mudar se a lista de ficheiros sofrer alterações
                //Colocar esta parte como synchronize!
                
                if (opt >= 0 && opt < ctrl.getFilesList().size()) {
                    // Criar cópia do registo para evitar problemas com as threads
                    VirtualFile choosedFile = new VirtualFile(ctrl.getFilesList().get(opt));
                    System.out.println("Start download of:\n\t" + choosedFile.toString());
                    
                    // RP: pensar no caso em que um cliente escolheu um ficheiro
                    //mas ainda não se iniciou o download e outro cliente
                    //remover esse ficheiro!
                    
                    ctrl.requestDownloadFile(choosedFile);
                    hasPendingRequest = true;
                    
                    System.out.println("Checking if file is available...");
                }
                else {
                    System.out.println("File don't exist.\n");
                    currentMenuOption = MenuOptions.OPT_NONE;
                }
                break;
            case OPT_UPLOAD:
                System.out.println("Choose file to upload: ");
                // Obter o índice do ficheiro
                opt = getOptionNumber();
                
                // ToDo

                System.out.println("File uploaded.\n");
                currentMenuOption = MenuOptions.OPT_NONE;
                break;
            case OPT_DELETE:
                System.out.println("Choose file to delete: ");
                // Obter o índice do ficheiro
                opt = getOptionNumber();
                
                // ToDo

                System.out.println("File deleted.\n");
                currentMenuOption = MenuOptions.OPT_NONE;
                break;
            default:
                System.out.println("Closing...");
                break;
        }            

        // A aplicação continua a correr
        return -1;
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
    
    private void printMenuOptions() {
        System.out.println("MENU:");
        System.out.println(" "+APP_EXIT+". Exit");
        System.out.println(" 1. Download file");
        System.out.println(" 2. Upload file");
        System.out.println(" 3. Delete file");
    }
    
    private void printCurrentInterface() {
        // Por defeito
        printCurrentInterface(true);
    }
    
    private void printCurrentInterface(boolean printChooseOption) {        
        if (hasPendingRequest) {
            // Tem acção pendente, ou seja, está a fazer download ou upload...
            printPendingRequest(printChooseOption);
        }
        else {
            // Está à espera da escolha do ficheiro
            printPendingChoice(printChooseOption);
        }
    }
    
    private void printPendingRequest(boolean printCancelOption) {
        // Se tiver opção seleccionada...
        switch (currentMenuOption) {
            case OPT_DOWNLOAD:
                System.out.println("File available.");
                break;
            case OPT_UPLOAD:
                System.out.println("Upload permited.");
                break;
            default:
                System.out.println("DEBUG: printCurrentInterface - Algo correu mal...");
                break;
        }
        if (printCancelOption)
            System.out.println(" Input key "+APP_DONE+" to cancel: ");
    }
    
    private void printPendingChoice(boolean printChooseOption) {
        // Imprime a lista de ficheiros
        System.out.println("\nFILES:");
        if (ctrl.canUseFilesList()) {
            System.out.println(ctrl.getFilesList().toString());
        }
        else {
            System.out.println("List file is empty.\n");
        }

        // Só imprime o Menu se não seleccionou nenhuma opção
        if (currentMenuOption == MenuOptions.OPT_NONE) {
            // Menu
            printMenuOptions();
        }
        
        // Se tiver opção seleccionada...
        if (currentMenuOption != MenuOptions.OPT_NONE) {
            switch (currentMenuOption) {
                case OPT_DOWNLOAD:
                    System.out.println("Choose file to download: ");
                    break;
                case OPT_UPLOAD:
                    System.out.println("Choose file to upload: ");
                    break;
                case OPT_DELETE:
                    System.out.println("Choose file to delete: ");
                    break;
            }       
        }
        else if (printChooseOption) {
            System.out.println(" Option: ");
        }
    }
    
    private ClientListener clientListener = new ClientListener() {

        @Override
        public void onFilesListChanged(FilesList newFilesList) {
            printCurrentInterface();
        }
        
        @Override
        public void onResponseError(ResponseType status) {
            
        }
        
        @Override
        public void onDownloadStarted(String fileName) {
            System.out.println(fileName);
        }
        
        @Override
        public void onDownloadProgress(int nbytes) {
            
        }
        
        @Override
        public void onDownloadFinished() {
            System.out.println("Done");
            System.out.println("Input 9 to return to menu: ");
            hasPendingRequest = false;
            currentMenuOption = MenuOptions.OPT_NONE;
        }
        
    };

}
