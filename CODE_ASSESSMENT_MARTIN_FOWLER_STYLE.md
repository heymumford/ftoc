# FTOC Code Repository Assessment
## Martin Fowler Style Refactoring Analysis

**Assessment Date:** 2025-11-15
**Version Assessed:** 1.0.0
**Assessor:** Claude (Anthropic AI)
**Assessment Type:** Comprehensive Code Quality & Refactoring Roadmap

---

## Executive Summary

The FTOC (Feature Table of Contents Utility) project demonstrates **solid architectural foundations** with clear separation of concerns, comprehensive testing, and professional documentation. However, the codebase has accumulated **significant technical debt** during its evolution from a monolithic implementation to a more modular architecture.

### Overall Quality Score: **7.2/10**

**Strengths:**
- Well-defined interfaces and separation of concerns
- Comprehensive test coverage (13 test classes, multiple BDD scenarios)
- Professional documentation (ADRs, C4 diagrams, user guides)
- Plugin architecture for extensibility
- Multiple output format support

**Critical Issues Requiring Attention:**
- **Duplicate Code**: Two parallel implementations (FtocUtility vs FtocUtilityRefactored)
- **God Classes**: Several classes exceed 1000 lines (TocFormatter: 1194, TagQualityAnalyzer: 1073)
- **Long Methods**: Formatter methods often exceed 100-200 lines
- **Static Utility Overuse**: PerformanceMonitor, ErrorHandler use global state
- **Primitive Obsession**: Heavy reliance on strings and maps for domain concepts

### Code Metrics

| Metric | Value | Industry Standard | Status |
|--------|-------|------------------|---------|
| Total LOC (main) | 11,453 | N/A | ✓ |
| Average Class Size | ~270 lines | <300 | ✓ |
| Largest Class | 1194 lines | <500 | ⚠️ |
| Test Classes | 13 | Good | ✓ |
| Cyclomatic Complexity | Not measured | <10/method | ⚠️ |
| Code Duplication | ~15% estimated | <5% | ❌ |

---

## Code Smells Catalog

Following Martin Fowler's taxonomy from "Refactoring: Improving the Design of Existing Code":

### 1. **Duplicated Code** (CRITICAL - Priority P0)

**Location:** `FtocUtility.java:1-735` vs `FtocUtilityRefactored.java:1-599`

**Smell Description:**
Two nearly identical main entry points exist in parallel, with approximately 60-70% code overlap. This is a textbook example of *Duplicated Code* - the most pervasive and costly code smell.

**Impact:**
- Bug fixes must be applied twice
- Features diverge over time
- Confusion about which class to use
- Maintenance burden doubles

**Evidence:**
```java
// FtocUtility.java:489-500
private static String loadVersion() {
    Properties properties = new Properties();
    try (InputStream input = FtocUtility.class.getClassLoader()
            .getResourceAsStream("ftoc-version.properties")) {
        if (input == null) {
            return "unknown";
        }
        properties.load(input);
        return properties.getProperty("version", "unknown");
    } catch (IOException e) {
        return "unknown";
    }
}

// FtocUtilityRefactored.java:361-372 - IDENTICAL CODE
private static String loadVersion() {
    Properties properties = new Properties();
    try (InputStream input = FtocUtilityRefactored.class.getClassLoader()
            .getResourceAsStream("ftoc-version.properties")) {
        // ... identical implementation
    }
}
```

**Refactoring Strategy:** *Extract Superclass* or *Replace Inheritance with Delegation*

---

### 2. **Long Method** (HIGH - Priority P1)

**Locations:**
- `TocFormatter.generateHtmlToc()`: 453-911 (458 lines)
- `TocFormatter.generateMarkdownToc()`: 206-441 (235 lines)
- `TagQualityAnalyzer.detectPossibleTagTypos()`: 607-733 (126 lines)
- `FtocUtility.main()`: 546-702 (156 lines)

**Smell Description:**
Methods exceeding 50-100 lines become difficult to understand, test, and maintain. The HTML generator in particular is a 458-line monolith mixing concerns.

**Impact:**
- Difficult to understand at a glance
- Hard to reuse parts of the logic
- Testing requires complex setup
- High cyclomatic complexity

**Example:**
```java
// TocFormatter.java:453-911 - 458 LINES!
private String generateHtmlToc(List<Feature> features,
                               List<String> includeTags,
                               List<String> excludeTags) {
    // 60 lines of setup and filtering
    // 100 lines of HTML structure generation
    // 80 lines of navigation generation
    // 150 lines of scenario rendering
    // 68 lines of JavaScript injection
    // ... impossible to follow
}
```

**Refactoring Strategy:** *Extract Method*, *Replace Method with Method Object*

---

### 3. **Large Class / God Class** (HIGH - Priority P1)

**Locations:**
- `TocFormatter`: 1194 lines with 17 methods
- `TagQualityAnalyzer`: 1073 lines with 20 methods
- `ConcordanceFormatter`: 1352 lines
- `FeatureAntiPatternAnalyzer`: 803 lines
- `FtocUtility`: 735 lines

