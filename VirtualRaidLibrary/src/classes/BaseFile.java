package classes;

import java.io.Serializable;
import java.util.Date;

public class BaseFile implements Serializable {
    
    private String name;
    private long size; //bytes
    private Date dateModified;
    
    public BaseFile(String name, long size, Date dateModified) {
        this.name = name;
        this.size = size;
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
    public long getSizeBytes() {
        return size;
    }
    
    public long getSizeKb() {
        return size/1024;
    }
    
    public long getSizeMb() {
        return getSizeKb()/1024;
    }

    /**
     * @param size the size bytes to set
     */
    public void setSizeBytes(int size) {
        this.size = size;
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
    
}
