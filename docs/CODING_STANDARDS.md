# Lumina Coding Standards and Patterns

This document defines the coding standards, patterns, and conventions for the Lumina project. All contributors must follow these guidelines to maintain consistency and quality.

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Java Code Style](#java-code-style)
4. [Spring Boot Patterns](#spring-boot-patterns)
5. [MongoDB Patterns](#mongodb-patterns)
6. [HTMX and Thymeleaf Patterns](#htmx-and-thymeleaf-patterns)
7. [Validation Patterns](#validation-patterns)
8. [Error Handling](#error-handling)
9. [Testing Requirements](#testing-requirements)
10. [Security Guidelines](#security-guidelines)
11. [API Design](#api-design)

---

## Technology Stack

| Component | Technology | Version                    |
|-----------|------------|----------------------------|
| Language | Java | 25 (with preview features) |
| Framework | Spring Boot | 3.5.x                      |
| Database | MongoDB | Latest                     |
| Build Tool | Gradle (Kotlin DSL) | 9.2.x                      |
| Frontend | Thymeleaf + HTMX | 2.0.x                      |
| Code Formatter | Google Java Format | 1.32.0                     |
| Testing | JUnit 5 + Mockito + AssertJ | Latest                     |

### Key Principles

- **Avoid JavaScript**: Use HTMX for dynamic interactions. JavaScript should only be used when strictly necessary (e.g., complex form building)
- **Server-side rendering**: Thymeleaf templates handle all HTML generation
- **Immutable data**: Use Java records for models and DTOs
- **Virtual threads**: Enabled for improved concurrency

---

## Project Structure

### Directory Layout

```
lumina/
├── application/                    # Main Spring Boot application
│   ├── src/main/
│   │   ├── java/com/lumina/       # Java source code
│   │   └── resources/
│   │       ├── templates/         # Thymeleaf templates
│   │       ├── static/            # CSS, images, minimal JS
│   │       └── application.yml    # Configuration
│   ├── src/test/
│   │   └── java/com/lumina/       # Test source code
│   └── build.gradle.kts           # Module build config
├── infrastructure/                 # AWS CDK infrastructure
├── build.gradle.kts               # Root build config
└── settings.gradle.kts            # Gradle settings
```

### Package Organization (Domain-Driven Design)

Organize code by domain/feature, not by technical layer:

```
com.lumina/
├── {domain}/                      # One package per domain
│   ├── model/                     # Domain entities and value objects
│   ├── dto/                       # Data Transfer Objects
│   ├── validation/                # Domain-specific validators
│   ├── defaults/                  # Default/preset data
│   ├── {Domain}Controller.java   # REST/MVC controller
│   ├── {Domain}Service.java      # Business logic
│   └── {Domain}Repository.java   # Data access
├── validation/                    # Cross-cutting validation utilities
├── web/                           # Web UI controllers
├── status/                        # Shared enums/constants
├── SecurityConfig.java            # Security configuration
├── MongoConfig.java               # MongoDB configuration
├── AuditConfig.java               # Auditing configuration
├── ErrorHandlingAdvice.java       # Global error handling
└── MeterConfigApplication.java    # Application entry point
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Entity/Model | `{Entity}` | `Client`, `Meter`, `CatalogueItem` |
| DTO (create) | `New{Entity}Dto` | `NewClientDto` |
| DTO (update) | `Update{Entity}Dto` | `UpdateClientDto` |
| DTO (response) | `{Entity}Dto` | `ClientDto` |
| Service | `{Entity}Service` | `ClientService` |
| Repository | `{Entity}Repository` | `ClientRepository` |
| Controller | `{Entity}Controller` | `ClientController` |
| Validator | `{Entity}Validator` | `MeterValidator` |
| Exception | `{Description}Exception` | `NotFoundException` |

---

## Java Code Style

### Code Formatting

- **Formatter**: Google Java Format (enforced via Spotless)
- **Indentation**: 2 spaces
- **Run before commit**: `./gradlew spotlessApply`

### Record Types (Preferred for Data Classes)

Use Java records for immutable data structures:

```java
// DO: Use records with @RecordBuilder for models
@Document(collection = "client")
@TypeAlias("Client")
@RecordBuilder
public record Client(
    @Id String id,
    String name,
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'project':?#{#self.id}}")
    List<Project> projects,
    @CreatedDate Instant createdAt,
    @LastModifiedDate Instant updatedAt,
    @CreatedBy String createdBy,
    @LastModifiedBy String updatedBy
) implements ClientBuilder.With {}

// DO: Use records for DTOs
public record NewClientDto(
    @NotBlank(message = "Name is required")
    String name
) {
    public Client toModel() {
        return new Client(null, name, List.of(), null, null, null, null);
    }
}

// DO: Use records for value objects
public record Violation(String field, String message) {}
```

### Dependency Injection

Always use constructor injection:

```java
// DO: Constructor injection
@Service
public class ClientService {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }
}

// DON'T: Field injection
@Service
public class ClientService {
    @Autowired  // Never do this
    private ClientRepository repository;
}
```

### Optional Handling

Use `Optional` for nullable return values:

```java
// DO: Return Optional for nullable lookups
public Optional<Client> findById(String id) {
    return repository.findById(id);
}

// DO: Use Optional methods
client.findById(id)
    .map(ClientDto::from)
    .orElseThrow(() -> new NotFoundException("Client not found"));

// DON'T: Return null
public Client findById(String id) {
    return repository.findById(id).orElse(null);  // Never do this
}
```

### Stream Operations

Use streams for collection processing:

```java
// DO: Use streams with method references
List<ClientDto> dtos = clients.stream()
    .map(ClientDto::from)
    .toList();

// DO: Use streams for filtering
List<Meter> active = meters.stream()
    .filter(m -> m.status() == Status.ACTIVE)
    .toList();
```

### Constants and Enums

```java
// DO: Use enums for fixed sets of values
public enum Status {
    INTAKE,
    CONNECTION,
    APPROVAL,
    OPERATION
}

// DO: Use enums with metadata
public enum ErrorCode {
    LESS_THAN("Value must be greater than {0}, got {1}"),
    GREATER_THAN("Value must be less than {0}, got {1}"),
    NOT_INTEGER("Value must be an integer, got {0}");

    private final String messageTemplate;

    ErrorCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String format(Object... args) {
        return MessageFormat.format(messageTemplate, args);
    }
}
```

---

## Spring Boot Patterns

### Controller Architecture

The application follows a strict separation between REST API controllers and MVC view controllers:

#### Architecture Principles

1. **REST Controllers** (`@RestController`) handle all JSON API endpoints under `/api/*`
2. **MVC Controller** (`@Controller`) handles only HTML view rendering via Thymeleaf
3. **No duplicate endpoints** - HTMX/JavaScript calls REST APIs directly, not custom web endpoints

#### Controller Responsibilities

| Controller Type | Responsibility | Base Path | Response Type |
|-----------------|---------------|-----------|---------------|
| REST Controllers | JSON API endpoints | `/api/*` | JSON (DTOs) |
| WebController | HTML page rendering | `/`, `/clients`, etc. | HTML (Thymeleaf) |

#### Why This Matters

**DON'T** create duplicate JSON endpoints in WebController:
```java
// BAD: Duplicates ClientController functionality
@Controller
public class WebController {
    @GetMapping("/web/api/clients")
    @ResponseBody
    public List<Map<String, String>> getClients() {  // Avoid this!
        return clientService.findAll().stream()
            .map(c -> Map.of("id", c.id(), "name", c.name()))
            .toList();
    }
}
```

**DO** use existing REST endpoints from JavaScript/HTMX:
```javascript
// GOOD: Calls the canonical REST API
async function loadClients() {
    const response = await fetch('/api/client');
    const clients = await response.json();
    // ...
}
```

**DO** add missing endpoints to REST controllers when needed:
```java
// GOOD: Add to the domain's REST controller
@RestController
@RequestMapping("/api/")
public class ProjectController {
    @GetMapping("project/client/{clientId}")
    public List<ProjectDto> getByClientId(@PathVariable String clientId) {
        return projectService.findByClientId(clientId).stream()
            .map(ProjectDto::from)
            .toList();
    }
}
```

#### Benefits

- **Single source of truth** for each API operation
- **Consistent response format** via DTOs
- **Reduced maintenance burden** - changes in one place
- **Better API documentation** - OpenAPI annotations in REST controllers
- **Easier testing** - test each endpoint once

### REST Controller Pattern

```java
@RestController
@RequestMapping("/api/")
@Tag(name = "Client", description = "Client management APIs")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("client")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new client")
    public ClientDto create(@RequestBody @Valid NewClientDto newClient) {
        Client saved = clientService.create(newClient.toModel());
        return ClientDto.from(saved);
    }

    @GetMapping("client/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ClientDto> getById(@PathVariable String id) {
        return clientService.findById(id)
            .map(ClientDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("client")
    public ClientDto update(@RequestBody @Valid UpdateClientDto updateClient) {
        Client updated = clientService.update(updateClient.toModel());
        return ClientDto.from(updated);
    }

    @DeleteMapping("client/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        clientService.delete(id);
    }

    @GetMapping("clients")
    public Page<ClientDto> getAll(Pageable pageable) {
        return clientService.findAll(pageable).map(ClientDto::from);
    }
}
```

### MVC Controller Pattern (for Thymeleaf)

```java
@Controller
public class WebController {
    private final ClientService clientService;

    public WebController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("clients", clientService.findAll());
        return "index";
    }

    // Return HTML fragments for HTMX
    @GetMapping("/fragments/client/{id}")
    public String clientDetail(@PathVariable String id, Model model) {
        Client client = clientService.findById(id)
            .orElseThrow(() -> new NotFoundException("Client not found"));
        model.addAttribute("client", client);
        return "fragments/client-detail :: content";
    }
}
```

### Service Pattern

```java
@Service
public class ClientService {
    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public Client create(Client client) {
        return repository.save(client);
    }

    public Client update(Client client) {
        if (!repository.existsById(client.id())) {
            throw new NotFoundException("Client with id " + client.id() + " not found");
        }
        return repository.save(client);
    }

    public Optional<Client> findById(String id) {
        return repository.findById(id);
    }

    public List<Client> findAll() {
        return repository.findAll();
    }

    public Page<Client> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }
}
```

### HTTP Status Codes

| Operation | Success Code | Error Codes |
|-----------|--------------|-------------|
| Create | 201 Created | 400 Bad Request, 409 Conflict |
| Read | 200 OK | 404 Not Found |
| Update | 200 OK | 400 Bad Request, 404 Not Found |
| Delete | 204 No Content | 404 Not Found |
| Validation Error | - | 400 Bad Request |

---

## MongoDB Patterns

### Document Models

```java
@Document(collection = "meter")
@TypeAlias("Meter")
@RecordBuilder
public record Meter(
    @Id String id,
    String model,
    String serialNumber,
    Status status,

    // Relationships
    @DocumentReference
    Client client,

    @DocumentReference
    Project project,

    // Audit fields (always include)
    @CreatedDate Instant createdAt,
    @LastModifiedDate Instant updatedAt,
    @CreatedBy String createdBy,
    @LastModifiedBy String updatedBy
) implements MeterBuilder.With {}
```

### Repository Interface

```java
public interface MeterRepository extends MongoRepository<Meter, String> {

    // Simple queries - use method naming
    Optional<Meter> findBySerialNumber(String serialNumber);
    List<Meter> findByStatus(Status status);
    List<Meter> findByClientId(String clientId);

    // Complex queries - use @Query annotation
    @Query("{'status': ?0, 'client.$id': ?1}")
    List<Meter> findByStatusAndClient(Status status, String clientId);

    // Existence checks
    boolean existsBySerialNumber(String serialNumber);
}
```

### Audit Configuration

All documents should include audit fields. Enable auditing:

```java
@Configuration
@EnableMongoAuditing
public class AuditConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
```

---

## HTMX and Thymeleaf Patterns

### Layout Template Structure

```html
<!-- layout.html - Master template -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:fragment="head">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lumina</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
    <!-- HTMX - always include -->
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
    <script src="https://unpkg.com/htmx.org@2.0.4/dist/ext/json-enc.js"></script>
</head>
<body>
    <nav th:fragment="navbar"><!-- Navigation --></nav>
    <footer th:fragment="footer"><!-- Footer --></footer>
</body>
</html>
```

### Page Template Structure

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{layout :: head}"></head>
<body>
    <nav th:replace="~{layout :: navbar}"></nav>

    <main>
        <!-- Page content -->
    </main>

    <footer th:replace="~{layout :: footer}"></footer>
</body>
</html>
```

### HTMX Patterns

#### Form Submission with JSON

```html
<!-- Use json-enc extension for JSON payloads -->
<form hx-post="/api/client"
      hx-ext="json-enc"
      hx-target="#result"
      hx-swap="innerHTML"
      hx-on::after-request="if(event.detail.successful) { closeModal(); }">

    <input type="text" name="name" required>
    <button type="submit">Create</button>
</form>
```

#### Loading Content Dynamically

```html
<!-- Load fragment into target -->
<button th:hx-get="@{/fragments/client/{id}(id=${client.id})}"
        hx-target="#detail-panel"
        hx-swap="innerHTML">
    View Details
</button>

<!-- Target container -->
<div id="detail-panel">
    <!-- Content loaded here -->
</div>
```

#### Modal Pattern

```html
<!-- Trigger button -->
<button th:hx-get="@{/fragments/edit-form/{id}(id=${item.id})}"
        hx-target="#modal-content"
        hx-swap="innerHTML"
        onclick="openModal()">
    Edit
</button>

<!-- Modal container -->
<div id="modal-overlay" class="hidden">
    <div id="modal-content">
        <!-- Form loaded here via HTMX -->
    </div>
</div>
```

#### Table Row Updates

```html
<!-- Update single row after edit -->
<tr th:id="'row-' + ${item.id}">
    <td th:text="${item.name}">Name</td>
    <td>
        <button th:hx-get="@{/fragments/row/{id}(id=${item.id})}"
                hx-target="closest tr"
                hx-swap="outerHTML">
            Refresh
        </button>
    </td>
</tr>
```

### Thymeleaf Best Practices

```html
<!-- DO: Use th:each for loops -->
<tr th:each="client : ${clients}">
    <td th:text="${client.name}">Client Name</td>
</tr>

<!-- DO: Use th:if/th:unless for conditionals -->
<span th:if="${#lists.isEmpty(clients)}">No clients found</span>

<!-- DO: Use @{} for URLs with parameters -->
<a th:href="@{/client/{id}(id=${client.id})}">View</a>

<!-- DO: Use #temporals for date formatting -->
<span th:text="${#temporals.format(item.createdAt, 'yyyy-MM-dd HH:mm')}">Date</span>

<!-- DO: Use th:classappend for conditional classes -->
<div th:classappend="${isActive} ? 'active' : ''">Content</div>

<!-- DON'T: Use inline JavaScript when HTMX suffices -->
<button onclick="loadData()">Load</button>  <!-- Avoid this -->
```

### When JavaScript is Acceptable

JavaScript should only be used for:

1. **Complex form building** (e.g., dynamic constraint arrays)
2. **Modal open/close state management**
3. **Client-side validation feedback**
4. **Third-party library integration**

```javascript
// Acceptable: Complex form data assembly
function buildConstraintsArray() {
    const constraints = [];
    document.querySelectorAll('.constraint-item').forEach((item) => {
        constraints.push({
            type: item.querySelector('.type-select').value,
            name: item.querySelector('.name-input').value
        });
    });
    return constraints;
}

// Acceptable: Modal management
function openModal() {
    document.getElementById('modal-overlay').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
}
```

---

## Validation Patterns

### Jakarta Bean Validation (DTOs)

```java
public record NewClientDto(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @Email(message = "Invalid email format")
    String email,

    @Pattern(regexp = "^[A-Z]{2}\\d{6}$", message = "Invalid reference format")
    String reference
) {}
```

### Custom Validators

```java
// Custom annotation
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueModelValidator.class)
public @interface UniqueModel {
    String message() default "Model already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validator implementation
public class UniqueModelValidator implements ConstraintValidator<UniqueModel, String> {
    private final ItemRepository repository;

    public UniqueModelValidator(ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return !repository.existsByModel(value);
    }
}
```

### Business Logic Validation (Errors Accumulation)

For complex, multi-field validation:

```java
@Component
public class MeterValidator {

    public void validate(Meter meter, Errors errors) {
        // Cross-field validation
        if (meter.status() == Status.OPERATION && meter.client() == null) {
            errors.rejectValue("client", ErrorCode.REQUIRED);
        }

        // Nested object validation
        if (meter.configuration() != null) {
            errors.pushContext("configuration");
            validateConfiguration(meter.configuration(), errors);
            errors.popContext();
        }
    }

    private void validateConfiguration(Configuration config, Errors errors) {
        if (config.maxValue() <= config.minValue()) {
            errors.rejectValue("maxValue", ErrorCode.LESS_THAN,
                config.minValue(), config.maxValue());
        }
    }
}

// Service usage
public Meter update(Meter meter) {
    Errors errors = new Errors("meter");
    meterValidator.validate(meter, errors);

    if (errors.hasErrors()) {
        throw new LuminaValidationException(errors);
    }

    return repository.save(meter);
}
```

---

## Error Handling

### Exception Classes

```java
// Not found
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

// Duplicate resource
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

// Business validation errors
public class LuminaValidationException extends RuntimeException {
    private final Errors errors;

    public LuminaValidationException(Errors errors) {
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }
}
```

### Global Exception Handler

```java
@ControllerAdvice
public class ErrorHandlingAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onConstraintViolation(ConstraintViolationException e) {
        List<Violation> violations = e.getConstraintViolations().stream()
            .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
            .toList();
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
            .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
            .toList();
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(LuminaValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorResponse onLuminaValidation(LuminaValidationException e) {
        List<Violation> violations = e.getErrors().getFieldErrors().stream()
            .map(error -> new Violation(error.field(), error.errorCode().format()))
            .toList();
        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ValidationErrorResponse onNotFound(NotFoundException e) {
        return new ValidationErrorResponse(
            List.of(new Violation("resource", e.getMessage()))
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ValidationErrorResponse onDuplicateResource(DuplicateResourceException e) {
        return new ValidationErrorResponse(
            List.of(new Violation("resource", e.getMessage()))
        );
    }
}
```

### Error Response Format

```json
{
  "errors": [
    {"field": "name", "message": "Name is required"},
    {"field": "email", "message": "Invalid email format"}
  ]
}
```

---

## Testing Requirements

### Mandatory Testing Policy

1. **All new code must have tests**
2. **When modifying existing code, add tests if missing**
3. **Minimum test coverage**: Service layer 80%, Controller layer 70%

### Unit Test Pattern (Services)

```java
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = new Client(
            "client-1",
            "Test Client",
            List.of(),
            Instant.now(),
            Instant.now(),
            "user",
            "user"
        );
    }

    @Test
    @DisplayName("create() should save and return client")
    void createShouldSaveAndReturnClient() {
        when(repository.save(any(Client.class))).thenReturn(testClient);

        Client result = clientService.create(testClient);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Client");
        verify(repository).save(testClient);
    }

    @Test
    @DisplayName("findById() should return Optional.empty when not found")
    void findByIdShouldReturnEmptyWhenNotFound() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        Optional<Client> result = clientService.findById("non-existent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("update() should throw NotFoundException when client doesn't exist")
    void updateShouldThrowNotFoundExceptionWhenClientDoesNotExist() {
        when(repository.existsById("non-existent")).thenReturn(false);
        Client nonExistent = testClient.withId("non-existent");

        assertThatThrownBy(() -> clientService.update(nonExistent))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("not found");
    }
}
```

### Integration Test Pattern (Controllers)

```java
@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientService clientService;

    @Test
    @DisplayName("POST /api/client should return 201 Created")
    void createClientShouldReturn201() throws Exception {
        NewClientDto dto = new NewClientDto("New Client");
        Client saved = new Client("client-1", "New Client", List.of(),
            Instant.now(), Instant.now(), "user", "user");

        when(clientService.create(any(Client.class))).thenReturn(saved);

        mockMvc.perform(post("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("client-1"))
            .andExpect(jsonPath("$.name").value("New Client"));
    }

    @Test
    @DisplayName("GET /api/client/{id} should return 404 when not found")
    void getClientByIdShouldReturn404WhenNotFound() throws Exception {
        when(clientService.findById("non-existent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/client/non-existent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/client should return 400 for invalid input")
    void createClientShouldReturn400ForInvalidInput() throws Exception {
        NewClientDto dto = new NewClientDto("");  // Empty name

        mockMvc.perform(post("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isArray());
    }
}
```

### Test Naming Convention

- Method: `{methodName}Should{ExpectedBehavior}When{Condition}`
- `@DisplayName`: Human-readable description

```java
@Test
@DisplayName("update() should throw NotFoundException when meter doesn't exist")
void updateShouldThrowNotFoundExceptionWhenMeterDoesNotExist() { }

@Test
@DisplayName("findByStatus() should return empty list when no meters match")
void findByStatusShouldReturnEmptyListWhenNoMetersMatch() { }
```

### Assertions (AssertJ)

```java
// DO: Use AssertJ fluent assertions
assertThat(result).isNotNull();
assertThat(result.name()).isEqualTo("Expected");
assertThat(results).hasSize(3);
assertThat(results).extracting(Client::name).containsExactly("A", "B", "C");
assertThatThrownBy(() -> service.delete("invalid"))
    .isInstanceOf(NotFoundException.class);

// DON'T: Use JUnit assertions
assertEquals("Expected", result.name());  // Avoid
assertTrue(result != null);  // Avoid
```

---

## Security Guidelines

### Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @ConditionalOnProperty(name = "lumina.security.enabled", havingValue = "true")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .csrf(csrf -> csrf.disable());  // API-only, CSRF not needed

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "lumina.security.enabled", havingValue = "false")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
```

### Security Best Practices

1. **Never log sensitive data** (passwords, tokens, PII)
2. **Validate all input** at API boundaries
3. **Use parameterized queries** (Spring Data handles this)
4. **Escape output** in templates (Thymeleaf does this by default)
5. **Set appropriate CORS headers** for production

---

## API Design

### REST Endpoints

| Method | Path | Description | Status Codes |
|--------|------|-------------|--------------|
| POST | `/api/{entity}` | Create new | 201, 400, 409 |
| GET | `/api/{entity}/{id}` | Get by ID | 200, 404 |
| GET | `/api/{entities}` | List all (paginated) | 200 |
| PUT | `/api/{entity}` | Update existing | 200, 400, 404 |
| DELETE | `/api/{entity}/{id}` | Delete | 204, 404 |

### OpenAPI Documentation

```java
@RestController
@RequestMapping("/api/")
@Tag(name = "Client", description = "Client management APIs")
public class ClientController {

    @PostMapping("client")
    @Operation(summary = "Create a new client")
    @ApiResponse(responseCode = "201", description = "Client created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Client already exists")
    public ClientDto create(@RequestBody @Valid NewClientDto newClient) {
        // ...
    }
}
```

### Pagination

```java
// Controller
@GetMapping("clients")
public Page<ClientDto> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "name") String sort
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    return clientService.findAll(pageable).map(ClientDto::from);
}

// Response includes pagination metadata
{
  "content": [...],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Checklist for Pull Requests

Before submitting a PR, ensure:

- [ ] Code follows Google Java Format (`./gradlew spotlessCheck`)
- [ ] All new code has corresponding tests
- [ ] Tests pass (`./gradlew test`)
- [ ] No new warnings or errors in build
- [ ] DTOs use records with validation annotations
- [ ] Services use constructor injection
- [ ] Controllers return appropriate HTTP status codes
- [ ] Error handling follows established patterns
- [ ] HTMX used instead of JavaScript where possible
- [ ] Thymeleaf templates follow fragment patterns
- [ ] MongoDB documents include audit fields
- [ ] API endpoints are documented with OpenAPI annotations

---

## Running Quality Checks

```bash
# Format code
./gradlew spotlessApply

# Run tests
./gradlew test

# Run all checks
./gradlew check

# Generate test coverage report
./gradlew jacocoTestReport
```
