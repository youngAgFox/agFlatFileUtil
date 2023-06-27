package ag.flatfile.csv;

import ag.logger.Logger;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class CsvMap {

    private final ArrayList<ArrayList<String>> data;
    private final Map<String, Integer> keyMap;
    private int errorOffset = 0;

    public CsvMap(String filename) throws IOException, ParseException {
        data = readCsv(new File(filename));
        keyMap = genKeyMap();
    }

    public int rows() {
        return data.size();
    }

    public int columns() {
        return data.isEmpty() ? 0 : data.get(0).size();
    }

    public String get(int row, int column) {
        return data.get(row).get(column);
    }

    public String get(int row, String key) {
        Integer keyIndex = keyMap.get(key);
        if (null == keyIndex) {
            throw new NoSuchElementException("The key " + key + " is not a valid argument");
        }
        return data.get(row).get(keyIndex);
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(keyMap.keySet());
    }

    private Map<String, Integer> genKeyMap() {
        Map<String, Integer> keyMap = new HashMap<>(columns());
        if (data.isEmpty()) {
            return keyMap;
        }
        ArrayList<String> headers = data.get(0);
        for (int i = 0; i < columns(); i++) {
            if (keyMap.containsKey(headers.get(i))) {
                throw new IllegalStateException("Csv has duplicate header columns!");
            }
            keyMap.put(headers.get(i), i);
        }
        return keyMap;
    }

    private ArrayList<ArrayList<String>> readCsv(File file) throws IOException, ParseException {
        Logger log = Logger.getDefaultLogger();
        ArrayList<ArrayList<String>> csv = new ArrayList<>();
        OptionalInt expColumns = OptionalInt.empty();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            log.debug("Reading csv row");
            while (reader.ready()) {
                csv.add(readRow(reader, expColumns));
                if (expColumns.isEmpty()) {
                    expColumns = OptionalInt.of(csv.get(0).size());
                }
            }
        }

        return csv;
    }

    private ArrayList<String> readRow(Reader reader, OptionalInt expColumns) throws IOException, ParseException {
        ArrayList<String> row = new ArrayList<>(expColumns.orElse(10));
        ParseResult col;

        do {
            col = parseColumn(reader);
            if (null != col.column) {
                row.add(col.column);
            }
        } while (!col.isEndOfRow);
        return row;
    }

    private static class ParseResult {
        private final boolean isEndOfRow;
        private final String column;

        public ParseResult(String column, boolean isEndOfRow) {
            this.column = column;
            this.isEndOfRow = isEndOfRow;
        }
    }

    private abstract static class Processor {

        protected boolean isProcessing = true;
        protected boolean isEndOfRow = false;

        abstract boolean process(Reader reader, int charCode) throws IOException, ParseException;

        boolean isProcessing() {
            return isProcessing;
        }

        public boolean isEndOfRow() {
            return isEndOfRow;
        }
    }

    private class QuotedProcessor extends Processor {
        @Override
        public boolean process(Reader reader, int charCode) throws IOException, ParseException {
            if (-1 == charCode) {
                throw new IOException("Found end of file before finding end of quoted column");
            }
            if ('"' == charCode) {
                switch (peek(reader)) {
                    case '"':
                        // skip escaped quote
                        skip(reader, 1);
                        break;
                    case '\n':
                        isEndOfRow = true;
                    case ',':
                        skip(reader, 1);
                    case -1:
                        // exit processing (without appending quote), found end of quoted string
                        isProcessing = false;
                        return false;
                    default:
                        throw new ParseException("Found excess characters after end of quoted column ("
                                + errorOffset + ")", errorOffset);
                }
            }
            return true;
        }
    }

    private class UnquotedProcessor extends Processor {
        @Override
        public boolean process(Reader reader, int charCode) throws IOException, ParseException {
            switch (charCode) {
                case '"':
                    // illegal char in unquoted column
                    throw new ParseException("Unquoted columns cannot contain quotes (" + errorOffset + ")", errorOffset);
                case '\n':
                    isEndOfRow = true;
                case ',':
                case -1:
                    isProcessing = false;
                    return false;
            }
            return true;
        }
    }

    private ParseResult parseColumn(Reader reader) throws IOException, ParseException {
        StringBuilder col = new StringBuilder();
        if (!reader.ready()) {
            return new ParseResult(null, true);
        }
        boolean isQuoted = '"' == peek(reader);
        Processor processor = isQuoted ? new QuotedProcessor() : new UnquotedProcessor();
        if (isQuoted) {
            // skip the initial quote
            skip(reader, 1);
        }
        int charCode;

        while (processor.isProcessing() && reader.ready()) {
            charCode = reader.read();
            errorOffset++;
            if (processor.process(reader, charCode)) {
                col.append((char)charCode);
            }
        }

        return new ParseResult(col.toString(), processor.isEndOfRow());
    }

    private int peek(Reader reader) throws IOException {
        reader.mark(1);
        int c = reader.read();
        reader.reset();
        return c;
    }

    private void skip(Reader reader, int n) throws IOException {
        if (n != reader.skip(n)) {
            throw new IOException("Failed to skip correct number of chars");
        }
        errorOffset += n;
    }

}
