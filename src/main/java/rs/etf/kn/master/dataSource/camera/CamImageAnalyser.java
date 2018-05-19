package rs.etf.kn.master.dataSource.camera;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import rs.etf.kn.master.model.CamStreetConfig;
import rs.etf.kn.master.opencv.OpenCV;
import rs.etf.kn.master.opencv.PerspectiveTransformator;

public class CamImageAnalyser extends Thread implements CamImageFetcher.CamImageListener {

    private boolean run = true;
    private CamStreetConfig camStreetConfig;
    private List<CamImage> camImageQueue;

    private Mat matReperImage;
    private MatOfPoint2f reperImageFeaturePoints;

    public CamImageAnalyser(CamStreetConfig c, BufferedImage reperImage) {
        camStreetConfig = c;
        camImageQueue = new LinkedList<>();
        updateReperImage(OpenCV.bufferedImgToMat(reperImage));
    }

    private synchronized void updateReperImage(Mat img) {
        matReperImage = img;
        reperImageFeaturePoints = new MatOfPoint2f();
        MatOfPoint corners = new MatOfPoint();
        double minDistance = Math.sqrt(img.width() * img.height() / 2000);
        Imgproc.goodFeaturesToTrack(img, corners, 1000, 0.01, minDistance);
        corners.convertTo(reperImageFeaturePoints, CvType.CV_32F);
    }

    private MatOfPoint2f goodFeaturesToTrack(Mat img) {
        MatOfPoint2f pts = new MatOfPoint2f();
        MatOfPoint corners = new MatOfPoint();
        double imgSize = img.width() * img.height();
        double minDistance = 1 / camStreetConfig.getMetersPerPixelRatio();
        int maxCorners = (int) (imgSize / (minDistance * minDistance));
        Imgproc.goodFeaturesToTrack(img, corners, maxCorners, 0.01, minDistance);
        return pts;
    }

    @Override
    public void run() {
        try {
            CamImage lastFrame = getNextValidImage();
            CamImage nextFrame;
            Mat lastFrameROI = PerspectiveTransformator.fourPointTransform(lastFrame.matImgGray, camStreetConfig.getPolyPoints());
            Mat nextFrameROI;
            while (run) {
                nextFrame = getNextValidImage();
                nextFrameROI = PerspectiveTransformator.fourPointTransform(nextFrame.matImgGray, camStreetConfig.getPolyPoints());
                long timeDiff = nextFrame.timeStamp - lastFrame.timeStamp;
                if (timeDiff > 1000) {
                    lastFrame = nextFrame;
                    lastFrameROI = nextFrameROI;
                    continue;
                }
                MatOfPoint2f matLastPoints = goodFeaturesToTrack(lastFrameROI);

                List<MotionVector> motionVectors = calcOpticalFlow(lastFrameROI, nextFrameROI, matLastPoints);

                for (MotionVector motion : motionVectors) {

                }

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CamImageAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void stopProcessing() {
        run = false;
        interrupt();
    }

    @Override
    public synchronized boolean onImageFetched(CamImage img) {
        camImageQueue.add(img);
        return true;
    }

    private synchronized CamImage getNextImage() throws InterruptedException {
        while (camImageQueue.isEmpty()) {
            wait();
        }
        return camImageQueue.remove(0);
    }

    private CamImage getNextValidImage() throws InterruptedException {
        CamImage img = getNextImage();
        while (reperImageSimilarity(img.matImgGray) < 0.5) {
            img = getNextImage();
        }
        return img;
    }

    private double reperImageSimilarity(Mat img) {
        MatOfPoint2f nextPts = new MatOfPoint2f();
        MatOfByte matStatus = new MatOfByte();
        MatOfFloat matError = new MatOfFloat();

        Video.calcOpticalFlowPyrLK(matReperImage, img, reperImageFeaturePoints, nextPts, matStatus, matError);

        Point[] prevp = reperImageFeaturePoints.toArray();
        Point[] nextp = nextPts.toArray();
        byte[] status = matStatus.toArray();
        float[] error = matError.toArray();

        double valid = 0, fix = 0;
        int total = prevp.length;
        int FIX_THRESHOLD = 2;

        for (int i = 0; i < total; i++) {
            if (status[i] != 0 && error[i] < 10) {
                double dx = Math.abs(prevp[i].x - nextp[i].x);
                double dy = Math.abs(prevp[i].y - nextp[i].y);
                valid++;
                if (dx <= FIX_THRESHOLD && dy <= FIX_THRESHOLD) {
                    fix++;
//                    Core.circle(img, nextp[i], 5, new Scalar(255, 255, 255));
                }
            }
        }
        if (valid == 0) {
            return 0;
        }
        double sim = valid / total > 0.3 ? fix / valid : 0;
        System.out.println("total = " + total + "   valid = " + valid + "   fix = " + fix + "   similarity = " + sim);
        return sim;
    }

    private List<MotionVector> calcOpticalFlow(Mat prevImg, Mat nextImg, MatOfPoint2f prevPts) {
        MatOfPoint2f nextPts = new MatOfPoint2f();
        MatOfByte matStatus = new MatOfByte();
        MatOfFloat matError = new MatOfFloat();

        Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts, nextPts, matStatus, matError);

        Point[] prevp = prevPts.toArray();
        Point[] nextp = nextPts.toArray();
        byte[] status = matStatus.toArray();
        float[] error = matError.toArray();

        List<MotionVector> motionVectors = new LinkedList<>();

        for (int i = 0; i < prevp.length; i++) {
            if (status[i] != 0 && error[i] < 10) {
                motionVectors.add(new MotionVector(prevp[i], nextp[i]));
            }
        }

        return motionVectors;
    }

    private class MotionVector {

        Point a, b;
        double length;

        public MotionVector(Point a, Point b) {
            this.a = a;
            this.b = b;
            length = Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
        }

        // in meters
        public double distance(double metersPerPixelRatio) {
            return length * metersPerPixelRatio;
        }
    }
}