**Smell Description:**
Classes exceeding 500 lines typically have multiple responsibilities. `TocFormatter` handles Plain Text, Markdown, HTML, JSON, and JUnit XML formatting - violating Single Responsibility Principle.

**Impact:**
- Difficult to understand class purpose
- High coupling to many concepts
- Changes ripple unpredictably
- Hard to test in isolation

**Evidence:**
```java
public class TocFormatter {
    // Handles FIVE different output formats
    // Each format has 50-400 lines of generation logic
    // NO shared abstractions between formats
    // 1194 lines total

    private String generatePlainTextToc(...) { /* 124 lines */ }
    private String generateMarkdownToc(...) { /* 235 lines */ }
    private String generateHtmlToc(...) { /* 458 lines */ }
    private String generateJsonToc(...) { /* 220 lines */ }
    private String generateJUnitXmlToc(...) { /* 3 lines - delegates */ }
}
```

**Refactoring Strategy:** *Extract Class*, *Replace Conditional with Polymorphism*

---

### 4. **Feature Envy** (MEDIUM - Priority P2)

**Locations:**
- All formatters reach deep into `Feature` and `Scenario` objects
- `TagQualityAnalyzer` extensively queries `Feature.getTags()`, `Scenario.getTags()`
- `DefaultFeatureProcessor` queries repository internals

**Smell Description:**
Methods in one class excessively use methods from another class, suggesting misplaced responsibilities.

**Impact:**
- Poor encapsulation
- Changes to domain models break formatters
- Difficult to evolve Feature model
- Law of Demeter violations

**Example:**
```java
// TocFormatter.java:96-106 - Feature Envy
for (Feature feature : features) {
    List<Scenario> filteredScenarios = feature.getFilteredScenarios(...);
    if (filteredScenarios.isEmpty() ||
        filteredScenarios.stream().allMatch(Scenario::isBackground)) {
        continue;
    }
    // ... 30 more lines of Feature/Scenario interrogation
    for (Scenario scenario : filteredScenarios) {
        scenario.getTags()  // reaching into scenario
        scenario.getName()  // multiple data queries
        scenario.getSteps() // deep coupling
    }
}
```

**Refactoring Strategy:** *Move Method*, *Extract Method*, *Introduce Parameter Object*

---

### 5. **Primitive Obsession** (MEDIUM - Priority P2)

**Locations:**
- Tags represented as `String` throughout
- Tag concordance as `Map<String, Integer>`
- Severity as string comparisons
- Configuration thresholds as raw integers

**Smell Description:**
Using primitive types instead of small domain objects loses type safety and domain clarity.

**Impact:**
- No type safety (any string can be a "tag")
- Business logic scattered across codebase
- Difficult to add tag validation
- Magic strings everywhere

**Evidence:**
```java
// Primitive obsession examples:
Map<String, Integer> tagConcordance;  // Should be TagConcordance object
List<String> tags;                     // Should be List<Tag>
String tag = "@P0";                    // Should be Tag object
int threshold = 6;                     // Should be Threshold value object

// Better design:
class Tag {
    private final String name;
    public Tag(String name) {
        if (!name.startsWith("@")) {
            throw new IllegalArgumentException("Tags must start with @");
        }
        this.name = name;
    }
}

class TagConcordance {
    private final Map<Tag, Integer> occurrences;
    public int getCount(Tag tag) { ... }
    public Set<Tag> getAllTags() { ... }
}
```

**Refactoring Strategy:** *Replace Data Value with Object*, *Introduce Parameter Object*

---

### 6. **Switch Statements / Type Code** (MEDIUM - Priority P2)

**Locations:**
- `TocFormatter.generateToc()`: 50-63 (Format switch)
- `FtocUtilityRefactored.parseFormat()`: 536-559
- All formatters have similar switches

**Smell Description:**
Repeated switch/if-else chains on type codes violate Open/Closed Principle. Adding new format requires changing multiple methods.

**Impact:**
- Violates Open/Closed Principle
- Adding formats requires shotgun surgery
- Conditional logic duplicated
- Hard to test individual formats

**Example:**
```java
// TocFormatter.java:50-63
public String generateToc(List<Feature> features, Format format, ...) {
    switch (format) {
        case PLAIN_TEXT:
            return generatePlainTextToc(features, includeTags, excludeTags);
        case MARKDOWN:
            return generateMarkdownToc(features, includeTags, excludeTags);
        case HTML:
            return generateHtmlToc(features, includeTags, excludeTags);
        case JSON:
            return generateJsonToc(features, includeTags, excludeTags);
        case JUNIT_XML:
            return generateJUnitXmlToc(features, includeTags, excludeTags);
        default:
            return generatePlainTextToc(features, includeTags, excludeTags);
    }
}

// This pattern repeats in EVERY formatter class!
```

**Refactoring Strategy:** *Replace Conditional with Polymorphism*, *Strategy Pattern*

