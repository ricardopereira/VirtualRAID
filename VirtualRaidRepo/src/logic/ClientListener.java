package logic;

/**
 * ClientListener protótipo.
 * Responsável por fechar e activar ligações.
 * 
 * @author Team
 */
public interface ClientListener {
    
    void onConnectedClient();
    void onClosingClient();
    
}
