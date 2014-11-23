package ui.text;

import classes.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Socket socket;
        try {
            // Teste
            socket = new Socket("127.0.0.1",9000);
        } catch (IOException e) {
            System.err.println("Não foi possível ligar ao servidor: " + e);
            return;
        }
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            BufferedReader pin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(pin.readLine());
            
            // Autenticação
            Login login = null;
            String word;
            Scanner sc = new Scanner(System.in);
            while ((word = sc.nextLine()) != null) {
                if (login == null) {
                    login = new Login(word);
                }
                else {
                    login.setPassword(word);
                    break;
                }
            }
            sc.close();
            
            oos.writeObject(login);
            
            // Teste
            System.out.println(pin.readLine());
            // Receber lista de ficheiros
            FilesList allFiles;
            try {
                allFiles = (FilesList) ois.readObject();
                System.out.println(allFiles.toString());
            } catch (ClassNotFoundException e) {
                System.err.println("Não foi possível receber a lista de ficheiros: " + e);
            }
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de ligação ao servidor: " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {/*Silencio*/}
        }
    }

}
