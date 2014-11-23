package classes;

import java.io.Serializable;

public class Login implements Serializable {
    final private String nome;
    private String password;
    
    public Login(String nome) {
        this.nome = nome;
        this.password = "";
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setPassword(String value) {
        password = value;
    }

    public String getPassword() {
        return password;
    }
}
