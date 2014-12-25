package classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Repository implements Serializable {
    
    private String address;
    private int port;
    final private ArrayList<RepositoryFile> files;
    
    public Repository(String address, int port) {
        this.address = address;
        this.port = port;
        files = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Repositório: ");
        sb.append(address);
        sb.append("\n");
        for (RepositoryFile item : files) {
            sb.append(item.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Repository) {
            return (((Repository) obj).address.equals(this.address));
        }
        return false;
    }
    
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the files
     */
    public synchronized ArrayList<RepositoryFile> getFiles() {
        return files;
    }
    
    /**
     * @return the files
     */
    public FilesList getFilesList() {
        // Obtem a lista de ficheiros num map
        FilesList map = new FilesList();
        for (RepositoryFile item : files) {
            map.add(new VirtualFile(item));
        }
        return map;
    }
    
}