package logic;

import classes.RMIApplicationInterface;
import classes.Client;
import classes.FilesList;
import classes.RMIServiceInterface;
import classes.Repository;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Monitor extends UnicastRemoteObject implements RMIApplicationInterface {
    
    private final RMIServiceInterface serviceInterface;
    
    public Monitor(RMIServiceInterface service) throws RemoteException {
        this.serviceInterface = service;
    }
    
    @Override
    public void updateListRepo() throws RemoteException {
        ArrayList<Repository> repo = serviceInterface.getRMIActiveRepositories();
        // System.out.println("Existem " +repo.size() + " repositorios");
        if (repo.isEmpty()) {
            System.out.println("Nao existem repositorios ativos!");
        } else {
            System.out.println("REPOSITORIOS ATIVOS: ");
            for (int i = 0; i < repo.size(); i++) {
                System.out.println("Repositorio " + i + " " + repo.get(i).getAddressAndPort());
            }
        }
    }
    
    @Override
    public void updateListFiles() throws RemoteException {
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

}
