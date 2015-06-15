package org.zalando.stups.swagger.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.util.Yaml;

/**
 * @author  jbellmann
 */
public class YamlToJson {

    private String outputDirectoryPath;

    private String yamlInputPath;

    private CodegeneratorLogger logger = new SystemOutCodegeneratorLogger();

    private YamlToJson(final String yamlInputPath, final String outputDirectoryPath,
            final CodegeneratorLogger codegeneratorLogger) {
        this.yamlInputPath = yamlInputPath;
        this.outputDirectoryPath = outputDirectoryPath;
        this.logger = codegeneratorLogger;
    }

    public void convert() {
        logger.info("Generate .json file from .yaml");

        File outputDirectory = new File(outputDirectoryPath);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
            logger.info("OutputDirectory created");
        }

        File jsonFile = new File(outputDirectory, getYamlFilename() + ".json");
        FileWriter fileWriter = null;
        try {

            fileWriter = new FileWriter(jsonFile);
            fileWriter.write(getYamlFileContentAsJson());
            fileWriter.flush();
            logger.info("File written to " + jsonFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    protected String getYamlFileContentAsJson() throws IOException {
        String data = new String(Files.readAllBytes(java.nio.file.Paths.get(new File(yamlInputPath).toURI())));

        ObjectMapper yamlMapper = Yaml.mapper();
        JsonNode rootNode = yamlMapper.readTree(data);

        // must have swagger node set
        JsonNode swaggerNode = rootNode.get("swagger");

        return rootNode.toString();
    }

    protected String getYamlFilename() {

        File file = new File(yamlInputPath);
        if (!file.exists()) {
            throw new RuntimeException("Api-File not found: " + yamlInputPath);
        } else {
            String filename = file.getName();
            return filename.substring(0, filename.indexOf("."));
        }
    }

    public static YamlToJsonBuilder builder() {
        return new YamlToJsonBuilder();
    }

    public static class YamlToJsonBuilder {
        private String yamlInputPath;
        private String outputDirectoryPath;
        private CodegeneratorLogger codegeneratorLogger = new SystemOutCodegeneratorLogger();

        public YamlToJsonBuilder withYamlInputPath(final String yamlInputPath) {
            this.yamlInputPath = yamlInputPath;
            return this;
        }

        public YamlToJsonBuilder withOutputDirectoryPath(final String outputDirectoryPath) {
            this.outputDirectoryPath = outputDirectoryPath;
            return this;
        }

        public YamlToJsonBuilder withCodegeneratorLogger(final CodegeneratorLogger codegeneratorLogger) {
            this.codegeneratorLogger = codegeneratorLogger;
            return this;
        }

        public YamlToJson build() {
            return new YamlToJson(yamlInputPath, outputDirectoryPath, codegeneratorLogger);
        }
    }
}