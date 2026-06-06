package cn.edu.app.douyu.core

import cn.edu.app.douyu.core.data.ApiException
import cn.edu.app.douyu.core.data.exceptionToUiState
import cn.edu.app.douyu.core.model.*
import cn.edu.app.douyu.core.network.*
import cn.edu.app.douyu.core.ui.UiState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import java.net.UnknownHostException

class ApiInterfaceContractTest {
    @Test
    fun authUploadAndPatternApisExposeIntegrationEndpoints() {
        assertPost(method<AuthApi>("refresh"), "/api/v1/auth/refresh")
        assertPost(method<AuthApi>("logout"), "/api/v1/auth/logout")
        assertPost(method<UploadApi>("confirm"), "/api/v1/uploads/confirm")
        assertPost(method<PatternApi>("createJob"), "/api/v1/patterns/jobs")
        assertGet(method<PatternApi>("job"), "/api/v1/patterns/jobs/{jobId}")
        assertPost(method<PatternApi>("cancelJob"), "/api/v1/patterns/jobs/{jobId}/cancel")
    }

    @Test
    fun communityCommercePaymentAndMessageApisExposeIntegrationEndpoints() {
        assertPost(method<CommunityApi>("createPost"), "/api/v1/posts")
        assertPost(method<CommunityApi>("likePost"), "/api/v1/posts/{postId}/like")
        assertDelete(method<CommunityApi>("unlikePost"), "/api/v1/posts/{postId}/like")
        assertPost(method<CommunityApi>("favoritePost"), "/api/v1/posts/{postId}/favorite")
        assertDelete(method<CommunityApi>("unfavoritePost"), "/api/v1/posts/{postId}/favorite")
        assertGet(method<CommunityApi>("comments"), "/api/v1/posts/{postId}/comments")
        assertPost(method<CommunityApi>("createComment"), "/api/v1/posts/{postId}/comments")
        assertGet(method<CommunityApi>("topics"), "/api/v1/topics")
        assertGet(method<CommunityApi>("topicPosts"), "/api/v1/topics/{topicId}/posts")
        assertGet(method<CommunityApi>("stickerPacks"), "/api/v1/sticker-packs")
        assertGet(method<UserApi>("searchUsers"), "/api/v1/users/search")
        assertPatch(method<UserApi>("updateMe"), "/api/v1/users/me")
        assertGet(method<UserApi>("likedPosts"), "/api/v1/users/me/liked-posts")
        assertGet(method<UserApi>("commentedPosts"), "/api/v1/users/me/commented-posts")
        assertGet(method<UserApi>("favoritePosts"), "/api/v1/users/me/favorite-posts")
        assertGet(method<UserApi>("followedPosts"), "/api/v1/users/me/followed-posts")

        assertPost(method<CartApi>("addItem"), "/api/v1/cart/items")
        assertPatch(method<CartApi>("updateItem"), "/api/v1/cart/items/{itemId}")
        assertDelete(method<CartApi>("removeItem"), "/api/v1/cart/items/{itemId}")

        assertPost(method<OrderApi>("createOrder"), "/api/v1/orders")
        assertHeader(method<OrderApi>("createOrder"), ApiHeaders.IDEMPOTENCY_KEY)
        assertGet(method<OrderApi>("orders"), "/api/v1/orders")
        assertPost(method<OrderApi>("cancelOrder"), "/api/v1/orders/{orderId}/cancel")
        assertPost(method<PaymentApi>("createPayment"), "/api/v1/payments")
        assertHeader(method<PaymentApi>("createPayment"), ApiHeaders.IDEMPOTENCY_KEY)
        assertGet(method<PaymentApi>("payment"), "/api/v1/payments/{paymentId}")

        assertPost(method<MessageApi>("markNotificationsRead"), "/api/v1/messages/notifications/read")
        assertGet(method<MessageApi>("conversation"), "/api/v1/messages/conversations/{conversationId}")
        assertPost(method<MessageApi>("sendMessage"), "/api/v1/messages/conversations/{conversationId}")

        assertPost(method<RewardApi>("checkin"), "/api/v1/checkins")
        assertGet(method<RewardApi>("checkinStatus"), "/api/v1/checkins/status")
        assertGet(method<RewardApi>("badges"), "/api/v1/badges/me")
    }

