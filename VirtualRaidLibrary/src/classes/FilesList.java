package classes;

import java.util.ArrayList;

public class FilesList extends ArrayList<VirtualFile> {
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (VirtualFile item : this) {
            sb.append(item.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
