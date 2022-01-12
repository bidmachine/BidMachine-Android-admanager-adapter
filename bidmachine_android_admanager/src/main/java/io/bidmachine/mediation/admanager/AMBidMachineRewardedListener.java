package io.bidmachine.mediation.admanager;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.rewarded.RewardItem;

public interface AMBidMachineRewardedListener extends AMBidMachineFullScreenListener<AMBidMachineRewarded> {

    void onAdRewarded(@NonNull AMBidMachineRewarded amBidMachineRewarded, @NonNull RewardItem rewardItem);

}