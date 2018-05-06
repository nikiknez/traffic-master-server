package rs.etf.kn.master.opencv;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class PerspectiveTransformator {

    public static BufferedImage fourPointTransform(BufferedImage orig, Point2D.Float[] points) {
        points = scalePoints(points, orig.getWidth(), orig.getHeight());

        Mat src_mat = new Mat(1, 4, CvType.CV_32FC2);
        Mat dst_mat = new Mat(1, 4, CvType.CV_32FC2);
        Dimension outSize = calcSize(points);
        float[] dst_points = {0.0f, 0.0f, outSize.width - 1, 0f, outSize.width - 1,
            outSize.height - 1, 0f, outSize.height - 1};
        dst_mat.put(0, 0, dst_points);
        src_mat.put(0, 0, toFloatArray(points));
        Mat M = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat in = OpenCV.bufferedImgToMat(orig);
        Mat out = new Mat(outSize.height, outSize.width, in.type());

        Imgproc.warpPerspective(in, out, M, out.size());

        BufferedImage img = OpenCV.matToBufferedImage(out);

        return img;
    }

    private static float[] toFloatArray(Point2D.Float[] points) {
        return new float[]{
            points[0].x, points[0].y,
            points[1].x, points[1].y,
            points[2].x, points[2].y,
            points[3].x, points[3].y};
    }

    private static Dimension calcSize(Point2D.Float[] points) {
        Point2D.Float[] orderedPoints = orderPoints(points);
        Point2D.Float tl = orderedPoints[0];
        Point2D.Float tr = orderedPoints[1];
        Point2D.Float br = orderedPoints[2];
        Point2D.Float bl = orderedPoints[3];

        double widthA = Math.sqrt((bl.x - br.x) * (bl.x - br.x) + (bl.y - br.y) * (bl.y - br.y));
        double widthB = Math.sqrt((tl.x - tr.x) * (tl.x - tr.x) + (tl.y - tr.y) * (tl.y - tr.y));
        int maxWidth = (int) Math.max(widthA, widthB);

        double heightA = Math.sqrt((tr.x - br.x) * (tr.x - br.x) + (tr.y - br.y) * (tr.y - br.y));
        double heightB = Math.sqrt((tl.x - bl.x) * (tl.x - bl.x) + (tl.y - bl.y) * (tl.y - bl.y));
        int maxHeight = (int) Math.max(heightA, heightB);
        
        return new Dimension(maxWidth, maxHeight);
    }

    private static Point2D.Float[] orderPoints(Point2D.Float[] points) {
        Point2D.Float[] ordered = new Point2D.Float[4];

        float[] sums = sumXYPoints(points);
        ordered[0] = points[minIndex(sums)];
        ordered[2] = points[maxIndex(sums)];

        float[] diffs = diffXYPoints(points);
        ordered[1] = points[minIndex(diffs)];
        ordered[3] = points[maxIndex(diffs)];

        return ordered;
    }

    private static float[] sumXYPoints(Point2D.Float[] points) {
        float[] sums = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            sums[i] = points[i].x + points[i].y;
        }
        return sums;
    }

    private static float[] diffXYPoints(Point2D.Float[] points) {
        float[] diffs = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            diffs[i] = points[i].y - points[i].x;
        }
        return diffs;
    }

    private static int minIndex(float[] a) {
        int idx = 0;
        float m = a[0];
        for (int i = 0; i < a.length; i++) {
            if (a[i] < m) {
                m = a[i];
                idx = i;
            }
        }
        return idx;
    }

    private static int maxIndex(float[] a) {
        int idx = 0;
        float m = a[0];
        for (int i = 0; i < a.length; i++) {
            if (a[i] > m) {
                m = a[i];
                idx = i;
            }
        }
        return idx;
    }

    private static Point2D.Float[] scalePoints(Point2D.Float[] points, float x, float y) {
        Point2D.Float[] newPoints = points.clone();
        for (Point2D.Float p : newPoints) {
            p.x *= x;
            p.y *= y;
        }
        return newPoints;
    }
}
