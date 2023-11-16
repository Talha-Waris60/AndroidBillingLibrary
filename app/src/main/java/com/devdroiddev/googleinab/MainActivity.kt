package com.devdroiddev.googleinab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.SkuDetailsParams
import com.devdroiddev.googleinab.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private var billingClient : BillingClient? = null
    private lateinit var skuList: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add products to arraylist
        skuList = ArrayList()
        skuList.add("android.test.purchased") // for testing purpose
        init()
        //setUpListeners()
    }

    private fun init() {

        // Initialize PurchaseUpdateListener
      /*  val purchaseUpdateListener = PurchasesUpdatedListener{
            billingResult, purchases ->
        }

        // Initialize Billing Client
        billingClient = BillingClient.newBuilder(this)  // pass any context Billing client take it as application context
            .setListener(purchaseUpdateListener)  // pass a reference of purchaseUpdateListener to setListener() to received updates on purchases
            .enablePendingPurchases()
            .build()*/

        // Initialize Billing Client
        initializeBillingClient()
    }

    private fun initializeBillingClient() {

        // Purchase Update Listener
        val purchaseUpdateListener = PurchasesUpdatedListener{
                billingResult, purchases ->
        }

        // Initialize billing client
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(purchaseUpdateListener)
            .build()

        //start the connection after initializing the billing client
        establishedConnection()
    }

    private fun establishedConnection() {

        // Established a connection to google play
        binding.purchaseProduct.setOnClickListener {

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {

                    // Try to restart the connection
                    establishedConnection()
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingResponseCode.OK) {
                        // Query for available products and show them
                        showProducts()
                    }
                }

            }) ?: 0
        }
    }

    private fun showProducts() {

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("product_id_example")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        // QueryProductDetailsParams -> Retrieve information about products
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build() // pass the reference of product list

        // queryProductDetailsAsync
        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult, productDetailsList ->

            // Iterate over each product
            for (productDetail in productDetailsList) {
                // show the product details on UI and perform necessary functionality
                // Boiler or sample code
                if (productDetail.productId == "product_id_example") {
                    Log.d("testOffers", "$productDetail")
                    // Launch Purchase Flow
                    launchPurchaseFlow(productDetail)
                }
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
        val billingResult = billingClient?.launchBillingFlow(this@MainActivity, billingFlowParams) ?: 3
    }


    /*private fun setUpListeners() {

        // This code established a connection to google play on button click
        binding.purchaseProduct.setOnClickListener {

            // Connect to Google Play
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    *//*  Try to restart the connection on the next request to Google Play by calling the startConnection() method. *//*
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query for purchases and in-app product details.
                        val params = SkuDetailsParams.newBuilder()
                        params.setSkusList(skuList)
                            .setType(BillingClient.SkuType.INAPP)

                        // To query for in-app product details, call queryProductDetailsAsync().
                        billingClient?.querySkuDetailsAsync(params.build()) {
                                billingResult, skuDetailsList ->

                            // Launch the purchase flow
                            for (skuDetails in skuDetailsList!!) {
                                val purchaseFlow = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()

                                // Launch the billing flow
                                val billingResult = billingClient?.launchBillingFlow(this@MainActivity, purchaseFlow)?.responseCode
                            }

                        }
                    }
                }

            })
        }

    }*/
}