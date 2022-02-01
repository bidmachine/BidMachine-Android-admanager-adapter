package io.bidmachine.mediation.admanager;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;

import java.util.Map;

import io.bidmachine.BidMachineFetcher;
import io.bidmachine.utils.BMError;

public class AMBidMachineUtils {

    private static final String ERROR_DOMAIN = "io.bidmachine.mediation.admanager";
    private static final String ON_APP_EVENT_BANNER_KEY = "bidmachine-banner";
    private static final String ON_APP_EVENT_INTERSTITIAL_KEY = "bidmachine-interstitial";
    private static final String AD_METADATA_REWARDED_KEY = "bidmachine-rewarded";
    private static final String AD_METADATA_KEY_AD_TITLE = "AdTitle";

    /**
     * {@link AdManagerAdRequest} creation method based on {@link io.bidmachine.AdRequest}
     *
     * @param adRequest - loaded {@link io.bidmachine.AdRequest}
     * @return {@link AdManagerAdRequest} ready to loading
     */
    @NonNull
    public static AdManagerAdRequest createAdManagerRequest(@NonNull io.bidmachine.AdRequest<?, ?, ?> adRequest) {
        return createAdManagerRequestBuilder(adRequest).build();
    }

    /**
     * {@link AdManagerAdRequest.Builder} creation method based on {@link io.bidmachine.AdRequest}
     *
     * @param adRequest - loaded {@link io.bidmachine.AdRequest}
     * @return {@link AdManagerAdRequest.Builder} ready to building and loading
     */
    @NonNull
    public static AdManagerAdRequest.Builder createAdManagerRequestBuilder(@NonNull io.bidmachine.AdRequest<?, ?, ?> adRequest) {
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();
        appendRequest(adRequestBuilder, adRequest);
        return adRequestBuilder;
    }

    /**
     * Fill {@link AdManagerAdRequest.Builder} by loaded {@link io.bidmachine.AdRequest}
     *
     * @param builder   - {@link AdManagerAdRequest.Builder} that will be built and loaded
     * @param adRequest - loaded {@link io.bidmachine.AdRequest}
     */
    public static void appendRequest(@NonNull AdManagerAdRequest.Builder builder,
                                     @NonNull io.bidmachine.AdRequest<?, ?, ?> adRequest) {
        appendCustomTargeting(builder, adRequest);
    }

    /**
     * Append custom targeting to {@link AdManagerAdRequest.Builder}
     * from loaded {@link io.bidmachine.AdRequest}
     *
     * @param builder   - {@link AdManagerAdRequest.Builder} that will be built and loaded
     * @param adRequest - loaded {@link io.bidmachine.AdRequest}
     */
    public static void appendCustomTargeting(@NonNull AdManagerAdRequest.Builder builder,
                                             @NonNull io.bidmachine.AdRequest<?, ?, ?> adRequest) {
        appendCustomTargeting(builder, BidMachineFetcher.toMap(adRequest));
    }

    /**
     * Append custom targeting to {@link AdManagerAdRequest.Builder}
     * from loaded {@link io.bidmachine.AdRequest}
     *
     * @param builder - {@link AdManagerAdRequest.Builder} that will be built and loaded
     * @param map     - parameters to be added as custom targeting
     */
    public static void appendCustomTargeting(@NonNull AdManagerAdRequest.Builder builder,
                                             @NonNull Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.addCustomTargeting(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Checking whether it is BidMachine creative or not
     *
     * @param key - key from creative
     * @return is BidMachine creative or not
     */
    public static boolean isBidMachineBanner(@Nullable String key) {
        return TextUtils.equals(ON_APP_EVENT_BANNER_KEY, key);
    }

    /**
     * Checking whether it is BidMachine creative or not
     *
     * @param key - key from creative
     * @return is BidMachine creative or not
     */
    public static boolean isBidMachineInterstitial(@Nullable String key) {
        return TextUtils.equals(ON_APP_EVENT_INTERSTITIAL_KEY, key);
    }

    /**
     * Checking whether it is BidMachine creative or not
     *
     * @param rewardedAd - loaded ad object
     * @return is BidMachine creative or not
     */
    public static boolean isBidMachineRewarded(@NonNull RewardedAd rewardedAd) {
        String key = rewardedAd.getAdMetadata().getString(AD_METADATA_KEY_AD_TITLE);
        return TextUtils.equals(AD_METADATA_REWARDED_KEY, key);
    }


    @NonNull
    static LoadAdError createLoadAdError(int code, String message) {
        return new LoadAdError(code, message, ERROR_DOMAIN, null, null);
    }

    @NonNull
    static AdError createAdError(int code, String message) {
        return new AdError(code, message, ERROR_DOMAIN);
    }

    @NonNull
    static LoadAdError toLoadAdError(@NonNull BMError bmError) {
        return createLoadAdError(transform(bmError.getCode()), bmError.getMessage());
    }

    @NonNull
    static AdError toAdError(@NonNull BMError bmError) {
        return createAdError(transform(bmError.getCode()), bmError.getMessage());
    }

    private static int transform(int bmErrorCode) {
        switch (bmErrorCode) {
            case BMError.NO_CONTENT:
                return AdManagerAdRequest.ERROR_CODE_NO_FILL;
            case BMError.SERVER:
            case BMError.TIMEOUT:
            case BMError.NO_CONNECTION:
                return AdManagerAdRequest.ERROR_CODE_NETWORK_ERROR;
            case BMError.HTTP_BAD_REQUEST:
            case BMError.BAD_CONTENT:
                return AdManagerAdRequest.ERROR_CODE_INVALID_REQUEST;
            default:
                return AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR;
        }
    }

}