package cn.edu.app.douyu.core.payment

import cn.edu.app.douyu.core.model.PaymentChannel

data class PaymentLaunchRequest(
    val paymentId: String,
    val orderId: String,
    val channel: PaymentChannel
)

sealed interface PaymentLaunchResult {
    data object Confirming : PaymentLaunchResult
    data class Failed(val reason: String) : PaymentLaunchResult
}

interface PaymentLauncher {
    fun launch(request: PaymentLaunchRequest): PaymentLaunchResult
}

class MockPaymentLauncher : PaymentLauncher {
    override fun launch(request: PaymentLaunchRequest): PaymentLaunchResult = PaymentLaunchResult.Confirming
}
