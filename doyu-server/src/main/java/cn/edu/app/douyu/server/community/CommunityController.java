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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final RewardAccountRepository rewardAccountRepository;
    private final FileAssetRepository fileAssetRepository;
    private final IdGenerator idGenerator;

    public CommunityController(PostRepository postRepository, CommentRepository commentRepository,
                               LikeRepository likeRepository, FavoriteRepository favoriteRepository,
                               UserRepository userRepository, FollowRepository followRepository,
                               RewardAccountRepository rewardAccountRepository, FileAssetRepository fileAssetRepository,
                               IdGenerator idGenerator) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.rewardAccountRepository = rewardAccountRepository;
        this.fileAssetRepository = fileAssetRepository;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "推荐 Feed", description = "获取推荐帖子列表（公开接口）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/posts/feed")
    PageResult<Map<String, Object>> feed(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<PostEntity> visible = postRepository.findByStatusOrderByPinnedDescCreatedAtDesc("VISIBLE");
        List<Map<String, Object>> items = visible.stream().map(this::postView).toList();
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
                : postRepository.findByAuthorIdInAndStatusOrderByCreatedAtDesc(followed, "VISIBLE");
        List<Map<String, Object>> views = items.stream().map(this::postView).toList();
        return PageResult.of(slice(views, page, size), page, size, views.size());
    }

    @Operation(summary = "发布帖子", description = "发布新帖子，进入审核状态")
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
                joinList(request.mediaFileIds()), joinList(request.topicIds()), request.linkedPatternId(),
                "REVIEWING", 0, 0, 0, false, now, now);
        postRepository.save(post);
        return postView(post);
    }

    @Operation(summary = "帖子详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "帖子不存在")
    })
    @GetMapping("/posts/{postId}")
    Map<String, Object> post(@PathVariable String postId) {
        return postView(requirePost(postId));
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
        if (request.linkedPatternId() != null) post.setLinkedPatternId(request.linkedPatternId());
        post.setStatus("REVIEWING");
        postRepository.save(post);
        return postView(post);
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
        likeRepository.findByUserIdAndTargetTypeAndTargetId(userId, "POST", postId).orElseGet(() ->
                likeRepository.save(new LikeEntity(idGenerator.next("like"), userId, "POST", postId, now, now)));
        post.setLikeCount((int) likeRepository.countByTargetTypeAndTargetId("POST", postId));
        postRepository.save(post);
        return Map.of("liked", true);
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
        likeRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, "POST", postId);
        post.setLikeCount((int) likeRepository.countByTargetTypeAndTargetId("POST", postId));
        postRepository.save(post);
        return Map.of("liked", false);
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
        favoriteRepository.findByUserIdAndTargetTypeAndTargetId(userId, "POST", postId).orElseGet(() ->
                favoriteRepository.save(new FavoriteEntity(idGenerator.next("fav"), userId, "POST", postId, now, now)));
        post.setFavoriteCount((int) favoriteRepository.countByTargetTypeAndTargetId("POST", postId));
        postRepository.save(post);
        return Map.of("favorited", true);
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
        favoriteRepository.deleteByUserIdAndTargetTypeAndTargetId(userId, "POST", postId);
        post.setFavoriteCount((int) favoriteRepository.countByTargetTypeAndTargetId("POST", postId));
        postRepository.save(post);
        return Map.of("favorited", false);
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
        String content = request.content() == null ? "" : request.content().trim();
        validateCommentPayload(userId, content, mediaFileIds);
        Instant now = Instant.now();
        CommentEntity comment = new CommentEntity(idGenerator.next("cmt"), postId, userId, request.parentId(),
                content, "REVIEWING", now, now);
        comment.setMediaFileIds(joinList(mediaFileIds));
        commentRepository.save(comment);
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

    private PostEntity requirePost(String postId) {
        PostEntity post = postRepository.findById(postId).orElse(null);
        if (post == null || "DELETED".equals(post.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private Map<String, Object> postView(PostEntity post) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("postId", post.getId());
        view.put("authorId", post.getAuthorId());
        view.put("author", authorInfo(post.getAuthorId()));
        view.put("title", post.getTitle() == null ? "" : post.getTitle());
        view.put("content", post.getContent());
        view.put("mediaFileIds", splitList(post.getMediaFileIds()));
        view.put("coverImageUrl", post.getCoverImageUrl() == null ? "" : post.getCoverImageUrl());
        view.put("mediaColors", List.of());
        view.put("topicIds", splitList(post.getTopicIds()));
        view.put("topicNames", List.of());
        view.put("linkedPatternId", post.getLinkedPatternId());
        view.put("status", post.getStatus());
        view.put("likeCount", post.getLikeCount());
        view.put("favoriteCount", post.getFavoriteCount());
        view.put("commentCount", post.getCommentCount());
        view.put("createdAt", post.getCreatedAt().toString());
        view.put("updatedAt", post.getUpdatedAt().toString());
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
            m.put("avatarUrl", u.getAvatarFileId() == null ? "" : u.getAvatarFileId());
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
            view.put("auditStatus", "NEED_MANUAL_REVIEW");
            return view;
        }
        view.put("publicUrl", file.getPublicUrl() == null ? "" : file.getPublicUrl());
        view.put("mimeType", file.getMimeType());
        view.put("width", file.getWidth());
        view.put("height", file.getHeight());
        view.put("auditStatus", file.getAuditStatus());
        return view;
    }

    private void validateCommentPayload(String userId, String content, List<String> mediaFileIds) {
        if (content.isBlank() && mediaFileIds.isEmpty()) {
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

    public record PostRequest(String title, @NotBlank String content,
                              List<String> mediaFileIds, List<String> topicIds, String linkedPatternId) {
    }

    public record CommentRequest(String content, String parentId, List<String> mediaFileIds) {
    }
}
