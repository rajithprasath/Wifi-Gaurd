package sliit.ranjitha.fyp.model;

public class WifiNetwork {

    private String id;
    private String ssid;
    private String bssid;
    private int isTrusted;
    private int isBlocked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getIsTrusted() {
        return isTrusted;
    }

    public void setIsTrusted(int isTrusted) {
        this.isTrusted = isTrusted;
    }

    public int getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(int isBlocked) {
        this.isBlocked = isBlocked;
    }
}
