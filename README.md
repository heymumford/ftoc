# `ftoc` - Feature Table of Contents Utility

`ftoc` is a command-line utility that analyzes Cucumber/Karate feature files to generate:

1. A Table of Contents (TOC) of all scenarios and scenario outlines
2. A concordance of all tags used across feature files
3. Warnings for missing tags or use of generic "dumb" tags

It operates by recursively traversing a specified directory, parsing `.feature` files, and outputting results in markdown format.

Key functions:
- Extracts scenario names, outline names, and tags from feature files
- Compiles tag usage statistics
- Identifies feature files without tags
- Warns about predefined "dumb" tags (e.g., "test", "temp", "ignore")

Output is designed for easy integration with documentation or code review processes.

## Building the Project

To build the `ftoc` utility:

1. Clone the repository:
   ```bash
   git clone https://github.com/heymumford/ftoc.git
   cd ftoc
   ```

2. Build the project using Maven:
   ```bash
   mvn clean package
   ```

This will create a JAR file in the `target` directory named `ftoc-<version>-jar-with-dependencies.jar`.

## Usage Instructions

After building the project, you can run `ftoc` as follows:

```bash
java -jar target/ftoc-<version>-jar-with-dependencies.jar [OPTIONS]
```

Replace `<version>` with the current version number of the utility.

### Options:

- `-d <directory>`: Specify the directory to analyze (default: current directory)
- `--version` or `-v`: Display version information
- `--help`: Display help message

### Examples:

1. Display version information:
   ```bash
   java -jar target/ftoc-<version>-jar-with-dependencies.jar --version
   ```

2. Analyze feature files in a specific directory:
   ```bash
   java -jar target/ftoc-<version>-jar-with-dependencies.jar -d /path/to/feature/files
   ```

3. Display help information:
   ```bash
   java -jar target/ftoc-<version>-jar-with-dependencies.jar --help
   ```

## Integration with Other Projects

To use `ftoc` in your Java project:

1. Build the `ftoc` project as described above.

2. Add the generated JAR file to your project's classpath.

3. You can then use the `FtocUtility` class in your code:

   ```java
   import com.heymumford.ftoc.FtocUtility;

   public class YourClass {
       public void runFtoc() {
           FtocUtility ftoc = new FtocUtility();
           ftoc.initialize();
           ftoc.processDirectory("/path/to/feature/files");
       }
   }
   ```

## Contribution and Support

If you'd like to contribute to `ftoc` or need support, please open an issue or submit a pull request on [GitHub](https://github.com/heymumford/ftoc).

## License

`ftoc` is open-source and available under the MIT license. See the [LICENSE](https://github.com/heymumford/ftoc/blob/main/LICENSE) file for more details.