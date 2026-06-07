package cn.edu.app.douyu.model;

public class Conversation {
    public String conversationId;
    public String peerUserId;
    public String peerName;
    public String peerAvatarUrl;
    public UserProfile peer;
    public String lastMessage;
    public String updatedAt;
    public Integer unreadCount;
    public Boolean mutualFollow;
    public Integer remainingNonMutualMessages;
    public Boolean canSend;
}
