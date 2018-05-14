package rs.etf.kn.master.dataSource.camera;

import org.opencv.core.Mat;

public class CamImage {

    public CamImage(Mat matImg, Mat matImgGray, long timeStamp) {
        this.matImg = matImg;
        this.matImgGray = matImgGray;
        this.timeStamp = timeStamp;
    }

    Mat matImg;
    Mat matImgGray;
    long timeStamp;
}
