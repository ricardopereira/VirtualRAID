package logic;

import classes.FilesList;
import enums.ResponseType;

/**
 * ClientListener protótipo.
 * Responsável por refrescar as opções do utilizador.
 * 
 * @author Team
 */
public interface ClientListener {
    
    void onFilesListChanged(FilesList newFilesList);
    void onFilesError(String message);
    void onResponseError(ResponseType status);
    void onOperationStarted(String fileName);
    void onOperationProgress(int nbytes);
    void onOperationFinished(String message);
    
}
