package enums;

/**
 * ResponseType enumerado.
 * Resultado do pedido efectuado.
 * 
 * @author Team
 */
public enum ResponseType {
    RES_FAILED, 
    RES_OK, 
    RES_NOFILE, 
    RES_NOPERMISSION, 
    RES_CANCELED, 
    RES_PROGRESS,
    RES_ALREADYEXIST;
    
    public static final String[] labels = {
        "Erro a obter resposta", 
        "Bem sucedido", 
        "Ficheiro não existe", 
        "Sem permissões para o ficheiro", 
        "Cancelado", 
        "",
        "Ficheiro já existe"
    };
}