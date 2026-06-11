package cn.edu.app.douyu.feature.community;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentMediaAsset;
import cn.edu.app.douyu.model.CommentMention;
import cn.edu.app.douyu.model.CommentSticker;
import cn.edu.app.douyu.model.CommentTopicRef;
import cn.edu.app.douyu.model.Post;
/**
 * 帖子详情格式工具：把帖子和评论数据转换成页面展示文本。
 */

final class PostDetailFormatter {
    private static final String COMMENT_EMPTY_TEXT = "还没有评论。登录后可以留下拼豆建议或材料清单。";

    private PostDetailFormatter() {
    }

    static List<String> imageUrls(Post post) {
        Set<String> urls = new LinkedHashSet<>();
        if (post != null && post.imageUrls != null) {
            for (String url : post.imageUrls) {
                addNonBlank(urls, url);
            }
        }
        if (urls.isEmpty() && post != null) {
            addNonBlank(urls, post.coverImageUrl);
        }
        return new ArrayList<>(urls);
    }

    static String carouselCountLabel(int index, int total) {
        if (total <= 0) {
            return "0/0";
        }
        int safeIndex = Math.max(0, Math.min(index, total - 1));
        return (safeIndex + 1) + "/" + total;
    }

    static List<String> topicChips(Post post) {
        List<String> chips = new ArrayList<>();
        if (post == null || post.topicNames == null) {
            return chips;
        }
        for (String topicName : post.topicNames) {
            String value = topicName == null ? "" : topicName.trim();
            if (!value.isEmpty()) {
                chips.add("#" + value);
            }
        }
        return chips;
    }

    static String authorFollowLabel(Post post, String currentUserId) {
        String authorId = post == null || post.author == null ? "" : safe(post.author.userId);
        if (!authorId.isEmpty() && authorId.equals(safe(currentUserId))) {
            return "作者";
        }
        return Boolean.TRUE.equals(post == null ? null : post.followedAuthorByMe) ? "已关注" : "未关注";
    }

    static String authorMeta(Post post) {
        String createdAt = post == null ? "" : safe(post.createdAt).trim();
        if (createdAt.isEmpty()) {
            return "作品详情";
        }
        int timeSeparator = createdAt.indexOf('T');
        String readableDate = timeSeparator > 0 ? createdAt.substring(0, timeSeparator) : createdAt;
        return "作品 · " + readableDate;
    }

    static String likeLabel(Post post) {
        return Boolean.TRUE.equals(post == null ? null : post.likedByMe) ? "已点赞" : "点赞";
    }

    static String favoriteLabel(Post post) {
        return Boolean.TRUE.equals(post == null ? null : post.favoritedByMe) ? "已收藏" : "收藏";
    }

    static String commentEmptyText() {
        return COMMENT_EMPTY_TEXT;
    }

    static String commentSummary(Comment comment) {
        if (comment == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (comment.mentions != null) {
            for (CommentMention mention : comment.mentions) {
                if (mention != null && mention.nickname != null && !mention.nickname.isEmpty()) {
                    builder.append("@").append(mention.nickname).append(" ");
                }
            }
        }
        if (comment.content != null && !comment.content.isBlank()) {
            builder.append(comment.content.trim());
        }
        if (comment.topics != null) {
            for (CommentTopicRef topic : comment.topics) {
                if (topic != null && topic.name != null && !topic.name.isEmpty()) {
                    if (builder.length() > 0) {
                        builder.append(" ");
                    }
                    builder.append("#").append(topic.name);
                }
            }
        }
        if (comment.stickers != null) {
            for (CommentSticker sticker : comment.stickers) {
                if (sticker != null && sticker.emojiText != null && !sticker.emojiText.isEmpty()) {
                    if (builder.length() > 0) {
                        builder.append(" ");
                    }
                    builder.append(sticker.emojiText);
                }
            }
        }
        int imageCount = commentImageCount(comment);
        if (imageCount > 0) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append("[图片评论 ").append(imageCount).append("]");
        }
        return builder.length() == 0 ? "图片/贴纸评论" : builder.toString();
    }

    static boolean commentHasImages(Comment comment) {
        return commentImageCount(comment) > 0;
    }

    static String countLabel(Integer value) {
        return String.valueOf(value == null ? 0 : Math.max(0, value));
    }

    private static void addNonBlank(Set<String> values, String value) {
        String trimmed = safe(value).trim();
        if (!trimmed.isEmpty()) {
            values.add(trimmed);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static int commentImageCount(Comment comment) {
        if (comment == null || comment.mediaAssets == null) {
            return 0;
        }
        int count = 0;
        for (CommentMediaAsset asset : comment.mediaAssets) {
            if (asset != null) {
                count++;
            }
        }
        return count;
    }
}
