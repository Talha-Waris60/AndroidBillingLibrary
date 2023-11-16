package com.devdroiddev.googleinab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.SkuDetailsParams
import com.devdroiddev.googleinab.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), PurchasesUpdatedListener, BillingClientStateListener {

    companion object {
        const val APP_TAG = "Billing_App"
    }

    private lateinit var binding: ActivityMainBinding
    private var billingClient: BillingClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initListeners()
    }

    private fun init() {

        // Initialize Billing Client
        initializeBillingClient()
        //start the connection after initializing the billing client
        establishedConnection()
    }

    private fun initListeners() {

    }

    private fun initializeBillingClient() {

        // Initialize billing client
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()

    }

    private fun establishedConnection() {

        // Established a connection to google play
        billingClient?.startConnection(this) ?: 0
    }

    private fun showProducts() {

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("android.test.purchased")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        // QueryProductDetailsParams -> Retrieve information about products
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build() // pass the reference of product list

        // queryProductDetailsAsync
        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                // Iterate over each product
                for (productDetail in productDetailsList) {
                    // show the product details on UI and perform necessary functionality
                    // Boiler or sample code
                    if (productDetail.productId == "android.test.purchased") {
                        Log.d("testOffers", "$productDetail")
                        // Launch Purchase Flow
                        binding.purchaseProduct.setOnClickListener {
                            launchPurchaseFlow(productDetail)
                        }
                    }
                }
            } else {
                // Show the error
            }
        } ?: 1
    }

    private fun launchPurchaseFlow(productDetail: ProductDetails) {

        // Step 1: Create a list of product details parameters for the billing flow
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetail)
                .setOfferToken("Sample")  // Optional: Set an offer token
                .build()
        )

        // Step 2: Create billing flow parameters using the product details list
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // launch the billing flow
        // Note: The result of launchBillingFlow is a BillingResult indicating the success or failure of the flow
        val billingResult =
            billingClient?.launchBillingFlow(this@MainActivity, billingFlowParams) ?: 3
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        //  handle the purchases
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            // Iterate over each purchase and handle it
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private fun handlePurchase(purchase: Purchase) {

        // handle the purchase here
        if (purchase.purchaseState == PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val listener = AcknowledgePurchaseResponseListener { billingResult ->
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        // Acknowledgment successful, you can perform additional actions if needed
                    } else {
                        // Handle acknowledgment failure
                    }
                }
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams, listener)
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        // Retry the connection
        establishedConnection()
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingResponseCode.OK) {
            // Query for available products and show them
            showProducts()
            Log.d(APP_TAG, "Billing setup success. Response code: ${billingResult.responseCode}")
        } else {
            Log.d(APP_TAG, "Billing setup Failed. Response code: ${billingResult.responseCode}")
        }
    }

}