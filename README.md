# BidMachine Android AdManager Adapter

[<img src="https://img.shields.io/badge/SDK%20Version-1.9.0-brightgreen">](https://github.com/bidmachine/BidMachine-Android-SDK)
[<img src="https://img.shields.io/badge/Adapter%20Version-1.9.0.2-green">](https://artifactory.bidmachine.io/bidmachine/io/bidmachine/ads.adapters.admanager/1.9.0.2/)
[<img src="https://img.shields.io/badge/AdManager%20Version-20.5.0-blue">](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start)

* [Useful links](#useful-links)
* [Integration](#integration)
* [Sample](#sample)
* [Working with price rounding](#working-with-price-rounding)
* [What's new in last version](#whats-new-in-last-version)

## Useful links
* [AdManager integration documentation](https://developers.google.com/ad-manager/mobile-ads-sdk/android/quick-start)
* [BidMachine integration documentation](https://docs.bidmachine.io/docs/in-house-mediation)
* [BidMachine adapter integration documentation](https://docs.bidmachine.io/docs/google-ad-manager)

## Integration
```gradle
repositories {
    // Add BidMachine maven repository
    maven {
        name 'BidMachine Ads maven repository'
        url 'https://artifactory.bidmachine.io/bidmachine'
    }
}

dependencies {
    // Add BidMachine SDK dependency
    implementation 'io.bidmachine:ads:1.9.0'
    // Add BidMachine SDK AdManager Adapter dependency
    implementation 'io.bidmachine:ads.adapters.admanager:1.9.0.2'
    // Add AdManager SDK dependency
    implementation 'com.google.android.gms:play-services-ads:20.5.0'
}
```

## Sample
* [Banner](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L129)
* [MREC](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L252)
* [Interstitial](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L375)
* [Rewarded](example_pre_bid/src/main/java/io/bidmachine/example/MainActivity.java#L489)

## Working with price rounding
BidMachine supports server-side price rounding.<br>
To setup it correctly - please contact your manager to set up your own rounding rules. Manager will provide you with the list of prices and you can use them to create orders/line items in partner's dashboard.<br>

## What's new in last version
Please view the [changelog](CHANGELOG.md) for details.
