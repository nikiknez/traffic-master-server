package rs.etf.kn.master.dataSource.camera;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import rs.etf.kn.master.opencv.OpenCV;

public class IpCamImageFetcher extends CamImageFetcher {

    private URL url;
    private long lastFetchedTimestamp = 0;
    private static final int FETCH_PERIOD_MS = 500;

    public IpCamImageFetcher(String source) throws MalformedURLException {
        url = new URL(source);
    }

    @Override
    protected CamImage fetchImage() throws IOException, InterruptedException {
        long diff = System.currentTimeMillis() - lastFetchedTimestamp;
        if (diff < FETCH_PERIOD_MS) {
            Thread.sleep(FETCH_PERIOD_MS - diff);
        }
        lastFetchedTimestamp = System.currentTimeMillis();
        BufferedImage img = ImageIO.read(url);
        Mat matImg = OpenCV.bufferedImgToMat(img);
        Mat matImgGray = OpenCV.createGray(matImg);
        return new CamImage(matImg, matImgGray, lastFetchedTimestamp);
    }

}
