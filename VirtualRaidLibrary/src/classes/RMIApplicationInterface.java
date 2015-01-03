package classes;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIApplicationInterface extends Remote{
    
    public void updateListRepo()throws RemoteException;
    public void updateListFiles()throws RemoteException;
    public void updateListUsers()throws RemoteException;
}
