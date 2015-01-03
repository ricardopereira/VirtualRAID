package classes;

import classes.BaseFile;
import classes.Repository;
import java.io.Serializable;
import java.util.ArrayList;

public class RepositoriesList extends ArrayList<Repository> implements Serializable{
    
    public Repository getItemWithFileAndMinorConnections(BaseFile file) {
        Repository repo = null;
        for (Repository item : this) {
            if (item.getFiles().contains(file)) {
                if (repo == null) {
                    repo = item;
                }
                else if (item.getNrConnections() < repo.getNrConnections()) {
                    repo = item;
                }
            }
        }
        return repo;
    }
    
    public Repository getItemWithMinorConnections(BaseFile file) {
        Repository repo = null;
        for (Repository item : this) {
            if (!item.getFiles().contains(file)) {
                if (repo == null) {
                    repo = item;
                }
                else if (item.getNrConnections() < repo.getNrConnections()) {
                    repo = item;
                }
            }
        }
        return repo;
    }
    
}
