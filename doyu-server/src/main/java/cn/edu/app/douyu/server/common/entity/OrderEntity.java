package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id @Column(name = "id", length = 64) private String id;
    @Column(name = "buyer_id", length = 64, nullable = false) private String buyerId;
    @Column(name = "seller_type", length = 32, nullable = false) private String sellerType;
    @Column(name = "seller_id", length = 64) private String sellerId;
    @Column(name = "order_type", length = 32, nullable = false) private String orderType;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "total_amount_cent", nullable = false) private int totalAmountCent;
    @Column(name = "payable_amount_cent", nullable = false) private int payableAmountCent;
    @Column(name = "address_snapshot", nullable = false, columnDefinition = "text") private String addressSnapshot;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public OrderEntity() {}

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getBuyerId() { return buyerId; } public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public String getSellerType() { return sellerType; } public void setSellerType(String sellerType) { this.sellerType = sellerType; }
    public String getSellerId() { return sellerId; } public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getOrderType() { return orderType; } public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public int getTotalAmountCent() { return totalAmountCent; } public void setTotalAmountCent(int totalAmountCent) { this.totalAmountCent = totalAmountCent; }
    public int getPayableAmountCent() { return payableAmountCent; } public void setPayableAmountCent(int payableAmountCent) { this.payableAmountCent = payableAmountCent; }
    public String getAddressSnapshot() { return addressSnapshot; } public void setAddressSnapshot(String addressSnapshot) { this.addressSnapshot = addressSnapshot; }
    public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
