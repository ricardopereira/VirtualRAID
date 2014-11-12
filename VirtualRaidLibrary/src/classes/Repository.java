package classes;

import java.util.ArrayList;

public class Repository {
    
    private String address;
    private int port;
    private ArrayList<RepositoryFile> files;
    
    public Repository(String address) {
        this.address = address;
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
    public ArrayList<RepositoryFile> getFiles() {
        return files;
    }
    
}