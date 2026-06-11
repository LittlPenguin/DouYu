package cn.edu.app.douyu.server.search;

import cn.edu.app.douyu.server.common.BizException;
import cn.edu.app.douyu.server.common.ErrorCode;
import cn.edu.app.douyu.server.common.PageResult;
import cn.edu.app.douyu.server.common.entity.FileAssetEntity;
import cn.edu.app.douyu.server.common.entity.FileAssetRepository;
import cn.edu.app.douyu.server.common.entity.PostEntity;
import cn.edu.app.douyu.server.common.entity.PostRepository;
import cn.edu.app.douyu.server.common.entity.ProductEntity;
import cn.edu.app.douyu.server.common.entity.ProductRepository;
import cn.edu.app.douyu.server.common.entity.TopicEntity;
import cn.edu.app.douyu.server.common.entity.TopicRepository;
import cn.edu.app.douyu.server.common.entity.UserEntity;
import cn.edu.app.douyu.server.common.entity.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Tag(name = "Search", description = "Global search for public posts, products, users, and topics")
@RestController
@RequestMapping("/api/v1")
public class SearchController {
    private static final List<String> VALID_TYPES = List.of("all", "posts", "products", "users", "topics");

    private final PostRepository postRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final FileAssetRepository fileAssetRepository;

    public SearchController(PostRepository postRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            TopicRepository topicRepository,
                            FileAssetRepository fileAssetRepository) {
        this.postRepository = postRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.fileAssetRepository = fileAssetRepository;
    }

    @Operation(summary = "Global search", description = "Search public retained surfaces by keyword.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/search")
    PageResult<Map<String, Object>> search(@RequestParam(defaultValue = "") String keyword,
                                           @RequestParam(defaultValue = "all") String type,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        String normalizedKeyword = normalizeKeyword(keyword);
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        if (normalizedKeyword.isEmpty()) {
            return PageResult.of(List.of(), safePage, safeSize, 0);
        }

        String normalizedType = normalizeType(type);
        List<Map<String, Object>> results = switch (normalizedType) {
            case "posts" -> searchPosts(normalizedKeyword);
            case "products" -> searchProducts(normalizedKeyword);
            case "users" -> searchUsers(normalizedKeyword);
            case "topics" -> searchTopics(normalizedKeyword);
            case "all" -> combinedResults(normalizedKeyword);
            default -> throw new BizException(ErrorCode.INVALID_ARGUMENT, "搜索类型不支持");
        };
        return PageResult.of(slice(results, safePage, safeSize), safePage, safeSize, results.size());
    }

    private List<Map<String, Object>> combinedResults(String keyword) {
        java.util.ArrayList<Map<String, Object>> results = new java.util.ArrayList<>();
        results.addAll(searchPosts(keyword));
        results.addAll(searchProducts(keyword));
        results.addAll(searchUsers(keyword));
        results.addAll(searchTopics(keyword));
        return results;
    }

