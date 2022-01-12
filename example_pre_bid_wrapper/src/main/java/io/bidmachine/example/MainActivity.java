package io.bidmachine.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.rewarded.RewardItem;

import io.bidmachine.BidMachine;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.example.databinding.ActivityMainBinding;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.mediation.admanager.AMBidMachineBanner;
import io.bidmachine.mediation.admanager.AMBidMachineBannerListener;
import io.bidmachine.mediation.admanager.AMBidMachineInterstitial;
import io.bidmachine.mediation.admanager.AMBidMachineInterstitialListener;
import io.bidmachine.mediation.admanager.AMBidMachineRewarded;
import io.bidmachine.mediation.admanager.AMBidMachineRewardedListener;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.utils.BMError;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BID_MACHINE_SELLER_ID = "5";
    private static final String BANNER_ID = "YOUR_BANNER_ID";
    private static final String MREC_ID = "YOUR_MREC_ID";
    private static final String INTERSTITIAL_ID = "YOUR_INTERSTITIAL_ID";
    private static final String REWARDED_ID = "YOUR_REWARDED_ID";

    private ActivityMainBinding binding;

    private BannerRequest bannerRequest;
    private AMBidMachineBanner amBidMachineBanner;

    private BannerRequest mrecRequest;
    private AMBidMachineBanner amBidMachineMrec;

    private InterstitialRequest interstitialRequest;
    private AMBidMachineInterstitial amBidMachineInterstitial;

    private RewardedRequest rewardedRequest;
    private AMBidMachineRewarded amBidMachineRewarded;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bInitialize.setOnClickListener(v -> initialize());
        binding.bLoadBanner.setOnClickListener(v -> loadBanner());
        binding.bShowBanner.setOnClickListener(v -> showBanner());
        binding.bLoadMrec.setOnClickListener(v -> loadMrec());
        binding.bShowMrec.setOnClickListener(v -> showMrec());
        binding.bLoadInterstitial.setOnClickListener(v -> loadInterstitial());
        binding.bShowInterstitial.setOnClickListener(v -> showInterstitial());
        binding.bLoadRewarded.setOnClickListener(v -> loadRewarded());
        binding.bShowRewarded.setOnClickListener(v -> showRewarded());

        if (BidMachine.isInitialized()) {
            enableButton();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        destroyBanner();
        destroyMrec();
        destroyInterstitial();
        destroyRewarded();
    }

    private void initialize() {
        // Initialize BidMachine SDK first
        BidMachine.setTestMode(true);
        BidMachine.setLoggingEnabled(true);
        BidMachine.initialize(this, BID_MACHINE_SELLER_ID);

        MobileAds.initialize(this);

        enableButton();
    }

    private void enableButton() {
        binding.bInitialize.setEnabled(false);

        binding.bLoadBanner.setEnabled(true);
        binding.bLoadMrec.setEnabled(true);
        binding.bLoadInterstitial.setEnabled(true);
        binding.bLoadRewarded.setEnabled(true);
    }

    private void addAdView(View view) {
        binding.adContainer.removeAllViews();
        binding.adContainer.addView(view);
    }

    /**
     * Method for load BannerRequest
     */
    private void loadBanner() {
        binding.bShowBanner.setEnabled(false);

        // Destroy previous ad
        destroyBanner();

        Log.d(TAG, "loadBanner");

        // Create new BidMachine request
        bannerRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_320x50)
                .setListener(new BannerRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        bannerRequest.request(this);
    }

    /**
     * Method for load AdManagerAdView
     */
    private void loadAdManagerBanner(@Nullable BannerRequest bannerRequest) {
        Log.d(TAG, "loadAdManagerBanner");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Create new AdView instance and load it
        AdManagerAdView adManagerAdView = new AdManagerAdView(this);
        adManagerAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.MATCH_PARENT));
        adManagerAdView.setAdUnitId(BANNER_ID);
        adManagerAdView.setAdSize(AdSize.BANNER);

        amBidMachineBanner = new AMBidMachineBanner();
        amBidMachineBanner.setListener(new BannerListener());
        amBidMachineBanner.load(this, adManagerAdView, adRequestBuilder, bannerRequest);
    }

    /**
     * Method for show BidMachine BannerView
     */
    private void showBanner() {
        Log.d(TAG, "showBanner");

        binding.bShowBanner.setEnabled(false);

        // Get loaded ad view
        View adView = amBidMachineBanner != null && amBidMachineBanner.isLoaded()
                ? amBidMachineBanner.getAdView()
                : null;
        if (adView != null) {
            addAdView(adView);
        } else {
            Log.d(TAG, "show error - banner object is null");
        }
    }

    /**
     * Method for destroy banner
     */
    private void destroyBanner() {
        Log.d(TAG, "destroyBanner");

        binding.adContainer.removeAllViews();
        if (amBidMachineBanner != null) {
            amBidMachineBanner.destroy();
            amBidMachineBanner = null;
        }
        if (bannerRequest != null) {
            bannerRequest = null;
        }
    }

    /**
     * Method for load BannerRequest
     */
    private void loadMrec() {
        binding.bShowMrec.setEnabled(false);

        // Destroy previous ad
        destroyMrec();

        Log.d(TAG, "loadMrec");

        // Create new BidMachine request
        mrecRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_300x250)
                .setListener(new MrecRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        mrecRequest.request(this);
    }

    /**
     * Method for load AdManagerAdView
     */
    private void loadAdManagerMrec(@Nullable BannerRequest mrecRequest) {
        Log.d(TAG, "loadAdManagerMrec");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        // Create new AdView instance and load it
        AdManagerAdView adManagerAdView = new AdManagerAdView(this);
        adManagerAdView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                   ViewGroup.LayoutParams.MATCH_PARENT));
        adManagerAdView.setAdUnitId(MREC_ID);
        adManagerAdView.setAdSizes(AdSize.MEDIUM_RECTANGLE);

        amBidMachineMrec = new AMBidMachineBanner();
        amBidMachineMrec.setListener(new MrecListener());
        amBidMachineMrec.load(this, adManagerAdView, adRequestBuilder, mrecRequest);
    }

    /**
     * Method for show BidMachine BannerView
     */
    private void showMrec() {
        Log.d(TAG, "showMrec");

        binding.bShowMrec.setEnabled(false);

        // Get loaded ad view
        View adView = amBidMachineMrec != null && amBidMachineMrec.isLoaded()
                ? amBidMachineMrec.getAdView()
                : null;
        if (adView != null) {
            addAdView(adView);
        } else {
            Log.d(TAG, "show error - mrec object is null");
        }
    }

    /**
     * Method for destroy mrec
     */
    private void destroyMrec() {
        Log.d(TAG, "destroyMrec");

        binding.adContainer.removeAllViews();
        if (amBidMachineMrec != null) {
            amBidMachineMrec.destroy();
            amBidMachineMrec = null;
        }
        if (mrecRequest != null) {
            mrecRequest = null;
        }
    }

    /**
     * Method for load InterstitialRequest
     */
    private void loadInterstitial() {
        binding.bShowInterstitial.setEnabled(false);

        // Destroy previous ad
        destroyInterstitial();

        Log.d(TAG, "loadInterstitial");

        // Create new BidMachine request
        interstitialRequest = new InterstitialRequest.Builder()
                .setListener(new InterstitialRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        interstitialRequest.request(this);
    }

    /**
     * Method for load AdManagerInterstitialAd
     */
    private void loadAdManagerInterstitial(@Nullable InterstitialRequest interstitialRequest) {
        Log.d(TAG, "loadAdManagerInterstitial");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        amBidMachineInterstitial = new AMBidMachineInterstitial();
        amBidMachineInterstitial.setListener(new InterstitialListener());
        amBidMachineInterstitial.load(this, INTERSTITIAL_ID, adRequestBuilder, interstitialRequest);
    }

    /**
     * Method for show BidMachine InterstitialAd
     */
    private void showInterstitial() {
        Log.d(TAG, "showInterstitial");

        binding.bShowInterstitial.setEnabled(false);

        // Check if an ad can be shown before actual impression
        if (amBidMachineInterstitial != null && amBidMachineInterstitial.isLoaded()) {
            amBidMachineInterstitial.show(this);
        } else {
            Log.d(TAG, "show error - interstitial object not loaded");
        }
    }

    /**
     * Method for destroy interstitial
     */
    private void destroyInterstitial() {
        Log.d(TAG, "destroyInterstitial");

        if (amBidMachineInterstitial != null) {
            amBidMachineInterstitial.destroy();
            amBidMachineInterstitial = null;
        }
        if (interstitialRequest != null) {
            interstitialRequest = null;
        }
    }

    /**
     * Method for load RewardedRequest
     */
    private void loadRewarded() {
        binding.bShowRewarded.setEnabled(false);

        // Destroy previous ad
        destroyRewarded();

        Log.d(TAG, "loadRewarded");

        // Create new BidMachine request
        rewardedRequest = new RewardedRequest.Builder()
                .setListener(new RewardedRequestListener())
                .build();

        // Request an ad from BidMachine without loading it
        rewardedRequest.request(this);
    }

    /**
     * Method for load AdManager RewardedAd
     */
    private void loadAdManagerRewarded(@Nullable RewardedRequest rewardedRequest) {
        Log.d(TAG, "loadAdManagerRewarded");

        // Create AdManagerAdRequest builder
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        amBidMachineRewarded = new AMBidMachineRewarded();
        amBidMachineRewarded.setListener(new RewardedListener());
        amBidMachineRewarded.load(this, REWARDED_ID, adRequestBuilder, rewardedRequest);
    }

    /**
     * Method for show BidMachine RewardedAd
     */
    private void showRewarded() {
        Log.d(TAG, "showRewarded");

        binding.bShowRewarded.setEnabled(false);

        // Check if an ad can be shown before actual impression
        if (amBidMachineRewarded != null && amBidMachineRewarded.isLoaded()) {
            amBidMachineRewarded.show(this);
        } else {
            Log.d(TAG, "show error - rewarded object not loaded");
        }
    }

    /**
     * Method for destroy rewarded ad
     */
    private void destroyRewarded() {
        Log.d(TAG, "destroyRewarded");

        if (amBidMachineRewarded != null) {
            amBidMachineRewarded.destroy();
            amBidMachineRewarded = null;
        }
        if (rewardedRequest != null) {
            rewardedRequest = null;
        }
    }


    private class BannerRequestListener implements BannerRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull BannerRequest bannerRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "BannerRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerBanner(bannerRequest));
        }

        @Override
        public void onRequestFailed(@NonNull BannerRequest bannerRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("BannerRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerBanner(null));
        }

        @Override
        public void onRequestExpired(@NonNull BannerRequest bannerRequest) {
            // ignore
        }

    }

    private class BannerListener implements AMBidMachineBannerListener {

        @Override
        public void onAdLoaded(@NonNull AMBidMachineBanner amBidMachineBanner) {
            binding.bShowBanner.setEnabled(true);

            Log.d(TAG, "BannerListener - onAdLoaded");
            Toast.makeText(MainActivity.this, "BannerLoaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdFailToLoad(@NonNull AMBidMachineBanner amBidMachineBanner, @NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("BannerListener - onAdFailToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this, "BannerFailedToLoad", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdShown(@NonNull AMBidMachineBanner amBidMachineBanner) {
            Log.d(TAG, "BannerListener - onAdShown");
        }

        @Override
        public void onAdClicked(@NonNull AMBidMachineBanner amBidMachineBanner) {
            Log.d(TAG, "BannerListener - onAdClicked");
        }

        @Override
        public void onAdExpired(@NonNull AMBidMachineBanner amBidMachineBanner) {
            Log.d(TAG, "BannerListener - onAdExpired");
        }

    }

    private class MrecRequestListener implements BannerRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull BannerRequest bannerRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "MrecRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerMrec(bannerRequest));
        }

        @Override
        public void onRequestFailed(@NonNull BannerRequest bannerRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("MrecRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerMrec(null));
        }

        @Override
        public void onRequestExpired(@NonNull BannerRequest bannerRequest) {
            // ignore
        }

    }

    private class MrecListener implements AMBidMachineBannerListener {

        @Override
        public void onAdLoaded(@NonNull AMBidMachineBanner amBidMachineBanner) {
            binding.bShowMrec.setEnabled(true);

            Log.d(TAG, "MrecListener - onAdLoaded");
            Toast.makeText(MainActivity.this, "MrecLoaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdFailToLoad(@NonNull AMBidMachineBanner amBidMachineBanner, @NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("MrecListener - onAdFailToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this, "MrecFailedToLoad", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdShown(@NonNull AMBidMachineBanner amBidMachineBanner) {
            Log.d(TAG, "MrecListener - onAdShown");
        }

        @Override
        public void onAdClicked(@NonNull AMBidMachineBanner amBidMachineBanner) {
            Log.d(TAG, "MrecListener - onAdClicked");
        }

        @Override
        public void onAdExpired(@NonNull AMBidMachineBanner amBidMachineBanner) {
            Log.d(TAG, "MrecListener - onAdExpired");
        }

    }

    private class InterstitialRequestListener implements InterstitialRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull InterstitialRequest interstitialRequest,
                                     @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "InterstitialRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerInterstitial(interstitialRequest));
        }

        @Override
        public void onRequestFailed(@NonNull InterstitialRequest interstitialRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("InterstitialRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerInterstitial(null));
        }

        @Override
        public void onRequestExpired(@NonNull InterstitialRequest interstitialRequest) {
            // ignore
        }

    }

    private class InterstitialListener implements AMBidMachineInterstitialListener {

        @Override
        public void onAdLoaded(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            binding.bShowInterstitial.setEnabled(true);

            Log.d(TAG, "InterstitialListener - onAdLoaded");
            Toast.makeText(MainActivity.this, "InterstitialLoaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdFailToLoad(@NonNull AMBidMachineInterstitial amBidMachineInterstitial,
                                   @NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("InterstitialListener - onAdFailToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this, "InterstitialFailedToLoad", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdShown(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            Log.d(TAG, "InterstitialListener - onAdShown");
        }

        @Override
        public void onAdFailToShow(@NonNull AMBidMachineInterstitial amBidMachineInterstitial,
                                   @NonNull AdError adError) {
            Log.d(TAG,
                  String.format("InterstitialListener - onAdFailToShow with message: %s",
                                adError.getMessage()));
            Toast.makeText(MainActivity.this, "InterstitialFailedToShow", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdClicked(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            Log.d(TAG, "InterstitialListener - onAdClicked");
        }

        @Override
        public void onAdClosed(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            Log.d(TAG, "InterstitialListener - onAdClosed");
        }

        @Override
        public void onAdExpired(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            Log.d(TAG, "InterstitialListener - onAdExpired");
        }

    }

    private class RewardedRequestListener implements RewardedRequest.AdRequestListener {

        @Override
        public void onRequestSuccess(@NonNull RewardedRequest rewardedRequest, @NonNull AuctionResult auctionResult) {
            Log.d(TAG, "RewardedRequestListener - onRequestSuccess");

            runOnUiThread(() -> loadAdManagerRewarded(rewardedRequest));
        }

        @Override
        public void onRequestFailed(@NonNull RewardedRequest rewardedRequest, @NonNull BMError bmError) {
            Log.d(TAG,
                  String.format("RewardedRequestListener - onRequestFailed with message: %s",
                                bmError.getMessage()));

            runOnUiThread(() -> loadAdManagerRewarded(null));
        }

        @Override
        public void onRequestExpired(@NonNull RewardedRequest rewardedRequest) {
            // ignore
        }

    }

    private class RewardedListener implements AMBidMachineRewardedListener {

        @Override
        public void onAdLoaded(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            binding.bShowRewarded.setEnabled(true);

            Log.d(TAG, "RewardedListener - onAdLoaded");
            Toast.makeText(MainActivity.this, "RewardedLoaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdFailToLoad(@NonNull AMBidMachineRewarded amBidMachineRewarded,
                                   @NonNull LoadAdError loadAdError) {
            Log.d(TAG,
                  String.format("RewardedListener - onAdFailToLoad with message: %s",
                                loadAdError.getMessage()));
            Toast.makeText(MainActivity.this, "RewardedFailedToLoad", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdShown(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            Log.d(TAG, "RewardedListener - onAdShown");
        }

        @Override
        public void onAdFailToShow(@NonNull AMBidMachineRewarded amBidMachineRewarded, @NonNull AdError adError) {
            Log.d(TAG,
                  String.format("RewardedListener - onAdFailToShow with message: %s",
                                adError.getMessage()));
            Toast.makeText(MainActivity.this, "RewardedFailedToShow", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdClicked(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            Log.d(TAG, "RewardedListener - onAdClicked");
        }

        @Override
        public void onAdClosed(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            Log.d(TAG, "RewardedListener - onAdClosed");
        }

        @Override
        public void onAdRewarded(@NonNull AMBidMachineRewarded amBidMachineRewarded, @NonNull RewardItem rewardItem) {
            Log.d(TAG, "RewardedListener - onAdRewarded");
        }

        @Override
        public void onAdExpired(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            Log.d(TAG, "RewardedListener - onAdExpired");
        }

    }

}