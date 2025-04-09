# Cross-Platform Support in FTOC

FTOC is built and tested on multiple operating systems to ensure it works consistently across different platforms.

## Platform-Specific Builds

Each release of FTOC includes builds specific to the following platforms:

- **Linux**: Optimized for Linux-based operating systems
- **macOS**: Optimized for Apple macOS systems
- **Windows**: Optimized for Microsoft Windows systems

## How to Download the Right Build

### From GitHub Releases

When you visit the [Releases page](https://github.com/heymumford/ftoc/releases) on GitHub, you'll see multiple JAR files available for download:

- `ftoc-linux.jar` - For Linux systems
- `ftoc-macos.jar` - For macOS systems
- `ftoc-windows.jar` - For Windows systems
- `ftoc.jar` - Generic build (Linux-based) for backward compatibility

Download the version that matches your operating system for the best experience.

### Using Docker

The Docker image works on any platform that supports Docker, providing a consistent experience regardless of your operating system:

```bash
docker run --rm \
  -v "/path/to/feature/files:/data" \
  -v "$(pwd)/output:/output" \
  ghcr.io/heymumford/ftoc --directory /data --output-directory /output
```

See the [Docker Usage](docker-usage.md) documentation for more details.

## Running FTOC on Different Platforms

### Linux

```bash
# Make the JAR executable
chmod +x ftoc-linux.jar

# Run FTOC
java -jar ftoc-linux.jar --directory /path/to/features
```

### macOS

```bash
# Make the JAR executable
chmod +x ftoc-macos.jar

# Run FTOC
java -jar ftoc-macos.jar --directory /path/to/features
```

### Windows

```bash
# Run FTOC
java -jar ftoc-windows.jar --directory C:\path\to\features
```

## Platform-Specific Considerations

### File Path Separators

- Linux/macOS: Forward slash (`/`)
- Windows: Backslash (`\`)

FTOC handles these differences automatically when you provide paths in your native format.

### Executable Scripts

For convenience, you can create platform-specific scripts:

#### Linux/macOS (`ftoc.sh`)

```bash
#!/bin/bash
java -jar /path/to/ftoc-linux.jar "$@"
```

#### Windows (`ftoc.bat`)

```batch
@echo off
java -jar C:\path\to\ftoc-windows.jar %*
```

## Verifying Builds

Each platform-specific build undergoes automated testing on its respective operating system in our CI/CD pipeline. You can view the test results in the [Actions tab](https://github.com/heymumford/ftoc/actions) of the GitHub repository.

## Troubleshooting

### Common Issues on Windows

- **Path Length Limitations**: Windows has a 260-character limit for file paths. If you encounter issues with deep directory structures, try moving your feature files closer to the root directory.
- **Command Prompt Limitations**: For best results on Windows, use PowerShell rather than Command Prompt.

### Common Issues on macOS

- **Security Warnings**: If you receive security warnings when running the JAR, you may need to adjust your security settings or use the `xattr -d com.apple.quarantine ftoc-macos.jar` command.

### Common Issues on Linux

- **Missing Java Runtime**: Ensure you have Java 11 or later installed (`java -version`).
- **Executable Permissions**: Make sure the JAR file has executable permissions (`chmod +x ftoc-linux.jar`).

## Reporting Platform-Specific Issues

If you encounter issues specific to a particular platform, please report them on the [Issues page](https://github.com/heymumford/ftoc/issues) with the following information:

1. Operating system name and version
2. Java version
3. FTOC version
4. Command executed
5. Error message or unexpected behavior
6. Steps to reproduce

This helps us identify and fix platform-specific issues quickly.