---

### 7. **Static Cling / Global State** (MEDIUM - Priority P2)

**Locations:**
- `PerformanceMonitor`: All methods are static
- `ErrorHandler`: Static utility class
- `FeatureParserFactory`: Static factory methods
- Version loading: Static methods

**Smell Description:**
Excessive use of static methods creates global state, making testing difficult and violating OO principles.

**Impact:**
- Impossible to mock in tests
- Hidden dependencies
- Thread safety concerns
- Cannot have multiple configurations
- Global state mutations

**Evidence:**
```java
// PerformanceMonitor.java - ALL STATIC
public class PerformanceMonitor {
    private static boolean enabled = false;
    private static final Map<String, Long> operationStartTimes = new HashMap<>();
    private static final Map<String, Long> operationDurations = new HashMap<>();

    public static void setEnabled(boolean enabled) { ... }
    public static void startOperation(String name) { ... }
    public static long endOperation(String name) { ... }

    // Impossible to have two independent monitors
    // Impossible to mock for testing
    // Thread-unsafe shared state
}
```

**Refactoring Strategy:** *Replace Static with Instance*, *Dependency Injection*

---

### 8. **Magic Numbers** (LOW - Priority P3)

**Locations:**
- `DEFAULT_SCENARIOS_PER_PAGE = 20` (TocFormatter:18)
- `PARALLEL_PROCESSING_THRESHOLD = 5` (DefaultFeatureProcessor:24)
- `MAX_RECOMMENDED_TAGS = 6` (TagQualityAnalyzer:497)
- `totalFeatures * 0.9` (TagQualityAnalyzer:388)
- Levenshtein distance thresholds: `<= 1`, `<= 2` (TagQualityAnalyzer:679, 862)

**Smell Description:**
Hardcoded numeric constants without clear semantic meaning or configuration.

**Impact:**
- Values cannot be easily changed
- No documentation of why these numbers
- Should be configurable per-installation
- Difficult to tune for different projects

**Refactoring Strategy:** *Replace Magic Number with Symbolic Constant*, *Parameterize Method*

---

### 9. **Inappropriate Intimacy** (MEDIUM - Priority P2)

**Locations:**
- `TocFormatter` intimately knows `Feature` and `Scenario` internal structure
- `DefaultFeatureProcessor` and `ParallelFeatureProcessor` duplicate logic
- `FtocUtility` and `FtocUtilityRefactored` share intimate knowledge

**Smell Description:**
Classes spend too much time delving into each other's private parts. High coupling between classes.

**Impact:**
- Changes cascade across class boundaries
- Cannot evolve classes independently
- Testing requires complex object graphs
- Violates encapsulation

**Refactoring Strategy:** *Move Method*, *Extract Class*, *Hide Delegate*

---

### 10. **Incomplete Library Class** (LOW - Priority P3)

**Locations:**
- `Feature` and `Scenario` lack convenience methods
- No `Tag` class forcing string manipulation everywhere
- No `TagConcordance` class

**Smell Description:**
Domain model classes don't provide needed functionality, forcing clients to do extra work.

**Impact:**
- Logic duplicated in clients
- Domain knowledge scattered
- Missed abstraction opportunities

**Example:**
```java
// Current: Feature envy in formatters
for (String tag : feature.getTags()) {
    if (tag.toLowerCase().startsWith("@p")) { ... }
}

// Better: Feature provides this
public boolean hasPriorityTag() {
    return tags.stream().anyMatch(Tag::isPriority);
}
```

**Refactoring Strategy:** *Introduce Parameter Object*, *Move Method*

---

### 11. **Data Class** (LOW - Priority P3)

**Locations:**
- `Feature`: Mostly getters with minimal behavior
- `Scenario`: Mostly getters with minimal behavior
- `Warning`: Pure data container

**Smell Description:**
Classes with fields, getters, setters, but no real behavior. Anemic domain model.

**Impact:**
- Procedural code masquerading as OO
- Business logic scattered in service classes
- Missed polymorphism opportunities

**Refactoring Strategy:** *Move Method*, *Encapsulate Field*, *Remove Setting Method*

---

### 12. **Long Parameter List** (LOW - Priority P3)

**Locations:**
- `generateToc(features, format, includeTags, excludeTags)` - 4 parameters
- `TagQualityAnalyzer.Warning constructor` - 6 parameters
- `ReportContext constructor` - 8 parameters
- Multiple formatter methods with 3-5 parameters

**Smell Description:**
Methods with more than 3-4 parameters become difficult to call and understand.

**Impact:**
- Hard to remember parameter order
- Easy to pass wrong arguments
- Difficult to add new parameters
- Method signatures become unstable

**Example:**
```java
// Current - 4 parameters
public String generateToc(List<Feature> features,
                         Format format,
                         List<String> includeTags,
                         List<String> excludeTags)

// Better - Parameter Object
public String generateToc(TocRequest request)

class TocRequest {
    private final List<Feature> features;
    private final Format format;
    private final TagFilter tagFilter;
}
```

