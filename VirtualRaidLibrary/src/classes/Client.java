package classes;

import enums.ClientOperation;
import java.io.Serializable;

public class Client implements Serializable {
    private String username;
    private ClientOperation currentOperation;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ClientOperation getCurrentOperation() {
        return currentOperation;
    }

    public void setCurrentOperation(ClientOperation currentOperation) {
        this.currentOperation = currentOperation;
    }
    
}
