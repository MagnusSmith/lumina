# Higher-Kinded-J and Immutables Integration Analysis

## Executive Summary

This document analyzes the potential integration of [Higher-Kinded-J](https://higher-kinded-j.github.io/) (v0.3.4) and [Immutables](https://immutables.github.io/) (v2.12.1) with the Lumina codebase, identifying concrete advantages and proposing a phased implementation approach.

---

## 1. Library Overview

### 1.1 Higher-Kinded-J (HKJ)

Higher-Kinded-J is a functional programming library that provides:

| Feature | Description |
|---------|-------------|
| **Effect Path API** | Composable error handling via railway-oriented programming |
| **Focus DSL** | Type-safe navigation through immutable data structures |
| **Optics** | Lenses, Prisms, Traversals, Isos for data manipulation |
| **Spring Boot Starter** | Auto-conversion of functional types to HTTP responses |
| **Type Classes** | Functor, Applicative, Monad implementations |
| **Virtual Threads** | VTask for lightweight concurrency on Java 25 |

**Available Effect Types:**
- `MaybePath<A>` - Optional values
- `EitherPath<E, A>` - Typed errors (Left=error, Right=success)
- `TryPath<A>` - Exception wrapping
- `ValidationPath<E, A>` - Error accumulation (collects ALL errors)
- `IOPath<A>` - Deferred side effects
- `VTaskPath<A>` - Virtual thread-based async
- `CompletableFuturePath<A>` - Async operations
- `ReaderPath<R, A>` - Dependency injection
- `WriterPath<W, A>` - Logging/audit trails

### 1.2 Immutables

Immutables is an annotation processor that generates:

| Feature | Description |
|---------|-------------|
| **Immutable Classes** | Thread-safe implementations from interfaces |
| **Builders** | Fluent builder pattern with validation |
| **Copy Methods** | `with*()` methods for creating modified instances |
| **Derived Attributes** | Computed values cached after first access |
| **Lazy Attributes** | Deferred computation |
| **Jackson Integration** | Automatic serialization/deserialization |
| **Style Customization** | Naming patterns, validation hooks |

---

## 2. Current Lumina Architecture

### 2.1 Existing Patterns

Lumina already employs modern Java patterns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Current Architecture                      │
├─────────────────────────────────────────────────────────────┤
│ Java Version    │ 25 (with preview features)                │
│ Immutability    │ Java Records + RecordBuilder              │
│ Type Safety     │ Sealed interfaces + pattern matching      │
│ Null Safety     │ Optional everywhere                       │
│ Validation      │ Custom Errors class (error accumulation)  │
│ Error Handling  │ Exceptions + @ControllerAdvice            │
│ Persistence     │ Spring Data MongoDB                       │
│ Async           │ None (blocking I/O)                       │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Current Domain Models

```java
// Current pattern: Records + RecordBuilder
@RecordBuilder
@Document(collection = "meter")
public record Meter(
    @Id String id,
    String locationId,
    String model,
    List<Line> lines,
    ValidationStage stage) {}

// Sealed interface hierarchy
public sealed interface Line permits Line.Number, Line.Text, Line.Pattern, Line.ReadOnly {
    record Number(String name, NumberType numberType, Double value) implements Line {}
    record Text(String name, String value) implements Line {}
    // ...
}
```

### 2.3 Current Validation Pattern

```java
// Current: Imperative error accumulation
public class Errors {
    private final List<Error> fieldErrors = new ArrayList<>();

    public Errors rejectIfEmpty(String field, Object value, ErrorCode code) {
        if (Objects.isNull(value) || StringUtils.isBlank(value.toString())) {
            add(ErrorBuilder.builder().field(field).rejectedValue(value).errorCode(code).build());
        }
        return this;
    }
}

// In service layer
public Meter create(Meter meter) {
    Errors errors = new Errors("meter");
    meterValidator.validate(meter, errors);
    if (errors.getErrorCount() > 0) {
        throw new LuminaValidationException(errors);
    }
    return repository.save(meter);
}
```

### 2.4 Current Data Navigation

```java
// Current: Sequential Optional chains with orElseThrow
public MeterViewDto toMeterViewDto(Meter meter) {
    var location = locationService.findById(meter.locationId())
        .orElseThrow(() -> new NotFoundException("Location not found"));
    var project = projectService.findById(location.projectId())
        .orElseThrow(() -> new NotFoundException("Project not found"));
    var client = clientService.findById(project.clientId())
        .orElseThrow(() -> new NotFoundException("Client not found"));
    return MeterViewDto.from(meter, location, project, client);
}
```

---

## 3. Advantages of Integration

### 3.1 Higher-Kinded-J Benefits

#### A. Effect Path API - Railway-Oriented Error Handling

**Before (current):**
```java
public Meter create(Meter meter) {
    Errors errors = new Errors("meter");
    meterValidator.validate(meter, errors);
    if (errors.getErrorCount() > 0) {
        throw new LuminaValidationException(errors);
    }
    return repository.save(meter);
}
```

**After (with HKJ):**
```java
public EitherPath<ValidationErrors, Meter> create(Meter meter) {
    return validateMeter(meter)                    // ValidationPath<Error, Meter>
        .toEither()                                // EitherPath<List<Error>, Meter>
        .via(validMeter -> repository.save(validMeter));
}

// In controller - automatic HTTP conversion
@PostMapping
public EitherPath<ValidationErrors, MeterDto> createMeter(@RequestBody MeterDto dto) {
    return meterService.create(dto.toMeter())
        .map(MeterDto::from);
    // Left(errors) -> HTTP 400 with JSON error list
    // Right(meter) -> HTTP 201 with meter JSON
}
```

**Advantages:**
- Explicit error handling in types (no hidden exceptions)
- Composable operations via `map`, `via`, `recover`
- Automatic HTTP response conversion (Spring Boot starter)
- No try-catch blocks needed

#### B. ValidationPath - Accumulating All Errors

**Before (current - already accumulates):**
```java
errors.rejectIfEmpty("locationId", meter.locationId(), NOT_EMPTY);
errors.rejectIfEmpty("model", meter.model(), NOT_EMPTY);
// Manual accumulation in mutable list
```

**After (with HKJ):**
```java
public ValidationPath<Error, Meter> validateMeter(Meter meter) {
    return ValidationPath.valid(MeterBuilder::builder)
        .ap(validateLocationId(meter.locationId()))
        .ap(validateModel(meter.model()))
        .ap(validateLines(meter.lines()))
        .ap(validateStage(meter.stage()))
        .map(MeterBuilder::build);
}

private ValidationPath<Error, String> validateLocationId(String id) {
    return StringUtils.isBlank(id)
        ? ValidationPath.invalid(Error.of("locationId", NOT_EMPTY))
        : ValidationPath.valid(id);
}
```

**Advantages:**
- Applicative style collects ALL errors (not just first)
- Type-safe composition
- Each validation is a pure function
- Easily testable in isolation

#### C. Focus DSL + Optics - Type-Safe Data Navigation

**Before (current):**
```java
// Update nested data requires manual reconstruction
Meter updateLineValue(Meter meter, String lineName, String newValue) {
    List<Line> updatedLines = meter.lines().stream()
        .map(line -> {
            if (line instanceof Line.Text text && text.name().equals(lineName)) {
                return new Line.Text(text.name(), newValue);
            }
            return line;
        })
        .toList();
    return MeterBuilder.builder()
        .from(meter)
        .lines(updatedLines)
        .build();
}
```

**After (with HKJ Optics):**
```java
// Generated optics for records
@Optics
public record Meter(String id, String locationId, String model,
                    List<Line> lines, ValidationStage stage) {}

// Type-safe, composable updates
Meter updateLineValue(Meter meter, String lineName, String newValue) {
    return MeterFocus.lines()
        .each()
        .filterWhen(line -> line.name().equals(lineName))
        .composeWith(LineFocus.text().value())
        .set(meter, newValue);
}
```

**Advantages:**
- Generated type-safe accessors for nested data
- Composable transformations
- Works with sealed interface hierarchies (Prisms)
- Eliminates boilerplate reconstruction code

#### D. Spring Boot Controller Integration

**Current:**
```java
@PostMapping
public ResponseEntity<MeterDto> create(@RequestBody @Valid MeterDto dto) {
    try {
        var meter = meterService.create(dto.toMeter());
        return ResponseEntity.status(HttpStatus.CREATED).body(MeterDto.from(meter));
    } catch (LuminaValidationException e) {
        // Handled by @ControllerAdvice
        throw e;
    }
}
```

**With HKJ Spring Boot Starter:**
```java
@PostMapping
public EitherPath<DomainError, MeterDto> create(@RequestBody MeterDto dto) {
    return meterService.create(dto.toMeter())
        .map(MeterDto::from);
}
// Automatic conversion:
// - Right(dto) -> HTTP 200/201 with JSON body
// - Left(error) -> HTTP 4xx with error details
```

### 3.2 Immutables Benefits

#### A. Rich Builder Features Beyond RecordBuilder

**Current (RecordBuilder):**
```java
@RecordBuilder
public record CatalogueItem(String model, Level level, Type type,
                            String description, List<Constraint<?>> constraints) {}

// Basic builder
CatalogueItemBuilder.builder()
    .model("A0001")
    .level(Level.DEVICE)
    .build(); // No validation!
```

**With Immutables:**
```java
@Value.Immutable
@Value.Style(
    visibility = ImplementationVisibility.PACKAGE,
    builderVisibility = BuilderVisibility.PUBLIC
)
public interface CatalogueItem {
    String model();
    Level level();
    Type type();
    String description();
    List<Constraint<?>> constraints();

    @Value.Default
    default List<Line> defaultLines() {
        return List.of();
    }

    @Value.Derived
    default String displayName() {
        return "%s (%s)".formatted(model(), manufacturer());
    }

    @Value.Check
    default CatalogueItem validate() {
        Preconditions.checkState(!model().isBlank(), "model required");
        return this;
    }
}

// Rich builder with validation
ImmutableCatalogueItem.builder()
    .model("A0001")
    .level(Level.DEVICE)
    .build(); // Runs @Value.Check automatically!
```

**Advantages:**
- `@Value.Default` - Default values without null checks
- `@Value.Derived` - Computed/cached values
- `@Value.Lazy` - Deferred computation
- `@Value.Check` - Builder-time validation
- `@Value.Redacted` - Hide sensitive data in toString()

#### B. Jackson Integration (Cleaner than Records)

**Current (Records + Jackson):**
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Line.Number.class, name = "number"),
    @JsonSubTypes.Type(value = Line.Text.class, name = "text")
})
public sealed interface Line permits Line.Number, Line.Text {...}
```

**With Immutables:**
```java
@Value.Immutable
@JsonSerialize(as = ImmutableLine.class)
@JsonDeserialize(as = ImmutableLine.class)
public interface Line {
    String name();
    String value();