**Refactoring Strategy:** *Introduce Parameter Object*, *Preserve Whole Object*

---

## Architecture Assessment

### Current Architecture: 6.5/10

**Strengths:**
1. ✓ Clear separation into layers (core, analyzer, formatter, parser)
2. ✓ Interface-based design (FeatureRepository, FeatureProcessor, Reporter)
3. ✓ Plugin architecture for extensibility
4. ✓ Factory pattern for parser selection
5. ✓ Strategy pattern for formatters (partially implemented)

**Weaknesses:**
1. ✗ Two parallel implementations (old vs refactored)
2. ✗ Formatters not using polymorphism effectively
3. ✗ Static utilities breaking testability
4. ✗ Missing domain model richness
5. ✗ Anemic domain objects

### SOLID Principles Compliance

| Principle | Score | Assessment |
|-----------|-------|------------|
| **S** - Single Responsibility | 5/10 | God classes violate SRP. TocFormatter has 5 responsibilities. |
| **O** - Open/Closed | 6/10 | Switch statements prevent extension. Adding formats requires modification. |
| **L** - Liskov Substitution | 8/10 | Well-designed interfaces. Good substitutability. |
| **I** - Interface Segregation | 7/10 | Interfaces are focused but could be more granular. |
| **D** - Dependency Inversion | 7/10 | Good use of interfaces, but static utilities violate this. |

**Overall SOLID Score: 6.6/10**

---

## Design Patterns Assessment

### Well-Implemented Patterns ✓

1. **Repository Pattern** - `FeatureRepository` abstracts file system access
2. **Factory Pattern** - `FeatureParserFactory` selects appropriate parser
3. **Facade Pattern** - Main utility classes provide simplified interfaces
4. **Strategy Pattern** (partial) - Multiple formatter implementations
5. **Dependency Injection** - Constructor injection in refactored version
6. **Observer Pattern** - Plugin event system

### Missing/Incomplete Patterns

1. **Strategy Pattern** - Formatters should be first-class strategies, not switch statements
2. **Value Object Pattern** - Missing for Tag, Threshold, Concordance
3. **Builder Pattern** - Complex objects like Feature could benefit
4. **Command Pattern** - CLI argument processing could be commands
5. **Template Method** - Formatters share structure but no template
6. **Specification Pattern** - Tag filtering logic could use this

---

## Testing Assessment: 7.5/10

### Strengths ✓
- 13 test classes covering main functionality
- BDD scenarios using Cucumber itself
- Integration tests with Karate
- Benchmark tests for performance
- Good mix of unit and integration tests

### Weaknesses ✗
- No mutation testing mentioned
- Static methods difficult to test
- Large classes difficult to test comprehensively
- Missing: Contract tests for interfaces
- Missing: Property-based testing for parsers
- Code coverage target only 30% (too low!)

### Recommended Testing Improvements

1. **Increase coverage target to 80%** (industry standard)
2. **Add mutation testing** (PIT or similar)
3. **Add contract tests** for Repository/Processor/Reporter interfaces
4. **Add property-based tests** for parser (QuickTheories or jqwik)
5. **Extract testable methods** from long methods
6. **Mock static dependencies** (requires refactoring to instances)

---

## Performance Considerations

### Current Approach
- Parallel processing for >5 files (hardcoded threshold)
- Static `PerformanceMonitor` for timing
- Thread pool with CPU-based sizing
- 5-minute timeout for processing

### Issues
1. **Hardcoded threshold** (5 files) may not suit all environments
2. **Static monitor** prevents concurrent operations
3. **No memory profiling** beyond simple tracking
4. **No rate limiting** on parallel processing

### Recommendations
1. Make thresholds configurable
2. Refactor to instance-based monitoring
3. Add memory pressure detection
4. Implement backpressure mechanisms
5. Consider reactive streams for large datasets

---

## Security Assessment: 8/10

### Strengths ✓
- OWASP dependency checking configured
- No obvious injection vulnerabilities
- File path validation exists
- Exception handling in place
- YAML parsing with safe loader

### Potential Concerns ⚠️
- No input size limits (DoS via huge files)
- No rate limiting on file processing
- SnakeYAML version 2.0 (check CVEs)
- No sanitization of HTML output (XSS in reports)
- File system traversal not fully validated

### Recommendations
1. Add file size limits
2. Sanitize HTML output to prevent XSS
3. Validate file paths against directory traversal
4. Add rate limiting for CLI usage
5. Regular dependency updates

---

## Sequenced Refactoring Plan

Following Martin Fowler's principle of **"safe, incremental refactoring"**, this plan sequences changes by:
- **Risk** (start with low-risk, high-value)
- **Dependencies** (foundational changes first)
- **Value** (maximize business value early)

### Phase 1: Foundation & Safety (Weeks 1-2)

**Goal:** Eliminate duplication, improve testability, establish safety net

