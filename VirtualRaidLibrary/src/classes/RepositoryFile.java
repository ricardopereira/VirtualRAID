package classes;

import java.util.Date;

public class RepositoryFile {
    
    private String name;
    private int size; //bytes
    private Date dateCreated;
    private Date dateModified;
    private int locks;
    
    public RepositoryFile(String name, int size, Date dateCreated, Date dateModified) {
        this.name = name;
        this.size = size;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
    }
    
    @Override
    public String toString() {
        return name + ", Tamanho: " + getSizeKb() + "Kb, Modificado em: " + getDateModified();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the size bytes
     */
    public int getSizeBytes() {
        return size;
    }
    
    public int getSizeKb() {
        return size/1024;
    }
    
    public int getSizeMb() {
        return getSizeKb()/1024;
    }

    /**
     * @param size the size bytes to set
     */
    public void setSizeBytes(int size) {
        this.size = size;
    }

    /**
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the dateModified
     */
    public Date getDateModified() {
        return dateModified;
    }

    /**
     * @param dateModified the dateModified to set
     */
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
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
