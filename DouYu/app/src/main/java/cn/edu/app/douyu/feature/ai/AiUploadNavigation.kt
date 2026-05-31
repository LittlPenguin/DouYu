package cn.edu.app.douyu.feature.ai

import cn.edu.app.douyu.core.navigation.AppRoute

internal fun canStartAiUpload(isLoggedIn: Boolean, hasSelectedImage: Boolean): Boolean =
    isLoggedIn && hasSelectedImage

internal fun aiUploadLoginRoute(): String = AppRoute.login(AppRoute.IMAGE_SELECT)

internal fun aiParamsRouteOrNull(uploadedFileId: String?): String? =
    uploadedFileId?.let(AppRoute::aiParams)
