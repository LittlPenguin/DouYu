package cn.edu.app.douyu.model;
/**
 * 订单地址快照 DTO：承载创建订单时提交的收货人、电话和地址。
 */

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
