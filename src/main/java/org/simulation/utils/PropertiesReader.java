package org.simulation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Properties propertyFile = new Properties();

    public PropertiesReader(String fileName) {
        try (FileInputStream stream = new FileInputStream(fileName)) {
            propertyFile.load(stream);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        LOGGER.info(propertyFile);
    }

    public String getBy(String locatorName) {
        return propertyFile.getProperty(locatorName);
    }
}
