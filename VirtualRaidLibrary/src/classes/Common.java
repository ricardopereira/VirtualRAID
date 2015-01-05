package classes;

public class Common {
    
    // Descobrir o servidor
    public static final String MULTICAST_ADDRESS = "230.30.30.30";
    public static final int MULTICAST_PORT = 9999;
    public static final String MULTICAST_SECRETKEY_UDP = "UDP-What'sYourIP?";
    public static final String MULTICAST_SECRETKEY_TCP = "TCP-What'sYourIP?";
    public static final int MULTICAST_MAX_SIZE = 100;
    public static final int MULTICAST_TIME_OUT = 10; //Segundos
    
    // Eliminar o ficheiro dos repositórios
    public static final String DELETE_ADDRESS = "230.30.30.35";
    public static final int DELETE_PORT = 10000;
    
    public static final int UDPOBJECT_MAX_SIZE = 1000;
    
    public static final int FILECHUNK_MAX_SIZE = 4000;
    
    // RMI
    public static final String RMIService = "RMIService";
    
}
