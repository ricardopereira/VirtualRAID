package classes;

import java.util.Date;

public class VirtualFile extends BaseFile {
    
    // Numero de réplicas nos repositórios
    private int nrCopies = 1;
    
    public VirtualFile(BaseFile file) {
        super(file.getName(), file.getSizeBytes(), file.getDateModified());
    }

    public VirtualFile(String name, long size, Date dateModified) {
        super(name, size, dateModified);
    }
    
    @Override
    public String toString() {
        return super.toString() + ", Nr cópias: "+getNrCopies();
    }

    /**
     * @return the nrCopies
     */
    public int getNrCopies() {
        return nrCopies;
    }

    public void incrementNrCopies() {
        nrCopies++;
    }
    
}
