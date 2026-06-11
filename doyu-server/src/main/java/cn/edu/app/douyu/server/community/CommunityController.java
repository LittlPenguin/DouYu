package cn.edu.app.douyu.server.community;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.entity.*;
import cn.edu.app.douyu.server.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "社区", description = "帖子 CRUD、点赞、收藏、评论、举报")
@RestController
@RequestMapping("/api/v1")
public class CommunityController {
    private static final List<String> PUBLIC_POST_STATUSES = List.of("VISIBLE");

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final RewardAccountRepository rewardAccountRepository;
    private final FileAssetRepository fileAssetRepository;
    private final TopicRepository topicRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final CommentTopicRepository commentTopicRepository;
    private final StickerPackRepository stickerPackRepository;
    private final StickerRepository stickerRepository;
    private final CommentStickerRepository commentStickerRepository;
    private final NotificationRepository notificationRepository;
    private final IdGenerator idGenerator;

    public CommunityController(PostRepository postRepository, CommentRepository commentRepository,
                               LikeRepository likeRepository, FavoriteRepository favoriteRepository,
                               UserRepository userRepository, FollowRepository followRepository,
                               RewardAccountRepository rewardAccountRepository, FileAssetRepository fileAssetRepository,
                               TopicRepository topicRepository, CommentMentionRepository commentMentionRepository,
                               CommentTopicRepository commentTopicRepository, StickerPackRepository stickerPackRepository,
                               StickerRepository stickerRepository, CommentStickerRepository commentStickerRepository,
                               NotificationRepository notificationRepository,
                               IdGenerator idGenerator) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.rewardAccountRepository = rewardAccountRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.topicRepository = topicRepository;
        this.commentMentionRepository = commentMentionRepository;
        this.commentTopicRepository = commentTopicRepository;
        this.stickerPackRepository = stickerPackRepository;
        this.stickerRepository = stickerRepository;
        this.commentStickerRepository = commentStickerRepository;
        this.notificationRepository = notificationRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "推荐 Feed", description = "获取推荐帖子列表（公开接口）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/posts/feed")
    PageResult<Map<String, Object>> feed(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = optionalUserId(authentication);
        List<PostEntity> visible = postRepository.findByStatusInOrderByPinnedDescCreatedAtDesc(PUBLIC_POST_STATUSES);
        List<Map<String, Object>> items = visible.stream().map(post -> postView(post, userId)).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "关注 Feed", description = "获取关注用户的帖子列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/posts/following")
    PageResult<Map<String, Object>> following(Authentication authentication, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<String> followed = followRepository.findByUserId(userId).stream()
                .map(FollowEntity::getTargetUserId).toList();
        List<PostEntity> items = followed.isEmpty() ? List.of()
                : postRepository.findByAuthorIdInAndStatusInOrderByCreatedAtDesc(followed, PUBLIC_POST_STATUSES);
        List<Map<String, Object>> views = items.stream().map(post -> postView(post, userId)).toList();
        return PageResult.of(slice(views, page, size), page, size, views.size());
    }

    @Operation(summary = "发布帖子", description = "发布新帖子，直接公开可见")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "发布成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/posts")
    Map<String, Object> createPost(Authentication authentication, @Valid @RequestBody PostRequest request) {
        String userId = CurrentUser.userId(authentication);
        Instant now = Instant.now();
        PostEntity post = new PostEntity(idGenerator.next("post"), userId, request.title(), request.content(),
                joinList(request.mediaFileIds()), joinList(request.topicIds()),
                "VISIBLE", 0, 0, 0, false, now, now);
        applyCoverFromMedia(post);
        postRepository.save(post);
        return postView(post, userId);
    }

    @Operation(summary = "帖子详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @GetMapping("/posts/{postId}")
    Map<String, Object> post(Authentication authentication, @PathVariable String postId) {
        return postView(requirePost(postId), optionalUserId(authentication));
    }

    @Operation(summary = "编辑帖子")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "编辑成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "只能编辑自己的帖子"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @PatchMapping("/posts/{postId}")
    Map<String, Object> updatePost(Authentication authentication, @PathVariable String postId, @RequestBody PostRequest request) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        if (!post.getAuthorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能编辑自己的帖子");
        }
        if (request.title() != null) post.setTitle(request.title());
        if (request.content() != null) post.setContent(request.content());
        if (request.mediaFileIds() != null) post.setMediaFileIds(joinList(request.mediaFileIds()));
        if (request.topicIds() != null) post.setTopicIds(joinList(request.topicIds()));
        if (request.mediaFileIds() != null) {
            applyCoverFromMedia(post);
        }
        post.setStatus("VISIBLE");
        postRepository.save(post);
        return postView(post, userId);
    }

    @Operation(summary = "删除帖子")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "只能删除自己的帖子"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @DeleteMapping("/posts/{postId}")
    Map<String, Object> deletePost(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        if (!post.getAuthorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的帖子");
        }
        post.setStatus("DELETED");
        postRepository.save(post);
        return Map.of("deleted", true);
    }

    @Operation(summary = "点赞帖子")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "点赞成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @PostMapping("/posts/{postId}/like")
    Map<String, Object> like(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        Instant now = Instant.now();
        boolean alreadyLiked = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, "POST", postId).isPresent();
        if (!alreadyLiked) {
            likeRepository.save(new LikeEntity(idGenerator.next("like"), userId, "POST", postId, now, now));
            post.setLikeCount(Math.max(0, post.getLikeCount()) + 1);
        }
        postRepository.save(post);
        return postInteractionView(post, userId, true, null);
    }

    @Operation(summary = "取消点赞")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @DeleteMapping("/posts/{postId}/like")
    Map<String, Object> unlike(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        boolean alreadyLiked = likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, "POST", postId).isPresent();
        likeRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, "POST", postId);
        if (alreadyLiked) {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        }
        postRepository.save(post);
        return postInteractionView(post, userId, false, null);
    }

    @Operation(summary = "收藏帖子")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "收藏成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @PostMapping("/posts/{postId}/favorite")
    Map<String, Object> favorite(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        Instant now = Instant.now();
        boolean alreadyFavorited = favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, "POST", postId).isPresent();
        if (!alreadyFavorited) {
            favoriteRepository.save(new FavoriteEntity(idGenerator.next("fav"), userId, "POST", postId, now, now));
            post.setFavoriteCount(Math.max(0, post.getFavoriteCount()) + 1);
        }
        postRepository.save(post);
        return postInteractionView(post, userId, null, true);
    }

    @Operation(summary = "取消收藏")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @DeleteMapping("/posts/{postId}/favorite")
    Map<String, Object> unfavorite(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        boolean alreadyFavorited = favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, "POST", postId).isPresent();
        favoriteRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, "POST", postId);
        if (alreadyFavorited) {
            post.setFavoriteCount(Math.max(0, post.getFavoriteCount() - 1));
        }
        postRepository.save(post);
        return postInteractionView(post, userId, null, false);
    }

    @Operation(summary = "评论列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @GetMapping("/posts/{postId}/comments")
    PageResult<Map<String, Object>> comments(@PathVariable String postId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        requirePost(postId);
        List<CommentEntity> entities = commentRepository.findByPostIdAndStatusNotOrderByCreatedAtAsc(postId, "DELETED");
        List<Map<String, Object>> items = entities.stream().map(this::commentView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "发表评论")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "评论成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @PostMapping("/posts/{postId}/comments")
    Map<String, Object> comment(Authentication authentication, @PathVariable String postId, @Valid @RequestBody CommentRequest request) {
        String userId = CurrentUser.userId(authentication);
        PostEntity post = requirePost(postId);
        List<String> mediaFileIds = request.mediaFileIds() == null ? List.of() : request.mediaFileIds();
        List<String> mentionUserIds = normalizedIds(request.mentionUserIds());
        List<String> topicIds = normalizedIds(request.topicIds());
        List<String> stickerIds = normalizedIds(request.stickerIds());
        String content = request.content() == null ? "" : request.content().trim();
        validateCommentPayload(userId, content, mediaFileIds, mentionUserIds, topicIds, stickerIds);
        Instant now = Instant.now();
        CommentEntity comment = new CommentEntity(idGenerator.next("cmt"), postId, userId, request.parentId(),
                content, "VISIBLE", now, now);
        comment.setMediaFileIds(joinList(mediaFileIds));
        commentRepository.save(comment);
        for (String mentionedUserId : mentionUserIds) {
            commentMentionRepository.save(new CommentMentionEntity(idGenerator.next("cmn"), comment.getId(), mentionedUserId, now));
            if (!mentionedUserId.equals(userId)) {
                NotificationEntity notification = new NotificationEntity();
                notification.setId(idGenerator.next("ntf"));
                notification.setUserId(mentionedUserId);
                notification.setSenderId(userId);
                notification.setRecipientId(mentionedUserId);
                notification.setType("MENTION");
                notification.setTitle("有人在评论中提到了你");
                notification.setContent(content.isBlank() ? "你被一条贴纸/图片评论提及" : content);
                notification.setCreatedAt(now);
                notification.setUpdatedAt(now);
                notificationRepository.save(notification);
            }
        }
        for (String topicId : topicIds) {
            commentTopicRepository.save(new CommentTopicEntity(idGenerator.next("ctp"), comment.getId(), topicId, now));
        }
        for (String stickerId : stickerIds) {
            commentStickerRepository.save(new CommentStickerEntity(idGenerator.next("cst"), comment.getId(), stickerId, now));
        }
        post.setCommentCount((int) commentRepository.countByPostIdAndStatusNot(postId, "DELETED"));
        postRepository.save(post);
        return commentView(comment);
    }

    @Operation(summary = "删除评论")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "403", description = "只能删除自己的评论"),
            @ApiResponse(responseCode = "404", description = "评论不存在")
    })
    @DeleteMapping("/comments/{commentId}")
    Map<String, Object> deleteComment(Authentication authentication, @PathVariable String commentId) {
        String userId = CurrentUser.userId(authentication);
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "评论不存在"));
        if (!comment.getAuthorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的评论");
        }
        comment.setStatus("DELETED");
        commentRepository.save(comment);
        return Map.of("deleted", true);
    }

    @Operation(summary = "话题列表")
    @GetMapping("/topics")
    PageResult<Map<String, Object>> topics(@RequestParam(defaultValue = "") String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        List<TopicEntity> allTopics = topicRepository.findAllByOrderByPostCountDescNameAsc();
        List<TopicEntity> topics;
        if (keyword == null || keyword.isBlank()) {
            topics = allTopics;
        } else {
            String normalizedKeyword = keyword.trim().toLowerCase();
            topics = allTopics.stream()
                    .filter(topic -> containsIgnoreCase(topic.getId(), normalizedKeyword)
                            || containsIgnoreCase(topic.getName(), normalizedKeyword)
                            || containsIgnoreCase(topic.getDescription(), normalizedKeyword))
                    .toList();
        }
        List<Map<String, Object>> items = topics.stream().map(this::topicView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "话题作品列表")
    @GetMapping("/topics/{topicId}/posts")
    PageResult<Map<String, Object>> topicPosts(Authentication authentication,
                                               @PathVariable String topicId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        String userId = optionalUserId(authentication);
        requireTopic(topicId);
        List<Map<String, Object>> items = postRepository
                .findByStatusInAndTopicIdsContainingOrderByCreatedAtDesc(PUBLIC_POST_STATUSES, topicId).stream()
                .map(post -> postView(post, userId))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "内置贴纸表情包")
    @GetMapping("/sticker-packs")
    PageResult<Map<String, Object>> stickerPacks() {
        List<Map<String, Object>> items = stickerPackRepository.findAllByOrderBySortOrderAsc().stream()
                .map(pack -> {
                    Map<String, Object> view = new java.util.LinkedHashMap<>();
                    view.put("packId", pack.getId());
                    view.put("name", pack.getName());
                    view.put("stickers", stickerRepository.findByPackIdOrderBySortOrderAsc(pack.getId()).stream()
                            .map(this::stickerView)
                            .toList());
                    return view;
                })
                .toList();
        return PageResult.of(items, 1, items.size(), items.size());
    }

    private PostEntity requirePost(String postId) {
        PostEntity post = postRepository.findById(postId).orElse(null);
        if (post == null || "DELETED".equals(post.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    public Map<String, Object> postView(PostEntity post, String currentUserId) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("postId", post.getId());
        view.put("authorId", post.getAuthorId());
        view.put("author", authorInfo(post.getAuthorId()));
        view.put("title", post.getTitle() == null ? "" : post.getTitle());
        view.put("content", post.getContent());
        List<String> mediaFileIds = splitList(post.getMediaFileIds());
        view.put("mediaFileIds", mediaFileIds);
        view.put("imageUrls", postImageUrls(mediaFileIds));
        CoverAsset cover = coverAsset(post);
        view.put("coverImageUrl", cover.url());
        view.put("coverWidth", cover.width());
        view.put("coverHeight", cover.height());
        view.put("mediaColors", List.of());
        List<String> topicIds = splitList(post.getTopicIds());
        view.put("topicIds", topicIds);
        view.put("topicNames", topicIds.stream()
                .map(topicId -> topicRepository.findById(topicId).map(TopicEntity::getName).orElse(""))
                .filter(name -> !name.isBlank())
                .toList());
        view.put("status", post.getStatus());
        view.put("likeCount", post.getLikeCount());
        view.put("favoriteCount", post.getFavoriteCount());
        view.put("commentCount", post.getCommentCount());
        view.put("likedByMe", currentUserId != null && likeRepository.findByUserIdAndTargetTypeAndTargetId(currentUserId, "POST", post.getId()).isPresent());
        view.put("favoritedByMe", currentUserId != null && favoriteRepository.findByUserIdAndTargetTypeAndTargetId(currentUserId, "POST", post.getId()).isPresent());
        view.put("followedAuthorByMe", currentUserId != null && followRepository.findByUserIdAndTargetUserId(currentUserId, post.getAuthorId()).isPresent());
        view.put("createdAt", post.getCreatedAt().toString());
        view.put("updatedAt", post.getUpdatedAt().toString());
        return view;
    }

    private Map<String, Object> postInteractionView(PostEntity post, String currentUserId, Boolean liked, Boolean favorited) {
        boolean likedByMe = currentUserId != null
                && likeRepository.findByUserIdAndTargetTypeAndTargetId(currentUserId, "POST", post.getId()).isPresent();
        boolean favoritedByMe = currentUserId != null
                && favoriteRepository.findByUserIdAndTargetTypeAndTargetId(currentUserId, "POST", post.getId()).isPresent();
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        if (liked != null) {
            view.put("liked", liked);
        }
        if (favorited != null) {
            view.put("favorited", favorited);
        }
        view.put("likedByMe", likedByMe);
        view.put("favoritedByMe", favoritedByMe);
        view.put("likeCount", post.getLikeCount());
        view.put("favoriteCount", post.getFavoriteCount());
        return view;
    }

    private Map<String, Object> authorInfo(String userId) {
        return userRepository.findById(userId).map(u -> {
            long following = followRepository.countByUserId(userId);
            long followers = followRepository.countByTargetUserId(userId);
            var reward = rewardAccountRepository.findByUserId(userId).orElse(null);
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("userId", u.getId());
            m.put("nickname", u.getNickname() == null ? "" : u.getNickname());
            m.put("avatarUrl", avatarUrl(u.getAvatarFileId()));
            m.put("bio", u.getBio() == null ? "" : u.getBio());
            m.put("level", reward != null ? reward.getLevel() : 1);
            m.put("isMinor", u.isMinor());
            m.put("followingCount", (int) following);
            m.put("followerCount", (int) followers);
            return (Map<String, Object>) m;
        }).orElseGet(() -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("userId", userId);
            m.put("nickname", "");
            m.put("avatarUrl", "");
            m.put("bio", "");
            m.put("level", 1);
            m.put("isMinor", false);
            m.put("followingCount", 0);
            m.put("followerCount", 0);
            return m;
        });
    }

    private Map<String, Object> commentView(CommentEntity comment) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("commentId", comment.getId());
        view.put("postId", comment.getPostId());
        view.put("authorId", comment.getAuthorId());
        view.put("author", authorInfo(comment.getAuthorId()));
        view.put("parentId", comment.getParentId());
        view.put("content", comment.getContent());
        List<String> mediaFileIds = splitList(comment.getMediaFileIds());
        view.put("mediaFileIds", mediaFileIds);
        view.put("mediaAssets", mediaFileIds.stream().map(this::commentMediaAssetView).toList());
        view.put("mentions", commentMentionRepository.findByCommentId(comment.getId()).stream()
                .map(mention -> userRepository.findById(mention.getUserId())
                        .<Map<String, Object>>map(user -> {
                            Map<String, Object> mentionView = new java.util.LinkedHashMap<>();
                            mentionView.put("userId", user.getId());
                            mentionView.put("nickname", user.getNickname() == null ? "" : user.getNickname());
                            mentionView.put("avatarUrl", avatarUrl(user.getAvatarFileId()));
                            return mentionView;
                        })
                        .orElseGet(() -> Map.of("userId", mention.getUserId(), "nickname", "", "avatarUrl", "")))
                .toList());
        view.put("topics", commentTopicRepository.findByCommentId(comment.getId()).stream()
                .map(link -> topicRepository.findById(link.getTopicId()).map(this::commentTopicView)
                        .orElseGet(() -> Map.of("topicId", link.getTopicId(), "name", "")))
                .toList());
        view.put("stickers", commentStickerRepository.findByCommentId(comment.getId()).stream()
                .map(link -> stickerRepository.findById(link.getStickerId()).map(this::stickerView)
                        .orElseGet(() -> Map.of("stickerId", link.getStickerId(), "packId", "", "name", "", "imageUrl", "", "emojiText", "")))
                .toList());
        view.put("status", comment.getStatus());
        return view;
    }

    private Map<String, Object> commentMediaAssetView(String fileId) {
        FileAssetEntity file = fileAssetRepository.findById(fileId).orElse(null);
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("fileId", fileId);
        if (file == null) {
            view.put("publicUrl", "");
            view.put("mimeType", "");
            view.put("width", null);
            view.put("height", null);
            view.put("auditStatus", "UNAVAILABLE");
            return view;
        }
        view.put("publicUrl", file.getPublicUrl() == null ? "" : file.getPublicUrl());
        view.put("mimeType", file.getMimeType());
        view.put("width", file.getWidth());
        view.put("height", file.getHeight());
        view.put("auditStatus", file.getAuditStatus());
        return view;
    }

    private void validateCommentPayload(String userId, String content, List<String> mediaFileIds,
                                        List<String> mentionUserIds, List<String> topicIds, List<String> stickerIds) {
        if (content.isBlank() && mediaFileIds.isEmpty() && stickerIds.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "评论内容或图片不能同时为空");
        }
        if (mediaFileIds.size() > 9) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "单条评论最多添加 9 张图片");
        }
        if (mediaFileIds.stream().distinct().count() != mediaFileIds.size()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "评论图片不能重复");
        }
        for (String fileId : mediaFileIds) {
            FileAssetEntity file = fileAssetRepository.findById(fileId)
                    .orElseThrow(() -> new BizException(ErrorCode.INVALID_ARGUMENT, "评论图片不存在"));
            if (!file.getOwnerId().equals(userId)) {
                throw new BizException(ErrorCode.FORBIDDEN, "只能使用自己上传的评论图片");
            }
            if (!"POST_IMAGE".equals(file.getUsage()) || !file.getMimeType().startsWith("image/")) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "评论只支持 POST_IMAGE 图片");
            }
        }
        for (String mentionUserId : mentionUserIds) {
            if (userRepository.findById(mentionUserId).isEmpty()) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "提及用户不存在");
            }
        }
        for (String topicId : topicIds) {
            requireTopic(topicId);
        }
        for (String stickerId : stickerIds) {
            if (stickerRepository.findById(stickerId).isEmpty()) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "贴纸不存在");
            }
        }
    }

    private TopicEntity requireTopic(String topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new BizException(ErrorCode.INVALID_ARGUMENT, "话题不存在"));
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    private String joinList(List<String> list) {
        return list == null || list.isEmpty() ? null : String.join(",", list);
    }

    private List<String> splitList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.asList(csv.split(","));
    }

    private List<String> normalizedIds(List<String> ids) {
        if (ids == null) return List.of();
        return ids.stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase().contains(normalizedKeyword);
    }

    private String optionalUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        try {
            return CurrentUser.userId(authentication);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, Object> topicView(TopicEntity topic) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("topicId", topic.getId());
        view.put("name", topic.getName());
        view.put("description", topic.getDescription() == null ? "" : topic.getDescription());
        view.put("postCount", topic.getPostCount());
        return view;
    }

    private void applyCoverFromMedia(PostEntity post) {
        List<String> mediaFileIds = splitList(post.getMediaFileIds());
        if (mediaFileIds.isEmpty()) {
            return;
        }
        FileAssetEntity file = fileAssetRepository.findById(mediaFileIds.get(0)).orElse(null);
        if (file == null) {
            return;
        }
        if (file.getPublicUrl() != null && !file.getPublicUrl().isBlank()) {
            post.setCoverImageUrl(file.getPublicUrl());
        }
        post.setCoverWidth(file.getWidth());
        post.setCoverHeight(file.getHeight());
    }

    private List<String> postImageUrls(List<String> mediaFileIds) {
        if (mediaFileIds == null || mediaFileIds.isEmpty()) {
            return List.of();
        }
        return mediaFileIds.stream()
                .map(fileId -> fileAssetRepository.findById(fileId)
                        .map(FileAssetEntity::getPublicUrl)
                        .orElse(""))
                .filter(url -> url != null && !url.isBlank())
                .toList();
    }

    private CoverAsset coverAsset(PostEntity post) {
        String coverUrl = post.getCoverImageUrl() == null ? "" : post.getCoverImageUrl();
        Integer coverWidth = post.getCoverWidth();
        Integer coverHeight = post.getCoverHeight();
        List<String> mediaFileIds = splitList(post.getMediaFileIds());
        if (!mediaFileIds.isEmpty()) {
            FileAssetEntity file = fileAssetRepository.findById(mediaFileIds.get(0)).orElse(null);
            if (file != null) {
                if (coverUrl.isBlank() && file.getPublicUrl() != null) {
                    coverUrl = file.getPublicUrl();
                }
                if (coverWidth == null) {
                    coverWidth = file.getWidth();
                }
                if (coverHeight == null) {
                    coverHeight = file.getHeight();
                }
            }
        }
        return new CoverAsset(coverUrl, coverWidth, coverHeight);
    }

    private Map<String, Object> commentTopicView(TopicEntity topic) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("topicId", topic.getId());
        view.put("name", topic.getName());
        return view;
    }

    private Map<String, Object> stickerView(StickerEntity sticker) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("stickerId", sticker.getId());
        view.put("packId", sticker.getPackId());
        view.put("name", sticker.getName());
        view.put("imageUrl", sticker.getImageUrl() == null ? "" : sticker.getImageUrl());
        view.put("emojiText", sticker.getEmojiText() == null ? "" : sticker.getEmojiText());
        return view;
    }

    private String avatarUrl(String avatarFileId) {
        if (avatarFileId == null || avatarFileId.isBlank()) {
            return "";
        }
        return fileAssetRepository.findById(avatarFileId)
                .map(FileAssetEntity::getPublicUrl)
                .filter(url -> url != null && !url.isBlank())
                .orElse("");
    }

    public record PostRequest(String title, @NotBlank String content,
                              List<String> mediaFileIds, List<String> topicIds) {
    }

    private record CoverAsset(String url, Integer width, Integer height) {
    }

    public record CommentRequest(String content, String parentId, List<String> mediaFileIds,
                                 List<String> mentionUserIds, List<String> topicIds, List<String> stickerIds) {
    }
}
