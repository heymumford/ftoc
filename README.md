# ```ftoc``` - Feature Table of Contents Utility

`ftoc` is a command-line utility that analyzes Cucumber/Karate feature files to generate:

1. A Table of Contents (TOC) of all scenarios and scenario outlines
2. A concordance of all tags used across feature files
3. Warnings for missing tags or use of generic "dumb" tags

It operates by recursively traversing a specified directory, parsing `.feature` files, and outputting results in markdown format. The utility can also generate a tree-like visualization of the directory structure and feature files.

Key functions:
- Extracts scenario names, outline names, and tags from feature files
- Compiles tag usage statistics
- Identifies feature files without tags
- Warns about predefined "dumb" tags (e.g., "test", "temp", "ignore")

Output is designed for easy integration with documentation or code review processes.

---

## **Installation Instructions**

### **1. Using Maven**

To install `ftoc` as part of your Maven project:

1. Add the following dependency to your `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.heymumford.ftoc</groupId>
       <artifactId>ftoc</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   ```

2. **Build the project**:

   ```bash
   mvn clean install
   ```

3. After installation, you can use the `ftoc` utility from within your project.

---

### **2. Standalone Installation**

To use `ftoc` as a standalone utility:

1. **Clone the repository**:

   ```bash
   git clone git@github.com:heymumford/ftoc.git
   ```

2. **Build the project**:

   ```bash
   mvn clean install
   ```

3. **Run the utility**:

   After building, you can run `ftoc` from the command line:

   ```bash
   java -jar target/ftoc-1.0-SNAPSHOT.jar -d /path/to/feature/files
   ```

---

## **Usage Instructions**

### **1. Running `ftoc` as a Standalone Utility**

By default, `ftoc` will generate a TOC and tag concordance for all feature files in the current directory or a specified directory:

```bash
java -jar target/ftoc-1.0-SNAPSHOT.jar -d /path/to/feature/files
```

Options:
- **`-d <directory>`**: Specify the directory to traverse (defaults to the current directory).
- **`-tree`**: Outputs a tree-like graphic structure of feature files and tags.
- **`-sf`**: Suppresses the feature file details (TOC).
- **`-sc`**: Suppresses the concordance summary.
- **`-h` or `--help`**: Displays usage information.
- **`-v` or `--version`**: Displays the current version and author information.

### **2. Example Usage**

#### **Generate a Table of Contents and Concordance**

This example outputs a TOC and concordance summary for the feature files in the `src/test/resources` directory:

```bash
java -jar target/ftoc-1.0-SNAPSHOT.jar -d src/test/resources
```

#### **Generate a Tree-like Structure**

To visualize the folder structure along with the feature files and their tags:

```bash
java -jar target/ftoc-1.0-SNAPSHOT.jar -d src/test/resources -tree
```

#### **Suppress Feature TOC or Concordance**

If you only want the tree structure but not the TOC, suppress the feature file details:

```bash
java -jar target/ftoc-1.0-SNAPSHOT.jar -d src/test/resources -tree -sf
```

---

### **3. Using `ftoc` Within Another Framework (Including Karate)**

For projects using the Karate framework or another BDD framework:

1. Add `ftoc` to your `pom.xml` as a dependency (as described in the Maven installation instructions).
2. You can invoke `ftoc` as part of your test suite or integrate it with your CI/CD pipeline to generate TOC and concordance data.

#### **Example Integration with Karate TestRunner:**

```java
public class KarateTestRunner {

    @Test
    public void runFtoc() {
        // Run ftoc to generate a TOC and log useful information
        String[] ftocArgs = {"-d", "src/test/resources", "-tree"};
        ftoc.main(ftocArgs);  // Call ftoc utility
    }

    @Test
    public void testAll() {
        // Regular Karate test runner
        Results results = Runner.path("classpath:features")
                                .outputCucumberJson(true)
                                .tags("@regression")
                                .parallel(5);
        assertTrue(results.getFailCount() == 0, results.getErrorMessages());
    }
}
```

In this example, `ftoc` is integrated into the Karate test suite, running alongside the main test runner.

---

### **4. Customizing the Configuration**

You can customize the list of "dumb tags" by modifying the `ftoc.config` file. This file is a simple JSON object that lists tags you want to avoid using, such as `test`, `temp`, or `ignore`. The utility will warn you if these tags are found in the feature files.

---

## **Contribution and Support**

If you'd like to contribute to `ftoc` or need support, please open an issue or submit a pull request on [GitHub](https://github.com/heymumford/ftoc).

---

## **License**

`ftoc` is open-source and available under the MIT license. See the [LICENSE](https://github.com/heymumford/ftoc/blob/main/LICENSE) file for more details.

---
