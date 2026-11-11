const cors = {
  "access-control-allow-origin": "*",
  "access-control-allow-methods": "GET, OPTIONS",
  "access-control-allow-headers": "content-type, authorization",
  "access-control-max-age": "86400"
};

function json(data, status = 200, extra = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
      ...cors,
      ...extra
    }
  });
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: cors });
    if (request.method !== "GET") return json({ ok: false, error: "method_not_allowed" }, 405);

    if (url.pathname === "/api/health") {
      return json({
        ok: true,
        service: "ARZ-NET market data gateway",
        version: "1.0.0",
        providerConfigured: Boolean(env.MARKET_DATA_URL),
        timestamp: new Date().toISOString()
      });
    }

    if (url.pathname === "/api/rates") {
      if (!env.MARKET_DATA_URL) {
        return json({
          ok: false,
          error: "provider_not_configured",
          message: "No verified market-data provider is configured. No sample or fabricated prices are returned."
        }, 503);
      }

      let provider;
      try { provider = new URL(env.MARKET_DATA_URL); }
      catch { return json({ ok: false, error: "invalid_provider_url" }, 500); }
      if (provider.protocol !== "https:") return json({ ok: false, error: "provider_must_use_https" }, 500);

      const headers = { accept: "application/json" };
      if (env.MARKET_DATA_TOKEN) headers.authorization = `Bearer ${env.MARKET_DATA_TOKEN}`;

      let upstream;
      try {
        upstream = await fetch(provider.toString(), {
          headers,
          signal: AbortSignal.timeout(8000),
          cf: { cacheTtl: 0 }
        });
      } catch {
        return json({ ok: false, error: "provider_unreachable" }, 502);
      }
      if (!upstream.ok) return json({ ok: false, error: "provider_error", upstreamStatus: upstream.status }, 502);

      let payload;
      try { payload = await upstream.json(); }
      catch { return json({ ok: false, error: "provider_invalid_json" }, 502); }

      if (!payload || typeof payload !== "object" || !Array.isArray(payload.rates)) {
        return json({ ok: false, error: "provider_schema_invalid", expected: "JSON object with a rates array" }, 502);
      }

      const valid = payload.rates.every(item =>
        item &&
        typeof item.symbol === "string" &&
        (typeof item.buy === "number" || typeof item.sell === "number" || typeof item.price === "number")
      );
      if (!valid) return json({ ok: false, error: "provider_contains_invalid_rates" }, 502);

      return json({
        ok: true,
        source: typeof payload.source === "string" ? payload.source : "Configured market-data provider",
        updatedAt: payload.updatedAt ?? null,
        currency: payload.currency ?? "IRR",
        unit: payload.unit ?? "toman",
        rates: payload.rates
      }, 200, { "cache-control": "public, max-age=10, s-maxage=10" });
    }

    return json({ ok: false, error: "not_found" }, 404);
  }
};