    @Test
    fun patternAssetDtoAcceptsPrivateStatusFromGeneratedResult() {
        val response = DoyuJson.decodeFromString<ApiResponse<PatternAsset>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "patternId": "pattern_private_contract",
                "jobId": "job_private_contract",
                "ownerId": "user_contract",
                "title": "generated pattern",
                "previewFileId": "file_preview",
                "gridFileId": "file_grid",
                "colorMapFileId": "file_color_map",
                "pdfFileId": "file_pdf",
                "beadSize": "MM_2_6",
                "widthCells": 32,
                "heightCells": 32,
                "totalBeads": 1024,
                "paletteName": "standard",
                "status": "PRIVATE",
                "colorStats": [],
                "materials": {
                  "totalBeads": 1024,
                  "colors": []
                }
              },
              "traceId": "trace_pattern_private"
            }
            """.trimIndent()
        )

        assertEquals(ContentStatus.PRIVATE, response.data?.status)
    }

    @Test
    fun loginAndCommunityDtosMatchDocumentedJsonContract() {
        val user = DoyuJson.decodeFromString<UserProfile>(
            """
            {
              "userId": "user_contract",
              "nickname": "契约用户",
              "avatarUrl": "https://cdn.example.test/avatar.png",
              "bio": "拼豆爱好者",
              "level": 3,
              "isMinor": false,
              "followingCount": 7,
              "followerCount": 12
            }
            """.trimIndent()
        )
        assertEquals("https://cdn.example.test/avatar.png", user.avatarUrl)

        val updateProfileJson = DoyuJson.encodeToString(
            UpdateProfileRequest(
                nickname = "豆屿岛民",
                avatarFileId = "file_avatar_1",
                bio = "喜欢拼豆和晒作品"
            )
        )
        assertTrue(updateProfileJson.contains("\"nickname\":\"豆屿岛民\""))
        assertTrue(updateProfileJson.contains("\"avatarFileId\":\"file_avatar_1\""))
        assertTrue(updateProfileJson.contains("\"bio\":\"喜欢拼豆和晒作品\""))

        val loginJson = DoyuJson.encodeToString(SmsLoginRequest("13800000000", "123456"))
        assertTrue(loginJson.contains("\"ageGroup\":\"AGE_18_PLUS\""))

        val likeResponse = DoyuJson.decodeFromString<ApiResponse<PostInteractionResult>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "liked": true,
                "likedByMe": true,
                "likeCount": 41,
                "favoriteCount": 7,
                "favoritedByMe": false
              },
              "traceId": "trace_like"
            }
            """.trimIndent()
        )
        assertEquals(true, likeResponse.data?.liked)
        assertEquals(true, likeResponse.data?.likedByMe)
        assertEquals(41, likeResponse.data?.likeCount)
        assertEquals(7, likeResponse.data?.favoriteCount)
        assertEquals(false, likeResponse.data?.favoritedByMe)
        assertNull(likeResponse.data?.favorited)

        val favoriteResponse = DoyuJson.decodeFromString<ApiResponse<PostInteractionResult>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "favorited": false,
                "favoritedByMe": false,
                "favoriteCount": 6,
                "likeCount": 41,
                "likedByMe": true
              },
              "traceId": "trace_favorite"
            }
            """.trimIndent()
        )
        assertEquals(false, favoriteResponse.data?.favorited)
        assertEquals(false, favoriteResponse.data?.favoritedByMe)
        assertEquals(6, favoriteResponse.data?.favoriteCount)
        assertEquals(41, favoriteResponse.data?.likeCount)
        assertEquals(true, favoriteResponse.data?.likedByMe)
        assertNull(favoriteResponse.data?.liked)

        val postResponse = DoyuJson.decodeFromString<ApiResponse<Post>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "postId": "post_interaction",
                "authorId": "user_author",
                "author": {
                  "userId": "user_author",
                  "nickname": "作者",
                  "avatarUrl": null,
                  "bio": "",
                  "level": 1,
                  "isMinor": false,
                  "followingCount": 0,
                  "followerCount": 0
                },
                "title": "作品",
                "content": "内容",
                "coverImageUrl": "",
                "mediaFileIds": [],
                "mediaColors": [],
                "topicIds": ["topic_beginner"],
                "topicNames": ["新手教程"],
                "linkedPatternId": null,
                "status": "VISIBLE",
                "likeCount": 8,
                "favoriteCount": 2,
                "commentCount": 1,
                "likedByMe": true,
                "favoritedByMe": true,
                "followedAuthorByMe": true
              },
              "traceId": "trace_post_interaction"
            }
            """.trimIndent()
        )
        assertEquals(true, postResponse.data?.likedByMe)
        assertEquals(true, postResponse.data?.favoritedByMe)
        assertEquals(true, postResponse.data?.followedAuthorByMe)

        val commentJson = DoyuJson.encodeToString(
            CreateCommentRequest(
                content = "带贴纸评论",
                mentionUserIds = listOf("user_2"),
                topicIds = listOf("topic_beginner"),
                stickerIds = listOf("sticker_like")
            )
        )
        assertTrue(commentJson.contains("\"mentionUserIds\":[\"user_2\"]"))
        assertTrue(commentJson.contains("\"topicIds\":[\"topic_beginner\"]"))
        assertTrue(commentJson.contains("\"stickerIds\":[\"sticker_like\"]"))

        val commentResponse = DoyuJson.decodeFromString<ApiResponse<Comment>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "commentId": "comment_1",
                "postId": "post_1",
                "authorId": "user_1",
                "author": {
                  "userId": "user_1",
                  "nickname": "评论者",
                  "avatarUrl": null,
                  "bio": "",
                  "level": 1,
                  "isMinor": false,
                  "followingCount": 0,
                  "followerCount": 0
                },
                "parentId": null,
                "content": "带贴纸评论",
                "status": "REVIEWING",
                "mediaFileIds": [],
                "mediaAssets": [],
                "mentions": [{ "userId": "user_2", "nickname": "被提及用户", "avatarUrl": null }],
                "topics": [{ "topicId": "topic_beginner", "name": "新手教程" }],
                "stickers": [{ "stickerId": "sticker_like", "packId": "pack_doyu_basic", "name": "喜欢", "imageUrl": null, "emojiText": "喜欢" }]
              },
              "traceId": "trace_comment_interaction"
            }
            """.trimIndent()
        )
        assertEquals("user_2", commentResponse.data?.mentions?.first()?.userId)
        assertEquals("topic_beginner", commentResponse.data?.topics?.first()?.topicId)
        assertEquals("sticker_like", commentResponse.data?.stickers?.first()?.stickerId)
    }

    @Test
    fun cartMutationDtosMatchBackendWriteResponses() {
        val addResponse = DoyuJson.decodeFromString<ApiResponse<CartMutationResult>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": { "itemId": "cart_1", "quantity": 2 },
              "traceId": "trace_cart_add"
            }
            """.trimIndent()
        )
        assertEquals("cart_1", addResponse.data?.itemId)
        assertEquals(2, addResponse.data?.quantity)
        assertNull(addResponse.data?.deleted)

        val deleteResponse = DoyuJson.decodeFromString<ApiResponse<CartMutationResult>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": { "deleted": true },
              "traceId": "trace_cart_delete"
            }
            """.trimIndent()
        )
        assertEquals(true, deleteResponse.data?.deleted)
        assertNull(deleteResponse.data?.itemId)
    }

    @Test
    fun cartDtoAcceptsBackendProductSummary() {
        val cartResponse = DoyuJson.decodeFromString<ApiResponse<Cart>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "items": [
                  {
                    "itemId": "cart_1",
                    "skuId": "sku_bead_red",
                    "sku": {
                      "skuId": "sku_bead_red",
                      "productId": "product_bead_red",
                      "specName": "2.6mm",
                      "priceCent": 1200,
                      "stock": 100,
                      "status": "ON_SALE"
                    },
                    "productId": "product_bead_red",
                    "product": {
                      "title": "2.6mm 豆子豆沙红",
                      "imageUrl": ""
                    },
                    "quantity": 2
                  }
                ]
              },
              "traceId": "trace_cart"
            }
            """.trimIndent()
        )
        val item = cartResponse.data?.items?.single()
        assertEquals("cart_1", item?.itemId)
        assertEquals("2.6mm 豆子豆沙红", item?.product?.title)
        assertEquals(2400, cartResponse.data?.payableAmountCent)
    }

    @Test
    fun previewImageDtosMatchBackendDisplayContract() {
        val productResponse = DoyuJson.decodeFromString<ApiResponse<Product>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "productId": "product_image_contract",
                "type": "SELF_OPERATED",
                "sellerId": null,
                "title": "新手材料包",
                "description": "含常用色和基础工具。",
                "categoryId": "cat_beginner",
                "categoryName": "新手套装",
                "status": "ON_SALE",
                "auditStatus": "PASS",
                "imageUrl": "https://cdn.example.test/products/starter-kit.jpg",
                "skus": [
                  {
                    "skuId": "sku_starter",
                    "productId": "product_image_contract",
                    "specName": "2.6mm 入门套装",
                    "priceCent": 6990,
                    "stock": 20,
                    "status": "ON_SALE"
                  }
                ],
                "swatchColor": 4294358706
              },
              "traceId": "trace_product_image"
            }
            """.trimIndent()
        )
        assertEquals("https://cdn.example.test/products/starter-kit.jpg", productResponse.data?.imageUrl)

        val postResponse = DoyuJson.decodeFromString<ApiResponse<Post>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "postId": "post_image_contract",
                "authorId": "user_contract",
                "author": {
                  "userId": "user_contract",
                  "nickname": "契约用户",
                  "avatarUrl": null,
                  "bio": "",
                  "level": 1,
                  "isMinor": false,
                  "followingCount": 0,
                  "followerCount": 0
                },
                "title": "晒一个新作品",
                "content": "真实图片应优先展示。",
                "coverImageUrl": "https://cdn.example.test/posts/work-cover.jpg",
                "mediaFileIds": ["file_post_cover"],
                "mediaColors": [4294358706],
                "topicIds": ["topic_work"],
                "topicNames": ["作品"],
                "linkedPatternId": null,
                "status": "VISIBLE",
                "likeCount": 8,
                "favoriteCount": 2,
                "commentCount": 1
              },
              "traceId": "trace_post_image"
            }
            """.trimIndent()
        )
        assertEquals("https://cdn.example.test/posts/work-cover.jpg", postResponse.data?.coverImageUrl)
    }

    @Test
    fun orderAndPaymentDtosMatchCommerceBoundaryContract() {
        val orderRequestJson = DoyuJson.encodeToString(CreateOrderRequest(listOf("cart_1", "cart_2"), "addr_test_1"))
        assertTrue(orderRequestJson.contains("\"itemIds\":[\"cart_1\",\"cart_2\"]"))
        assertTrue(orderRequestJson.contains("\"addressId\":\"addr_test_1\""))
        assertTrue(!orderRequestJson.contains("\"remark\""))

        val paymentResponse = DoyuJson.decodeFromString<ApiResponse<Payment>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "paymentId": "pay_1",
                "orderId": "ord_1",
                "channel": "WECHAT_APP",
                "status": "CREATED",
                "amountCent": 2400,
                "payParams": { "provider": "STUB", "payload": "stub-pay-payload-pay_1" },
                "channelTradeNo": "",
                "paidAt": null
              },
              "traceId": "trace_payment"
            }
            """.trimIndent()
        )

        val payment = paymentResponse.data
        assertEquals("pay_1", payment?.paymentId)
        assertEquals("ord_1", payment?.orderId)
        assertEquals(PaymentChannel.WECHAT_APP, payment?.channel)
        assertEquals(PaymentStatus.CREATED, payment?.status)
        assertEquals(2400, payment?.amountCent)
        assertEquals("STUB", payment?.payParams?.get("provider"))
        assertTrue(PaymentStatus.entries.map { it.name }.containsAll(listOf("CREATED", "PROCESSING", "SUCCEEDED", "FAILED", "CLOSED")))

        val orderResponse = DoyuJson.decodeFromString<ApiResponse<PageResponse<Order>>>(
            """
            {
              "code": "OK",
              "message": "success",
              "data": {
                "items": [
                  {
                    "orderId": "ord_1",
                    "buyerId": "user_1",
                    "sellerType": "SELF_OPERATED",
                    "sellerId": null,
                    "orderType": "SELF_OPERATED",
                    "status": "WAITING_PAYMENT",
                    "totalAmountCent": 2400,
                    "payableAmountCent": 2400,
                    "items": [
                      {
                        "orderItemId": "oi_1",
                        "productId": "prod_1",
                        "skuId": "sku_1",
                        "title": "2.6mm 豆子豆沙红",
                        "specName": "1000颗",
                        "priceCent": 1200,
                        "quantity": 2
                      }
                    ],
                    "addressSnapshot": { "addressId": "addr_test_1" }
                  }
                ],
                "page": 1,
                "size": 20,
                "total": 1,
                "hasMore": false
              },
              "traceId": "trace_order"
            }
            """.trimIndent()
        )
        val order = orderResponse.data?.items?.single()
        assertEquals(SellerType.SELF_OPERATED, order?.sellerType)
        assertEquals(OrderStatus.WAITING_PAYMENT, order?.status)
        assertEquals(2400, order?.payableAmountCent)
        assertEquals("addr_test_1", order?.addressSnapshot?.addressId)
    }

    @Test
    fun apiErrorCodesMapToExplicitUiStates() {
        assertEquals(
            UiState.RequireLogin,
            exceptionToUiState<Unit>(ApiException("UNAUTHORIZED", "token expired", "trace_auth"))
        )
        assertEquals(
            UiState.Forbidden,
            exceptionToUiState<Unit>(ApiException("FORBIDDEN", "forbidden", "trace_forbidden"))
        )
        assertEquals(UiState.WeakNetwork, exceptionToUiState<Unit>(UnknownHostException("offline")))

        val state = exceptionToUiState<Unit>(ApiException("NOT_FOUND", "missing", "trace_missing"))
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("trace_missing"))
    }

    private inline fun <reified T> method(name: String): java.lang.reflect.Method =
        T::class.java.declaredMethods.first { it.name == name }

    private fun assertPost(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(POST::class.java)?.value)
    }

    private fun assertGet(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(GET::class.java)?.value)
    }

    private fun assertPatch(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(PATCH::class.java)?.value)
    }

    private fun assertDelete(method: java.lang.reflect.Method, path: String) {
        assertEquals(path, method.getAnnotation(DELETE::class.java)?.value)
    }

    private fun assertHeader(method: java.lang.reflect.Method, header: String) {
        assertTrue(
            method.parameterAnnotations
                .flatMap { annotations -> annotations.filterIsInstance<Header>() }
                .any { it.value == header }
        )
    }
}
