package rs.etf.kn.master.main;

import com.google.maps.GeoApiContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import rs.etf.kn.master.dataSource.StreetDataManager;
import rs.etf.kn.master.dataSource.camera.CamProcessingManager;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.opencv.OpenCV;

public class Main implements ServletContextListener {

    private static String GOOGLE_MAPS_KEY = "AIzaSyCn64R6bJT58MAxEOu9X2rBNdNfB8OjfDM";
    
    public static GeoApiContext geoApiContext;
    public Main() {
        System.out.println("Constructor of Main");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized begin");
        Configuration.load();
        StreetDataManager.loadMockMobileData();
        initializeBackend();
        geoApiContext = new GeoApiContext.Builder().apiKey(GOOGLE_MAPS_KEY).build();
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
