
import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Carine
 */
public class Login implements Serializable{
    private String nome;
    private String password;
    
    public Login(String nome, String password) {
        this.nome = nome;
        this.password = password;
    }
    
    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }
}
