package classes;

import java.io.Serializable;

public class Login implements Serializable {
    
    final private String username;
    private String password;
    
    public Login(String username) {
        this.username = username;
        this.password = "";
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setPassword(String value) {
        password = value;
    }

    public String getPassword() {
        return password;
    }
    
}
