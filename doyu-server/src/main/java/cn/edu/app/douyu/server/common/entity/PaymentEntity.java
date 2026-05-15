package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "order_id", length = 64, nullable = false) private String orderId;
    @Column(name = "channel", length = 32, nullable = false) private String channel;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "amount_cent", nullable = false) private int amountCent;
    @Column(name = "channel_trade_no", length = 128) private String channelTradeNo;
    @Column(name = "paid_at") private Instant paidAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public PaymentEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; } public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getChannel() { return channel; } public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public int getAmountCent() { return amountCent; } public void setAmountCent(int amountCent) { this.amountCent = amountCent; }
    public String getChannelTradeNo() { return channelTradeNo; } public void setChannelTradeNo(String channelTradeNo) { this.channelTradeNo = channelTradeNo; }
    public Instant getPaidAt() { return paidAt; } public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
