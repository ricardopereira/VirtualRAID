package ui.text;

import classes.FilesList;
import classes.Login;
import classes.VirtualFile;
import enums.ResponseType;
import java.util.Date;
import java.util.Scanner;
import logic.ClientController;
import logic.ClientListener;

public class UIText {
    
    private ClientController ctrl;
    // Servidor principal
    private String serverAddress = "127.0.0.1"; //Por defeito
    private int serverPort = 9000; //Por defeito
    
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
    
    public void startInterface(String localFilesDir) {
        // Diretoria
        ctrl.setLocalFilesDirectory(localFilesDir);

        // Verificar se está ligado ao servidor
        if (!ctrl.getIsConnected()) {
            // Endereço e porto do servidor
            if (ctrl.connectToServer(getServerAddress(),getServerPort())) {
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
                System.out.println("Prima a tecla "+APP_DONE+" para cancelar: ");
                opt = getOptionNumber();
            } while (opt != APP_DONE && hasPendingRequest);
            
            // ToDo: Cancel...
            return APP_DONE;
        }
        else {
            // Escolha da opção do Menu...
            do {
                System.out.println(" Opção: ");
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
                System.out.println("Escolha o ficheiro para fazer download: ");
                // Obter o índice do ficheiro
                opt = getOptionNumber();
                
                // ToDo: o índice pode mudar se a lista de ficheiros sofrer alterações
                //Colocar esta parte como synchronize!
                
                if (opt >= 0 && opt < ctrl.getFilesList().size()) {
                    // Criar cópia do registo para evitar problemas com as threads
                    VirtualFile choosedFile = new VirtualFile(ctrl.getFilesList().get(opt));
                    System.out.println("Iniciar transferencia de :\n\t" + choosedFile.toString());
                    
                    // RP: pensar no caso em que um cliente escolheu um ficheiro
                    //mas ainda não se iniciou o download e outro cliente
                    //remover esse ficheiro!
                    
                    ctrl.requestDownloadFile(choosedFile);
                    hasPendingRequest = true;
                    
                    System.out.println("A verificar se o ficheiro existe...");
                }
                else {
                    System.out.println("O ficheiro não existe.\n");
                    currentMenuOption = MenuOptions.OPT_NONE;
                }
                break;
            case OPT_UPLOAD:
                System.out.println("Escolha o ficheiro para fazer upload: ");
                // Obter o índice do ficheiro
                opt = getOptionNumber();
                
                // ToDo
                ctrl.requestUploadFile(new VirtualFile("ScreenShot2014.png", 1000, new Date()));
                hasPendingRequest = true;
                
                break;
            case OPT_DELETE:
                System.out.println("Escolha o ficheiro para eliminar: ");
                // Obter o índice do ficheiro
                opt = getOptionNumber();
                
                // ToDo
                ctrl.requestDeleteFile(new VirtualFile("ScreenShot2014.png", 1000, new Date()));
                hasPendingRequest = true;
                
                break;
            default:
                System.out.println("Adeus...");
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
        System.out.println(" "+APP_EXIT+". Sair");
        System.out.println(" 1. Download ficheiro");
        System.out.println(" 2. Upload ficheiro");
        System.out.println(" 3. Delete ficheiro");
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
                
                break;
            case OPT_UPLOAD:
                
                break;
            default:
                System.out.println("[DEBUG] printPendingRequest: algo correu mal...");
                break;
        }
        if (printCancelOption)
            System.out.println(" Prima a tecla "+APP_DONE+" para cancelar: ");
    }
    
    private void printPendingChoice(boolean printChooseOption) {
        // Imprime a lista de ficheiros
        System.out.println("\nFICHEIROS:");
        if (ctrl.canUseFilesList()) {
            System.out.println(ctrl.getFilesList().toString());
        }
        else {
            System.out.println("Lista de ficheiros vazia.\n");
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
                    System.out.println("Escolha o ficheiro para fazer download: ");
                    break;
                case OPT_UPLOAD:
                    System.out.println("Escolha o ficheiro para fazer upload: ");
                    break;
                case OPT_DELETE:
                    System.out.println("Escolha o ficheiro para eliminar: ");
                    break;
            }       
        }
        else if (printChooseOption) {
            System.out.println(" Opcao: ");
        }
    }
    
    private ClientListener clientListener = new ClientListener() {

        @Override
        public void onFilesListChanged(FilesList newFilesList) {
            printCurrentInterface();
        }
        
        @Override
        public void onFilesError(String message) {
            System.out.println("Erro:\n\t"+message);
            restoreInterface();
        }
        
        @Override
        public void onResponseError(ResponseType status) {
            System.out.println("Erro:\n\t"+ResponseType.labels[status.ordinal()]);
            restoreInterface();
        }
        
        @Override
        public void onOperationStarted(String fileName) {
            System.out.println(fileName);
        }
        
        @Override
        public void onOperationProgress(int nbytes) {
            System.out.println("Transferido... "+nbytes);
        }
        
        @Override
        public void onOperationFinished(String message) {
            System.out.println(message);
            restoreInterface();
        }
        
        private void restoreInterface() {
            if (hasPendingRequest) {
                System.out.println("Prima a tecla 9 para voltar ao menu: ");                
            }
            hasPendingRequest = false;
            currentMenuOption = MenuOptions.OPT_NONE;
        }
        
    };

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

}