    @Value.Redacted  // Won't appear in toString/JSON
    Optional<String> sensitiveData();
}
```

#### C. Copy Methods (with-methods)

**Current (RecordBuilder):**
```java
// Manual copy via builder
Meter updated = MeterBuilder.builder()
    .from(originalMeter)
    .stage(ValidationStage.Deployment)
    .build();
```

**With Immutables:**
```java
// Generated with-methods
Meter updated = originalMeter.withStage(ValidationStage.Deployment);

// Chained updates
Meter updated = originalMeter
    .withStage(ValidationStage.Deployment)
    .withLines(newLines);
```

### 3.3 Combined Benefits: HKJ + Immutables

Higher-Kinded-J explicitly supports Immutables via spec interfaces:

```java
// HKJ generates optics for Immutables objects
@Optics
@Value.Immutable
public interface Order {
    String orderId();
    Customer customer();
    List<OrderLine> lines();
}

// Compose optics with effects
public EitherPath<Error, Order> updateCustomerEmail(
    Order order, String newEmail) {

    return validateEmail(newEmail)
        .map(validEmail ->
            OrderFocus.customer()
                .composeWith(CustomerFocus.email())
                .set(order, validEmail));
}
```

---

## 4. Gap Analysis

| Aspect | Current Lumina | With HKJ + Immutables |
|--------|---------------|----------------------|
| **Error Handling** | Exceptions + manual accumulation | Type-safe Effects (EitherPath, ValidationPath) |
| **Validation** | Imperative Errors class | Applicative ValidationPath (composable) |
| **Data Updates** | Manual record reconstruction | Generated optics (Lenses, Prisms) |
| **Null Safety** | Optional everywhere | Optional + MaybePath with effect composition |
| **Async** | None (blocking) | VTaskPath, CompletableFuturePath |
| **Builder Validation** | None (RecordBuilder) | @Value.Check auto-validation |
| **Computed Fields** | Manual methods | @Value.Derived, @Value.Lazy |
| **Controller Returns** | ResponseEntity + exceptions | Direct effect types with auto-conversion |
| **Testing** | Mockito + exceptions | Pure functions, property-based testing |

---

## 5. Phased Implementation Approach

### Phase 1: Foundation (2-3 weeks)

**Goal:** Add dependencies and validate compatibility without breaking changes.

#### 5.1.1 Add Dependencies

```kotlin
// build.gradle.kts additions
repositories {
    mavenCentral()
}