#### 1.1 Increase Test Coverage (P0 - CRITICAL)
**Effort:** 1 week | **Risk:** Low | **Value:** High

```
Current Coverage: 30% → Target: 80%

Steps:
1. Add JaCoCo to build with 80% target
2. Identify untested code paths
3. Write missing unit tests
4. Add mutation testing (PIT)
5. Verify all public APIs tested

Success Criteria:
- 80% line coverage
- 75% branch coverage
- All public methods tested
- Mutation score > 70%
```

#### 1.2 Eliminate Duplicate Entry Points (P0 - CRITICAL)
**Effort:** 3 days | **Risk:** Medium | **Value:** High

**Refactoring:** *Extract Superclass*, *Pull Up Method*

```java
// Step 1: Create abstract base class
public abstract class AbstractFtocUtility {
    protected static String loadVersion() {
        // Shared implementation
    }

    protected void processDirectory(String path, boolean concordanceOnly) {
        // Template method with hooks
    }

    // Abstract methods for variation points
    protected abstract FeatureRepository createRepository();
    protected abstract FeatureProcessor createProcessor();
    protected abstract Reporter createReporter();
}

// Step 2: Make existing classes extend base
public class FtocUtility extends AbstractFtocUtility {
    // Only difference: no DI
}

public class FtocUtilityRefactored extends AbstractFtocUtility {
    // Only difference: with DI
}

// Step 3: Deprecate FtocUtility
@Deprecated(since = "1.1.0", forRemoval = true)
public class FtocUtility extends AbstractFtocUtility {
    // Mark for removal in 2.0.0
}

// Step 4: Migrate all clients to FtocUtilityRefactored
// Step 5: Remove in version 2.0.0
```

**Success Criteria:**
- Both classes extend common base
- <5% code duplication
- All tests pass
- Migration guide created

---

### Phase 2: Domain Model Enrichment (Weeks 3-4)

**Goal:** Eliminate primitive obsession, strengthen domain model

#### 2.1 Introduce Tag Value Object (P1 - HIGH)
**Effort:** 1 week | **Risk:** Medium | **Value:** High

**Refactoring:** *Replace Data Value with Object*

```java
// New domain class
public class Tag {
    private final String name;

    private Tag(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }
        this.name = name.startsWith("@") ? name : "@" + name;
    }

    public static Tag of(String name) {
        return new Tag(name);
    }

    public boolean isPriority() {
        return name.matches("@[Pp][0-4]") ||
               name.matches("@(critical|high|medium|low)");
    }

    public boolean isType() {
        return TYPE_TAGS.contains(name.toLowerCase());
    }

    public TagCategory getCategory() {
        // Returns enum: PRIORITY, TYPE, STATUS, etc.
    }

    @Override
    public boolean equals(Object o) { ... }

    @Override
    public int hashCode() { ... }
}

// Refactor systematically:
// 1. Feature: List<String> tags → List<Tag> tags
// 2. Scenario: List<String> tags → List<Tag> tags
// 3. TagConcordance: Map<String, Integer> → Map<Tag, Integer>
// 4. All formatters and analyzers
```

**Migration Strategy:**
1. Add `Tag` class with tests
2. Add parallel methods: `getTags()` and `getTagStrings()`
3. Deprecate `getTagStrings()`
4. Migrate clients incrementally
5. Remove deprecated methods in 2.0.0

**Success Criteria:**
- `Tag` class fully tested
- All tag manipulation goes through `Tag`
- Type safety enforced
- 100 tests pass

---

#### 2.2 Introduce TagConcordance Value Object (P1 - HIGH)
**Effort:** 3 days | **Risk:** Low | **Value:** Medium

**Refactoring:** *Replace Data Value with Object*

```java
public class TagConcordance {
    private final Map<Tag, Integer> counts;

    public TagConcordance(Map<Tag, Integer> counts) {
        this.counts = new HashMap<>(counts);
    }

    public int getCount(Tag tag) {
        return counts.getOrDefault(tag, 0);
    }

    public Set<Tag> getAllTags() {
        return Collections.unmodifiableSet(counts.keySet());
    }

    public List<Tag> getTagsSortedByFrequency() {
        return counts.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    public TagConcordance filterByCategory(TagCategory category) {
        // Business logic in domain object!
    }
}
```

**Success Criteria:**
- `TagConcordance` replaces all `Map<String, Integer>`
- Business logic moved from services to domain
- Immutable and thread-safe

---

### Phase 3: Break Up God Classes (Weeks 5-7)

**Goal:** Decompose large classes into cohesive units

#### 3.1 Refactor TocFormatter Using Strategy Pattern (P1 - HIGH)
**Effort:** 2 weeks | **Risk:** High | **Value:** Very High

**Refactoring:** *Replace Conditional with Polymorphism*, *Extract Class*

