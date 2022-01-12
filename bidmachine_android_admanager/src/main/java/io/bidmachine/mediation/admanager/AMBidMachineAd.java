package io.bidmachine.mediation.admanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import io.bidmachine.AdRequest;

public abstract class AMBidMachineAd<
        SelfType extends AMBidMachineAd<SelfType, Listener, BidMachineAdRequest>,
        Listener extends AMBidMachineListener<SelfType>,
        BidMachineAdRequest extends AdRequest<BidMachineAdRequest, ?>> {

    private static final long ON_APP_EVENT_WAIT_DEFAULT = 200;

    private final AtomicBoolean isLoaded = new AtomicBoolean(false);

    private BidMachineAdRequest bidMachineAdRequest;
    private Listener listener;
    private long onAppEventDelay = ON_APP_EVENT_WAIT_DEFAULT;
    private Context applicationContext;

    public boolean isLoaded() {
        return isLoaded.get();
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public long getOnAppEventDelay() {
        return onAppEventDelay;
    }

    public void setOnAppEventDelay(long onAppEventDelay) {
        if (onAppEventDelay > 0) {
            this.onAppEventDelay = onAppEventDelay;
        }
    }

    public void setBidMachineAdRequest(@NonNull BidMachineAdRequest bidMachineAdRequest) {
        this.bidMachineAdRequest = bidMachineAdRequest;
    }

    protected boolean setupRequest(@NonNull Context context,
                                   @NonNull AdManagerAdRequest.Builder adManagerAdRequestBuilder,
                                   @Nullable BidMachineAdRequest bidMachineAdRequest) {
        if (bidMachineAdRequest == null) {
            return false;
        }

        applicationContext = context.getApplicationContext();
        setBidMachineAdRequest(bidMachineAdRequest);
        AMBidMachineUtils.appendRequest(adManagerAdRequestBuilder, bidMachineAdRequest);
        return true;
    }

    protected abstract void loadBidMachine(@NonNull Context context, @NonNull BidMachineAdRequest bidMachineAdRequest);

    protected void bidMachineWinMediation() {
        destroyAdManagerAdObjects();

        if (applicationContext == null) {
            sendOnFailToLoad(AMBidMachineUtils.createLoadAdError(AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR,
                                                                 "Context is null"));
            return;
        }
        if (bidMachineAdRequest == null) {
            sendOnFailToLoad(AMBidMachineUtils.createLoadAdError(AdManagerAdRequest.ERROR_CODE_INTERNAL_ERROR,
                                                                 "BidMachine AdRequest is null"));
            return;
        }

        bidMachineAdRequest.notifyMediationWin();
        loadBidMachine(applicationContext, bidMachineAdRequest);
    }

    protected void bidMachineLossMediation() {
        if (bidMachineAdRequest != null) {
            bidMachineAdRequest.notifyMediationLoss();
        }
        sendOnLoad();
    }

    protected void sendOnLoad() {
        isLoaded.set(true);
        if (listener != null) {
            //noinspection unchecked
            listener.onAdLoaded((SelfType) this);
        }
    }

    protected void sendOnFailToLoad(@NonNull LoadAdError loadAdError) {
        isLoaded.set(false);
        if (listener != null) {
            //noinspection unchecked
            listener.onAdFailToLoad((SelfType) this, loadAdError);
        }
    }

    protected void sendOnShown() {
        if (listener != null) {
            //noinspection unchecked
            listener.onAdShown((SelfType) this);
        }
    }

    protected void sendOnClicked() {
        if (listener != null) {
            //noinspection unchecked
            listener.onAdClicked((SelfType) this);
        }
    }

    protected void sendOnExpired() {
        isLoaded.set(false);
        if (listener != null) {
            //noinspection unchecked
            listener.onAdExpired((SelfType) this);
        }
    }

    public void destroy() {
        isLoaded.set(false);
        applicationContext = null;
        listener = null;

        if (bidMachineAdRequest != null) {
            bidMachineAdRequest.destroy();
            bidMachineAdRequest = null;
        }
        destroyBidMachineAdObjects();
        destroyAdManagerAdObjects();
    }

    abstract void destroyAdManagerAdObjects();

    abstract void destroyBidMachineAdObjects();


    protected static class OnAppEventDelayRunnable<AMBidMachineAdType extends AMBidMachineAd<AMBidMachineAdType, ?, ?>>
            implements Runnable {

        private WeakReference<AMBidMachineAdType> weakAMBidMachineBanner;

        public OnAppEventDelayRunnable(@Nullable WeakReference<AMBidMachineAdType> weakAMBidMachineBanner) {
            this.weakAMBidMachineBanner = weakAMBidMachineBanner;
        }

        @Override
        public void run() {
            if (weakAMBidMachineBanner != null) {
                AMBidMachineAdType amBidMachineAd = weakAMBidMachineBanner.get();
                if (amBidMachineAd != null) {
                    amBidMachineAd.bidMachineLossMediation();
                }
            }
        }

        public void destroy() {
            weakAMBidMachineBanner = null;
        }

    }

}