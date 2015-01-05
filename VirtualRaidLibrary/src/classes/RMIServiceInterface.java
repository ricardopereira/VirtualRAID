package classes;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIServiceInterface extends Remote {
    
    public void addObserver(RMIApplicationInterface app) throws RemoteException;
    public void removeObserver(RMIApplicationInterface app) throws RemoteException;
    public RepositoriesList getRMIActiveRepositories() throws RemoteException;
    public FilesList getRMIAllFiles() throws RemoteException;
    public ArrayList<Client> getRMIActiveUsers() throws RemoteException;
    
}
