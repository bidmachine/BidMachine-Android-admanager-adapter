package io.bidmachine.mediation.admanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AppEventListener;

import java.lang.ref.WeakReference;

import io.bidmachine.banner.BannerListener;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerView;
import io.bidmachine.core.Utils;
import io.bidmachine.utils.BMError;

public class AMBidMachineBanner extends AMBidMachineAd<AMBidMachineBanner, AMBidMachineBannerListener, BannerRequest> {

    private AdManagerAdView adManagerAdView;
    private AdManagerListenerWrapper adManagerListenerWrapper;
    private BannerView bidMachineBannerView;
    private BidMachineAdListener bidMachineAdListener;

    @SuppressLint("MissingPermission")
    public void load(@NonNull Context context,
                     @NonNull AdManagerAdView adManagerAdView,
                     @NonNull AdManagerAdRequest.Builder adManagerAdRequestBuilder,
                     @Nullable BannerRequest bannerRequest) {
        boolean isBidMachineParticipatesInMediation = setupRequest(context, adManagerAdRequestBuilder, bannerRequest);

        this.adManagerAdView = adManagerAdView;
        this.adManagerListenerWrapper = new AdManagerListenerWrapper(this,
                                                                     adManagerAdView.getAdListener(),
                                                                     adManagerAdView.getAppEventListener(),
                                                                     isBidMachineParticipatesInMediation);
        adManagerAdView.setAdListener(adManagerListenerWrapper);
        adManagerAdView.setAppEventListener(adManagerListenerWrapper);
        adManagerAdView.loadAd(adManagerAdRequestBuilder.build());
    }

    @Override
    protected void loadBidMachine(@NonNull Context context, @NonNull BannerRequest bannerRequest) {
        bidMachineAdListener = new BidMachineAdListener(this);
        bidMachineBannerView = new BannerView(context);
        bidMachineBannerView.setListener(bidMachineAdListener);
        bidMachineBannerView.load(bannerRequest);
    }

    @Nullable
    public View getAdView() {
        if (!isLoaded()) {
            return null;
        }
        if (bidMachineBannerView != null) {
            if (bidMachineBannerView.canShow()) {
                return bidMachineBannerView;
            }
        } else {
            return adManagerAdView;
        }
        return null;
    }

    @Override
    protected void destroyAdManagerAdObjects() {
        if (adManagerListenerWrapper != null) {
            adManagerListenerWrapper.destroy();
            adManagerListenerWrapper = null;
        }
        if (adManagerAdView != null) {
            adManagerAdView.destroy();
            adManagerAdView = null;
        }
    }

    @Override
    protected void destroyBidMachineAdObjects() {
        if (bidMachineAdListener != null) {
            bidMachineAdListener.destroy();
            bidMachineAdListener = null;
        }
        if (bidMachineBannerView != null) {
            bidMachineBannerView.destroy();
            bidMachineBannerView = null;
        }
    }


    private static class AdManagerListenerWrapper extends AdListener implements AppEventListener {

        private WeakReference<AMBidMachineBanner> weakAMBidMachineBanner;
        private AdListener adListener;
        private AppEventListener appEventListener;
        private OnAppEventDelayRunnable<AMBidMachineBanner> onAppEventDelayRunnable;

        private final boolean isBidMachineParticipatesInMediation;

        public AdManagerListenerWrapper(@NonNull AMBidMachineBanner amBidMachineBanner,
                                        @Nullable AdListener adListener,
                                        @Nullable AppEventListener appEventListener,
                                        boolean isBidMachineParticipatesInMediation) {
            this.weakAMBidMachineBanner = new WeakReference<>(amBidMachineBanner);
            this.adListener = adListener;
            this.appEventListener = appEventListener;
            this.onAppEventDelayRunnable = new OnAppEventDelayRunnable<>(weakAMBidMachineBanner);

            this.isBidMachineParticipatesInMediation = isBidMachineParticipatesInMediation;
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

            if (adListener != null) {
                adListener.onAdLoaded();
            }
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                if (isBidMachineParticipatesInMediation) {
                    startWaitOnAppEvent(amBidMachineAd);
                } else {
                    amBidMachineAd.sendOnLoad();
                }
            }
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);

            if (adListener != null) {
                adListener.onAdFailedToLoad(loadAdError);
            }
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToLoad(loadAdError);
            }
        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();

            if (adListener != null) {
                adListener.onAdOpened();
            }
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();

            if (adListener != null) {
                adListener.onAdImpression();
            }
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnShown();
            }
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();

            if (adListener != null) {
                adListener.onAdClicked();
            }
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClicked();
            }
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();

            if (adListener != null) {
                adListener.onAdClosed();
            }
        }

        @Override
        public void onAppEvent(@NonNull String key, @NonNull String value) {
            if (appEventListener != null) {
                appEventListener.onAppEvent(key, value);
            }
            stopWaitOnAppEvent();

            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                if (AMBidMachineUtils.isBidMachineBanner(key)) {
                    amBidMachineAd.bidMachineWinMediation();
                } else {
                    amBidMachineAd.bidMachineLossMediation();
                }
            }
        }

        private void startWaitOnAppEvent(@NonNull AMBidMachineBanner amBidMachineAd) {
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
        private AMBidMachineBanner getAMBidMachineAd() {
            if (weakAMBidMachineBanner == null) {
                return null;
            }
            return weakAMBidMachineBanner.get();
        }

        private void destroy() {
            stopWaitOnAppEvent();

            adListener = null;
            appEventListener = null;
            if (weakAMBidMachineBanner != null) {
                weakAMBidMachineBanner.clear();
                weakAMBidMachineBanner = null;
            }
        }

    }

    private static class BidMachineAdListener implements BannerListener {

        private WeakReference<AMBidMachineBanner> weakAMBidMachineBanner;

        public BidMachineAdListener(@NonNull AMBidMachineBanner amBidMachineBanner) {
            weakAMBidMachineBanner = new WeakReference<>(amBidMachineBanner);
        }

        @Override
        public void onAdLoaded(@NonNull BannerView bannerView) {
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnLoad();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull BannerView bannerView, @NonNull BMError bmError) {
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnFailToLoad(AMBidMachineUtils.toLoadAdError(bmError));
            }
        }

        @Override
        public void onAdShown(@NonNull BannerView bannerView) {

        }

        @Override
        public void onAdImpression(@NonNull BannerView bannerView) {
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnShown();
            }
        }

        @Override
        public void onAdClicked(@NonNull BannerView bannerView) {
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnClicked();
            }
        }

        @Override
        public void onAdExpired(@NonNull BannerView bannerView) {
            AMBidMachineBanner amBidMachineAd = getAMBidMachineAd();
            if (amBidMachineAd != null) {
                amBidMachineAd.sendOnExpired();
            }
        }

        @Nullable
        private AMBidMachineBanner getAMBidMachineAd() {
            if (weakAMBidMachineBanner == null) {
                return null;
            }
            return weakAMBidMachineBanner.get();
        }

        private void destroy() {
            if (weakAMBidMachineBanner != null) {
                weakAMBidMachineBanner.clear();
                weakAMBidMachineBanner = null;
            }
        }

    }

}