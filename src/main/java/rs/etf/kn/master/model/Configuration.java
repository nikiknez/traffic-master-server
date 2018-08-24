package rs.etf.kn.master.model;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.kn.master.main.Main;

public class Configuration {

    private List<Camera> cameras = new ArrayList<>();
    private List<Street> streets = new ArrayList<>();
    private List<Mark> marks = new ArrayList<>();
    private List<MapView> mapViews = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    private int lastId = 1;

    public static final String BASE_DIR = "/var/master/";
    public static final String VIDEOS_DIR = BASE_DIR + "videos/";
    public static final String REPERS_DIR = BASE_DIR + "repers/";
    public static final String TEMP_DIR = BASE_DIR + "temp/";
    private static final String CONFIG_FILE_PATH = BASE_DIR + "config.json";

    private static Configuration config = null;

    public static Configuration get() {
        return config;
    }

    private Configuration() {
    }

    public static boolean load() {
        if (config != null) {
            return true;
        }
        try {
            config = new Gson().fromJson(new FileReader(CONFIG_FILE_PATH), Configuration.class);
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean save() {
        if (config == null) {
            return false;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String configJson = gson.toJson(config);

        try (PrintWriter pw = new PrintWriter(CONFIG_FILE_PATH)) {
            pw.write(configJson);
            pw.flush();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public List<Street> getStreets() {
        return streets;
    }

    public void setStreets(List<Street> streets) {
        this.streets = streets;
    }

    public List<Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = marks;
    }

    public List<MapView> getMapViews() {
        return mapViews;
    }

    public void setMapViews(List<MapView> mapViews) {
        this.mapViews = mapViews;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public synchronized int getNextId() {
        return lastId++;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public User getUser(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public void addView(MapView v) {
        mapViews.add(v);
    }

    public void addUser(User u) {
        users.add(u);
    }

    public void addStreet(Street s) {
        streets.add(s);
    }

    public void addMark(Mark s) {
        marks.add(s);
    }

    public void removeMark(String id) {
        for (Mark m : marks) {
            if (m.getId().equals(id)) {
                marks.remove(m);
                break;
            }
        }
    }

    public void addCamera(Camera c) {
        cameras.add(c);
    }

    public Camera getCamById(String id) {
        for (Camera c : cameras) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public Street getStreetById(String id) {
        for (Street s : streets) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    private static Gson createGsonSerializer() {
        return new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fa) {
                return fa.getName().equals("users") || fa.getName().equals("lastId");
            }

            @Override
            public boolean shouldSkipClass(Class<?> type) {
                return false;
            }
        }).registerTypeAdapter(CamStreetConfig.class, new JsonSerializer<CamStreetConfig>() {
            @Override
            public JsonElement serialize(CamStreetConfig t, Type type, JsonSerializationContext jsc) {
                return new JsonPrimitive(t.getStreetId());
            }
        }).create();
    }

    private static Gson serializer = createGsonSerializer();

    public String toJson() {
        return serializer.toJson(this);
    }

    public class IntensityColorMap {
        public int lowIntensityColor = 0xFFF23C32;
        public int lowIntensityLevel = 5;
        public int midIntensityColor = 0xFFFF974D;
        public int midIntensityLevel = 10;
        public int highIntensityColor = 0XFF63D668;
    }
    public IntensityColorMap intensityColorMap  = new IntensityColorMap();
}
