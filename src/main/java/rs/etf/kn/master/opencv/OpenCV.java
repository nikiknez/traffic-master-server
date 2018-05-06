/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.kn.master.opencv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileNotFoundException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

/**
 *
 * @author NikLik
 */
public class OpenCV {

    public static boolean loadNativeLibraries() {
        try {
            System.out.println("Trying to load opencv library");
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("loaded opencv library: " + Core.NATIVE_LIBRARY_NAME);
            
            System.loadLibrary("opencv_ffmpeg2413_64");
            return true;
        } catch (Exception | UnsatisfiedLinkError e) {
            System.out.println("err : " + e.getMessage());
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
        if(!file.isOpened()){
            throw new FileNotFoundException("Can't open file " + fileName);
        }
        
        Mat frame = new Mat();
        
        file.set(0, time);
        
        file.read(frame);
        
        return matToBufferedImage(frame);
    }
}
