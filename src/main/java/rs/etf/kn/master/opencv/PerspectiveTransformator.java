package rs.etf.kn.master.opencv;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class PerspectiveTransformator {

    public static BufferedImage fourPointTransform(BufferedImage orig, Point2D.Float[] points) {
        Mat in = OpenCV.bufferedImgToMat(orig);
        Mat out = fourPointTransform(in, points);
        return OpenCV.matToBufferedImage(out);
    }

    public static Mat fourPointTransform(Mat orig, Point2D.Float[] points) {
        Mat src_mat = new Mat(1, 4, CvType.CV_32FC2);
        Mat dst_mat = new Mat(1, 4, CvType.CV_32FC2);
        Dimension outSize = calcSize(points);
        float[] dst_points = {0.0f, 0.0f, outSize.width - 1, 0f, outSize.width - 1,
            outSize.height - 1, 0f, outSize.height - 1};
        dst_mat.put(0, 0, dst_points);
        src_mat.put(0, 0, toFloatArray(points));
        Mat M = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Mat out = new Mat(outSize.height, outSize.width, orig.type());

        Imgproc.warpPerspective(orig, out, M, out.size());

        return out;
    }

    private static float[] toFloatArray(Point2D.Float[] points) {
        return new float[]{
            points[0].x, points[0].y,
            points[1].x, points[1].y,
            points[2].x, points[2].y,
            points[3].x, points[3].y};
    }

    private static Dimension calcSize(Point2D.Float[] points) {
        Point2D.Float tl = points[0];
        Point2D.Float tr = points[1];
        Point2D.Float br = points[2];
        Point2D.Float bl = points[3];

        double widthA = Math.sqrt((bl.x - br.x) * (bl.x - br.x) + (bl.y - br.y) * (bl.y - br.y));
        double widthB = Math.sqrt((tl.x - tr.x) * (tl.x - tr.x) + (tl.y - tr.y) * (tl.y - tr.y));
        int maxWidth = (int) Math.max(widthA, widthB);

        double heightA = Math.sqrt((tr.x - br.x) * (tr.x - br.x) + (tr.y - br.y) * (tr.y - br.y));
        double heightB = Math.sqrt((tl.x - bl.x) * (tl.x - bl.x) + (tl.y - bl.y) * (tl.y - bl.y));
        int maxHeight = (int) Math.max(heightA, heightB);

        return new Dimension(maxWidth, maxHeight);
    }

    private static Point2D.Float computeCenter(Point2D.Float[] points) {
        float x = 0;
        float y = 0;
        for (Point2D.Float p : points) {
            x += p.x;
            y += p.y;
        }
        x /= points.length;
        y /= points.length;
        return new Point2D.Float(x, y);
    }

    private static double computeAngle(Point2D center, Point2D point) {
        double dx = point.getX() - center.getX();
        double dy = point.getY() - center.getY();
        double angleRad = Math.atan2(dy, dx);
        return angleRad;
    }

    private static Comparator<Point2D.Float> createComparator(final Point2D.Float center) {
        return new Comparator<Point2D.Float>() {
            @Override
            public int compare(Point2D.Float p0, Point2D.Float p1) {
                double angle0 = computeAngle(center, p0);
                double angle1 = computeAngle(center, p1);
                return Double.compare(angle0, angle1);
            }
        };
    }

    public static void sortPoints(Point2D.Float[] points) {
        Arrays.sort(points, createComparator(computeCenter(points)));
    }
}
