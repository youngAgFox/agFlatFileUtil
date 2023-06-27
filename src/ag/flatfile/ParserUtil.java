package ag.flatfile;

import java.io.IOException;
import java.io.Reader;

public class ParserUtil {

    public static int peek(Reader reader) throws IOException {
        reader.mark(1);
        int next = reader.read();
        reader.reset();
        return next;
    }

    /**
     * Reads a String that is defined by a start and end quote character. Supports an escape character,
     * which allows the quote character to be in the string provided the escape precedes it.
     *
     * @param reader
     * @param quote quoting char that marks the start and end of the string. Usually " or '
     * @param escapeChar
     * @return
     * @throws IOException
     */
    public static String readString(Reader reader, char quote, char escapeChar) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean isEscaped = false;
        while (reader.ready()) {
            int c = reader.read();
            // if quote is not escaped
            if (quote == c && !isEscaped) {
                break;
            }
            if (escapeChar != c || isEscaped) {
                sb.append((char)c);
                isEscaped = false;
            } else {
                isEscaped = true;
            }
        }
        return sb.toString();
    }

    /**
     * Reads characters into a string until the separator char or whitespace is found
     *
     * @param reader the reader character source
     * @param separators characters that indicate the end of the literal.
     * @return The non-whitespace literal String
     * @throws IOException when mark() is not supported on the Reader or on read() failure
     */
    public static String readLiteral(Reader reader, String separators) throws IOException {
        StringBuilder sb = new StringBuilder();
        int peeked = peek(reader);
        while (reader.ready() && isValidLiteralChar(peeked, separators)) {
            int c = reader.read();
            sb.append((char)c);
            peeked = peek(reader);
        }
        return sb.toString();
    }

    private static boolean isValidLiteralChar(int c, String separators) {
        return -1 != c && !separators.contains("" + (char)c) && !Character.isWhitespace(c);
    }

}
