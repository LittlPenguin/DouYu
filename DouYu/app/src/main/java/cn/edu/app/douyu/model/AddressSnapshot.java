package cn.edu.app.douyu.model;

public class AddressSnapshot {
    public String recipient;
    public String phone;
    public String region;
    public String detail;

    public AddressSnapshot() {
    }

    public AddressSnapshot(String recipient, String phone, String region, String detail) {
        this.recipient = recipient;
        this.phone = phone;
        this.region = region;
        this.detail = detail;
    }
}
