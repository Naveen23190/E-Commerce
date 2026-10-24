# E-Cart India — full-stack shop

A small India-focused full-stack storefront built with Spring Boot, PostgreSQL, Hibernate/JPA, Spring Security/JWT, and React/Vite. The storefront pairs a dark black-and-gold palette with restrained saffron, ivory, and green accents and Hindi greetings. Product prices are in Indian rupees (INR), order dates use the Indian locale, and checkout requests an Indian delivery address and six-digit PIN. It includes 60 uniquely named sample products with distinct Unsplash images, India-oriented descriptions and INR pricing, product search/category and price/rating/stock filtering, price sorting, locally saved favorites, customer registration and login, a stock-aware cart, product details and verified-purchase reviews, saved delivery addresses, order confirmation/history/tracking, inventory change history and low-stock alerts, and role-protected product and fulfillment management. Checkout adds a server-calculated flat ₹99 delivery fee; product prices remain GST-inclusive and no separate GST rate or breakdown is invented. Payment options are COD or UPI/card via Razorpay test mode; online checkout requires test credentials and cannot process live charges. Email confirmations are demo-mode logs only, with no actual email sent. PIN validation checks format only and does not verify serviceability. Interface animations respect reduced-motion preferences. Product photos, Razorpay Checkout, and the optional Devanagari font require an internet connection.

## Requirements

- Java 17+
- Maven 3.9+
- Node.js 20+
- PostgreSQL running locally with database `E_Commerce` available (or override `DB_URL`)
- Docker Desktop (optional, for the containerized setup)

## Configure local environment

