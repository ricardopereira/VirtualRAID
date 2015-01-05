package ui.text;

import classes.Client;
import classes.Common;
import classes.FilesList;
import classes.RMIServiceInterface;
import classes.Repository;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import logic.Monitor;

public class Main {
    
    public static Monitor monitor = null;
    public static RMIServiceInterface serviceInterface;
    
    public static void main(String[] args) {
                
        String objectUrl = "rmi://localhost/" + Common.RMIService;

        if (args.length > 0) {
            objectUrl = "rmi://" + args[0] + "/"+Common.RMIService;
        }

        try {
            serviceInterface = (RMIServiceInterface) Naming.lookup(objectUrl);
            
            // Procura objeto remoto
            monitor = new Monitor(serviceInterface);

            serviceInterface.addObserver(monitor);
            
            System.out.println("Monitor iniciado...");

            FilesList files = serviceInterface.getRMIAllFiles();
            // System.out.println("Existem " + files.size() + " ficheiros");
            ArrayList<Repository> repo  = serviceInterface.getRMIActiveRepositories();
            // System.out.println("Existem " + repo.size() + " repositorios");
            ArrayList<Client> cli = serviceInterface.getRMIActiveUsers();
            // System.out.println("Existem " + cli.size() + " clientes");

        } catch (RemoteException e) {
            System.out.println("Erro remoto:\n\t" + e);
        } catch (NotBoundException e) {
            System.out.println("Erro na ligacao:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao:\n\t" + e);
        } finally {
            //if (serviceInterface != null && monitor != null)
                //try {
                    //serviceInterface.removeObserver(monitor);
                //} catch (RemoteException e) {}
        }

    }
    
}
