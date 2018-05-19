package rs.etf.kn.master.dataSource.camera;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.kn.master.model.Camera;

public abstract class CamImageFetcher extends Thread {

    private boolean run = true;
    private List<CamImageListener> listeners = new LinkedList<>();

    @Override
    public void run() {
        waitForListeners();
        try {
            while (run) {
                CamImage img = fetchImage();

                for (CamImageListener l : listeners) {
                    l.onImageFetched(img);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CamImageFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized void waitForListeners() {
        try {
            while (listeners.isEmpty()) {
                wait();
            }
        } catch (InterruptedException ex) {
            run = false;
        }
    }

    public synchronized void stopFetching() {
        run = false;
        interrupt();
    }

    public synchronized void addListener(CamImageListener l) {
        listeners.add(l);
        notify();
    }

    public static CamImageFetcher create(Camera c) throws FileNotFoundException, MalformedURLException {
        if ("ip".equals(c.getType())) {
            return new IpCamImageFetcher(c.getIpAddress());
        }
        return new FileCamImageFetcher(c.getVideoFileName());
    }

    protected abstract CamImage fetchImage() throws IOException;

    public interface CamImageListener {

        public boolean onImageFetched(CamImage img);
    }
}
