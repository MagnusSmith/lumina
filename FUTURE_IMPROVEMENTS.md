# Future Improvements for Lumina

This document outlines planned enhancements and improvements for the Lumina meter configuration service. These are lower priority items that would improve the system but are not critical for initial deployment.

---

## 1. Add Integration Tests with Testcontainers

**Priority**: Medium
**Effort**: 2-3 days
**Labels**: `testing`, `enhancement`

### Description

Currently, the project only has unit tests with mocked dependencies. Adding integration tests would provide better confidence in the system's behavior and catch integration issues early.

### Implementation Plan

```java
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientControllerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndRetrieveClient() {
        // Full end-to-end test with real MongoDB
        NewClientDto newClient = new NewClientDto("Test Client");

        ResponseEntity<ClientDto> createResponse = restTemplate.postForEntity(
            "/api/client", newClient, ClientDto.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().name()).isEqualTo("Test Client");

        // Verify we can retrieve it
        String clientId = createResponse.getBody().id();
        ResponseEntity<ClientDto> getResponse = restTemplate.getForEntity(
            "/api/client/" + clientId, ClientDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().name()).isEqualTo("Test Client");
    }
}
```

### Required Dependencies

```kotlin
// Add to build.gradle.kts
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:testcontainers:1.19.0")
testImplementation("org.testcontainers:mongodb:1.19.0")
testImplementation("org.testcontainers:junit-jupiter:1.19.0")
```

### Test Coverage Goals

- **Controller layer**: Full API endpoint testing
- **Service layer**: Integration with real MongoDB
- **Repository layer**: Complex query testing
- **Validation**: End-to-end validation flow testing

### Benefits

- Catch integration issues early
- Test against real database behavior
- Validate MongoDB queries work correctly
- More confidence in deployments

---

## 2. Add API Versioning

**Priority**: Low
**Effort**: 1-2 days
**Labels**: `api`, `enhancement`

### Description

Currently, all API endpoints are under `/api/`. Adding versioning would allow for backward-compatible API evolution.

### Recommended Approach

Use URI versioning with a `v1` prefix:

```java
@RestController
@RequestMapping("/api/v1/client")
public class ClientController {
    // ... endpoints
}
```

### Migration Strategy

1. Add `/api/v1/` prefix to all controllers
2. Keep `/api/` endpoints as aliases to `/api/v1/` for backward compatibility
3. Add deprecation warnings to `/api/` endpoints
4. Remove `/api/` endpoints in next major version

### Configuration

```yaml
# application.yml
api:
  version:
    current: v1
    deprecated: []  # List versions to mark as deprecated
```

### Future Benefits

- Ability to introduce breaking changes in v2 while maintaining v1
- Clear API evolution path
- Better API documentation structure
- Client migration flexibility

---

## 3. Improve Error Messages with Examples

**Priority**: Low
**Effort**: 2-3 days
**Labels**: `ux`, `documentation`

### Description

Current error messages provide basic information but could be more helpful by including examples of valid values and suggested fixes.

### Current State

```json
{
  "field": "pattern",
  "errorCode": "INVALID_PATTERN",
  "message": "Value 'abc' does not match required pattern '\\d{4}'"
}
```

### Proposed Enhancement

```json
{
  "field": "pattern",
  "errorCode": "INVALID_PATTERN",
  "message": "Value 'abc' does not match required pattern '\\d{4}'",
  "example": "1234",
  "suggestion": "Pattern must be exactly 4 digits",
  "documentation": "https://docs.lumina.io/patterns/numeric"
}
```

### Implementation

```java
public enum ErrorCode {
    INVALID_PATTERN(
        "Value '{0}' does not match required pattern '{1}'",
        "Example: {2}",
        "Pattern requirements: {3}"),
    VALUE_TOO_LONG(
        "Value length {0} exceeds maximum {1}",
        "Please shorten the value to {1} characters or less",
        null),
    // ... with examples and suggestions
}

public record Error(
    String field,
    ErrorCode errorCode,
    String message,
    Object rejectedValue,
    String example,      // NEW
    String suggestion,   // NEW
    String documentation // NEW
) {
    // ...
}
```

### Benefits

- Faster developer onboarding
- Reduced support requests
- Better API user experience
- Self-documenting validation rules

---

## 4. Add Metrics and Monitoring

**Priority**: Medium
**Effort**: 2-3 days
**Labels**: `monitoring`, `production-ready`

### Description

Add comprehensive metrics collection for monitoring application health, performance, and usage patterns in production.

### Implementation

#### Dependencies

```kotlin
// build.gradle.kts
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
implementation("io.micrometer:micrometer-tracing-bridge-brave")
```

#### Configuration

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:dev}
  tracing:
    sampling:
      probability: 0.1  # Sample 10% of traces
```

#### Custom Metrics

```java
@Component
public class MeterMetrics {
    private final MeterRegistry meterRegistry;

