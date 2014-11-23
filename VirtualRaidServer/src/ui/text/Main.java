package ui.text;

import classes.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Main {
    // Teste
    public static ArrayList<Repository> repositories;
            
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Teste
        FilesList allFiles = testeFicheirosRecebidosDeRepositorios();
        
        // RP: Receber parâmetro porto pelo "args"
        int porto=9000; //Por defeito
        Servidor serv = new Servidor(porto,allFiles);
        // RP: Uma thread?!
        //O servidor terá apenas um ServerSocket,
        //porque senão vamos ter vários portos para ligar ao servidor.
        serv.start();
        try {
            serv.join();
        } catch (InterruptedException e) {
            System.err.println("<servidor:main> Ocorreu um erro com a thread do servidor: " + e);
        }
    }
    
    public static FilesList testeFicheirosRecebidosDeRepositorios() {
        // Teste
        repositories = new ArrayList<>();
        
        // Repositorio 001
        Repository r001 = new Repository("192.0.0.1");
        // Repositorio 002
        Repository r002 = new Repository("192.0.0.2");
        
        // Repositório 001 se conectou!
        repositories.add(r001);
        r001.getFiles().add(new RepositoryFile("Motocross.avi",1024,new Date()));
        r001.getFiles().add(new RepositoryFile("HannaMontana.avi",1024,new Date()));
        r001.getFiles().add(new RepositoryFile("PowerRangers.avi",3024,new Date()));
        r001.getFiles().add(new RepositoryFile("DragonBall.avi",1024,new Date()));
        // Repositório 002 se conectou!
        repositories.add(r002);
        r002.getFiles().add(new RepositoryFile("HelloKitty.avi",2024,new Date()));
        
        // Mostra o que tem
        showAllFiles();
        
        FilesList allFiles = new FilesList();
        for (Repository item : repositories) {
            allFiles.addAll(item.getFilesList());
        }    
        return allFiles;
    }
    
    public static void showAllFiles() {
        for (Repository item : repositories) {
            System.out.println(item.toString());
        }
    }
    
}
