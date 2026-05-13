package cn.edu.app.douyu.server.community;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.InMemoryStore;
import cn.edu.app.douyu.server.common.Models.Comment;
import cn.edu.app.douyu.server.common.Models.Post;
import cn.edu.app.douyu.server.common.PageResult;
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

@RestController
@RequestMapping("/api/v1")
public class CommunityController {
    private final InMemoryStore store;
    private final IdGenerator idGenerator;

    public CommunityController(InMemoryStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    @GetMapping("/posts/feed")
    PageResult<Map<String, Object>> feed(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        List<Map<String, Object>> items = store.visiblePosts().stream().map(this::postView).toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

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

    @PostMapping("/posts")
    Map<String, Object> createPost(Authentication authentication, @Valid @RequestBody PostRequest request) {
        String userId = CurrentUser.userId(authentication);
        Post post = new Post(idGenerator.next("post"), userId, request.title(), request.content(), "REVIEWING", 0, 0, 0, false, Instant.now());
        store.posts.put(post.id(), post);
        return postView(post);
    }

    @GetMapping("/posts/{postId}")
    Map<String, Object> post(@PathVariable String postId) {
        return postView(requirePost(postId));
    }

    @PatchMapping("/posts/{postId}")
    Map<String, Object> updatePost(Authentication authentication, @PathVariable String postId, @RequestBody PostRequest request) {
        String userId = CurrentUser.userId(authentication);
        Post post = requirePost(postId);
        if (!post.authorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能编辑自己的帖子");
        }
        Post updated = new Post(post.id(), post.authorId(), request.title() == null ? post.title() : request.title(),
                request.content() == null ? post.content() : request.content(), "REVIEWING", post.likeCount(), post.favoriteCount(), post.commentCount(), post.pinned(), post.createdAt());
        store.posts.put(postId, updated);
        return postView(updated);
    }

    @DeleteMapping("/posts/{postId}")
    Map<String, Object> deletePost(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        Post post = requirePost(postId);
        if (!post.authorId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "只能删除自己的帖子");
        }
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(), "DELETED", post.likeCount(), post.favoriteCount(), post.commentCount(), post.pinned(), post.createdAt()));
        return Map.of("deleted", true);
    }

    @PostMapping("/posts/{postId}/like")
    Map<String, Object> like(Authentication authentication, @PathVariable String postId) {
        CurrentUser.userId(authentication);
        Post post = requirePost(postId);
        store.likes.add(CurrentUser.userId(authentication) + ":POST:" + postId);
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(), post.status(), post.likeCount() + 1, post.favoriteCount(), post.commentCount(), post.pinned(), post.createdAt()));
        return Map.of("liked", true);
    }

    @DeleteMapping("/posts/{postId}/like")
    Map<String, Object> unlike(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        store.likes.remove(userId + ":POST:" + postId);
        return Map.of("liked", false);
    }

    @PostMapping("/posts/{postId}/favorite")
    Map<String, Object> favorite(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        Post post = requirePost(postId);
        store.favorites.add(userId + ":POST:" + postId);
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(), post.status(), post.likeCount(), post.favoriteCount() + 1, post.commentCount(), post.pinned(), post.createdAt()));
        return Map.of("favorited", true);
    }

    @DeleteMapping("/posts/{postId}/favorite")
    Map<String, Object> unfavorite(Authentication authentication, @PathVariable String postId) {
        String userId = CurrentUser.userId(authentication);
        store.favorites.remove(userId + ":POST:" + postId);
        return Map.of("favorited", false);
    }

    @GetMapping("/posts/{postId}/comments")
    PageResult<Map<String, Object>> comments(@PathVariable String postId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
        requirePost(postId);
        List<Map<String, Object>> items = store.comments.values().stream()
                .filter(comment -> comment.postId().equals(postId) && !"DELETED".equals(comment.status()))
                .map(this::commentView)
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @PostMapping("/posts/{postId}/comments")
    Map<String, Object> comment(Authentication authentication, @PathVariable String postId, @Valid @RequestBody CommentRequest request) {
        String userId = CurrentUser.userId(authentication);
        Post post = requirePost(postId);
        Comment comment = new Comment(idGenerator.next("cmt"), postId, userId, request.parentId(), request.content(), "REVIEWING", Instant.now());
        store.comments.put(comment.id(), comment);
        store.posts.put(postId, new Post(post.id(), post.authorId(), post.title(), post.content(), post.status(), post.likeCount(), post.favoriteCount(), post.commentCount() + 1, post.pinned(), post.createdAt()));
        return commentView(comment);
    }

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
        return Map.of(
                "postId", post.id(),
                "authorId", post.authorId(),
                "title", post.title() == null ? "" : post.title(),
                "content", post.content(),
                "status", post.status(),
                "likeCount", post.likeCount(),
                "favoriteCount", post.favoriteCount(),
                "commentCount", post.commentCount()
        );
    }

    private Map<String, Object> commentView(Comment comment) {
        return Map.of(
                "commentId", comment.id(),
                "postId", comment.postId(),
                "authorId", comment.authorId(),
                "content", comment.content(),
                "status", comment.status()
        );
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    public record PostRequest(String title, @NotBlank String content) {
    }

    public record CommentRequest(@NotBlank String content, String parentId) {
    }
}
