package logic;

import classes.FilesList;

public interface IFilesListListener {
    
    void onFilesListChanged(FilesList newFilesList);
    
}
