package logic;

import classes.RMIApplicationInterface;
import classes.Client;
import classes.FilesList;
import classes.Repository;
import classes.RMIServiceInterface;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RMIApplication extends UnicastRemoteObject implements RMIApplicationInterface{
    
    public static RMIApplication appRMI;
    public static RMIServiceInterface serviceInterface;
    
    public RMIApplication()throws RemoteException{}
    
    
    @Override
    public void updateListRepo()throws RemoteException{
        ArrayList<Repository> repo = serviceInterface.getRMIActiveRepositories();
        System.out.println("Existem " +repo.size() + " repositorios");
    }
    
    @Override
    public void updateListFiles()throws RemoteException {
        FilesList files = serviceInterface.getRMIAllFiles();
        System.out.println("Existem " + files.size()+ " ficheiros");
    }
    
    @Override
    public void updateListUsers()throws RemoteException{
        ArrayList<Client> cli = serviceInterface.getRMIActiveUsers();
        System.out.println("Existem " + cli.size()+ " clientes");
    }

    
    public static void main(String[] args){

        try {
            //procura objeto remoto
            appRMI = new RMIApplication();
            String objectUrl = "rmi://127.0.0.1/RMIService";

            if (args.length > 0) {
                objectUrl = "rmi://" + args[0] + "/RMIService";
            }

            serviceInterface = (RMIServiceInterface) Naming.lookup(objectUrl);
            serviceInterface.addObserver(appRMI);

            FilesList files = serviceInterface.getRMIAllFiles();
            System.out.println("Existem " + files.size() + " ficheiros");
            ArrayList<Repository> repo  = serviceInterface.getRMIActiveRepositories();
            System.out.println("Existem " + repo.size() + " repositorios");
            ArrayList<Client> cli = serviceInterface.getRMIActiveUsers();
            System.out.println("Existem " + cli.size() + " clientes");

        } catch (RemoteException e) {
            System.out.println("Remote error - " + e);
        } catch (NotBoundException e) {
            System.out.println("Remote Service unknown - " + e);
        } catch (Exception e) {
            System.out.println("Error - " + e);
        }


    }
}
