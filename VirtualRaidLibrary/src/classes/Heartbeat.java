package classes;

import java.io.Serializable;

public class Heartbeat implements Serializable {
    
    private final int currentConnections;
    
    public Heartbeat(int currentConnections) {
        this.currentConnections = currentConnections;
    }
    
    public int getCurrentConnections() {
        return currentConnections;
    }
    
}
