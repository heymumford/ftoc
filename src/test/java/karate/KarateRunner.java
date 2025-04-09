package karate;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class KarateRunner {
    private static final Logger logger = LoggerFactory.getLogger(KarateRunner.class);

    @BeforeAll
    static void beforeAll() {
        // Find the FTOC JAR file
        File targetDir = new File("target");
        File[] jars = targetDir.listFiles((dir, name) -> 
            name.startsWith("ftoc-") && 
            name.endsWith("-jar-with-dependencies.jar") && 
            !name.contains("sources") && 
            !name.contains("javadoc")
        );
        
        if (jars != null && jars.length > 0) {
            // Set the jar path as a system property for Karate
            String jarPath = jars[0].getAbsolutePath();
            logger.info("Setting ftoc.path system property to: {}", jarPath);
            System.setProperty("ftoc.path", jarPath);
        } else {
            logger.warn("No FTOC JAR found in target directory. Make sure to build the project first.");
        }
    }

    @Karate.Test
    Karate testCliCommands() {
        return Karate.run("cli").relativeTo(getClass());
    }

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:karate").relativeTo(getClass());
    }
    
    /**
     * Run all Karate tests in parallel and generate JUnit XML report
     */
    @Test
    void testParallel() {
        // Set up the report directory
        String reportDir = "target/karate-reports";
        Results results = Runner.path("classpath:karate")
                .outputJunitXml(true)
                .reportDir(reportDir)
                .parallel(5); // Run up to 5 threads in parallel
        
        // Output results summary
        logger.info("Karate tests completed. Total: {}, Passed: {}, Failed: {}, Skipped: {}",
            results.getFeaturesTotal(),
            results.getFeaturesPassed(),
            results.getFeaturesFailed(),
            0); // Karate 1.4.0 doesn't track skipped features
        
        // Fail the test if there are any failures
        assertEquals(0, results.getFeaturesFailed(), 
            results.getFeaturesFailed() + " Karate test(s) failed. See the report at: " + reportDir);
    }
}