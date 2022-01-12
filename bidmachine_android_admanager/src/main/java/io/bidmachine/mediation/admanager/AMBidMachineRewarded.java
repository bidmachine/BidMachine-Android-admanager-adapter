package io.bidmachine.mediation.admanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.lang.ref.WeakReference;

import io.bidmachine.rewarded.RewardedListener;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.utils.BMError;

public class AMBidMachineRewarded
        extends AMBidMachineFullScreenAd<AMBidMachineRewarded, AMBidMachineRewardedListener, RewardedRequest> {

    private AdManagerLoadListener adManagerLoadListener;
    private AdManagerShowListener adManagerShowListener;
    private AdManagerRewardListener adManagerRewardListener;
    private RewardedAd adManagerRewardedAd;
    private io.bidmachine.rewarded.RewardedAd bidMachineRewardedAd;
    private BidMachineAdListener bidMachineAdListener;

    @SuppressLint("MissingPermission")
    public void load(@NonNull Context context,
                     @NonNull String adUnitId,
                     @NonNull AdManagerAdRequest.Builder adManagerAdRequestBuilder,
                     @Nullable RewardedRequest rewardedRequest) {
        setupRequest(context, adManagerAdRequestBuilder, rewardedRequest);

        adManagerLoadListener = new AdManagerLoadListener(this);
        RewardedAd.load(context,
                        adUnitId,
                        adManagerAdRequestBuilder.build(),
                        adManagerLoadListener);
    }

    @Override
    protected void loadBidMachine(@NonNull Context context, @NonNull RewardedRequest rewardedRequest) {
        bidMachineAdListener = new BidMachineAdListener(this);
        bidMachineRewardedAd = new io.bidmachine.rewarded.RewardedAd(context);
        bidMachineRewardedAd.setListener(bidMachineAdListener);
        bidMachineRewardedAd.load(rewardedRequest);
    }

    public void show(@NonNull Activity activity) {
        if (!isLoaded()) {
            sendOnFailToShow(AMBidMachineUtils.createAdError(AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR,
                                                             "Not found loaded ad object"));
            return;
        }

        if (bidMachineRewardedAd != null) {
            if (bidMachineRewardedAd.canShow()) {
                bidMachineRewardedAd.show();
            }
        } else if (adManagerRewardedAd != null) {
            adManagerShowListener = new AdManagerShowListener(this);
            adManagerRewardListener = new AdManagerRewardListener(this);
            adManagerRewardedAd.setFullScreenContentCallback(adManagerShowListener);
            adManagerRewardedAd.show(activity, adManagerRewardListener);
        } else {
            sendOnFailToShow(AMBidMachineUtils.createAdError(AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR,
                                                             "Not found loaded ad object"));
        }
    }

    @Override
    protected void destroyAdManagerAdObjects() {
        if (adManagerLoadListener != null) {
            adManagerLoadListener.destroy();
            adManagerLoadListener = null;
        }
        if (adManagerShowListener != null) {
            adManagerShowListener.destroy();
            adManagerShowListener = null;
        }
        if (adManagerRewardListener != null) {
            adManagerRewardListener.destroy();
            adManagerRewardListener = null;
        }
        adManagerRewardedAd = null;
    }

    @Override
    protected void destroyBidMachineAdObjects() {
        if (bidMachineAdListener != null) {
            bidMachineAdListener.destroy();
            bidMachineAdListener = null;
        }
        if (bidMachineRewardedAd != null) {
            bidMachineRewardedAd.destroy();
            bidMachineRewardedAd = null;
        }
    }

    private void sendOnReward(@NonNull RewardItem rewardItem) {
        AMBidMachineRewardedListener listener = getListener();
        if (listener != null) {
            listener.onAdRewarded(this, rewardItem);
        }
    }


    private static class AdManagerLoadListener extends RewardedAdLoadCallback {

        private WeakReference<AMBidMachineRewarded> weakAMBidMachineRewarded;

        public AdManagerLoadListener(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            weakAMBidMachineRewarded = new WeakReference<>(amBidMachineRewarded);
        }

        @Override
        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
            super.onAdLoaded(rewardedAd);

            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                if (AMBidMachineUtils.isBidMachineRewarded(rewardedAd)) {
                    amBidMachineAd.bidMachineWinMediation();
                } else {
                    amBidMachineAd.adManagerRewardedAd = rewardedAd;
                    amBidMachineAd.bidMachineLossMediation();
                }
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);

            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToLoad(loadAdError);
            }
        }

        @Nullable
        private AMBidMachineRewarded getAMBidMachineAd() {
            if (weakAMBidMachineRewarded == null) {
                return null;
            }
            return weakAMBidMachineRewarded.get();
        }

        private void destroy() {
            if (weakAMBidMachineRewarded != null) {
                weakAMBidMachineRewarded.clear();
                weakAMBidMachineRewarded = null;
            }
        }

    }

    private static class AdManagerShowListener extends FullScreenContentCallback {

        private WeakReference<AMBidMachineRewarded> weakAMBidMachineRewarded;

        public AdManagerShowListener(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            weakAMBidMachineRewarded = new WeakReference<>(amBidMachineRewarded);
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            super.onAdFailedToShowFullScreenContent(adError);

            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToShow(adError);
            }
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();

            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnShown();
            }
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();

            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClicked();
            }
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent();

            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClosed();
            }
        }

        @Nullable
        private AMBidMachineRewarded getAMBidMachineAd() {
            if (weakAMBidMachineRewarded == null) {
                return null;
            }
            return weakAMBidMachineRewarded.get();
        }

        private void destroy() {
            if (weakAMBidMachineRewarded != null) {
                weakAMBidMachineRewarded.clear();
                weakAMBidMachineRewarded = null;
            }
        }

    }

    private static class AdManagerRewardListener implements OnUserEarnedRewardListener {

        private WeakReference<AMBidMachineRewarded> weakAMBidMachineRewarded;

        public AdManagerRewardListener(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            weakAMBidMachineRewarded = new WeakReference<>(amBidMachineRewarded);
        }

        @Override
        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
            if (weakAMBidMachineRewarded == null) {
                return;
            }
            AMBidMachineRewarded amBidMachineRewarded = weakAMBidMachineRewarded.get();
            if (amBidMachineRewarded == null) {
                return;
            }
            amBidMachineRewarded.sendOnReward(rewardItem);
        }

        private void destroy() {
            if (weakAMBidMachineRewarded != null) {
                weakAMBidMachineRewarded.clear();
                weakAMBidMachineRewarded = null;
            }
        }

    }

    private static class BidMachineAdListener implements RewardedListener {

        private WeakReference<AMBidMachineRewarded> weakAMBidMachineRewarded;

        public BidMachineAdListener(@NonNull AMBidMachineRewarded amBidMachineRewarded) {
            weakAMBidMachineRewarded = new WeakReference<>(amBidMachineRewarded);
        }

        @Override
        public void onAdLoaded(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnLoad();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd, @NonNull BMError bmError) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToLoad(AMBidMachineUtils.toLoadAdError(bmError));
            }
        }

        @Override
        public void onAdShown(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {

        }

        @Override
        public void onAdShowFailed(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd, @NonNull BMError bmError) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToShow(AMBidMachineUtils.toAdError(bmError));
            }
        }

        @Override
        public void onAdImpression(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnShown();
            }
        }

        @Override
        public void onAdClicked(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClicked();
            }
        }

        @Override
        public void onAdClosed(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd, boolean finished) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClosed();
            }
        }

        @Override
        public void onAdRewarded(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnReward(RewardItem.DEFAULT_REWARD);
            }
        }

        @Override
        public void onAdExpired(@NonNull io.bidmachine.rewarded.RewardedAd rewardedAd) {
            AMBidMachineRewarded amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnExpired();
            }
        }

        @Nullable
        private AMBidMachineRewarded getAMBidMachineAd() {
            if (weakAMBidMachineRewarded == null) {
                return null;
            }
            return weakAMBidMachineRewarded.get();
        }

        private void destroy() {
            if (weakAMBidMachineRewarded != null) {
                weakAMBidMachineRewarded.clear();
                weakAMBidMachineRewarded = null;
            }
        }

    }

}