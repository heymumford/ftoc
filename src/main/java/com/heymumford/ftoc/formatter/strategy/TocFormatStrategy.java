package com.heymumford.ftoc.formatter.strategy;

import com.heymumford.ftoc.model.Feature;

import java.util.List;

/**
 * Strategy interface for generating Table of Contents in different formats.
 *
 * This interface follows the Strategy pattern from the Gang of Four design patterns,
 * allowing different formatting algorithms to be used interchangeably.
 *
 * Each implementation handles one specific output format (Plain Text, Markdown, HTML, etc.)
 * and is responsible for rendering features into that format.
 *
 * Benefits of this design:
 * - Single Responsibility: Each strategy handles one format
 * - Open/Closed: New formats can be added without modifying existing code
 * - Testability: Each strategy can be tested independently
 * - Maintainability: Smaller, focused classes instead of one large class
 *
 * @see <a href="https://refactoring.com/catalog/replaceConditionalWithPolymorphism.html">Replace Conditional with Polymorphism</a>
 */
public interface TocFormatStrategy {

    /**
     * Generate a Table of Contents for the given features.
     *
     * @param context The context containing features and filter settings
     * @return The formatted TOC as a string
     */
    String generateToc(TocContext context);

    /**
     * Get the name of this format strategy.
     *
     * @return A human-readable name (e.g., "Plain Text", "Markdown", "HTML")
     */
    String getFormatName();
}
