# Adyen Drop-in (PayPal) — Java 8 + Spring Boot (Docker-ready)

**What this project is**
- Minimal Spring Boot (Java 8) backend exposing:
  - `POST /api/paymentMethods` -> proxies to Adyen /paymentMethods
  - `POST /api/payments` -> proxies to Adyen /payments
  - `POST /api/details` -> proxies to Adyen /payments/details
- Static frontend `frontend/index.html` that mounts Adyen Drop-in and shows PayPal automatically.
- Dockerfile and docker-compose for easy deployment (suitable for Render.com).

**Important - fill these environment variables before running**
- `ADYEN_API_KEY` — Adyen API key (test or live)
- `ADYEN_MERCHANT_ACCOUNT` — merchantAccount value
- `ADYEN_CLIENT_KEY` — clientKey for frontend (test or live)
- `ADYEN_ENVIRONMENT` — "test" or "live" (defaults to test)

**How to run locally (docker)**
1. Build and run:
   ```
   docker build -t adyen-dropin-springboot .
   docker run -p 8080:8080 -e ADYEN_API_KEY=your_api_key -e ADYEN_CLIENT_KEY=your_client_key -e ADYEN_MERCHANT_ACCOUNT=YourMerchantAccount adyen-dropin-springboot
   ```
2. Visit `http://localhost:8080/` to see the Drop-in page.

**How to deploy to Render.com**
- Use the included Dockerfile; set the environment variables in the Render service settings.
- Ensure `ADYEN_CLIENT_KEY` matches the environment (test or live) and that PayPal is enabled in Adyen Customer Area.

**Notes**
- This is a minimal example for demonstration and sandbox testing. Do **not** store secrets in code.
- The backend simply forwards requests to Adyen; you should add validation, logging, error handling and webhook processing in production.