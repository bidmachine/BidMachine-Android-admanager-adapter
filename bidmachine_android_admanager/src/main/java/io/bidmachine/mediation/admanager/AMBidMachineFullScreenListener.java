package io.bidmachine.mediation.admanager;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;

public interface AMBidMachineFullScreenListener<T> extends AMBidMachineListener<T> {

    void onAdFailToShow(@NonNull T t, @NonNull AdError adError);

    void onAdClosed(@NonNull T t);

}