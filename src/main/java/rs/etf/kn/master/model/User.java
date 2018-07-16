package rs.etf.kn.master.model;

public class User {

    private String name;
    private String username;
    private String password;

    private boolean canAddCamera;
    private boolean canAddStreet;
    private boolean canAddMark;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCanAddCamera() {
        return canAddCamera;
    }

    public void setCanAddCamera(boolean canAddCamera) {
        this.canAddCamera = canAddCamera;
    }

    public boolean isCanAddStreet() {
        return canAddStreet;
    }

    public void setCanAddStreet(boolean canAddStreet) {
        this.canAddStreet = canAddStreet;
    }

    public boolean isCanAddMark() {
        return canAddMark;
    }

    public void setCanAddMark(boolean canAddMark) {
        this.canAddMark = canAddMark;
    }

}
