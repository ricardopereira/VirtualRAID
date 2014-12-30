package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Repository implements Serializable {
    
    private String address;
    private int port;
    private int currentConnections;
    private final ArrayList<RepositoryFile> files;
    
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
            return (((Repository) obj).getAddressAndPort().equals(this.getAddressAndPort()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.address);
        hash = 71 * hash + this.port;
        return hash;
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
     * @return address and port
     */
    public String getAddressAndPort() {
        return getAddress()+":"+getPort();
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

    // ToDo
    public int getCurrentConnections() {
        return currentConnections;
    }
    
}