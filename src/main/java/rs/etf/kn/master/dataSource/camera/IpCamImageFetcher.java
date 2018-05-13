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

    public IpCamImageFetcher(String source) throws MalformedURLException {
        url = new URL(source);
    }

    @Override
    protected CamImage fetchImage() throws IOException {

        BufferedImage img = ImageIO.read(url);
        long ts = System.currentTimeMillis();

        Mat matImg = OpenCV.bufferedImgToMat(img);

        return new CamImage(matImg, ts);
    }

}