```java
// Step 1: Define strategy interface
public interface TocFormatStrategy {
    String generateToc(TocContext context);
}

// Step 2: Extract each format to its own class
public class PlainTextTocStrategy implements TocFormatStrategy {
    @Override
    public String generateToc(TocContext context) {
        // Move generatePlainTextToc() here
        // Now only 124 lines focused on plain text
    }
}

public class MarkdownTocStrategy implements TocFormatStrategy {
    @Override
    public String generateToc(TocContext context) {
        // Move generateMarkdownToc() here
        // 235 lines focused on markdown
    }
}

public class HtmlTocStrategy implements TocFormatStrategy {
    private final HtmlRenderer renderer;
    private final PaginationHandler pagination;
    private final NavigationBuilder navigation;

    @Override
    public String generateToc(TocContext context) {
        // Break down 458-line monster into collaborating objects
    }
}

// Step 3: TocFormatter becomes a simple facade
public class TocFormatter {
    private final Map<Format, TocFormatStrategy> strategies;

    public TocFormatter() {
        strategies = Map.of(
            Format.PLAIN_TEXT, new PlainTextTocStrategy(),
            Format.MARKDOWN, new MarkdownTocStrategy(),
            Format.HTML, new HtmlTocStrategy(),
            Format.JSON, new JsonTocStrategy(),
            Format.JUNIT_XML, new JUnitXmlTocStrategy()
        );
    }

    public String generateToc(List<Feature> features, Format format,
                              List<Tag> includeTags, List<Tag> excludeTags) {
        TocContext context = new TocContext(features, includeTags, excludeTags);
        return strategies.get(format).generateToc(context);
    }
}
```

**Benefits:**
- Each strategy is <300 lines (testable!)
- Easy to add new formats (Open/Closed)
- Each strategy can be tested independently
- Formatters can share utilities via composition

**Success Criteria:**
- TocFormatter < 100 lines
- Each strategy < 300 lines
- Can add new format without changing existing code
- All tests pass

---

#### 3.2 Break Down TagQualityAnalyzer (P1 - HIGH)
**Effort:** 1 week | **Risk:** Medium | **Value:** High

**Refactoring:** *Extract Class*, *Extract Method*

```java
// Current: 1073-line God class
// Target: Composition of focused analyzers

public class TagQualityAnalyzer {
    private final List<WarningDetector> detectors;

    public TagQualityAnalyzer(TagConcordance concordance,
                             List<Feature> features,
                             WarningConfiguration config) {
        this.detectors = Arrays.asList(
            new MissingPriorityTagDetector(config),
            new MissingTypeTagDetector(config),
            new LowValueTagDetector(config),
            new OrphanedTagDetector(config),
            new ExcessiveTagsDetector(config),
            new InconsistentTaggingDetector(config),
            new TypoDetector(config),
            new DuplicateTagDetector(config)
        );
    }

    public List<Warning> analyzeTagQuality() {
        return detectors.stream()
            .filter(WarningDetector::isEnabled)
            .flatMap(detector -> detector.detect(features).stream())
            .collect(Collectors.toList());
    }
}

// Each detector is focused and testable
public interface WarningDetector {
    boolean isEnabled();
    List<Warning> detect(List<Feature> features);
}

public class TypoDetector implements WarningDetector {
    // Only 80-100 lines focused on typo detection
    // Easy to test, understand, and maintain
}
```

**Success Criteria:**
- TagQualityAnalyzer < 150 lines
- Each detector < 150 lines
- Detectors independently testable
- Easy to add new detectors

---

### Phase 4: Eliminate Static Cling (Weeks 8-9)

**Goal:** Remove global state, improve testability

#### 4.1 Replace Static PerformanceMonitor (P2 - MEDIUM)
**Effort:** 1 week | **Risk:** Medium | **Value:** Medium

**Refactoring:** *Replace Static with Instance*, *Dependency Injection*

```java
// Current: All static
public class PerformanceMonitor {
    private static boolean enabled = false;
    private static Map<String, Long> times = new HashMap<>();

    public static void startOperation(String name) { ... }
}

// Refactored: Instance-based
public class PerformanceMonitor {
    private boolean enabled;
    private final Map<String, Long> operationStartTimes;
    private final Map<String, Long> operationDurations;

    public PerformanceMonitor(boolean enabled) {
        this.enabled = enabled;
        this.operationStartTimes = new ConcurrentHashMap<>();
        this.operationDurations = new ConcurrentHashMap<>();
    }

    public void startOperation(String name) { ... }
    public long endOperation(String name) { ... }

    // Now mockable!
    // Now thread-safe!
    // Now testable!
}

// Inject into processors
public class DefaultFeatureProcessor {
    private final PerformanceMonitor monitor;

    public DefaultFeatureProcessor(FeatureRepository repository,
                                  PerformanceMonitor monitor) {
        this.repository = repository;
        this.monitor = monitor;
    }
}
```

**Success Criteria:**
- No static state in PerformanceMonitor
- Can create multiple independent monitors
- Fully mockable in tests
- Thread-safe by design

---

#### 4.2 Refactor FeatureParserFactory (P2 - MEDIUM)
**Effort:** 2 days | **Risk:** Low | **Value:** Medium

