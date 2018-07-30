package rs.etf.kn.master.dataSource;

import java.util.HashMap;
import java.util.Map;

public class StreetDataSource {

    String id;
    //streetId, data
    Map<String, StreetData> data;

    public StreetDataSource(String id) {
        this.id = id;
        data = new HashMap<>();
    }

    public void addData(String streetId, StreetData d) {
        data.put(streetId, d);
    }

    public void addData(String streetId, int intensity) {
        data.put(streetId, new StreetData(intensity));
    }

    public StreetData getData(String streetId) {
        return data.get(streetId);
    }
}
