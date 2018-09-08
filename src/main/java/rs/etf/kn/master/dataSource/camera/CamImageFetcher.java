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

    private static final Logger LOG = Logger.getLogger(CamImageFetcher.class.getName());

    private boolean run = true;
    private List<CamImageListener> listeners = new LinkedList<>();

    @Override
    public void run() {
        waitForListeners();
        while (run) {
            try {
                CamImage img = fetchImage();
                boolean consumed = true;
                for (CamImageListener l : listeners) {
                    consumed &= l.onImageFetched(img);
                }
                if (!consumed) {
                    Thread.sleep(5000);
                }
            } catch (IOException | InterruptedException ex) {
                LOG.log(Level.SEVERE, "Fetcher stopped", ex);
            }
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

    public synchronized void removeListener(CamImageListener l) {
        listeners.remove(l);
    }

    public static CamImageFetcher create(Camera c) throws FileNotFoundException, MalformedURLException {
        if ("ip".equals(c.getType())) {
//            throw  new MalformedURLException();
            return new IpCamImageFetcher(c.getIpAddress());
        }
        return new FileCamImageFetcher(c.getVideoFileName());
    }

    protected abstract CamImage fetchImage() throws IOException, InterruptedException;

    public interface CamImageListener {

        public boolean onImageFetched(CamImage img);
    }
}
