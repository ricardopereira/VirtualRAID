package ui.text;

import classes.*;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    // Teste
    public static ArrayList<Repository> repositories;
            
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Teste
        repositories = new ArrayList<>();
        
        // Repositorio 001
        Repository r001 = new Repository("192.0.0.1");
        // Repositorio 002
        Repository r002 = new Repository("192.0.0.2");
        
        // Repositório 001 se conectou!
        repositories.add(r001);
        r001.getFiles().add(new RepositoryFile("Motocross.avi",1024,new Date(),new Date()));
        r001.getFiles().add(new RepositoryFile("HannaMontana.avi",1024,new Date(),new Date()));
        r001.getFiles().add(new RepositoryFile("PowerRangers.avi",3024,new Date(),new Date()));
        r001.getFiles().add(new RepositoryFile("DragonBall.avi",1024,new Date(),new Date()));
        // Repositório 002 se conectou!
        repositories.add(r002);
        r002.getFiles().add(new RepositoryFile("HelloKitty.avi",2024,new Date(),new Date()));
        
        // Mostra o que tem
        showAllFiles();
    }
    
    public static void showAllFiles() {
        for (Repository item : repositories) {
            System.out.println(item.toString());
        }
    }
    
}
