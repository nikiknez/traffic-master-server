package rs.etf.kn.master.dataSource.camera;

import org.opencv.core.Mat;

public class CamImage {

    public CamImage(Mat matImg, long timeStamp) {
        this.matImg = matImg;
        this.timeStamp = timeStamp;
    }

    Mat matImg;
    long timeStamp;
}
