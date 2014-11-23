package classes;

import java.util.Date;

public class RepositoryFile extends BaseFile {
    
    // Número de ligações ao ficheiro (nr downloads do momento)
    private transient int locks;

    public RepositoryFile(String name, long size, Date dateModified) {
        super(name, size, dateModified);
    }
    
    /**
     * @return the locks
     */
    public int getLocks() {
        return locks;
    }

    /**
     * @param locks the locks to set
     */
    public void setLocks(int locks) {
        this.locks = locks;
    }
}