dependencies {
    // Higher-Kinded-J core
    implementation("io.github.higher-kinded-j:hkj-core:0.3.4")
    annotationProcessor("io.github.higher-kinded-j:hkj-processor-plugins:0.3.4")

    // Immutables
    implementation("org.immutables:value-annotations:2.12.1")
    annotationProcessor("org.immutables:value:2.12.1")

    // Keep RecordBuilder for existing code (gradual migration)
    implementation("io.soabase.record-builder:record-builder-core:51")
    annotationProcessor("io.soabase.record-builder:record-builder-processor:51")
}
```

#### 5.1.2 Create Proof-of-Concept Models

Create new models alongside existing ones:

```java
// New: com.lumina.catalogue.model.v2.CatalogueItemV2
@Value.Immutable
@Value.Style(
    visibility = ImplementationVisibility.PACKAGE,
    builderVisibility = BuilderVisibility.PUBLIC,
    defaults = @Value.Immutable(copy = true)
)
public interface CatalogueItemV2 {
    String model();
    Level level();
    Type type();
    String description();

    @Value.Default
    default List<Constraint<?>> constraints() {
        return List.of();
    }

    @Value.Derived
    default String displayName() {
        return "%s - %s".formatted(model(), description());
    }
}
```

#### 5.1.3 Create Effect-Based Validation POC

```java
// New: com.lumina.validation.v2.MeterValidation
public class MeterValidation {

