import ag.logger.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordleDictParser {

    // Regex to separate a dictionary line into three groups
    // 1. The word
    // 2. The type of speech
    // 3. The description
    private final Pattern dictRegex = Pattern.compile("([^(\\n]*)\\((.*?)\\)([^\\n]*)\n?");

    public static class Definition {
        public final String word;
        public final String typeOfSpeech;
        public final String description;

        /**
         * Creates a dictionary definition of a word
         *
         * @param word word
         * @param typeOfSpeech type of speech the word belongs to
         * @param description description of the word
         */
        public Definition(String word, String typeOfSpeech, String description) {
            this.word = word;
            this.typeOfSpeech = typeOfSpeech;
            this.description = description;
        }
    }

    private List<Definition> parseRawFile(String path) throws IOException {
        File file = new File(path);
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines.filter(str -> !str.isBlank())
                    .map(this::parseEntry)
                    .collect(Collectors.toList());
        }
    }

    private Definition parseEntry(String line) {
        Matcher matcher = dictRegex.matcher(line.trim());
        if (!matcher.find()) {
            throw new IllegalStateException("Failed to match for line: '" + line + "' isBlank(): " + line.isBlank());
        }
        MatchResult match = matcher.toMatchResult();
        return new Definition(
                trimIfPresent(match.group(1)),
                trimIfPresent(match.group(2)),
                trimIfPresent(match.group(3)));
    }

    private String trimIfPresent(String str) {
        return null == str ? null : str.trim();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            Logger.getDefaultLogger().error("Expected <Raw dictionary path>");
            return;
        }
        WordleDictParser parser = new WordleDictParser();
        try {
            List<Definition> entries = parser.parseRawFile(args[0]);
            String csvPath = toCsvFilename(args[0]);
            printCsv(csvPath, entries);
            Logger.getDefaultLogger().info("Printed " + entries.size() + " definitions to " + csvPath);
        } catch (IOException e) {
            Logger.getDefaultLogger().error(e.getMessage());
        }
    }

    private static String toCsvFilename(String path) {
        int lastPeriodPos = path.lastIndexOf(".");
        if (-1 == lastPeriodPos) {
            return path + ".csv";
        }
        return path.substring(0, lastPeriodPos) + ".csv";
    }

    public static void printCsv(String filename, List<Definition> list) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            printDefinitionHeader(writer);
            for (Definition entry : list) {
                printDefinition(writer, entry);
            }
        }
    }

    private static void printDefinitionHeader(FileWriter writer) throws IOException {
        writer.write("word");
        writer.write(',');
        writer.write("type of speech");
        writer.write(',');
        writer.write("description");
        writer.write('\n');
    }

    private static void printDefinition(FileWriter writer, Definition entry) throws IOException {
        writer.write(toCsvStr(entry.word));
        writer.write(',');
        writer.write(toCsvStr(entry.typeOfSpeech));
        writer.write(',');
        writer.write(toCsvStr(entry.description));
        writer.write('\n');
    }

    /**
     * Transforms a string such that it can be parsed by a csv parser
     *
     * @param string the string to convert
     * @return string the valid csv string
     */
    private static String toCsvStr(String string) {
        if (string.matches(".*[\"\n,].*")) {
            return quote(string.replaceAll("\"", "\"\""));
        }
        return string;
    }

    private static String quote(String toQuote) {
        return '"' + toQuote + '"';
    }

}