    public MeterMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Gauge for total meters
        Gauge.builder("meters.total", this::getTotalMeterCount)
            .description("Total number of meters in the system")
            .register(meterRegistry);
    }

    public void recordMeterCreation(String meterType) {
        meterRegistry.counter("meters.created",
            "type", meterType).increment();
    }

    public void recordValidationError(String errorCode) {
        meterRegistry.counter("validation.errors",
            "code", errorCode).increment();
    }

    private long getTotalMeterCount() {
        return meterRepository.count();
    }
}
```

#### Dashboarding

Metrics endpoint available at: `/actuator/prometheus`

Compatible with:
- **Prometheus** for scraping
- **Grafana** for visualization
- **Datadog**, **New Relic**, or other APM tools

### Key Metrics to Track

- Request rates and latencies per endpoint
- Database query performance
- Validation error rates by type
- Meter creation/update rates
- Cache hit/miss ratios (if caching added)
- JVM memory and GC metrics
- Authentication success/failure rates

---

## 5. Add Request/Response DTOs for Better API Evolution

**Priority**: Low
**Effort**: 3-5 days
**Labels**: `api`, `refactoring`

### Description

Currently, some controllers return domain models directly. Creating separate API contracts would allow independent evolution of the API and domain models.

### Current State

```java
@RestController
public class ProjectController {
    @GetMapping("/api/project/{id}")
    public Project findById(@PathVariable String id) {
        return projectService.findById(id);  // Domain model exposed directly
    }
}
```

### Proposed Enhancement

Create separate API package structure:

```
com.lumina/
  ├── api/
  │   └── v1/
  │       ├── client/
  │       │   ├── ClientResponse.java
  │       │   ├── CreateClientRequest.java
  │       │   └── UpdateClientRequest.java
  │       ├── project/
  │       │   ├── ProjectResponse.java
  │       │   ├── ProjectSummary.java
  │       │   └── CreateProjectRequest.java
  │       └── common/
  │           ├── PageResponse.java
  │           └── ErrorResponse.java
  ├── domain/
  │   ├── client/
  │   ├── project/
  │   └── meter/
  └── ...
```

### Example Implementation

```java
package com.lumina.api.v1.client;

/**
 * API response for client data.
 * Separate from domain model to allow API evolution.
 */
public record ClientResponse(
    String id,
    String name,
    List<ProjectSummary> projects,
    Instant createdAt,
    Instant updatedAt
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
            client.id(),
            client.name(),
            client.projects() != null
                ? client.projects().stream()
                    .map(ProjectSummary::from)
                    .toList()
                : List.of(),
            client.createdAt(),
            client.updatedAt()
        );
    }
}

/**
 * Lightweight project summary for inclusion in client responses.
 */
public record ProjectSummary(
    String id,
    String name
) {
    public static ProjectSummary from(Project project) {
        return new ProjectSummary(project.id(), project.name());
    }
}
```

### Benefits

- **API Stability**: Change domain models without breaking API contracts
- **Versioning**: Different API versions can use same domain models
- **Optimization**: API responses can include computed fields, aggregations
- **Security**: Don't accidentally expose sensitive domain fields
- **Documentation**: API contracts self-document expected responses

### Migration Strategy

1. Create `api.v1` package structure
2. Add API DTOs for each endpoint
3. Update controllers to use API DTOs
4. Keep domain models internal
5. Add mapping layer between domain and API models

---

## Implementation Priority

Based on impact and effort, recommended implementation order:

1. **Add Metrics and Monitoring** (Medium priority, high production value)
2. **Add Integration Tests** (Medium priority, improves quality)
3. **Improve Error Messages** (Low priority, good UX improvement)
4. **Add Request/Response DTOs** (Low priority, enables future growth)
5. **Add API Versioning** (Low priority, prerequisite for breaking changes)

---

## Additional Considerations

### Other Potential Enhancements

- **Caching Layer**: Add Redis for frequently accessed catalogue items
- **Event Sourcing**: Track all changes to meters for audit trail
- **GraphQL API**: Alternative to REST for flexible queries
- **Bulk Operations**: Endpoints for creating/updating multiple resources
- **Export Functionality**: CSV/Excel export of meter configurations
- **Search**: Full-text search across clients, projects, meters
- **Webhooks**: Notify external systems of meter changes
- **Rate Limiting**: Protect API from abuse
- **API Gateway**: Centralized entry point with auth, routing, rate limiting

### Documentation Improvements

- **OpenAPI Enhancements**: Add more examples, descriptions
- **Postman Collection**: Pre-configured API examples
- **Architecture Decision Records (ADRs)**: Document key decisions
- **Runbooks**: Operational procedures for common scenarios
- **Developer Guide**: Detailed development environment setup

---

## Contributing

When implementing these improvements:

1. Create a GitHub issue referencing this document
2. Discuss approach in issue comments
3. Create feature branch from `main`
4. Implement with tests
5. Update documentation
6. Submit pull request

## Questions?

For questions about these improvements, please:
- Open a GitHub issue with the `question` label
- Tag relevant team members
- Reference this document