    private List<Map<String, Object>> searchPosts(String keyword) {
        Map<String, TopicEntity> topicsById = topicRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(TopicEntity::getId, topic -> topic, (a, b) -> a));
        return postRepository.findByStatusInOrderByPinnedDescCreatedAtDesc(List.of("VISIBLE")).stream()
                .filter(post -> postMatches(post, keyword, topicsById))
                .map(post -> postResult(post, topicsById))
                .toList();
    }

    private List<Map<String, Object>> searchProducts(String keyword) {
        return productRepository.findByStatusAndAuditStatus("ON_SALE", "PASS").stream()
                .filter(product -> matchesAny(keyword,
                        product.getId(),
                        product.getTitle(),
                        product.getDescription(),
                        product.getCategoryId(),
                        product.getCategoryName(),
                        product.getType()))
                .sorted(Comparator.comparing(ProductEntity::getCreatedAt, SearchController::compareInstantDesc))
                .map(this::productResult)
                .toList();
    }

    private List<Map<String, Object>> searchUsers(String keyword) {
        return userRepository.findAll().stream()
                .filter(user -> "ACTIVE".equals(user.getAccountStatus()))
                .filter(user -> matchesAny(keyword,
                        user.getId(),
                        user.getNickname(),
                        user.getBio(),
                        user.getRegion()))
                .sorted(Comparator.comparing(UserEntity::getCreatedAt, SearchController::compareInstantDesc))
                .map(this::userResult)
                .toList();
    }

    private List<Map<String, Object>> searchTopics(String keyword) {
        return topicRepository.findAllByOrderByPostCountDescNameAsc().stream()
                .filter(topic -> matchesAny(keyword, topic.getId(), topic.getName(), topic.getDescription()))
                .map(this::topicResult)
                .toList();
    }

    private boolean postMatches(PostEntity post, String keyword, Map<String, TopicEntity> topicsById) {
        if (matchesAny(keyword, post.getId(), post.getTitle(), post.getContent(), post.getTopicIds())) {
            return true;
        }
        return splitList(post.getTopicIds()).stream()
                .map(topicsById::get)
                .anyMatch(topic -> topic != null && matchesAny(keyword, topic.getId(), topic.getName(), topic.getDescription()));
    }

    private Map<String, Object> postResult(PostEntity post, Map<String, TopicEntity> topicsById) {
        List<String> topicNames = splitList(post.getTopicIds()).stream()
                .map(topicsById::get)
                .filter(topic -> topic != null && topic.getName() != null && !topic.getName().isBlank())
                .map(TopicEntity::getName)
                .toList();
        String title = firstNonBlank(post.getTitle(), excerpt(post.getContent()), "未命名作品");
        String subtitle = topicNames.isEmpty()
                ? countText(post.getLikeCount(), post.getFavoriteCount(), post.getCommentCount())
                : "#" + String.join(" #", topicNames) + " · " + countText(post.getLikeCount(), post.getFavoriteCount(), post.getCommentCount());
        return result("POST", post.getId(), title, subtitle, post.getCoverImageUrl(), "作品");
    }

    private Map<String, Object> productResult(ProductEntity product) {
        String subtitle = firstNonBlank(product.getCategoryName(), product.getCategoryId(), product.getType(), "商品");
        return result("PRODUCT", product.getId(), firstNonBlank(product.getTitle(), "未命名商品"),
                subtitle, product.getImageUrl(), "商品");
    }

    private Map<String, Object> userResult(UserEntity user) {
        String subtitle = firstNonBlank(user.getBio(), user.getRegion(), "用户");
        return result("USER", user.getId(), firstNonBlank(user.getNickname(), "豆友"),
                subtitle, avatarUrl(user.getAvatarFileId()), "用户");
    }

    private Map<String, Object> topicResult(TopicEntity topic) {
        String subtitle = firstNonBlank(topic.getDescription(), topic.getPostCount() + " 个作品");
        return result("TOPIC", topic.getId(), firstNonBlank(topic.getName(), topic.getId()),
                subtitle, "", "话题");
    }

    private Map<String, Object> result(String resultType, String targetId, String title,
                                       String subtitle, String imageUrl, String meta) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("resultType", resultType);
        view.put("targetId", targetId);
        view.put("title", title == null ? "" : title);
        view.put("subtitle", subtitle == null ? "" : subtitle);
        view.put("imageUrl", imageUrl == null ? "" : imageUrl);
        view.put("meta", meta == null ? "" : meta);
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

    private String countText(int likes, int favorites, int comments) {
        return Math.max(0, likes) + " 赞 · " + Math.max(0, favorites) + " 收藏 · " + Math.max(0, comments) + " 评论";
    }

    private String excerpt(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.length() > 28 ? trimmed.substring(0, 28) : trimmed;
    }

    private boolean matchesAny(String normalizedKeyword, String... values) {
        for (String value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    private List<String> splitList(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private <T> List<T> slice(List<T> items, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(items.size(), from + size);
        return from >= items.size() ? List.of() : items.subList(from, to);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeType(String type) {
        String normalizedType = type == null || type.isBlank() ? "all" : type.trim().toLowerCase(Locale.ROOT);
        if (!VALID_TYPES.contains(normalizedType)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "搜索类型不支持");
        }
        return normalizedType;
    }

    private String firstNonBlank(String first, String fallback) {
        return firstNonBlank(first, null, null, fallback);
    }

    private String firstNonBlank(String first, String second, String fallback) {
        return firstNonBlank(first, second, null, fallback);
    }

    private String firstNonBlank(String first, String second, String third, String fallback) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        if (third != null && !third.isBlank()) {
            return third.trim();
        }
        return fallback;
    }

    private static int compareInstantDesc(Instant left, Instant right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }
}
