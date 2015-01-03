package logic;

import classes.RepositoriesList;
import classes.FilesList;

/**
 * ServerListener protótipo.
 * Callback para métodos do ServerController.
 * 
 * @author Team
 */
public interface ServerListener {
    
    void onConnectedClient();
    void onClosingClient();
    
    RepositoriesList getRepositoriesList();
    
    FilesList getFilesList();
    void notifyFileChanged();
}