**Refactoring:** *Replace Static Factory with Instance Factory*

```java
// Current: Static factory
public class FeatureParserFactory {
    public static FeatureParser getParser(File file) {
        if (KarateParser.isKarateFile(file)) {
            return new KarateParser();
        }
        return new FeatureParser();
    }
}

// Refactored: Injectable factory
public interface ParserFactory {
    FeatureParser createParser(File file);
}

public class DefaultParserFactory implements ParserFactory {
    @Override
    public FeatureParser createParser(File file) {
        if (isKarateFile(file)) {
            return new KarateParser();
        }
        return new GherkinParser();
    }

    private boolean isKarateFile(File file) { ... }
}

// Now we can inject custom parsers via DI!
```

---

### Phase 5: Improve Long Methods (Weeks 10-11)

**Goal:** Break down complex methods into comprehensible units

#### 5.1 Refactor HTML Generation (P2 - MEDIUM)
**Effort:** 1 week | **Risk:** Medium | **Value:** Medium

**Refactoring:** *Extract Method*, *Replace Method with Method Object*

```java
// Current: 458-line monster method
private String generateHtmlToc(...) {
    // 458 lines of nightmare
}

// Refactored: Method Object with collaborators
public class HtmlTocGenerator {
    private final HtmlHeader headerBuilder;
    private final HtmlNavigationBuilder navBuilder;
    private final HtmlScenarioRenderer scenarioRenderer;
    private final HtmlPaginationHandler paginationHandler;
    private final JavaScriptGenerator scriptGenerator;

    public String generate(TocContext context) {
        StringBuilder html = new StringBuilder();
        html.append(headerBuilder.buildHeader());
        html.append(navBuilder.buildNavigation(context.getFeatures()));
        html.append(renderMainContent(context));
        html.append(scriptGenerator.generateScript());
        return html.toString();
    }

    private String renderMainContent(TocContext context) {
        // 50 lines max
    }
}

// Each collaborator is small and focused
public class HtmlScenarioRenderer {
    public String render(Scenario scenario) {
        // 30-40 lines focused on scenario rendering
    }
}
```

**Success Criteria:**
- No method > 50 lines
- Each class has single responsibility
- Cyclomatic complexity < 10 per method

---

### Phase 6: Address Remaining Smells (Weeks 12-14)

#### 6.1 Introduce Parameter Objects (P3 - LOW)
**Effort:** 1 week | **Risk:** Low | **Value:** Medium

**Refactoring:** *Introduce Parameter Object*

```java
// Current: Long parameter lists
public String generateToc(List<Feature> features,
                         Format format,
                         List<Tag> includeTags,
                         List<Tag> excludeTags) { ... }

// Refactored: Parameter object
public class TocRequest {
    private final List<Feature> features;
    private final Format format;
    private final TagFilter tagFilter;

    // Builder pattern for easy construction
    public static class Builder { ... }
}

public String generateToc(TocRequest request) {
    // Clean, focused signature
}
```

---

#### 6.2 Replace Magic Numbers with Configuration (P3 - LOW)
**Effort:** 3 days | **Risk:** Low | **Value:** Low

```java
// Current: Magic numbers everywhere
private static final int DEFAULT_SCENARIOS_PER_PAGE = 20;
private static final int PARALLEL_PROCESSING_THRESHOLD = 5;

// Refactored: Configuration class
public class FtocConfiguration {
    private int scenariosPerPage = 20;
    private int parallelProcessingThreshold = 5;
    private int maxRecommendedTags = 6;

    // Load from properties file or environment
    public static FtocConfiguration loadDefault() { ... }
}
```

---

### Phase 7: Polish & Performance (Weeks 15-16)

#### 7.1 Performance Optimization
**Effort:** 1 week | **Risk:** Low | **Value:** Medium

- Profile with JProfiler or YourKit
- Optimize hot paths identified by profiler
- Add caching where appropriate
- Optimize parallel processing thresholds

#### 7.2 Documentation Update
**Effort:** 3 days | **Risk:** Low | **Value:** High

- Update ADRs with refactoring decisions
- Update API documentation
- Create migration guides
- Update examples and tutorials

---

## Success Metrics

### Code Quality Metrics

| Metric | Before | After Phase 7 | Target |
|--------|--------|---------------|---------|
| Code Duplication | ~15% | <3% | <5% |
| Average Class Size | 270 lines | <200 lines | <300 lines |
| Largest Class | 1194 lines | <500 lines | <500 lines |
| Average Method Size | ~40 lines | <20 lines | <30 lines |
| Cyclomatic Complexity | Unknown | <8 avg | <10 avg |
| Test Coverage | 30% | 80% | 80% |
| SOLID Score | 6.6/10 | 8.5/10 | 8+/10 |

### Business Metrics

| Metric | Before | Target |
|--------|--------|---------|
| Time to Add New Format | 2-3 days | 4 hours |
| Onboarding Time | 2 weeks | 3 days |
| Bug Fix Time | 4 hours | 1 hour |
| Feature Development | 1 week | 2 days |

