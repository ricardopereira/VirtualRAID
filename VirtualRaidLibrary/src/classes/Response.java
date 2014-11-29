package classes;

import enums.RequestType;
import enums.ResponseType;
import java.io.Serializable;

/**
 * Response classe.
 * Resultado do pedido efectuado.
 * 
 * @author Team
 */
public class Response implements Serializable {
    
    private final ResponseType status;
    private final String message;
    private final String repositoryAddress;
    private final Integer repositoryPort;
    private final Request requested;
    
    public Response(String repositoryAddress, Integer repositoryPort, Request requested) {
        this.status = ResponseType.RES_OK;
        this.message = "Successfully";
        this.repositoryAddress = repositoryAddress;
        this.repositoryPort = repositoryPort;
        this.requested = requested;
    }
    
    public Response(ResponseType status, String message, Request requested) {
        this.status = status;
        this.message = message;
        this.repositoryAddress = "";
        this.repositoryPort = 0;
        this.requested = requested;
    }
    
    public ResponseType getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }

    public String getRepositoryAddress() {
        return repositoryAddress;
    }

    public Integer getRepositoryPort() {
        return repositoryPort;
    }

    public Request getRequested() {
        return requested;
    }

}
