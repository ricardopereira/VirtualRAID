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
    
    public void addRepositoryFiles(Repository repository) {
        if (repository == null)
            return;
        for (BaseFile file : repository.getFiles()) {
            int fileIndex = indexOf(file);
            // Verifica se o ficheiro já existe
            if (fileIndex >= 0) {
                // Já existe, incrementar o número de cópias
                get(fileIndex).incrementNrCopies();
            }
            else {
                // Ainda não existe, adicionar à lista
                add(new VirtualFile(file));
            }
        }
    }
    
}