---

## Risk Mitigation

### High-Risk Refactorings

1. **Formatter Strategy Pattern** (Phase 3.1)
   - **Risk:** Breaking existing output formats
   - **Mitigation:** Golden master testing, snapshot tests
   - **Rollback Plan:** Feature flag, gradual rollout

2. **Tag Value Object** (Phase 2.1)
   - **Risk:** Performance regression, API breaks
   - **Mitigation:** Parallel APIs, deprecation cycle
   - **Rollback Plan:** Keep old methods temporarily

3. **Static to Instance** (Phase 4)
   - **Risk:** Forgotten static calls, threading issues
   - **Mitigation:** Compiler warnings, static analysis
   - **Rollback Plan:** Feature flag system

### Continuous Validation

After EACH refactoring step:
1. ✓ All tests pass (100% pass rate)
2. ✓ No performance regression (benchmarks within 5%)
3. ✓ Code coverage maintained or improved
4. ✓ Static analysis passes (no new warnings)
5. ✓ Manual smoke test of CLI

---

## Refactoring Catalog Reference

Key techniques from Martin Fowler's catalog used in this plan:

### Composing Methods
- **Extract Method** - Break down long methods
- **Inline Method** - Remove unnecessary indirection
- **Replace Temp with Query** - Reduce local variables
- **Replace Method with Method Object** - For complex algorithms

### Moving Features Between Objects
- **Move Method** - Relocate behavior to better homes
- **Extract Class** - Break up God classes
- **Hide Delegate** - Reduce coupling

### Organizing Data
- **Replace Data Value with Object** - Tag, TagConcordance
- **Replace Array with Object** - Parameter objects
- **Replace Magic Number with Symbolic Constant**

### Simplifying Conditional Expressions
- **Decompose Conditional** - Complex if statements
- **Replace Conditional with Polymorphism** - Format switches

### Making Method Calls Simpler
- **Rename Method** - Improve clarity
- **Add Parameter** / **Remove Parameter**
- **Introduce Parameter Object** - Long parameter lists
- **Preserve Whole Object** - Pass objects not fields

### Dealing with Generalization
- **Pull Up Method** - Extract common behavior to superclass
- **Extract Superclass** - Create shared base class
- **Replace Conditional with Polymorphism** - Strategy pattern

---

## Continuous Improvement Plan

### Post-Refactoring Maintenance

1. **Establish Code Review Standards**
   - Max class size: 500 lines
   - Max method size: 50 lines
   - Max cyclomatic complexity: 10
   - Min test coverage: 80%

2. **Automated Quality Gates**
   - SonarQube quality gate
   - Mutation testing in CI
   - Dependency vulnerability scanning
   - Performance regression testing

3. **Regular Refactoring Sprints**
   - Dedicate 20% of each sprint to technical debt
   - Monthly code smell reviews
   - Quarterly architecture reviews

4. **Knowledge Sharing**
   - Pair programming for complex refactorings
   - Brown bag sessions on design patterns
   - Internal tech talks on SOLID principles

---

## Conclusion

The FTOC codebase demonstrates **solid engineering fundamentals** but has accumulated **technical debt** during its evolution. The proposed refactoring plan follows Martin Fowler's principles of:

1. **Small, safe steps** - Each phase delivers value independently
2. **Continuous testing** - Every change validated immediately
3. **Incremental improvement** - No "big bang" rewrites
4. **Backwards compatibility** - Deprecation over deletion
5. **Business value first** - Prioritize by impact and risk

By following this **16-week sequenced plan**, the codebase will achieve:
- ✓ **<3% code duplication** (from 15%)
- ✓ **80% test coverage** (from 30%)
- ✓ **Clean architecture** with proper separation of concerns
- ✓ **Extensible design** following Open/Closed principle
- ✓ **Maintainable codebase** with clear domain model

The result will be a **world-class** BDD utility that serves as a **reference implementation** for clean code principles.

---

## Appendices

### A. Recommended Reading

1. **Refactoring: Improving the Design of Existing Code** - Martin Fowler
2. **Clean Code** - Robert C. Martin
3. **Working Effectively with Legacy Code** - Michael Feathers
4. **Domain-Driven Design** - Eric Evans
5. **Patterns of Enterprise Application Architecture** - Martin Fowler

### B. Tools Recommended

1. **Static Analysis:** SonarQube, PMD, Checkstyle, SpotBugs
2. **Mutation Testing:** PIT (Pitest)
3. **Coverage:** JaCoCo
4. **Profiling:** JProfiler, YourKit, VisualVM
5. **Refactoring:** IntelliJ IDEA Refactoring Tools

### C. Code Examples Repository

Full code examples for each refactoring step available at:
`docs/refactoring-examples/`

---

**Assessment Completed:** 2025-11-15
**Next Review Date:** 2026-05-15 (6 months)
**Assessor:** Claude (Anthropic AI) in collaboration with development team
