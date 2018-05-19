package rs.etf.kn.master.dataSource.camera;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import rs.etf.kn.master.model.CamStreetConfig;
import rs.etf.kn.master.model.Camera;
import rs.etf.kn.master.model.Configuration;

public class CamProcessingManager {
    private static final Logger LOG = Logger.getLogger(CamProcessingManager.class.getName());

    private static List<CamImageFetcher> fetchers;
    private static List<CamImageAnalyser> analysers;

    public static void initialize() {
        fetchers = new LinkedList<>();
        analysers = new LinkedList<>();
        for (Camera c : Configuration.get().getCameras()) {
            try {
                CamImageFetcher fetcher = CamImageFetcher.create(c);
                for (CamStreetConfig config : c.getStreets()) {
                    BufferedImage reperImage = readReperImage(config.getStreetId());
                    CamImageAnalyser analyser = new CamImageAnalyser(config, reperImage);
                    analyser.start();
                    analysers.add(analyser);
                }
                fetcher.start();
                fetchers.add(fetcher);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void deinitialize() {
        for (CamImageAnalyser a : analysers) {
            a.stopProcessing();
        }
        for (CamImageAnalyser a : analysers) {
            try {
                a.join();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        for (CamImageFetcher f : fetchers) {
            f.stopFetching();
        }
        for (CamImageFetcher f : fetchers) {
            try {
                f.join();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addCamera(Camera c) {
        try {
            CamImageFetcher fetcher = CamImageFetcher.create(c);
            fetcher.start();
            fetchers.add(fetcher);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static BufferedImage readReperImage(String imgId) throws IOException {
        String path = Configuration.REPERS_DIR + imgId + "jpg";
        File file = new File(path);
        return ImageIO.read(file);
    }
}
