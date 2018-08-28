package rs.etf.kn.master.dataSource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.kn.master.dataSource.mobile.MobileStreetData;
import rs.etf.kn.master.dataSource.mobile.MobileStreetDataSource;

public class StreetDataManager {

    //                 sourceId,  source
    private static Map<String, StreetDataSource> sources = new ConcurrentHashMap<>();

    public static StreetDataSource createDataSource(String sourceId) {
        if (sources.get(sourceId) != null) {
            return sources.get(sourceId);
        }
        if("mobile".equals(sourceId)){
            MobileStreetDataSource ms = new MobileStreetDataSource();
            sources.put(sourceId, ms);
            return ms;
        }
        StreetDataSource source = new StreetDataSource(sourceId);
        sources.put(sourceId, source);
        return source;
    }

    public static void addDataSource(StreetDataSource source) {
        sources.put(source.id, source);
    }

    public static StreetDataSource getDataSource(String id) {
        StreetDataSource s = sources.get(id);
        if (s != null) {
            return s;
        }
        return createDataSource(id);
    }

    public synchronized static void addStreetData(String sourceId, String streetId, StreetData d) {
        StreetDataSource source = sources.get(sourceId);
        if (source == null) {
            source = new StreetDataSource(sourceId);
            sources.put(sourceId, source);
        }
        source.addData(streetId, d);
    }

    public synchronized static String toJSON() {
        return new Gson().toJson(sources);
    }

    public static void loadMockMobileData() {
        String file = "/Users/NikLik/Downloads/mobileTrafficData.json";
        Type type = new TypeToken<HashMap<String, MobileStreetData>>() {
        }.getType();
        try {
            HashMap<String, MobileStreetData> msds = new Gson().fromJson(new String(Files.readAllBytes(Paths.get(file))), type);
            StreetDataSource sds = new MobileStreetDataSource();
            for (String id : msds.keySet()) {
                sds.addData(id, msds.get(id));
            }
            sources.put("mobile", sds);
        } catch (IOException ex) {
            Logger.getLogger(StreetDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
