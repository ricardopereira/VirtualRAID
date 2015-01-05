package logic;

import classes.Repository;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class CheckRepositoriesTimer extends Thread {
    
    private static final int TIMER_RECUR = 4; //Segundos
    private static final int REPO_TOLERATION = 6; //Segundos
    
    private final ServerController ctrl;
    
    public CheckRepositoriesTimer(ServerController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(TIMER_RECUR * 1000);
            } catch (InterruptedException e) { break; }
            
            Date currentDate = new Date();
            boolean hasChanged = false;
            
            Iterator<Repository> i = ctrl.getActiveRepositories().iterator();
            while (i.hasNext()) {
                Repository item = i.next();
                
                long timeElapsed = currentDate.getTime() - item.getLastUpdate().getTime();
                
                // Se o repositório não enviar heartbeat no tempo estipulado,
                //será removido...
                if ((timeElapsed/1000) > REPO_TOLERATION && item.getNrConnections() <= 0) {
                    // Remove o repositório activo
                    i.remove();
                    hasChanged = true;
                    // Debug
                    System.out.println("Repositorio "+item.getAddressAndPort()+" removido por inactividade");
                }
            }
            
            if (hasChanged) {
                ctrl.updateClients();
                ctrl.removeActiveRepository();
            }
        }
    }
    
}
