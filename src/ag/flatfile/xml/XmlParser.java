package ag.flatfile.xml;

import ag.flatfile.ParserUtil;

import java.io.*;
import java.util.*;

public class XmlParser {

    private static final char XML_START_TAG = '<';
    private static final char XML_END_TAG = '>';
    private static final char XML_ATTR_VAL_SEP = '=';
    private static final char XML_LITERAL_OP_SEP = '&';
    private static final char XML_END_TAG_PAIR = '/';
    private static final String XML_LITERAL_SEPARATORS = "<>=&/";
    private static final char XML_STRING_SEP = '"';
    private static final char XML_ESCAPE = '\\';

    private static final Set<Character> xmlOperators = Set.of(
            XML_START_TAG,
            XML_END_TAG,
            XML_ATTR_VAL_SEP,
            XML_LITERAL_OP_SEP,
            XML_END_TAG_PAIR
    );

    private static final Map<String, String> xmlLiteralOperators = Map.of(
            "amp", "&",
            "lt", "<",
            "gt", ">",
            "apos", "'",
            "quot", "\""
    );

    static class XmlToken {

        public enum Type {
            OPERATOR, STRING, LITERAL, WHITESPACE
        }

        private final String token;
        private final Type type;

        public XmlToken(String token, Type type) {
            this.token = token;
            this.type = type;
        }

        @Override
        public String toString() {
            if (Type.STRING == type) {
                return '"' + token + '"';
            } else if (Type.LITERAL == type) {
                return "'" + token + "'";
            } else if (Type.OPERATOR == type) {
                return '<' + token + '>';
            } else if (Type.WHITESPACE == type) {
                return "[" + Character.getName(token.charAt(0)) + "]";
            }
            throw new IllegalStateException("JsonToken with unknown TokenType: " + type.name());
        }
    }

    public Entity parse(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            LinkedList<XmlToken> tokens = parseTokens(reader);
            System.out.println(tokens);
            return buildXml(tokens);
        }
    }

    private LinkedList<XmlToken> parseTokens(Reader reader) throws IOException {
        LinkedList<XmlToken> tokens = new LinkedList<>();
        while (reader.ready()) {
            int c = reader.read();
            if (-1 == c) {
                throw new IllegalStateException("Hit EOF while reader was ready");
            }
            if (xmlOperators.contains((char)c)) {
                tokens.addLast(new XmlToken("" + (char)c, XmlToken.Type.OPERATOR));
            } else if (XML_STRING_SEP == c) {
                tokens.addLast(new XmlToken(ParserUtil.readString(reader, XML_STRING_SEP, XML_ESCAPE), XmlToken.Type.STRING));
            } else if (Character.isWhitespace(c)) {
                tokens.addLast(new XmlToken("" + (char)c, XmlToken.Type.WHITESPACE));
            } else {
                tokens.addLast(new XmlToken((char) c + ParserUtil.readLiteral(reader, XML_LITERAL_SEPARATORS), XmlToken.Type.LITERAL));
            }
        }
        return tokens;
    }

    private Entity buildXml(LinkedList<XmlToken> tokens) {
        Entity root;

        while (!tokens.isEmpty()) {
    // TODO
        }
        return null;
    }

}
