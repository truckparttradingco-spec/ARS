# ARZ-NET

ARZ-NET is an Android market-rate dashboard.

## Current implementation
- Persian RTL Compose interface inspired by the supplied reference.
- Two-column cards for USD, EUR, CNY, TRY, AED, USDT, gold coin, and 18K gold per gram.
- Dedicated global spot cards for XAU/USD and BTC/USD.
- Buy and sell fields are separated.
- No sample or fabricated market prices are displayed.

## Data integration
Live prices are intentionally not fabricated. Provider selection, API credentials (if required), source attribution, timestamping, and the 15-second refresh policy must be connected and validated before live values are shown.

## Build
The GitHub Actions workflow builds a debug APK with Gradle 8.9, JDK 17, and Android SDK 35.
