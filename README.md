# Lumina Meter Config Service

A REST API service for managing IoT meter configurations with hierarchical organization and validation. Features include:

- **Catalogue-based validation** - Define meter models with validation constraints
- **Hierarchical structure** - Organize meters by Client → Project → Location → Meter
- **Web UI** - Modern htmx-powered frontend (no Node.js required)
- **REST API** - Full CRUD operations with OpenAPI/Swagger documentation
- **Stage-based validation** - Progressive validation through different stages (INTAKE, CONNECTION, etc.)

## Tech Stack

- **Backend**: Spring Boot 3.5.7, Java 25
- **Database**: MongoDB
- **Frontend**: Thymeleaf + htmx (server-side rendering)
- **API Documentation**: SpringDoc OpenAPI 3
- **Security**: Spring Security with optional OAuth2 JWT

## Quick Start

### Prerequisites

- Java 25
- Docker & Docker Compose

### 1. Start MongoDB

```bash
# From the project root directory
docker compose up -d
```

This starts MongoDB on port 28017 with credentials `tester:tester`.

### 2. Run the Application

```bash
cd application
./gradlew bootRun
```

### 3. Access the Application

- **Web UI**: http://localhost:8080/
- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health

## Web Frontend

The application includes a modern web interface built with htmx and Thymeleaf:

### Features
- **Home Page** (`/`) - Overview and quick navigation
- **Catalogue Management** (`/catalogue`) - Create and view catalogue items
- **Meters** (`/meters`) - View and filter meters by location
- **Clients** (`/clients`) - Manage clients and organizations

### Key Benefits
- **No Node.js required** - Pure server-side rendering
- **Interactive** - htmx provides AJAX-like interactions without JavaScript frameworks
- **Fast development** - Spring DevTools enables hot reload
- **Reuses REST API** - No code duplication

## Running with Docker (Production)

### Option 1: Using GitHub Container Registry Image

Login to GitHub Container Registry:
```bash
export CRPAT=<your_personal_access_token>
echo $CRPAT | docker login ghcr.io -u USERNAME --password-stdin
```

Run the application stack:
```bash
docker compose --profile=repository up
```

### Option 2: Build and Run Locally

```bash
# Build the Docker image
./gradlew jib

# Run with docker-compose
docker compose up
```

## Configuration

### Environment Variables

The application can be configured using environment variables:

**MongoDB Configuration:**
```bash
export MONGODB_URI=mongodb://tester:tester@localhost:28017/test
export MONGODB_HOST=localhost
export MONGODB_PORT=28017
export MONGODB_USERNAME=tester
export MONGODB_PASSWORD=tester
export MONGODB_DATABASE=test
```

**Security Configuration:**
```bash
# Enable OAuth2 JWT authentication (disabled by default for development)
export SECURITY_ENABLED=true
export OAUTH2_ISSUER_URI=https://your-oauth-provider.com
export OAUTH2_JWK_SET_URI=https://your-oauth-provider.com/.well-known/jwks.json
```

### Development Mode

By default, security is **disabled** for development (`lumina.security.enabled=false`). This allows unrestricted access to all endpoints.

For production, set `SECURITY_ENABLED=true` and configure OAuth2 properties.

## Architecture

### Domain Model

```
Client
  └── Projects
      └── Locations
          └── Meters (validated against Catalogue Items)
```

### Catalogue System

The **Catalogue** defines meter models with validation constraints:

- **Model**: Unique identifier (e.g., "GATEWAY-LORAWAN-V1")
- **Level**: GATEWAY or DEVICE
- **Type**: LORAWAN, MODBUS, or SIDEWALK
- **Constraints**: Validation rules applied to meter configurations

### Validation Constraints

#### 1. Text Constraint

Validates string values with optional length restrictions:

```json
{
  "type": "TEXT",
  "name": "publicKey",
  "description": "Public key configuration",
  "minLength": 0,
  "maxLength": 256,
  "isRequired": false,
  "stage": "INTAKE"
}
```

#### 2. Numeric Constraint

Validates numeric values (INTEGER or FLOAT) with optional bounds:

```json
{
  "type": "NUMERIC",
  "name": "age",
  "description": "Device age in days",
  "numberType": "INTEGER",
  "min": 0,
  "max": 100,
  "isRequired": true,
  "stage": "INTAKE"
}
```

#### 3. Pattern Constraint

Validates values against regex patterns:

```json
{
  "type": "PATTERN",
  "name": "specialId",
  "description": "Format: XXXX-XXXX-XXXX-XXXX",
  "pattern": "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
  "isRequired": true,
  "stage": "CONNECTION"
}
```

### Stage-based Validation

- `stage`: Defines when validation starts (e.g., INTAKE, CONNECTION)
- `isRequired`:
  - `true`: Field must be present and valid at the specified stage and all subsequent stages
  - `false`: Field is only validated if present; optional fields pass validation when missing

