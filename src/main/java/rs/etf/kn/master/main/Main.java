
package rs.etf.kn.master.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.opencv.OpenCV;

public class Main implements ServletContextListener {

    public Main() {
        System.out.println("Constructor of Main");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("ServletContextListener started");
        Configuration.load();
        
        initializeBackend();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("ServletContextListener destroyed");
        Configuration.save();
    }
    
    private void initializeBackend(){
        OpenCV.loadNativeLibraries();
    }
}
