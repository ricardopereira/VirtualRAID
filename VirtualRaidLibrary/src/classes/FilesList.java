package classes;

import java.util.ArrayList;

public class FilesList extends ArrayList<VirtualFile> {
        
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (VirtualFile item : this) {
            sb.append(index++);
            sb.append(". ");
            sb.append(item.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
    
}
