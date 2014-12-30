package ui.text;

import logic.ClientController;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Repositório não foi iniciado:\n\t- Argumentos <diretoria> [<ip_servidor> <porto_servidor>]");
            return;
        }
        // Arguments
        String localFilesDir = args[0];
        // Start Text User Interface (TUI)
        UIText uiText = new UIText(new ClientController());
        if (args.length > 1)
            uiText.setServerAddress(args[1]);
        if (args.length > 2)
            uiText.setServerPort(Integer.parseInt(args[2]));
        uiText.startInterface(localFilesDir);
    }

}
