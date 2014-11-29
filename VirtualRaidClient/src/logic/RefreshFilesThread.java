package logic;

import classes.FilesList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class RefreshFilesThread extends Thread {
    
    private Socket serverSocket;
    private boolean isCanceled = false;
    private IFilesListListener filesListChangedListener;
    
    public RefreshFilesThread(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }
    
    @Override
    public void run() {
        if (serverSocket == null)
            return;
        if (isCanceled)
            return;
        
        while (true) {
            FilesList filesList = null;
            try {
                ObjectInputStream ois = new ObjectInputStream(serverSocket.getInputStream());
                filesList = (FilesList) ois.readObject();
            } catch (ClassNotFoundException | IOException e) {
                System.err.println("Não foi possível receber a lista de ficheiros:\n\t" + e);
            }
            
            if (isCanceled)
                return;

            if (filesList != null)
                performFilesListChanged(filesList); //Callback
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }
    
    public void performFilesListChanged(FilesList filesList) {
        if (filesListChangedListener != null)
            filesListChangedListener.onFilesListChanged(filesList);
    }
    
    public void setFilesListChangedListener(IFilesListListener listener) {
        filesListChangedListener = listener;
    }
    
}