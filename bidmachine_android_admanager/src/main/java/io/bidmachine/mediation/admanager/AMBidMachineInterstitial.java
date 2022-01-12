package io.bidmachine.mediation.admanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.admanager.AppEventListener;

import java.lang.ref.WeakReference;

import io.bidmachine.core.Utils;
import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.interstitial.InterstitialListener;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.utils.BMError;

public class AMBidMachineInterstitial
        extends AMBidMachineFullScreenAd<AMBidMachineInterstitial, AMBidMachineInterstitialListener, InterstitialRequest> {

    private AdManagerLoadListener adManagerLoadListener;
    private AdManagerShowListener adManagerShowListener;
    private AdManagerInterstitialAd adManagerInterstitialAd;
    private InterstitialAd bidMachineInterstitialAd;
    private BidMachineAdListener bidMachineAdListener;

    @SuppressLint("MissingPermission")
    public void load(@NonNull Context context,
                     @NonNull String adUnitId,
                     @NonNull AdManagerAdRequest.Builder adManagerAdRequestBuilder,
                     @Nullable InterstitialRequest interstitialRequest) {
        boolean isBidMachineParticipatesInMediation = setupRequest(context,
                                                                   adManagerAdRequestBuilder,
                                                                   interstitialRequest);

        adManagerLoadListener = new AdManagerLoadListener(this, isBidMachineParticipatesInMediation);
        AdManagerInterstitialAd.load(context,
                                     adUnitId,
                                     adManagerAdRequestBuilder.build(),
                                     adManagerLoadListener);
    }

    @Override
    protected void loadBidMachine(@NonNull Context context, @NonNull InterstitialRequest interstitialRequest) {
        bidMachineAdListener = new BidMachineAdListener(this);
        bidMachineInterstitialAd = new InterstitialAd(context);
        bidMachineInterstitialAd.setListener(bidMachineAdListener);
        bidMachineInterstitialAd.load(interstitialRequest);
    }

    public void show(@NonNull Activity activity) {
        if (!isLoaded()) {
            sendOnFailToShow(AMBidMachineUtils.createAdError(AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR,
                                                             "Not found loaded ad object"));
            return;
        }

        if (bidMachineInterstitialAd != null) {
            if (bidMachineInterstitialAd.canShow()) {
                bidMachineInterstitialAd.show();
            }
        } else if (adManagerInterstitialAd != null) {
            adManagerShowListener = new AdManagerShowListener(this);
            adManagerInterstitialAd.setFullScreenContentCallback(adManagerShowListener);
            adManagerInterstitialAd.show(activity);
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
        adManagerInterstitialAd = null;
    }

    @Override
    protected void destroyBidMachineAdObjects() {
        if (bidMachineAdListener != null) {
            bidMachineAdListener.destroy();
            bidMachineAdListener = null;
        }
        if (bidMachineInterstitialAd != null) {
            bidMachineInterstitialAd.destroy();
            bidMachineInterstitialAd = null;
        }
    }


    private static class AdManagerLoadListener extends AdManagerInterstitialAdLoadCallback implements AppEventListener {

        private WeakReference<AMBidMachineInterstitial> weakAMBidMachineInterstitial;
        private OnAppEventDelayRunnable<AMBidMachineInterstitial> onAppEventDelayRunnable;

        private final boolean isBidMachineParticipatesInMediation;

        public AdManagerLoadListener(@NonNull AMBidMachineInterstitial amBidMachineInterstitial,
                                     boolean isBidMachineParticipatesInMediation) {
            weakAMBidMachineInterstitial = new WeakReference<>(amBidMachineInterstitial);
            onAppEventDelayRunnable = new OnAppEventDelayRunnable<>(weakAMBidMachineInterstitial);

            this.isBidMachineParticipatesInMediation = isBidMachineParticipatesInMediation;
        }

        @Override
        public void onAdLoaded(@NonNull AdManagerInterstitialAd adManagerInterstitialAd) {
            super.onAdLoaded(adManagerInterstitialAd);

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.adManagerInterstitialAd = adManagerInterstitialAd;

                if (isBidMachineParticipatesInMediation) {
                    adManagerInterstitialAd.setAppEventListener(this);
                    startWaitOnAppEvent(amBidMachineAd);
                } else {
                    amBidMachineAd.sendOnLoad();
                }
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToLoad(loadAdError);
            }
        }

        @Override
        public void onAppEvent(@NonNull String key, @NonNull String value) {
            stopWaitOnAppEvent();

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                if (AMBidMachineUtils.isBidMachineInterstitial(key)) {
                    amBidMachineAd.bidMachineWinMediation();
                } else {
                    amBidMachineAd.bidMachineLossMediation();
                }
            }
        }

        private void startWaitOnAppEvent(@NonNull AMBidMachineInterstitial amBidMachineAd) {
            if (onAppEventDelayRunnable != null) {
                Utils.onUiThread(onAppEventDelayRunnable, amBidMachineAd.getOnAppEventDelay());
            } else {
                amBidMachineAd.sendOnLoad();
            }
        }

        private void stopWaitOnAppEvent() {
            if (onAppEventDelayRunnable != null) {
                Utils.cancelUiThreadTask(onAppEventDelayRunnable);
                onAppEventDelayRunnable.destroy();
                onAppEventDelayRunnable = null;
            }
        }

        @Nullable
        private AMBidMachineInterstitial getAMBidMachineAd() {
            if (weakAMBidMachineInterstitial == null) {
                return null;
            }
            return weakAMBidMachineInterstitial.get();
        }

        private void destroy() {
            stopWaitOnAppEvent();

            if (weakAMBidMachineInterstitial != null) {
                weakAMBidMachineInterstitial.clear();
                weakAMBidMachineInterstitial = null;
            }
        }

    }

    private static class AdManagerShowListener extends FullScreenContentCallback {

        private WeakReference<AMBidMachineInterstitial> weakAMBidMachineInterstitial;

        public AdManagerShowListener(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            weakAMBidMachineInterstitial = new WeakReference<>(amBidMachineInterstitial);
        }

        @Override
        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
            super.onAdFailedToShowFullScreenContent(adError);

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToShow(adError);
            }
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnShown();
            }
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClicked();
            }
        }

        @Override
        public void onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent();

            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClosed();
            }
        }

        @Nullable
        private AMBidMachineInterstitial getAMBidMachineAd() {
            if (weakAMBidMachineInterstitial == null) {
                return null;
            }
            return weakAMBidMachineInterstitial.get();
        }

        private void destroy() {
            if (weakAMBidMachineInterstitial != null) {
                weakAMBidMachineInterstitial.clear();
                weakAMBidMachineInterstitial = null;
            }
        }

    }

    private static class BidMachineAdListener implements InterstitialListener {

        private WeakReference<AMBidMachineInterstitial> weakAMBidMachineInterstitial;

        public BidMachineAdListener(@NonNull AMBidMachineInterstitial amBidMachineInterstitial) {
            weakAMBidMachineInterstitial = new WeakReference<>(amBidMachineInterstitial);
        }

        @Override
        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnLoad();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull InterstitialAd interstitialAd, @NonNull BMError bmError) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToLoad(AMBidMachineUtils.toLoadAdError(bmError));
            }
        }

        @Override
        public void onAdShown(@NonNull InterstitialAd interstitialAd) {

        }

        @Override
        public void onAdShowFailed(@NonNull InterstitialAd interstitialAd, @NonNull BMError bmError) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToShow(AMBidMachineUtils.toAdError(bmError));
            }
        }

        @Override
        public void onAdImpression(@NonNull InterstitialAd interstitialAd) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnShown();
            }
        }

        @Override
        public void onAdClicked(@NonNull InterstitialAd interstitialAd) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClicked();
            }
        }

        @Override
        public void onAdClosed(@NonNull InterstitialAd interstitialAd, boolean finished) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClosed();
            }
        }

        @Override
        public void onAdExpired(@NonNull InterstitialAd interstitialAd) {
            AMBidMachineInterstitial amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnExpired();
            }
        }

        @Nullable
        private AMBidMachineInterstitial getAMBidMachineAd() {
            if (weakAMBidMachineInterstitial == null) {
                return null;
            }
            return weakAMBidMachineInterstitial.get();
        }

        private void destroy() {
            if (weakAMBidMachineInterstitial != null) {
                weakAMBidMachineInterstitial.clear();
                weakAMBidMachineInterstitial = null;
            }
        }

    }

}