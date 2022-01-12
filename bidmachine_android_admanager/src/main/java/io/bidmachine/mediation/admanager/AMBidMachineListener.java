package io.bidmachine.mediation.admanager;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.LoadAdError;

public interface AMBidMachineListener<T> {

    void onAdLoaded(@NonNull T t);

    void onAdFailToLoad(@NonNull T t, @NonNull LoadAdError loadAdError);

    void onAdShown(@NonNull T t);

    void onAdClicked(@NonNull T t);

    void onAdExpired(@NonNull T t);

}