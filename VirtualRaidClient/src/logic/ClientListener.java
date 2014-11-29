package logic;

import classes.FilesList;
import enums.ResponseType;

public interface ClientListener {
    
    void onFilesListChanged(FilesList newFilesList);
    void onResponseError(ResponseType status);
    void onDownloadStarted(String fileName);
    void onDownloadProgress(int nbytes);
    void onDownloadFinished();
    
}
