package classes;

import enums.RequestType;
import java.io.Serializable;

/**
 * Request classe.
 * Pedido a um repositório ou servidor
 * 
 * @author Team
 */
public class Request implements Serializable {
    
    final private VirtualFile file;
    final private RequestType option;
    
    public Request(VirtualFile file, RequestType option) {
        this.file = file;
        this.option = option;
    }
    
    public VirtualFile getFile() {
        return file;
    }
    
    public RequestType getOption() {
        return option;
    }
    
}
