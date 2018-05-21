package rs.etf.kn.master.dataSource.camera;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.opencv.OpenCV;

public class FileCamImageFetcher extends CamImageFetcher {

    private VideoCapture fileSource;
    private Mat currentFrame;

    public FileCamImageFetcher(String source) throws FileNotFoundException {
        source = Configuration.BASE_DIR + source;
        fileSource = new VideoCapture(source);
        if (!fileSource.isOpened()) {
            throw new FileNotFoundException("Can't open file " + source);
        }
        currentFrame = new Mat();
    }

    @Override
    protected CamImage fetchImage() throws IOException {
        boolean success = fileSource.read(currentFrame);
        if (!success) {
            throw new IOException("End of video file");
        }
        long ts = (long) fileSource.get(0);
        
        Mat matImgGray = OpenCV.createGray(currentFrame);
        return new CamImage(currentFrame, matImgGray, ts);
    }

}
