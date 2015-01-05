package classes;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class BaseFile implements Serializable {
    
    private String name;
    private long size; //bytes
    private Date dateModified;
    
    public BaseFile(String name, long size, Date dateModified) {
        this.name = name;
        this.size = size;
        this.dateModified = dateModified;
    }
    
    public String uniqueKey() {
        return getName().trim() +":"+ getSizeBytes();
    }
    
    @Override
    public String toString() {
        return name + ", Tam: " + getSizeKb() + "Kb, Modificado: " + getDateModified();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof BaseFile) {
            return (((BaseFile) obj).uniqueKey().equalsIgnoreCase(this.uniqueKey()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.name);
        hash = 73 * hash + (int) (this.size ^ (this.size >>> 32));
        return hash;
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
