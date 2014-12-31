package logic;

import classes.FilesList;
import classes.Response;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * ResponsesManager classe.
 * Thread responável por gerir as respostas do Servidor.
 * 
 * Respostas possíveis:
 *  - Nova lista de ficheiros.
 *  - Resposta de um pedido (Download, Upload ou Delete).
 * 
 * @author Team
 */
public class ResponsesManager extends Thread {
    
    final private ClientController ctrl;
    private boolean isCanceled = false;
    
    public ResponsesManager(ClientController ctrl) {
        this.ctrl = ctrl;
    }
    
    @Override
    public void run() {
        if (!ctrl.getIsAuthenticated())
            return;
        
        if (isCanceled)
            return;

        while (true) {
            try {
                ObjectInputStream ois = new ObjectInputStream(ctrl.getMainSocket().getInputStream());

                // Resposta do Servidor
                Object resultFromServer = ois.readObject();
                
                if (resultFromServer == null) { //EOF
                    // Para terminar a thread
                    break;
                }
                
                if (isCanceled)
                    return;
                
                if (resultFromServer instanceof FilesList) {
                    // Debug
                    System.out.println("<ResponsesManager> FilesList");
                    // Recebeu uma nova lista de ficheiros
                    notifyFilesList((FilesList) resultFromServer);
                }
                else if (resultFromServer instanceof Response) {
                    // Debug
                    System.out.println("<ResponsesManager> Response");
                    // Recebeu uma resposta de um pedido efectuado
                    notifyResponse((Response) resultFromServer);
                }                
            } catch (EOFException e) {
                return;
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("<ResponsesManager> Não foi possível receber a resposta do servidor:\n\t" + e);
            }
            
            if (isCanceled)
                return;
        }
    }
    
    public void cancel() {
        isCanceled = true;
    }
    
    public void notifyFilesList(FilesList filesList) {
        if (isCanceled)
            return;

        if (filesList != null)
            ctrl.setFilesList(filesList);
    }
    
    public void notifyResponse(Response res) {
        if (isCanceled)
            return;
        
        if (res != null)
            ctrl.interpretResponse(res);
    }
}