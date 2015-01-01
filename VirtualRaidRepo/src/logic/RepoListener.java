package logic;

import classes.BaseFile;
import classes.RepositoryFile;

/**
 * RepoListener protótipo.
 * Responsável por fechar e activar ligações, etc.
 * 
 * @author Team
 */
public interface RepoListener {
    
    void onConnectedClient();
    void onClosingClient();
    
    boolean onCheckFile(RepositoryFile file);
    void onRemoveFile(RepositoryFile file);
    void onNewFile(RepositoryFile file);
    
}
