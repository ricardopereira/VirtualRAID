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
       // System.out.println("Existem " +repo.size() + " repositorios");
       if (repo.isEmpty())
            System.out.println("Nao existem repositorios ativos!");
       else{
            System.out.println("REPOSITORIOS ATIVOS: ");
            for (int i = 0; i < repo.size(); i++) {
                System.out.println("Repositorio "+ i + " " + repo.get(i).getAddressAndPort());
            } 
       }
    }
    
    @Override
    public void updateListFiles()throws RemoteException {
        FilesList files = serviceInterface.getRMIAllFiles();
        //System.out.println("Existem " + files.size()+ " ficheiros");
        if (files.isEmpty())
            System.out.println("Nao existem ficheiros!");
        else {
            System.out.println("FICHEIROS EXISTENTES: ");
            for (int i = 0; i < files.size(); i++) {
                System.out.println(files.get(i).toString());
            } 
        }
    }
    
    @Override
    public void updateListUsers() throws RemoteException {
        ArrayList<Client> cli = serviceInterface.getRMIActiveUsers();
        if (cli.isEmpty()) 
            System.out.println("Nao existem utilizadores conectados!");
        else {
            System.out.println("UTILIZADORES CONECTADOS: ");
            for (int i = 0; i < cli.size(); i++) {
                System.out.println(cli.get(i).getUsername());
            }
        }
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
            //System.out.println("Existem " + files.size() + " ficheiros");
            ArrayList<Repository> repo  = serviceInterface.getRMIActiveRepositories();
           // System.out.println("Existem " + repo.size() + " repositorios");
            ArrayList<Client> cli = serviceInterface.getRMIActiveUsers();
           // System.out.println("Existem " + cli.size() + " clientes");

        } catch (RemoteException e) {
            System.out.println("Remote error - " + e);
        } catch (NotBoundException e) {
            System.out.println("Remote Service unknown - " + e);
        } catch (Exception e) {
            System.out.println("Error - " + e);
        }


    }
}