    public static ValidationPath<Error, Meter> validate(Meter meter) {
        return ValidationPath.valid(MeterBuilder::builder)
            .ap(validateLocationId(meter.locationId()))
            .ap(validateModel(meter.model()))
            .ap(validateStage(meter.stage()))
            .ap(validateLines(meter.lines()))
            .map(MeterBuilder::build);
    }

    private static ValidationPath<Error, String> validateLocationId(String id) {
        return StringUtils.isBlank(id)
            ? ValidationPath.invalid(Error.of("locationId", NOT_EMPTY))
            : ValidationPath.valid(id);
    }
}
```

#### 5.1.4 Deliverables
- [ ] Dependencies added and compiling
- [ ] One Immutables model created (CatalogueItemV2)
- [ ] One ValidationPath implementation (MeterValidation)
- [ ] Unit tests for new components
- [ ] Documentation of patterns

---

### Phase 2: Spring Integration (2-3 weeks)

**Goal:** Integrate HKJ Spring Boot Starter for automatic HTTP conversion.

#### 5.2.1 Add Spring Boot Starter

```kotlin
dependencies {
    implementation("io.github.higher-kinded-j:hkj-spring-boot-starter:0.3.4")
}
```

#### 5.2.2 Create Effect-Based Controller

```java
// New controller using effect types
@RestController
@RequestMapping("/api/v2/meter")
public class MeterControllerV2 {

    private final MeterServiceV2 meterService;

    @PostMapping
    public EitherPath<DomainError, MeterDto> create(@RequestBody MeterDto dto) {
        return meterService.create(dto.toMeter())
            .map(MeterDto::from);
    }

