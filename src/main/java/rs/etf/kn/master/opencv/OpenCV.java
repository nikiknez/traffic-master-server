/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.kn.master.opencv;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import rs.etf.kn.master.model.Configuration;

/**
 *
 * @author NikLik
 */
public class OpenCV {

    public static boolean loadNativeLibraries() {
        try {
            System.out.println("Trying to load opencv library");
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.loadLibrary("opencv_ffmpeg2413_64");
            System.out.println("loaded opencv library: " + Core.NATIVE_LIBRARY_NAME);

            return true;
        } catch (Exception | UnsatisfiedLinkError e) {
            e.printStackTrace();
//            throw new RuntimeException("Failed to load opencv native library", e);
        }
        return false;
    }

    public static Mat bufferedImgToMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer())
                .getData();

        int cvType = biTypeToCvType(image.getType());
        Mat mat = new Mat(image.getHeight(), image.getWidth(), cvType);
        mat.put(0, 0, data);
        return mat;
    }

    public static BufferedImage matToBufferedImage(Mat m) {
        int biType = cvTypeToBiType(m.channels());
        BufferedImage buffImg = new BufferedImage(m.cols(), m.rows(), biType);
        byte[] data = ((DataBufferByte) buffImg.getRaster().getDataBuffer())
                .getData();

        m.get(0, 0, data);
        return buffImg;
    }

    private static int biTypeToCvType(int biType) {
        switch (biType) {
            case BufferedImage.TYPE_BYTE_GRAY:
                return CvType.CV_8UC1;
            case BufferedImage.TYPE_3BYTE_BGR:
                return CvType.CV_8UC3;
            case BufferedImage.TYPE_4BYTE_ABGR:
                return CvType.CV_8UC4;
            default:
                return CvType.CV_8UC4;
        }
    }

    private static int cvTypeToBiType(int channels) {
        switch (channels) {
            case 1:
                return BufferedImage.TYPE_BYTE_GRAY;
            case 3:
                return BufferedImage.TYPE_3BYTE_BGR;
            case 4:
                return BufferedImage.TYPE_4BYTE_ABGR;
            default:
                return BufferedImage.TYPE_4BYTE_ABGR;
        }
    }

    public static BufferedImage readFrame(String fileName, double time) throws FileNotFoundException {
        VideoCapture file = new VideoCapture(fileName);
        if (!file.isOpened()) {
            throw new FileNotFoundException("Can't open file " + fileName);
        }

        Mat frame = new Mat();

        file.set(0, time);

        boolean r = file.read(frame);

        System.out.println(fileName + " | Read frame at " + time + ": " + r);

        return r ? matToBufferedImage(frame) : null;
    }

    public static void toGray(Mat colored) {
        Imgproc.cvtColor(colored, colored, Imgproc.COLOR_RGB2GRAY);
    }

    public static Mat createGray(Mat colored) {
        Mat m = new Mat();
        Imgproc.cvtColor(colored, m, Imgproc.COLOR_RGB2GRAY);
        return m;
    }

    public static void scalePoints(Point2D.Float[] points, float x, float y) {
        for (Point2D.Float p : points) {
            p.x *= x;
            p.y *= y;
        }
    }

    public static void loadLibraries() {
        try {
            String openCvPath = Configuration.BASE_DIR + "opencv/";
            int bitness = Integer.parseInt(System
                    .getProperty("sun.arch.data.model"));
            if (bitness == 64) {
                openCvPath += "x64/";
            } else {
                openCvPath += "x32/";
            }
            String ffmpegPath = openCvPath + "opencv_ffmpeg" + getVersion() + (bitness == 64 ? "_64" : "") + ".dll";
            openCvPath += Core.NATIVE_LIBRARY_NAME + ".dll";
            loadLibrary(openCvPath);
            loadLibrary(ffmpegPath);
            System.out.println("Successfully loaded libraries: " + openCvPath + " & " + ffmpegPath);
        } catch (NumberFormatException | IOException e) {
            throw new RuntimeException("Failed to load opencv native library", e);
        }
    }

    private static void loadLibrary(String path) throws IOException {
        File tempFile = File.createTempFile("lib", ".dll");
        File libFile = new File(path);
        Files.copy(libFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.load(tempFile.getPath());
    }

    private static String getVersion() {
        return "" + Core.VERSION_EPOCH + Core.VERSION_MAJOR + Core.VERSION_MINOR;
    }
}
