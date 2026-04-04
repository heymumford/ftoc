# Changelog

## [1.1.0](https://github.com/heymumford/ftoc/compare/v1.0.0...v1.1.0) (2026-04-04)

### Features
* Maven wrapper for reproducible builds
* Release-please automation with conventional commits
* CODEOWNERS, FUNDING.yml, weekly security audit workflow
* MIT license headers on all 57 Java source files
* 7 TDD vertical slices: config validation, concordance accuracy, parser robustness, output correctness, report formatting, CLI exit codes, performance baseline
* Markdown escaping for link text with special characters

### Bug Fixes
* XSS: escape scenario steps, example tables, and markdown raw HTML
* NullPointerException guards in FtocException and WarningConfiguration
* WarningConfiguration: type validation for all YAML sections
* Parser: empty feature name and multi-line tag handling
* CLI: InvalidPathException catch, dead code removal
* CI: consolidated Maven invocations, Docker test gating
* Wildcard imports replaced with explicit imports (Checkstyle compliance)
* System.exit safety in test JVM
* JSON output structural validation, XML well-formedness parsing


## [1.0.0](https://github.com/heymumford/ftoc/releases/tag/v1.0.0) (2026-04-03)

### Features
* 3-sprint modernization: 46 to 25 classes, 80 to 264 tests
* 7 TDD vertical slices with acceptance and regression tests
* Cucumber BDD runner fixed (28 scenarios executing)
* XSS protection in HTML output
* CLI exit codes and invalid argument handling
* Config validation with clear error messages
* KarateParser file-path bug fixed
