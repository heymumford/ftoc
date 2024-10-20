# `ftoc` - Feature Table of Contents Utility

This utility is under construction as of Oct 2024 and this line will be deleted when it's ready for prime time.

`ftoc` (Feature-File Table of Contents) is a utility that tells you things about your Cucumber feature files.
It should work with all Cucumber text files (Java, Javascript, Karate framework).

It tries to produce:

1. A Table of Contents (TOC) of all scenarios and scenario outlines you point it to
2. A concordance (count of groups) of all tags used across feature files
3. Warnings for missing tags or use of generic "dumb" tags like [debug, test, sanity, stage] or other overloaded words

... in order to show you useful information about existing tests. I've scripted variants of this
utility across several consulting engagements and figured I'd produce a formal open source version.


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