    @GetMapping("/{id}")
    public MaybePath<MeterDto> findById(@PathVariable String id) {
        return meterService.findById(id)
            .map(MeterDto::from);
    }
}
```

#### 5.2.3 Create Effect-Based Service

```java
@Service
public class MeterServiceV2 {

    public EitherPath<DomainError, Meter> create(Meter meter) {
        return MeterValidation.validate(meter)
            .toEither()
            .mapLeft(DomainError::validationErrors)
            .via(this::saveMeter);
    }

    private EitherPath<DomainError, Meter> saveMeter(Meter meter) {
        return TryPath.of(() -> repository.save(meter))
            .toEither()
            .mapLeft(DomainError::persistence);
    }
}
```

#### 5.2.4 Deliverables
- [ ] Spring Boot Starter integrated
- [ ] V2 controller with effect return types
- [ ] V2 service with effect-based error handling
- [ ] Integration tests for new endpoints
- [ ] Side-by-side operation with existing controllers

---

### Phase 3: Optics Integration (2-3 weeks)

**Goal:** Generate and use optics for type-safe data navigation.

#### 5.3.1 Add Optics Annotations

```java
@Optics
@Value.Immutable
public interface Meter {
    String id();
    String locationId();
    String model();
    List<Line> lines();
    ValidationStage stage();
}

// Sealed interface with Prism support
@Optics
public sealed interface Line permits NumberLine, TextLine, PatternLine {
    String name();
}

@Optics
@Value.Immutable
public interface NumberLine extends Line {
    NumberType numberType();
    Double value();
}
```

#### 5.3.2 Use Optics for Data Updates

```java
public class MeterOperations {

    // Update specific line by name
    public static Meter updateLineValue(Meter meter, String lineName, String newValue) {
        return MeterFocus.lines()
            .each()
            .filterWhen(line -> line.name().equals(lineName))
            .composeWith(TextLineFocus.value())
            .set(meter, newValue);
    }

    // Navigate hierarchy with effects
    public static EitherPath<Error, String> getCustomerName(Meter meter) {
        return MeterFocus.location()
            .composeWith(LocationFocus.project())
            .composeWith(ProjectFocus.client())
            .composeWith(ClientFocus.name())
            .getOrError(meter, Error.of("path", "Customer not found"));
    }
}
```

#### 5.3.3 Deliverables
- [ ] Optics generated for all domain models
- [ ] Optics-based update operations
- [ ] Effect + Optics composition examples
- [ ] Performance benchmarks vs manual navigation

---

### Phase 4: Full Migration (4-6 weeks)

**Goal:** Migrate existing code to new patterns.

#### 5.4.1 Model Migration Strategy

```
Priority Order:
1. CatalogueItem → ImmutableCatalogueItem (most isolated)
2. Constraint hierarchy → Immutables sealed interface
3. Error/Errors → DomainError with EitherPath
4. Line hierarchy → Immutables + Optics
5. Location/Project/Client → Immutables
6. Meter → Immutables + full optics
```

#### 5.4.2 Service Migration

```java
// Migrate MeterService methods one-by-one
@Service
public class MeterService {

    // Keep old method signature, delegate to new implementation
    public Meter create(Meter meter) {
        return createV2(meter)
            .run()  // Execute effect
            .fold(
                error -> { throw error.toException(); },
                Function.identity()
            );
    }

    // New implementation with effects
    public EitherPath<DomainError, Meter> createV2(Meter meter) {
        return MeterValidation.validate(meter)
            .toEither()
            .mapLeft(DomainError::validation)
            .via(repository::save);
    }
}
```

#### 5.4.3 Controller Migration

```java
// Gradually migrate endpoints
@RestController
@RequestMapping("/api/meter")
public class MeterController {

    // Old endpoint (deprecated)
    @Deprecated
    @PostMapping("/v1")
    public ResponseEntity<MeterDto> createV1(@RequestBody MeterDto dto) {
        // Old implementation
    }

