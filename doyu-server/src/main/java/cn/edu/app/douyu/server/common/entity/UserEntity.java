package cn.edu.app.douyu.server.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "phone", length = 64, unique = true)
    private String phone;

    @Column(name = "email", length = 160, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 128)
    private String passwordHash;

    @Column(name = "nickname", length = 80, nullable = false)
    private String nickname;

    @Column(name = "avatar_file_id", length = 64)
    private String avatarFileId;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "region", length = 80)
    private String region;

    @Column(name = "age_group", length = 32, nullable = false)
    private String ageGroup;

    @Column(name = "is_minor", nullable = false)
    private boolean isMinor;

    @Column(name = "real_name_status", length = 32, nullable = false)
    private String realNameStatus;

    @Column(name = "account_status", length = 32, nullable = false)
    private String accountStatus;

    @Column(name = "allow_recommendation", nullable = false)
    private boolean allowRecommendation = true;

    @Column(name = "allow_stranger_messages", nullable = false)
    private boolean allowStrangerMessages = true;

    @Column(name = "allow_favorites", nullable = false)
    private boolean allowFavorites = true;

    @Column(name = "notify_messages", nullable = false)
    private boolean notifyMessages = true;

    @Column(name = "notify_interactions", nullable = false)
    private boolean notifyInteractions = true;

    @Column(name = "notify_publish", nullable = false)
    private boolean notifyPublish = true;

    @Column(name = "notify_system", nullable = false)
    private boolean notifySystem = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserEntity() {}

    public UserEntity(String id, String phone, String nickname, String avatarFileId, String bio,
                      String ageGroup, boolean isMinor, String realNameStatus, String accountStatus,
                      Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.phone = phone;
        this.email = defaultEmail(id, phone);
        this.nickname = nickname;
        this.avatarFileId = avatarFileId;
        this.bio = bio;
        this.region = "";
        this.ageGroup = ageGroup;
        this.isMinor = isMinor;
        this.realNameStatus = realNameStatus;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarFileId() { return avatarFileId; }
    public void setAvatarFileId(String avatarFileId) { this.avatarFileId = avatarFileId; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    public boolean isMinor() { return isMinor; }
    public void setMinor(boolean isMinor) { this.isMinor = isMinor; }
    public String getRealNameStatus() { return realNameStatus; }
    public void setRealNameStatus(String realNameStatus) { this.realNameStatus = realNameStatus; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public boolean isAllowRecommendation() { return allowRecommendation; }
    public void setAllowRecommendation(boolean allowRecommendation) { this.allowRecommendation = allowRecommendation; }
    public boolean isAllowStrangerMessages() { return allowStrangerMessages; }
    public void setAllowStrangerMessages(boolean allowStrangerMessages) { this.allowStrangerMessages = allowStrangerMessages; }
    public boolean isAllowFavorites() { return allowFavorites; }
    public void setAllowFavorites(boolean allowFavorites) { this.allowFavorites = allowFavorites; }
    public boolean isNotifyMessages() { return notifyMessages; }
    public void setNotifyMessages(boolean notifyMessages) { this.notifyMessages = notifyMessages; }
    public boolean isNotifyInteractions() { return notifyInteractions; }
    public void setNotifyInteractions(boolean notifyInteractions) { this.notifyInteractions = notifyInteractions; }
    public boolean isNotifyPublish() { return notifyPublish; }
    public void setNotifyPublish(boolean notifyPublish) { this.notifyPublish = notifyPublish; }
    public boolean isNotifySystem() { return notifySystem; }
    public void setNotifySystem(boolean notifySystem) { this.notifySystem = notifySystem; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    private static String defaultEmail(String id, String phone) {
        String source = phone == null || phone.isBlank() ? id : phone;
        return source == null || source.isBlank() ? null : source.toLowerCase() + "@legacy.local";
    }
}
