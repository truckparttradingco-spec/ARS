# ARZ-NET

ARZ-NET is an Android market-rate dashboard with a Cloudflare Worker API gateway.

## Android app

- Persian RTL Compose interface.
- Ten dashboard cards: USD, EUR, CNY, TRY, AED, USDT, gold coin, 18K gold per gram, XAU/USD and BTC/USD.
- Buy/sell values are separate where applicable.
- Requests the market API every 15 seconds.
- Shows source/status and leaves prices blank when a verified provider is unavailable.
- Never displays sample or fabricated prices.

## Cloudflare market API

The Worker source is in `worker/index.js`; configuration is in `wrangler.toml`.

Endpoints:
- `GET /api/health`: service status and whether a provider URL is configured.
- `GET /api/rates`: validates and forwards the configured provider's rates.

Expected provider response:

```json
{
  "source": "Provider name",
  "updatedAt": "2026-11-11T12:00:00Z",
  "currency": "IRR",
  "unit": "toman",
  "rates": [
    { "symbol": "USD", "buy": 0, "sell": 0 },
    { "symbol": "XAU/USD", "price": 0 },
    { "symbol": "BTC/USD", "price": 0 }
  ]
}
```

The zero values above are schema examples only and must never be used as live prices.

Configure these Worker variables/secrets in Cloudflare:
- `MARKET_DATA_URL` (variable): HTTPS endpoint of the selected, trusted market-data provider.
- `MARKET_DATA_TOKEN` (secret, only if the provider requires it): bearer token.

The provider must return real numeric values, source attribution, and update time. Iranian free-market buy/sell quotes, gold and coin prices must come from a provider that explicitly supports those markets; global XAU/USD and BTC/USD must use global-market sources. The gateway deliberately returns `provider_not_configured` until a provider is selected. It does not invent prices or derive unsupported local quotes.

## Deployment

The workflow `.github/workflows/cloudflare-worker.yml` deploys the Worker on pushes to `main` that touch Worker files.

Add these GitHub repository Actions secrets:
- `CLOUDFLARE_API_TOKEN`: token with permission to edit Workers Scripts in the target account.
- `CLOUDFLARE_ACCOUNT_ID`: the Cloudflare account ID.

After deployment, the API base URL is:

`https://arz-net-market-api.truckparttrading-co.workers.dev`

## Android build

The `Android APK Build` workflow builds a debug APK with JDK 17, Gradle 8.9, and Android SDK 35. The APK is uploaded as the `ARZ-NET-debug-apk` artifact.
