package io.bidmachine.mediation.admanager;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;

import io.bidmachine.FullScreenAdRequest;

public abstract class AMBidMachineFullScreenAd<
        SelfType extends AMBidMachineFullScreenAd<SelfType, Listener, BidMachineAdRequest>,
        Listener extends AMBidMachineFullScreenListener<SelfType>,
        BidMachineAdRequest extends FullScreenAdRequest<BidMachineAdRequest>>
        extends AMBidMachineAd<SelfType, Listener, BidMachineAdRequest> {

    protected void sendOnFailToShow(@NonNull AdError adError) {
        Listener listener = getListener();
        if (listener != null) {
            //noinspection unchecked
            listener.onAdFailToShow((SelfType) this, adError);
        }
    }

    protected void sendOnClosed() {
        Listener listener = getListener();
        if (listener != null) {
            //noinspection unchecked
            listener.onAdClosed((SelfType) this);
        }
    }

}