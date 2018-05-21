package rs.etf.kn.master.main;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import rs.etf.kn.master.dataSource.camera.CamProcessingManager;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.opencv.OpenCV;

public class Main implements ServletContextListener {

    public Main() {
        System.out.println("Constructor of Main");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized begin");
        Configuration.load();
        initializeBackend();
        System.out.println("contextInitialized end");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("contextDestroyed begin");
        Configuration.save();
        deinitializeBackend();
        System.out.println("contextDestroyed end");
    }

    private void initializeBackend() {
        OpenCV.loadNativeLibraries();
        CamProcessingManager.initialize();
    }

    private void deinitializeBackend() {
        CamProcessingManager.deinitialize();
    }
}
