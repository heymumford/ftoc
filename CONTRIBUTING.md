# Contributing

Thanks for taking the time to contribute.

## Prerequisites

- Java 11+
- Maven 3.8+ (the included Maven wrapper handles this automatically)

## Building

```bash
./mvnw clean package
```

This compiles the source, runs all tests, and produces the executable JAR in `target/`. The `mvnw` wrapper downloads the correct Maven version on first run -- no local Maven install required.

## Running Tests

```bash
./mvnw test
```

For a full verification pass including integration checks:

```bash
./mvnw verify
```

## Code Style

The project enforces Checkstyle rules during the build. Fix any violations before submitting:

```bash
./mvnw checkstyle:check
```

## Pull Requests

1. Fork the repository and create a feature branch from `main`.
2. Keep PRs focused and small when possible.
3. Include context in the PR description: what changed and why.
4. Add or update tests when behavior changes.
5. Run `./mvnw verify` locally and confirm a clean build before opening the PR.
6. CI must pass before merge.

## Reporting Issues

- Search existing issues and discussions before opening a new one.
- If you plan a large change, open an issue first to align on direction.

## License

By contributing, you agree that your contributions will be licensed under this repository's MIT license.
