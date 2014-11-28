package ui.text;

import logic.ClientController;

public class Main {

    public static void main(String[] args) {
        startTextUserInterface();
    }
    
    public static void startTextUserInterface()
    {        
        UIText uiText = new UIText(new ClientController());
        // Start
        uiText.startInterface();
    }

}
