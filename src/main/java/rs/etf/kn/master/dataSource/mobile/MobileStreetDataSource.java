package rs.etf.kn.master.dataSource.mobile;

import com.google.maps.model.SnappedPoint;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.kn.master.dataSource.StreetDataSource;
import rs.etf.kn.master.model.Location;

public class MobileStreetDataSource extends StreetDataSource {

    private static final Logger LOG = Logger.getLogger(MobileStreetDataSource.class.getName());

    public MobileStreetDataSource() {
        super("mobile");
    }

    public synchronized void addData(SnappedPoint[] points, double[] speeds) {
        List<Location> path = new LinkedList<>();
        int origIdx = 0;
        double speed = 0;
        String lastStreetId = points[0].placeId;
        SnappedPoint lastSnappedPoint = points[points.length - 1];
        for (SnappedPoint point : points) {
            if (point.originalIndex != -1) {
                origIdx = point.originalIndex;
            }
            Location l = new Location(point.location.lat, point.location.lng);

            if (!point.placeId.equals(lastStreetId)) {
                speed /= path.size();
                addStreetSegment(lastStreetId, path, speed);
                speed = 0;
                path = new LinkedList<>();
            }
            lastStreetId = point.placeId;
            speed += speeds[origIdx];
            path.add(l);
            if (point == lastSnappedPoint) {
                speed /= path.size();
                addStreetSegment(lastStreetId, path, speed);
            }
        }
    }

    private void addStreetSegment(String streetId, List<Location> path, double speed) {
        MobileStreetData newData = new MobileStreetData(path, (int) speed);
        MobileStreetData oldData = (MobileStreetData) getData(streetId);
        if (oldData != null) {
            Location lastPoint = oldData.getPath().get(oldData.getPath().size() - 1);
            if (lastPoint.equals(path.get(0))) {
                oldData.getPath().addAll(path);
            }
            oldData.setIntensity((int) speed);
        } else {
            addData(streetId, newData);
        }
    }
}
