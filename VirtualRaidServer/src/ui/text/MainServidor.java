/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Carine
 */
public class MainServidor {
     public static void main(String[] args) {
        int porto=0;
        Servidor serv = new Servidor(porto);

        try
        {
            serv.start();
            serv.join();
            
        } catch (InterruptedException e) {
            System.err.println("<servidor:main> Ocorreu um erro com a thread do servidor: " + e);
            System.exit(1);
        }
     }
     
}