    // New endpoint with effects
    @PostMapping
    public EitherPath<DomainError, MeterDto> create(@RequestBody MeterDto dto) {
        return meterService.createV2(dto.toMeter())
            .map(MeterDto::from);
    }
}
```

#### 5.4.4 Deliverables
- [ ] All domain models migrated to Immutables
- [ ] All services using effect-based error handling
- [ ] All controllers returning effect types
- [ ] RecordBuilder dependency removed
- [ ] Custom Errors class deprecated/removed
- [ ] Full test coverage

---

### Phase 5: Advanced Features (Ongoing)

**Goal:** Leverage advanced HKJ features for enhanced capabilities.

#### 5.5.1 Virtual Thread Integration (VTask)

```java
// Parallel data fetching with virtual threads
public VTaskPath<MeterViewDto> toMeterViewDtoAsync(Meter meter) {
    return VTaskPath.parMap3(
        () -> locationService.findByIdAsync(meter.locationId()),
        () -> catalogueService.findByModelAsync(meter.model()),
        () -> statusService.findByMeterIdAsync(meter.id()),
        (location, catalogue, status) -> MeterViewDto.from(meter, location, catalogue, status)
    );
}
```

#### 5.5.2 Reader Monad for Dependency Injection

```java
// Configuration-aware operations
public ReaderPath<AppConfig, EitherPath<Error, Meter>> validateWithConfig(Meter meter) {
    return ReaderPath.ask()
        .map(config -> MeterValidation.validate(meter, config.validationRules()));
}
```

#### 5.5.3 Writer Monad for Audit Logging

```java
// Automatic audit trail
public WriterPath<List<AuditEvent>, Meter> createWithAudit(Meter meter) {
    return WriterPath.of(meter)
        .tell(List.of(AuditEvent.created(meter)))
        .via(this::save)
        .tell(List.of(AuditEvent.saved(meter)));
}
```

---

## 6. Risk Assessment

| Risk | Mitigation |
|------|------------|
| **Learning curve** | Phase 1 POC allows team familiarization before full adoption |
| **Breaking changes** | V2 APIs run alongside V1; gradual migration |
| **Performance** | Benchmark optics vs manual code; HKJ optimized for Java 25 |
| **Dependency conflicts** | HKJ requires JDK 25 (Lumina already uses 25) |
| **Jackson compatibility** | Immutables has mature Jackson support; test with MongoDB |
| **MongoDB integration** | Test Immutables with Spring Data MongoDB early |

---

## 7. Success Metrics

1. **Code Quality**
   - Reduced cyclomatic complexity in services
   - Elimination of nested if-else for error handling
   - Reduced boilerplate for data transformation

2. **Error Handling**
   - 100% of validation errors accumulated (not fail-fast)
   - Explicit error types in method signatures
   - No unchecked exceptions in business logic

3. **Maintainability**
   - Generated optics reduce manual update code by ~60%
   - Immutable models with derived/default values
   - Composable validation rules

4. **Testing**
   - Pure functions enable property-based testing
   - Effect types enable deterministic testing
   - Reduced mocking requirements

---

## 8. Conclusion

The integration of Higher-Kinded-J and Immutables with Lumina offers significant advantages:

1. **Type-safe error handling** via Effect Path API eliminates exception-based control flow
2. **Validation accumulation** via ValidationPath improves user feedback
3. **Optics** reduce boilerplate for immutable data manipulation
4. **Spring Boot integration** simplifies controller implementation
5. **Immutables** provides richer builder features than RecordBuilder

The phased approach ensures:
- Zero disruption to existing functionality
- Team learning through POC development
- Gradual migration with rollback capability
- Measurable improvements at each phase

**Recommended next step:** Begin Phase 1 by adding dependencies and creating the CatalogueItemV2 proof-of-concept.

---

## References

- [Higher-Kinded-J Documentation](https://higher-kinded-j.github.io/)
- [Higher-Kinded-J GitHub](https://github.com/higher-kinded-j/higher-kinded-j)
- [Immutables Documentation](https://immutables.github.io/)
- [Immutables Getting Started](https://immutables.github.io/getstarted.html)
- [Railway-Oriented Programming](https://fsharpforfunandprofit.com/rop/)
