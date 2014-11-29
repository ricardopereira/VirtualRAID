package tests;

import logic.ClientThread;

public class SimulateFileChangeThread extends Thread {

    ClientThread client;
    
    public SimulateFileChangeThread(ClientThread client) {
        this.client = client;
    }
    
    @Override
    public void run() {
        if (client == null)
            return;
        
        while (true) {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) { break; }
            
            if (client.isAlive())
                client.fileChangedEvent();
        }
        
    }
    
}