Use `.env.example` as a reference for the required values. Spring Boot does not automatically load a `.env` file, so export the values in the same terminal that starts the backend (or configure them in your IDE's run configuration). Keep any local `.env` private; it is ignored by Git. Set both admin variables or leave both empty. If the configured admin email is already registered to a customer, startup stops rather than elevating that account. Razorpay credentials are optional and must be test-mode credentials (`rzp_test_...`); never use live credentials for this project.

Environment variables must be exported into the shell that launches the backend. For PowerShell, from the project directory:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/E_Commerce"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-db-password"
$env:JWT_SECRET = "generate-a-random-secret-at-least-32-characters-long"
$env:ADMIN_EMAIL = "admin@example.com"
$env:ADMIN_PASSWORD = "use-a-unique-password-at-least-14-characters"
# Optional: Razorpay test credentials only
$env:RAZORPAY_KEY_ID = "rzp_test_your_key_id"
$env:RAZORPAY_KEY_SECRET = "your_razorpay_test_secret"
```

Do not paste credentials into source files or commit a `.env`. Flyway manages the schema and Hibernate validates it (`ddl-auto=validate`). Fresh databases apply migrations V1–V3; a pre-existing database is baselined at V1 and applies V2 and V3. **Back up an existing database before the first upgraded startup** and confirm that the database/schema is the one intended. At startup, the app adds missing sample products without overwriting existing products. Existing sample prices are adjusted to the current product-based INR suggestions only when the stored price still exactly matches a previous sample price; admin-edited prices are left unchanged.

## Run

Terminal 1, backend:

```powershell
mvn spring-boot:run
```

Terminal 2, frontend:

```powershell
npm install
npm run dev
```

Open the Vite URL (normally `http://localhost:5173`). The Vite development server proxies `/api` requests to `http://localhost:8080`. To use another API origin, add `VITE_API_URL=http://localhost:8080/api` to a frontend `.env.local`. CORS defaults to the local Vite origin and can be changed with `CORS_ALLOWED_ORIGIN`.

## Run with Docker Compose

From the project directory, copy `.env.example` to `.env`, then set a strong local `DB_PASSWORD` and `JWT_SECRET`. `DB_URL` in `.env` is for local non-container runs; Compose supplies the correct internal PostgreSQL URL automatically. Add test-only Razorpay keys only if you want to exercise its sandbox flow.

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Open `http://localhost:5173`. PostgreSQL data persists in the `postgres-data` volume. Stop the services with `Ctrl+C`, then run `docker compose down` to remove the containers; this retains the database volume. **Do not run `docker compose down -v` unless you intend to delete that persisted database.**

## Deploy a free public demo on Render

The repository includes [`render.yaml`](./render.yaml), a Render Blueprint for the static React storefront, Dockerized Spring Boot API, and free PostgreSQL database. The API builds its JDBC URL from the database host, port, and name provided by Render. The Blueprint generates a private `JWT_SECRET` and configures the storefront origin for CORS. Razorpay credentials are intentionally not included; leave online payments unavailable unless you add test-mode credentials to the API service in Render.

1. Push the project to a GitHub repository you control. This workspace currently has no Git repository, so initialize one if needed; inspect staged files and ensure `.env` and credentials are not included before pushing.
2. In the Render Dashboard, choose **New → Blueprint**, connect that GitHub repository, and review the services described by `render.yaml`.
3. Apply the Blueprint and wait for the API health check and storefront deployment to complete. The public storefront URL is `https://e-cart-storefront.onrender.com`.
4. Verify the storefront can load the catalog and register/sign in. To enable the admin dashboard, set both `ADMIN_EMAIL` and `ADMIN_PASSWORD` in the `e-cart-api` service environment; use a new admin email and a unique password of at least 14 characters. For Razorpay sandbox checkout only, add test credentials (`RAZORPAY_KEY_ID` beginning with `rzp_test_` and `RAZORPAY_KEY_SECRET`) to the same service; never add live keys.

This free-tier setup is for a disposable portfolio demo, **not customer or production data**. Free web services can sleep while idle and take time to wake. Render's free PostgreSQL has limited retention and no durable production backup guarantees; assume the database can expire or be deleted and do not store real customer/order data. Review Render's current pricing, free-service limits, and database-retention policy before deploying. For a durable deployment, use a paid persistent PostgreSQL plan, backups, and an always-on API; back up and deliberately migrate any existing local data rather than expecting it to transfer automatically.

## API

- `POST /api/auth/register`, `POST /api/auth/login`
- `GET /api/users/me`
- `GET /api/products?query=&category=&minimumPrice=&maximumPrice=&minimumRating=0&inStockOnly=false&sort=&direction=&page=0&size=12` returns a page (`content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`); search, category, price, rating, and stock filters are applied before pagination, and page size is limited to 100. `GET /api/products/categories` returns the available category filters. `GET /api/products/{id}` returns one product.
- Admin only: `POST /api/products`, `PUT /api/products/{id}`, `DELETE /api/products/{id}` (deletion is blocked while a customer's cart contains the item)
- Authenticated customer: `GET /api/cart`, `POST /api/cart/items/{productId}`, `PUT /api/cart/items/{productId}`, `DELETE /api/cart/items/{productId}`
- Authenticated customer: `POST /api/orders` for COD (body: `{"shippingAddress":"Flat 1, Street, City, State","pinCode":"560001","paymentMethod":"COD"}`), `GET /api/orders?page=0&size=5` for paginated order history.
- Razorpay test mode: choose UPI or card in checkout to open the corresponding method in the Razorpay sandbox. `POST /api/orders/razorpay/order` creates a server-priced payment attempt and `POST /api/orders/razorpay/verify` verifies a captured test payment. Both require configured test credentials. Checkout permits retry and keeps the cart when payment is cancelled or fails. If a captured payment cannot be fulfilled because the checkout expired or stock or price changed, the app requests a full sandbox refund and records whether it is pending or processed. If Razorpay cannot confirm the refund, the attempt is flagged for manual recovery using its checkout and payment references. Captured Razorpay orders cannot be cancelled through admin fulfillment; coordinate any refund and cancellation separately through the provider dashboard and support. Never configure live keys; test payments do not collect or charge real money.
- Authenticated customer: `/api/addresses` supports listing, creating, updating, deleting, and selecting the default saved address.
- Admin only: `GET /api/inventory/movements`, `GET /api/products/{id}/inventory/movements`, and `POST /api/products/{id}/inventory/adjust` for stock history and adjustments.
- Product reviews: public `GET /api/products/{id}/reviews`; authenticated customers who purchased the product may submit one review using `POST /api/products/{id}/reviews` (body: `{"rating":5,"comment":"..."}`)
- Admin only: `GET /api/orders/admin?page=0&size=5` for paginated fulfillment orders; `PUT /api/orders/{id}/status` (body: `{"status":"CONFIRMED"}`); allowed transitions are sequential fulfillment steps, with cancellation only before packing

Send the login/register token as `Authorization: Bearer <token>`. Registration always creates a customer role; the only initial admin path is the local environment bootstrap. Prices are recalculated from locked product records during the transactional checkout, and insufficient stock rolls back the entire order.

## Checks

Run backend tests with `mvn test` and build the frontend with `npm run build`. A database is not required for those checks. Flyway applies versioned migrations at application startup; back up existing data before starting the upgraded app. No database connection or seeding operation is performed by the build checks.
