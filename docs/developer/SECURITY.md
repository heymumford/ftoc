# Security Practices for FTOC

This document outlines the security practices implemented in the FTOC project, including vulnerability scanning, secure coding practices, and how to report security issues.

## Vulnerability Scanning

### OWASP Dependency Check

FTOC uses the OWASP Dependency Check to scan dependencies for known security vulnerabilities.

#### CI/CD Integration

The vulnerability scan is automatically performed in our CI/CD pipeline:

1. The scan runs as part of the `security-scan` job in the GitHub Actions workflow
2. Reports are generated in HTML, XML, and SARIF formats
3. The HTML report is uploaded as an artifact for every build
4. The SARIF report is uploaded to GitHub's Security tab
5. If vulnerabilities are found, notifications are sent via Slack (if configured)
6. Builds only fail for critical vulnerabilities (CVSS score >= 9.0)

#### Running the Scan Locally

To run the vulnerability scan locally:

```bash
# Standard scan
mvn org.owasp:dependency-check-maven:check

# Scan with NVD API key for better results
NVD_API_KEY=your-api-key mvn org.owasp:dependency-check-maven:check
```

The reports will be available in the `target` directory:
- `dependency-check-report.html` - Human-readable HTML report
- `dependency-check-report.xml` - XML report for CI integration
- `dependency-check-report.json` - JSON report for programmatic processing

### Managing False Positives

If a vulnerability is a false positive or cannot be fixed immediately, add it to the suppression file:

1. Open `/config/dependency-check-suppressions.xml`
2. Add a new `<suppress>` entry with appropriate details
3. Commit the change

Example suppression:

```xml
<suppress>
   <notes>CVE applies to feature we don't use or fixed in version we use</notes>
   <packageUrl regex="true">pkg:maven/org\.example/some-library@.*</packageUrl>
   <cve>CVE-2023-12345</cve>
</suppress>
```

### NVD API Key

For better scan results, set up an NVD API key:

1. Register for a free API key at [https://nvd.nist.gov/developers/request-an-api-key](https://nvd.nist.gov/developers/request-an-api-key)
2. Add it as a GitHub secret (see `.github/SETUP_SECRETS.md`)
3. For local scans, set the `NVD_API_KEY` environment variable

## Secure Coding Practices

The FTOC project follows these secure coding practices:

1. **Input Validation** - All user inputs are validated before processing
2. **Logging Best Practices** - No sensitive data is logged
3. **Principle of Least Privilege** - Docker containers run as non-root users
4. **Dependency Management** - Dependencies are regularly updated and scanned
5. **Code Review** - All PRs undergo security-focused code review

## Reporting Security Issues

To report a security vulnerability:

1. **Do not** report security vulnerabilities through public GitHub issues
2. Contact the project maintainers directly via email at security@heymumford.com
3. Include as much information as possible:
   - Type of issue
   - Steps to reproduce
   - Affected versions
   - Potential impact

## Security Update Process

When security vulnerabilities are reported:

1. The maintainers will acknowledge receipt within 48 hours
2. The team will investigate and determine impact
3. A fix will be developed and tested privately
4. A new version will be released with the fix
5. An advisory will be published (if applicable)

## Third-Party Notification

For vulnerabilities in third-party dependencies:

1. If the vulnerability is already known (has a CVE), we will update or mitigate as appropriate
2. If the vulnerability is new, we will contact the maintainer of the affected package
3. We will implement workarounds until an official fix is available