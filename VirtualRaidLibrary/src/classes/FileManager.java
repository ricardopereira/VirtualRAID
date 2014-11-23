package classes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class FileManager {
    
    File currentDirectory = null;
    
    // Excepção: Directoria não existe
    public static class DirectoryNotFound extends IOException {
        public DirectoryNotFound(String message) {
            super(message);
        }
    };
    
    // Excepção: Directoria inválida
    public static class DirectoryInvalid extends IOException {
        public DirectoryInvalid(String message) {
            super(message);
        }
    };
    
    // Excepção: Directoria sem permissões
    public static class DirectoryNoPermissions extends IOException {
        public DirectoryNoPermissions(String message) {
            super(message);
        }
    };
    
    public FileManager(String directoryPath) throws DirectoryNotFound, DirectoryInvalid, DirectoryNoPermissions {
        // Criação do objecto para a directoria
        currentDirectory = new File(directoryPath);
        
        if (!currentDirectory.exists()) {
            throw new DirectoryNotFound("A directoria \"" + directoryPath + "\" não existe.");
        }
        
        if (!currentDirectory.isDirectory()) {
            throw new DirectoryInvalid("O caminho \"" + directoryPath + "\" não se refere a uma directoria.");
        }
        
        if (!currentDirectory.canWrite()) {
            throw new DirectoryNoPermissions("Sem permissoes de escrita na directoria \"" + directoryPath + "\".");
        }
    }
    
    // Obter o caminho absoluto já com o separador da directoria actual
    public String getCurrentDirectoryPath() {
        try {
            return currentDirectory.getCanonicalPath() + File.separator;
        } catch (IOException e) {
            return "";
        }
    }
    
    // Verifica se o ficheiro existe na directoria actual
    public Boolean checkFileExists(String filePath) {
        File file = new File(getCurrentDirectoryPath() + filePath);
        return file.exists();
    }
    
    // Carregar a lista de ficheiros
    public void loadFiles(ArrayList<RepositoryFile> files) {
        if (files == null)
            return;
        // Limpar a lista
        files.clear();
        // Lista de ficheiros da directoria
        for (File file : currentDirectory.listFiles()) {
            if (file.isFile() && !file.isHidden()) {
                // Atributos do ficheiro
                files.add(new RepositoryFile(file.getName(), file.length(), new Date(file.lastModified())));
            }
        }
    }
            
}
