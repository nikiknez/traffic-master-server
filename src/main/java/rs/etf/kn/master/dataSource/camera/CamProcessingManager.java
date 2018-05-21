package rs.etf.kn.master.dataSource.camera;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import rs.etf.kn.master.model.CamStreetConfig;
import rs.etf.kn.master.model.Camera;
import rs.etf.kn.master.model.Configuration;

public class CamProcessingManager {

    private static final Logger LOG = Logger.getLogger(CamProcessingManager.class.getName());

    private static ConcurrentHashMap<Camera, CamImageFetcher> fetchers;
    private static ConcurrentHashMap<String, CamImageAnalyser> analysers;

    public static void initialize() {
        fetchers = new ConcurrentHashMap<>();
        analysers = new ConcurrentHashMap<>();
        for (Camera c : Configuration.get().getCameras()) {
            try {
                CamImageFetcher fetcher = CamImageFetcher.create(c);
                for (CamStreetConfig config : c.getStreets()) {
                    BufferedImage reperImage = readReperImage(config.getStreetId());
                    CamImageAnalyser analyser = new CamImageAnalyser(config, reperImage);
                    analyser.start();
                    analysers.put(config.getStreetId(), analyser);
                    fetcher.addListener(analyser);
                }
                fetcher.start();
                fetchers.put(c, fetcher);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void deinitialize() {
        for (CamImageFetcher f : fetchers.values()) {
            f.stopFetching();
        }
        for (CamImageFetcher f : fetchers.values()) {
            try {
                f.join();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        for (CamImageAnalyser a : analysers.values()) {
            a.stopProcessing();
        }
        for (CamImageAnalyser a : analysers.values()) {
            try {
                a.join();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addCamera(Camera c) {
        try {
            CamImageFetcher fetcher = CamImageFetcher.create(c);
            fetcher.start();
            fetchers.put(c, fetcher);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void addCameraConfig(Camera c, CamStreetConfig config, BufferedImage reperImage) throws InterruptedException {
        CamImageFetcher fetcher = fetchers.get(c);
        CamImageAnalyser analyser = analysers.get(config.getStreetId());
        if (analyser != null) {
            fetcher.removeListener(analyser);
            analyser.stopProcessing();
            analyser.join();
        }
        analyser = new CamImageAnalyser(config, reperImage);
        analyser.start();
        analysers.put(config.getStreetId(), analyser);
        fetcher.addListener(analyser);
    }

    private static BufferedImage readReperImage(String imgId) throws IOException {
        String path = Configuration.REPERS_DIR + imgId + ".jpg";
        File file = new File(path);
        return ImageIO.read(file);
    }
}
