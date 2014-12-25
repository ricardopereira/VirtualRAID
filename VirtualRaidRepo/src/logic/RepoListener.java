package logic;

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
    
    void onNewFile(RepositoryFile file);
    
}
