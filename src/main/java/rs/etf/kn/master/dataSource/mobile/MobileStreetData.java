package rs.etf.kn.master.dataSource.mobile;

import java.util.List;
import rs.etf.kn.master.dataSource.StreetData;
import rs.etf.kn.master.model.Location;

public class MobileStreetData extends StreetData {
    
    private List<Location> path;

    public MobileStreetData(List<Location> path, int intensity) {
        super(intensity);
        this.path = path;
    }

    public List<Location> getPath() {
        return path;
    }

    public void setPath(List<Location> path) {
        this.path = path;
    }
    
    
}
