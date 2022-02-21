package com.sstechcanada.todo.activities

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener

object BillingClientSetup {
    private val instance: BillingClient? = null
    fun getInstance(context: Context, listener: PurchasesUpdatedListener): BillingClient {
        return instance ?: setupBillingClient(context, listener)
    }

    private fun setupBillingClient(
        context: Context,
        listener: PurchasesUpdatedListener
    ): BillingClient {
        return BillingClient.newBuilder(context)
            .setListener(listener)
            .enablePendingPurchases()
            .build()
    }
}