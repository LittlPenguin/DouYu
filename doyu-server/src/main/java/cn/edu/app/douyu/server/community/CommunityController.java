package cn.edu.app.douyu.server.community;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Comment;
import cn.edu.app.douyu.server.common.Models.Post;
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
import java.util.List;
import java.util.Map;

@Tag(name = "社区", description = "帖子 CRUD、点赞、收藏、评论、举报")
@RestController
@RequestMapping("/api/v1")
public class CommunityController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public CommunityController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "推荐 Feed", description = "获取推荐帖子列表（公开接口）")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/posts/feed")
    PageResult<Map<String, Object>> feed(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.visiblePosts().stream().map(this::postView).toList();
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
        List<String> followed = store.follows.stream()
                .filter(key -> key.startsWith(userId + ":"))
                .map(key -> key.substring((userId + ":").length()))
                .toList();
        List<Map<String, Object>> items = store.visiblePosts().stream()
                .filter(post -> followed.contains(post.authorId()))
                .map(this::postView)
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
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
        Post post = new Post(idGenerator.next("post"), userId, request.title(), request.content(),
                request.mediaFileIds() == null ? List.of() : request.mediaFileIds(),
                request.topicIds() == null ? List.of() : request.topicIds(),
                request.linkedPatternId(),
                "REVIEWING", 0, 0, 0, false, Instant.now());
        store.posts.put(post.id(), post);
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
        Post post = requirePost(postId);
        if (!post.authorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能编辑自己的帖子");
        }
        Post updated = new Post(post.id(), post.authorId(), request.title() == null ? post.title() : request.title(),
                request.content() == null ? post.content() : request.content(),
                request.mediaFileIds() == null ? post.mediaFileIds() : request.mediaFileIds(),
                request.topicIds() == null ? post.topicIds() : request.topicIds(),
                request.linkedPatternId() == null ? post.linkedPatternId() : request.linkedPatternId(),
                "REVIEWING", post.likeCount(), post.favoriteCount(), post.commentCount(), post.pinned(), post.createdAt());
        store.posts.put(postId, updated);
        return postView(updated);
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
        Post post = requirePost(postId);
        if (!post.authorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的帖子");
        }
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(),
                post.mediaFileIds(), post.topicIds(), post.linkedPatternId(),
                "DELETED", post.likeCount(), post.favoriteCount(), post.commentCount(), post.pinned(), post.createdAt()));
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
        CurrentUser.userId(authentication);
        Post post = requirePost(postId);
        store.likes.add(CurrentUser.userId(authentication) + ":POST:" + postId);
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(),
                post.mediaFileIds(), post.topicIds(), post.linkedPatternId(),
                post.status(), post.likeCount() + 1, post.favoriteCount(), post.commentCount(), post.pinned(), post.createdAt()));
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
        store.likes.remove(userId + ":POST:" + postId);
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
        Post post = requirePost(postId);
        store.favorites.add(userId + ":POST:" + postId);
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(),
                post.mediaFileIds(), post.topicIds(), post.linkedPatternId(),
                post.status(), post.likeCount(), post.favoriteCount() + 1, post.commentCount(), post.pinned(), post.createdAt()));
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
        store.favorites.remove(userId + ":POST:" + postId);
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
        List<Map<String, Object>> items = store.comments.values().stream()
                .filter(comment -> comment.postId().equals(postId) && !"DELETED".equals(comment.status()))
                .map(this::commentView)
                .toList();
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
        Post post = requirePost(postId);
        Comment comment = new Comment(idGenerator.next("cmt"), postId, userId, request.parentId(), request.content(), "REVIEWING", Instant.now());
        store.comments.put(comment.id(), comment);
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(),
                post.mediaFileIds(), post.topicIds(), post.linkedPatternId(),
                post.status(), post.likeCount(), post.favoriteCount(), post.commentCount() + 1, post.pinned(), post.createdAt()));
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
        Comment comment = store.comments.get(commentId);
        if (comment == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        if (!comment.authorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的评论");
        }
        store.comments.put(commentId, new Comment(comment.id(), comment.postId(), comment.authorId(), comment.parentId(), comment.content(), "DELETED", comment.createdAt()));
        return Map.of("deleted", true);
    }

    private Post requirePost(String postId) {
        Post post = store.posts.get(postId);
        if (post == null || "DELETED".equals(post.status())) {
            throw new BizException(ErrorCode.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private Map<String, Object> postView(Post post) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("postId", post.id());
        view.put("authorId", post.authorId());
        view.put("author", authorInfo(post.authorId()));
        view.put("title", post.title() == null ? "" : post.title());
        view.put("content", post.content());
        view.put("mediaFileIds", post.mediaFileIds() == null ? List.of() : post.mediaFileIds());
        view.put("mediaColors", List.of());
        view.put("topicIds", post.topicIds() == null ? List.of() : post.topicIds());
        view.put("topicNames", List.of());
        view.put("linkedPatternId", post.linkedPatternId());
        view.put("status", post.status());
        view.put("likeCount", post.likeCount());
        view.put("favoriteCount", post.favoriteCount());
        view.put("commentCount", post.commentCount());
        return view;
    }

    private Map<String, Object> authorInfo(String userId) {
        return store.users.values().stream()
                .filter(u -> u.id().equals(userId))
                .findFirst()
                .map(u -> {
                    long following = store.follows.stream().filter(k -> k.startsWith(userId + ":")).count();
                    long followers = store.follows.stream().filter(k -> k.endsWith(":" + userId)).count();
                    var reward = store.rewards.get(userId);
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("userId", u.id());
                    m.put("nickname", u.nickname() == null ? "" : u.nickname());
                    m.put("avatarUrl", u.avatarFileId() == null ? "" : u.avatarFileId());
                    m.put("bio", u.bio() == null ? "" : u.bio());
                    m.put("level", reward != null ? reward.levelCode() : "LV1");
                    m.put("isMinor", u.isMinor());
                    m.put("followingCount", (int) following);
                    m.put("followerCount", (int) followers);
                    return (Map<String, Object>) m;
                })
                .orElseGet(() -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("userId", userId);
                    m.put("nickname", "");
                    m.put("avatarUrl", "");
                    m.put("bio", "");
                    m.put("level", "LV1");
                    m.put("isMinor", false);
                    m.put("followingCount", 0);
                    m.put("followerCount", 0);
                    return m;
                });
    }

    private Map<String, Object> commentView(Comment comment) {
        Map<String, Object> view = new java.util.LinkedHashMap<>();
        view.put("commentId", comment.id());
        view.put("postId", comment.postId());
        view.put("authorId", comment.authorId());
        view.put("author", authorInfo(comment.authorId()));
        view.put("parentId", comment.parentId());
        view.put("content", comment.content());
        view.put("status", comment.status());
        return view;
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record PostRequest(String title, @NotBlank String content,
                              List<String> mediaFileIds, List<String> topicIds, String linkedPatternId) {
    }

    public record CommentRequest(@NotBlank String content, String parentId) {
    }
}
