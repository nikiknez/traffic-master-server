package rs.etf.kn.master.dataSource.camera;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import rs.etf.kn.master.dataSource.StreetData;
import rs.etf.kn.master.dataSource.StreetDataManager;
import rs.etf.kn.master.model.CamStreetConfig;
import rs.etf.kn.master.model.Configuration;
import rs.etf.kn.master.opencv.OpenCV;
import rs.etf.kn.master.opencv.PerspectiveTransformator;

public class CamImageAnalyser extends Thread implements CamImageFetcher.CamImageListener {

    private static final Logger LOG = Logger.getLogger(CamImageAnalyser.class.getName());

    private boolean run = true;
    private CamStreetConfig camStreetConfig;
    private List<CamImage> camImageQueue;

    private Mat matReperImage;
    private MatOfPoint2f reperImageFeaturePoints;
    private long reperImageTimeStamp = 0;

    public CamImageAnalyser(CamStreetConfig c, BufferedImage reperImage) {
        camStreetConfig = c;
        camImageQueue = new LinkedList<>();
        updateReperImage(OpenCV.bufferedImgToMat(reperImage));
    }

    private synchronized void updateReperImage(Mat img) {
        if (img.channels() > 1) {
            OpenCV.toGray(img);
        }
        matReperImage = img;
        reperImageFeaturePoints = new MatOfPoint2f();
        MatOfPoint corners = new MatOfPoint();
        double minDistance = Math.sqrt(img.width() * img.height() / 2000);
        Imgproc.goodFeaturesToTrack(img, corners, 1000, 0.05, minDistance);
        corners.convertTo(reperImageFeaturePoints, CvType.CV_32F);
    }

    private void updateReperImage(CamImage img) {
        if (img.timeStamp - reperImageTimeStamp > 60 * 1000) {
            reperImageTimeStamp = img.timeStamp;
            LOG.info("Updating reper image");
            updateReperImage(img.matImgGray);
            try {
                String name = Configuration.REPERS_DIR + camStreetConfig.getStreetId() + ".jpg";
                BufferedImage image = OpenCV.matToBufferedImage(img.matImg);
                ImageIO.write(image, "jpg", new File(name));
            } catch (IOException ex) {
            }
        }
    }

    private MatOfPoint2f goodFeaturesToTrack(Mat img) {
        MatOfPoint2f pts = new MatOfPoint2f();
        MatOfPoint corners = new MatOfPoint();
        double imgSize = img.width() * img.height();
        double minDistance = 1 / camStreetConfig.getMetersPerPixelRatio();
        int maxCorners = (int) (imgSize / (minDistance * minDistance));
        Imgproc.goodFeaturesToTrack(img, corners, maxCorners, 0.01, minDistance);
        corners.convertTo(pts, CvType.CV_32F);
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
                if (timeDiff > 1000 || timeDiff <= 0) {
                    lastFrame = nextFrame;
                    lastFrameROI = nextFrameROI;
                    LOG.severe("Time between frames > 1000 ms");
                    continue;
                }
                MatOfPoint2f matLastPoints = goodFeaturesToTrack(lastFrameROI);

                List<Double> distances = calcOpticalFlow(lastFrameROI, nextFrameROI, matLastPoints);

                processMotionVectors(distances, timeDiff);

                lastFrame = nextFrame;
                lastFrameROI = nextFrameROI;
            }
        } catch (InterruptedException ex) {
            LOG.log(Level.WARNING, "stoped analyzing street {0}", camStreetConfig.getStreetId());
        }
    }

    private void processMotionVectors(List<Double> distances, long timeDiff) {
        int totalDetections = distances.size();
        int totalMotions = 1;
        double totalDistance = 0;
        int maxIntensity = 0;
        for (Double distance : distances) {
            double intensity = distance * 1000 / timeDiff;
            if (intensity >= 1) {
                totalMotions++;
                totalDistance += distance;
                if (intensity > maxIntensity) {
                    maxIntensity = (int) intensity;
                }
            }
        }
        double avgDistance = totalDistance / totalMotions;
        double intensity = avgDistance * 1000 / timeDiff;
        double movedPercent = totalMotions / totalDetections;
        LOG.log(Level.INFO, "moved percent = {0}; avg intensity = {1}; max intensity = {2};", new Object[]{movedPercent, intensity, maxIntensity});
        publishResult((int) Math.ceil(maxIntensity));
    }

    private List<Integer> intensityHistory = new LinkedList<>();

    private void publishResult(int intensity) {
        intensityHistory.add(intensity);
        intensity = max(intensityHistory);
        if (intensityHistory.size() > 10) {
            intensityHistory.remove(0);
        }
        StreetDataManager.addStreetData("cam", camStreetConfig.getStreetId(), new StreetData(intensity));
    }

    private int max(List<Integer> l) {
        int max = 0;
        for (Integer i : l) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    public synchronized void stopProcessing() {
        run = false;
        interrupt();
    }

    @Override
    public synchronized boolean onImageFetched(CamImage img) {
        if (camImageQueue.size() > 50) {
            return false;
        }
        camImageQueue.add(img);
        notify();
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
        while (reperImageSimilarity(img.matImgGray) < 0.6) {
            img = getNextImage();
        }
        updateReperImage(img);
        dumpMatchedImage(img);
        return img;
    }

    private void dumpMatchedImage(CamImage img) {
        try {
            String name = Configuration.TEMP_DIR + camStreetConfig.getStreetId() + "-" + img.timeStamp + ".jpg";
            Mat transformed = PerspectiveTransformator.fourPointTransform(img.matImg, camStreetConfig.getPolyPoints());
            BufferedImage image = OpenCV.matToBufferedImage(transformed);
            ImageIO.write(image, "jpg", new File(name));
        } catch (IOException ex) {
        }
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
            LOG.log(Level.INFO, "total = {0}   valid = {1}   fix = {2}   similarity = {3}", new Object[]{total, valid, fix, 0});
            return 0;
        }
        double sim = valid / total > 0.2 ? fix / valid : 0;
        LOG.log(Level.INFO, "total = {0}   valid = {1}   fix = {2}   similarity = {3}", new Object[]{total, valid, fix, sim});
        return sim;
    }

    private List<Double> calcOpticalFlow(Mat prevImg, Mat nextImg, MatOfPoint2f prevPts) {
        MatOfPoint2f nextPts = new MatOfPoint2f();
        MatOfByte matStatus = new MatOfByte();
        MatOfFloat matError = new MatOfFloat();

        Video.calcOpticalFlowPyrLK(prevImg, nextImg, prevPts, nextPts, matStatus, matError);

        Point[] prevp = prevPts.toArray();
        Point[] nextp = nextPts.toArray();
        byte[] status = matStatus.toArray();
        float[] error = matError.toArray();

        List<Double> distances = new LinkedList<>();

        for (int i = 0; i < prevp.length; i++) {
            if (status[i] != 0 && error[i] < 10) {
                distances.add(getDistance(prevp[i], nextp[i]));
            }
        }

        return distances;
    }

    private double getDistance(Point a, Point b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)) * camStreetConfig.getMetersPerPixelRatio();
    }
}
