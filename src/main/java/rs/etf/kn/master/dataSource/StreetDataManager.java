package rs.etf.kn.master.dataSource;

import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreetDataManager {

    //                 sourceId,  source
    private static Map<String, StreetDataSource> sources = new ConcurrentHashMap<>();

    public static StreetDataSource addDataSource(String sourceId) {
        StreetDataSource source = new StreetDataSource(sourceId);
        sources.put(sourceId, source);
        return source;
    }

    public static void addDataSource(StreetDataSource source) {
        sources.put(source.id, source);
    }

    public static void addStreetData(String sourceId, String streetId, StreetData d) {
        StreetDataSource source = sources.get(sourceId);
        if (source == null) {
            source = new StreetDataSource(sourceId);
            sources.put(sourceId, source);
        }
        source.addData(streetId, d);
    }

    public static String toJSON() {
        return new Gson().toJson(sources);
    }
}
