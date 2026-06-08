package cn.edu.app.douyu.server.user;

import cn.edu.app.douyu.server.auth.AuthService;
import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.CurrentUser;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.IdGenerator;
import cn.edu.app.douyu.server.common.Models.User;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.community.CommunityController;
import cn.edu.app.douyu.server.common.entity.CommentRepository;
import cn.edu.app.douyu.server.common.entity.FavoriteRepository;
import cn.edu.app.douyu.server.common.entity.FollowEntity;
import cn.edu.app.douyu.server.common.entity.FollowRepository;
import cn.edu.app.douyu.server.common.entity.LikeRepository;
import cn.edu.app.douyu.server.common.entity.PostEntity;
import cn.edu.app.douyu.server.common.entity.PostRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Tag(name = "用户", description = "用户资料、关注/取关、实名认证")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommunityController communityController;
    private final IdGenerator idGenerator;

    public UserController(AuthService authService, UserRepository userRepository,
                          FollowRepository followRepository, LikeRepository likeRepository,
                          FavoriteRepository favoriteRepository, CommentRepository commentRepository,
                          PostRepository postRepository, CommunityController communityController,
                          IdGenerator idGenerator) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.likeRepository = likeRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.communityController = communityController;
        this.idGenerator = idGenerator;
    }

    @Operation(summary = "获取当前用户资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @GetMapping("/me")
    Map<String, Object> me(Authentication authentication) {
        String userId = CurrentUser.userId(authentication);
        Map<String, Object> view = authService.userView(authService.requireUser(userId));
        List<PostEntity> myPosts = postRepository.findByAuthorIdInOrderByCreatedAtDesc(List.of(userId)).stream()
                .filter(this::isListablePost)
                .toList();
        long likes = myPosts.stream().mapToLong(PostEntity::getLikeCount).sum();
        view.put("likedCount", (int) likes);
        view.put("postCount", myPosts.size());
        return view;
    }

    @Operation(summary = "搜索可提及用户")
    @GetMapping("/search")
    PageResult<Map<String, Object>> search(@RequestParam(defaultValue = "") String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        var users = keyword == null || keyword.isBlank()
                ? userRepository.findAll(pageable)
                : userRepository.findByPhoneContainingOrNicknameContainingIgnoreCase(keyword, keyword, pageable);
        List<Map<String, Object>> items = users.getContent().stream()
                .map(user -> authService.userView(toModel(user)))
                .toList();
        return PageResult.of(items, page, size, users.getTotalElements());
    }

    @Operation(summary = "我点赞过的作品")
    @GetMapping("/me/liked-posts")
    PageResult<Map<String, Object>> likedPosts(Authentication authentication,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<String> postIds = likeRepository.findByUserIdAndTargetTypeOrderByCreatedAtDesc(userId, "POST").stream()
                .map(link -> link.getTargetId())
                .toList();
        return postPage(postIds, userId, page, size);
    }

    @Operation(summary = "我评论过的作品")
    @GetMapping("/me/commented-posts")
    PageResult<Map<String, Object>> commentedPosts(Authentication authentication,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        LinkedHashSet<String> postIds = new LinkedHashSet<>();
        commentRepository.findByAuthorIdAndStatusNotOrderByCreatedAtDesc(userId, "DELETED")
                .forEach(comment -> postIds.add(comment.getPostId()));
        return postPage(postIds.stream().toList(), userId, page, size);
    }

    @Operation(summary = "我收藏过的作品")
    @GetMapping("/me/favorite-posts")
    PageResult<Map<String, Object>> favoritePosts(Authentication authentication,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<String> postIds = favoriteRepository.findByUserIdAndTargetTypeOrderByCreatedAtDesc(userId, "POST").stream()
                .map(link -> link.getTargetId())
                .toList();
        return postPage(postIds, userId, page, size);
    }

    @Operation(summary = "我关注作者的作品")
    @GetMapping("/me/followed-posts")
    PageResult<Map<String, Object>> followedPosts(Authentication authentication,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        String userId = CurrentUser.userId(authentication);
        List<String> followed = followRepository.findByUserId(userId).stream()
                .map(FollowEntity::getTargetUserId)
                .toList();
        List<Map<String, Object>> items = followed.isEmpty() ? List.of()
                : postRepository.findByAuthorIdInOrderByCreatedAtDesc(followed).stream()
                .filter(this::isListablePost)
                .map(post -> communityController.postView(post, userId))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    @Operation(summary = "更新当前用户资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PatchMapping("/me")
    Map<String, Object> updateMe(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        String userId = CurrentUser.userId(authentication);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.avatarFileId() != null) user.setAvatarFileId(request.avatarFileId());
        if (request.bio() != null) user.setBio(request.bio());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return authService.userView(toModel(user));
    }

    @Operation(summary = "获取用户公开资料")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @GetMapping("/{userId}")
    Map<String, Object> user(@PathVariable String userId) {
        return authService.userView(authService.requireUser(userId));
    }

    @Operation(summary = "关注用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "关注成功"),
            @ApiResponse(responseCode = "401", description = "未登录"),
            @ApiResponse(responseCode = "404", description = "目标用户不存在")
    })
    @PostMapping("/{userId}/follow")
    Map<String, Object> follow(Authentication authentication, @PathVariable String userId) {
        String currentUserId = CurrentUser.userId(authentication);
        authService.requireUser(userId);
        Instant now = Instant.now();
        followRepository.findByUserIdAndTargetUserId(currentUserId, userId).orElseGet(() ->
                followRepository.save(new FollowEntity(idGenerator.next("flw"), currentUserId, userId, now, now)));
        boolean followsMe = followRepository.findByUserIdAndTargetUserId(userId, currentUserId).isPresent();
        return Map.of("followed", true, "followedByMe", true, "followsMe", followsMe, "mutualFollow", followsMe);
    }

    @Operation(summary = "取消关注")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "取消关注成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @DeleteMapping("/{userId}/follow")
    Map<String, Object> unfollow(Authentication authentication, @PathVariable String userId) {
        String currentUserId = CurrentUser.userId(authentication);
        followRepository.deleteByUserIdAndTargetUserId(currentUserId, userId);
        boolean followsMe = followRepository.findByUserIdAndTargetUserId(userId, currentUserId).isPresent();
        return Map.of("followed", false, "followedByMe", false, "followsMe", followsMe, "mutualFollow", false);
    }

    @Operation(summary = "提交实名信息", description = "提交真实姓名和身份证号进行实名认证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/real-name")
    Map<String, Object> realName(Authentication authentication, @Valid @RequestBody RealNameRequest request) {
        String userId = CurrentUser.userId(authentication);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
        user.setRealNameStatus("VERIFIED");
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        return Map.of("realNameStatus", user.getRealNameStatus());
    }

    private User toModel(UserEntity entity) {
        return new User(entity.getId(), entity.getPhone(), entity.getNickname(), entity.getAvatarFileId(),
                entity.getBio(), entity.getAgeGroup(), entity.isMinor(), entity.getRealNameStatus(),
                entity.getAccountStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public record UpdateProfileRequest(String nickname, String avatarFileId, String bio) {
    }

    public record RealNameRequest(String realName, String idCardNo) {
    }

    private PageResult<Map<String, Object>> postPage(List<String> postIds, String currentUserId, int page, int size) {
        List<PostEntity> posts = postRepository.findAllById(postIds);
        Map<String, PostEntity> byId = posts.stream().collect(java.util.stream.Collectors.toMap(PostEntity::getId, post -> post));
        List<Map<String, Object>> items = postIds.stream()
                .map(byId::get)
                .filter(this::isListablePost)
                .map(post -> communityController.postView(post, currentUserId))
                .toList();
        return PageResult.of(slice(items, page, size), page, size, items.size());
    }

    private boolean isListablePost(PostEntity post) {
        return post != null && ("VISIBLE".equals(post.getStatus()) || "REVIEWING".equals(post.getStatus()));
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }
}
