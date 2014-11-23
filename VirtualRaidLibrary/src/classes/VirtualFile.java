package classes;

import java.util.Date;

public class VirtualFile extends BaseFile {
    
    // Numero de réplicas nos repositórios
    private int duplicated;
    
    public VirtualFile(BaseFile file) {
        super(file.getName(), file.getSizeBytes(), file.getDateModified());
    }

    public VirtualFile(String name, long size, Date dateModified) {
        super(name, size, dateModified);
    }

    /**
     * @return the duplicated
     */
    public int getDuplicated() {
        return duplicated;
    }

    /**
     * @param duplicated the duplicated to set
     */
    public void setDuplicated(int duplicated) {
        this.duplicated = duplicated;
    }
    
}