## API Usage Examples

### 1. Create a Catalogue Item

```bash
curl -X 'POST' \
  'http://localhost:8080/api/catalogue/item' \
  -H 'Content-Type: application/json' \
  -d '{
  "model": "GATEWAY-LORAWAN-V1",
  "level": "GATEWAY",
  "type": "LORAWAN",
  "description": "LoRaWAN Gateway Version 1",
  "manufacturer": "Acme IoT Devices Inc."
}'
```

Preset constraints are automatically added based on the level and type.

### 2. Create a Client

```bash
curl -X 'POST' \
  'http://localhost:8080/api/client' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Acme Corporation"
}'
```

Response includes the client `id` to use in subsequent requests.

### 3. Create a Project

```bash
curl -X 'POST' \
  'http://localhost:8080/api/project' \
  -H 'Content-Type: application/json' \
  -d '{
  "clientId": "660ae9a0c1e5a312013963ef",
  "name": "Solar Farm Project"
}'
```

### 4. Create a Location

```bash
curl -X 'POST' \
  'http://localhost:8080/api/location' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Building A",
  "projectId": "660aea55c1e5a312013963f0"
}'
```

### 5. Create a Meter

```bash
curl -X 'POST' \
  'http://localhost:8080/api/meter' \
  -H 'Content-Type: application/json' \
  -d '{
  "locationId": "660aeb0fc1e5a312013963f1",
  "model": "GATEWAY-LORAWAN-V1",
  "configurationLines": [
    {
      "variable": "age",
      "value": "50",
      "stage": "INTAKE"
    },
    {
      "variable": "publicKey",
      "value": "SHA-256:mypublickeyvalue",
      "stage": "INTAKE"
    }
  ]
}'
```

The meter configuration is validated against the catalogue item constraints. Validation errors are returned with detailed messages if constraints are violated.

### 6. Get All Clients with Nested Data

```bash
curl -X 'GET' 'http://localhost:8080/api/client'
```

Returns clients with nested projects, locations, and meter IDs.

### 7. Get Meters by Location

```bash
curl -X 'GET' 'http://localhost:8080/api/meter/location/660aeb0fc1e5a312013963f1'
```

## Development

### Project Structure

```
lumina/
├── application/              # Main Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lumina/
│   │   │   │   ├── catalogue/     # Catalogue management
│   │   │   │   ├── client/        # Client management
│   │   │   │   ├── project/       # Project management
│   │   │   │   ├── location/      # Location management
│   │   │   │   ├── meter/         # Meter configuration
│   │   │   │   ├── web/           # Web controllers
│   │   │   │   └── validation/    # Validation framework
│   │   │   └── resources/
│   │   │       ├── templates/     # Thymeleaf templates
│   │   │       ├── static/css/    # CSS stylesheets
│   │   │       └── application.yml
│   │   └── test/
│   └── build.gradle.kts
├── infrastructure/           # AWS CDK infrastructure code
└── compose.yaml             # Docker Compose configuration
```

### Building

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Build Docker image
./gradlew jib
```

### Hot Reload

Spring DevTools is included for development. Changes to Java code and templates will automatically reload.

## API Documentation

Full interactive API documentation is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Security

### Development Mode (Default)

Security is **disabled** by default (`lumina.security.enabled=false`):
- All endpoints are accessible without authentication
- CSRF protection is disabled
- OAuth2 JWT is not configured

### Production Mode

Enable security by setting `SECURITY_ENABLED=true` and configuring OAuth2:

```bash
export SECURITY_ENABLED=true
export OAUTH2_ISSUER_URI=https://your-oauth-provider.com
export OAUTH2_JWK_SET_URI=https://your-oauth-provider.com/.well-known/jwks.json
```

In production mode:
- All API endpoints require JWT authentication (except health checks and Swagger UI)
- CSRF protection is disabled for API endpoints (JWT is used instead)
- Web UI endpoints require authentication

## Monitoring

Spring Boot Actuator endpoints:
- **Health**: http://localhost:8080/actuator/health
- **Info**: http://localhost:8080/actuator/info

## Troubleshooting

### MongoDB Connection Issues

If you see MongoDB connection errors:

1. Ensure MongoDB is running:
   ```bash
   docker compose ps
   ```

2. Check MongoDB logs:
   ```bash
   docker compose logs mongodb
   ```

3. Verify connection string in `application.yml` or environment variables

### Port Already in Use

If port 8080 is already in use, you can change it:

```bash
SERVER_PORT=8081 ./gradlew bootRun
```

### Swagger UI Not Loading

If you get errors accessing `/swagger-ui/index.html`, ensure:
1. The application is fully started (check logs)
2. SpringDoc OpenAPI dependency is included (version 2.7.0 for Spring Boot 3.5.x)
3. Security is disabled or properly configured

## License

[Add your license here]

## Contributing

[Add contributing guidelines here]
