package ui.text;

import logic.ClientController;

public class Main {
    // ToDo: receber por argumento
    public static final String FILES_DIR = "/Users/ricardopereira/Desktop";

    public static void main(String[] args) {
        startTextUserInterface();
    }
    
    public static void startTextUserInterface()
    {        
        UIText uiText = new UIText(new ClientController(FILES_DIR));
        // Start
        uiText.startInterface();
    }

}
