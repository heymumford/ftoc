/*
 * Copyright (c) 2026 Eric C. Mumford (@heymumford)
 * Licensed under the MIT License. See LICENSE file in the project root.
 */

package com.heymumford.ftoc.parser;

import com.heymumford.ftoc.exception.ErrorCode;
import com.heymumford.ftoc.exception.FtocException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FeatureParserFactory {
    private static final Logger logger = LoggerFactory.getLogger(FeatureParserFactory.class);

    public static FeatureParser getParser(String filePath) throws FtocException {
        if (filePath == null || filePath.isEmpty()) {
            throw new FtocException("Feature file path cannot be null or empty", ErrorCode.FILE_NOT_FOUND);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FtocException("Feature file not found: " + filePath, ErrorCode.FILE_NOT_FOUND);
        }
        return getParser(file);
    }

    public static FeatureParser getParser(File file) throws FtocException {
        if (file == null) {
            throw new FtocException("Feature file cannot be null", ErrorCode.FILE_NOT_FOUND);
        }
        if (!file.exists()) {
            throw new FtocException("Feature file not found: " + file.getAbsolutePath(), ErrorCode.FILE_NOT_FOUND);
        }
        if (!file.canRead()) {
            throw new FtocException("Feature file cannot be read (check permissions): " + file.getAbsolutePath(), ErrorCode.FILE_READ_ERROR);
        }
        FeatureParser standardParser = new FeatureParser();
        try {
            if (standardParser.isKarateFile(file)) {
                logger.info("Detected Karate-style feature file: {}", file.getName());
                return new KarateParser();
            }
        } catch (FtocException e) {
            logger.warn("Error detecting Karate syntax (using standard parser): {}", e.getMessage());
        }
        return standardParser;
    }
}
