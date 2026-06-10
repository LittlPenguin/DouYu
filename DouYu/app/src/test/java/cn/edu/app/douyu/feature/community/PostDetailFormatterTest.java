package cn.edu.app.douyu.feature.community;

import org.junit.Test;

import java.util.List;

import cn.edu.app.douyu.model.Comment;
import cn.edu.app.douyu.model.CommentMediaAsset;
import cn.edu.app.douyu.model.CommentMention;
import cn.edu.app.douyu.model.CommentSticker;
import cn.edu.app.douyu.model.CommentTopicRef;
import cn.edu.app.douyu.model.Post;
import cn.edu.app.douyu.model.UserProfile;

import static org.junit.Assert.assertEquals;

public class PostDetailFormatterTest {
    @Test
    public void imageUrlsPreferPostImagesAndFallbackToCover() {
        Post post = new Post();
        post.coverImageUrl = "https://example.invalid/cover.jpg";
        post.imageUrls = List.of("", "https://example.invalid/one.jpg", "https://example.invalid/one.jpg",
                "https://example.invalid/two.jpg");

        assertEquals(List.of("https://example.invalid/one.jpg", "https://example.invalid/two.jpg"),
                PostDetailFormatter.imageUrls(post));

        Post fallback = new Post();
        fallback.coverImageUrl = "https://example.invalid/cover.jpg";
        assertEquals(List.of("https://example.invalid/cover.jpg"), PostDetailFormatter.imageUrls(fallback));
    }

    @Test
    public void emptyImagesUseZeroCountLabel() {
        assertEquals("0/0", PostDetailFormatter.carouselCountLabel(0, 0));
        assertEquals("1/3", PostDetailFormatter.carouselCountLabel(0, 3));
        assertEquals("3/3", PostDetailFormatter.carouselCountLabel(7, 3));
    }

    @Test
    public void topicChipsUseBackendTopicNamesOnly() {
        Post post = new Post();
        post.topicNames = List.of("草莓色卡", "", "新手杯垫");

        assertEquals(List.of("#草莓色卡", "#新手杯垫"), PostDetailFormatter.topicChips(post));
        assertEquals(List.of(), PostDetailFormatter.topicChips(new Post()));
    }

    @Test
    public void authorFollowLabelUsesAuthorAndFollowState() {
        Post own = postWithAuthor("u1");
        assertEquals("作者", PostDetailFormatter.authorFollowLabel(own, "u1"));

        Post followed = postWithAuthor("u2");
        followed.followedAuthorByMe = true;
        assertEquals("已关注", PostDetailFormatter.authorFollowLabel(followed, "u1"));

        Post notFollowed = postWithAuthor("u3");
        assertEquals("未关注", PostDetailFormatter.authorFollowLabel(notFollowed, "u1"));
    }

    @Test
    public void authorMetaUsesReadableDateWithoutBackendStatus() {
        Post post = new Post();
        post.status = "VISIBLE";
        post.createdAt = "2026-06-07T10:20:45.457037Z";

        assertEquals("作品 · 2026-06-07", PostDetailFormatter.authorMeta(post));

        Post missingDate = new Post();
        missingDate.status = "VISIBLE";
        assertEquals("作品详情", PostDetailFormatter.authorMeta(missingDate));
    }

    @Test
    public void interactionLabelsUseBackendState() {
        Post post = new Post();
        post.likedByMe = true;
        post.favoritedByMe = true;
        assertEquals("已点赞", PostDetailFormatter.likeLabel(post));
        assertEquals("已收藏", PostDetailFormatter.favoriteLabel(post));

        Post inactive = new Post();
        assertEquals("点赞", PostDetailFormatter.likeLabel(inactive));
        assertEquals("收藏", PostDetailFormatter.favoriteLabel(inactive));
    }

    @Test
    public void commentEmptyTextMatchesOpenDesign() {
        assertEquals("还没有评论。登录后可以留下拼豆建议或材料清单。", PostDetailFormatter.commentEmptyText());
    }

    @Test
    public void commentSummaryCombinesMentionsContentTopicsStickersAndImages() {
        Comment comment = new Comment();
        comment.content = "边缘可以再压紧一点";
        CommentMention mention = new CommentMention();
        mention.nickname = "小屿";
        CommentTopicRef topic = new CommentTopicRef();
        topic.name = "草莓色卡";
        CommentSticker sticker = new CommentSticker();
        sticker.emojiText = "[爱心]";
        CommentMediaAsset image = new CommentMediaAsset();
        image.publicUrl = "https://example.invalid/comment.png";
        comment.mentions = List.of(mention);
        comment.topics = List.of(topic);
        comment.stickers = List.of(sticker);
        comment.mediaAssets = List.of(image);

        assertEquals("@小屿 边缘可以再压紧一点 #草莓色卡 [爱心] [图片评论 1]",
                PostDetailFormatter.commentSummary(comment));
    }

    @Test
    public void imageOnlyCommentUsesReadableBoundaryText() {
        Comment comment = new Comment();
        CommentMediaAsset image = new CommentMediaAsset();
        image.publicUrl = "https://example.invalid/comment.png";
        comment.mediaAssets = List.of(image);

        assertEquals("[图片评论 1]", PostDetailFormatter.commentSummary(comment));
        assertEquals("图片/贴纸评论", PostDetailFormatter.commentSummary(new Comment()));
    }

    private static Post postWithAuthor(String userId) {
        Post post = new Post();
        UserProfile author = new UserProfile();
        author.userId = userId;
        post.author = author;
        return post;
    }
}
