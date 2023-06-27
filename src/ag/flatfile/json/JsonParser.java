package ag.flatfile.json;

import ag.flatfile.ParserUtil;

import java.io.*;
import java.util.*;

public class JsonParser {

    private static final char JSON_ARRAY_START = '[';
    private static final char JSON_ARRAY_END = ']';
    private static final char JSON_OBJECT_START = '{';
    private static final char JSON_OBJECT_END = '}';
    private static final char JSON_ASSIGN = ':';
    private static final char JSON_VALUE_SEP = ',';
    private static final String literalSeparators = ",";

    private static final char JSON_STRING_SEP = '"';
    private static final char JSON_ESCAPE = '\\';

    private static final String JSON_LITERAL_TRUE = "true";
    private static final String JSON_LITERAL_FALSE = "false";
    private static final String JSON_LITERAL_NULL = "null";

    private static final Set<Character> jsonOperators = Set.of(
            JSON_ARRAY_START,
            JSON_ARRAY_END,
            JSON_OBJECT_START,
            JSON_OBJECT_END,
            JSON_ASSIGN,
            JSON_VALUE_SEP
    );

    private enum TokenType {
        STRING, LITERAL, OPERATOR
    }

    private static class JsonToken {
        public final String token;
        public final TokenType type;

        public JsonToken(String token, TokenType type) {
            if (null == type || null == token) {
                throw new IllegalStateException("JsonToken with null Token (" + token + ") or TokenType (" + type + ")");
            }
            this.token = token;
            this.type = type;
        }

        @Override
        public String toString() {
            if (TokenType.STRING == type) {
                return '"' + token + '"';
            } else if (TokenType.LITERAL == type) {
                return "'" + token + "'";
            } else if (TokenType.OPERATOR == type) {
                return '<' + token + '>';
            }
            throw new IllegalStateException("JsonToken with unknown TokenType: " + type.name());
        }
    }

    public JsonObject parse(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            LinkedList<JsonToken> tokens = tokenize(reader);
            return buildJson(tokens);
        }
    }

    private JsonObject buildJson(LinkedList<JsonToken> tokens) {
        try {
            return buildOperator(tokens.pop(), tokens).build();
        } catch (NoSuchElementException e) {
            throw new JsonParseException("Ran out of tokens to continue parsing. Unmatched operators.");
        }
    }

    private JsonObjectBuilder buildOperator(JsonToken token, LinkedList<JsonToken> tokens) {
        if (TokenType.OPERATOR != token.type) {
            throw new JsonParseException("Did not parse a valid starting Operator: " + token);
        }
        char op = token.token.charAt(0);
        switch (op) {
            case JSON_ARRAY_START:
                return buildArray(tokens);
            case JSON_OBJECT_START:
                return buildObject(tokens);
            default:
                throw new JsonParseException("Bad or unknown Json starting operator: " + token);
        }
    }

    private JsonObjectBuilder buildArray(LinkedList<JsonToken> tokens) {
        JsonObjectBuilder parent = new JsonObjectBuilder(JsonObject.Type.ARRAY);
        JsonToken token;
        boolean isBuilding = true;
        while (isBuilding) {
            token = tokens.pop();
            if (TokenType.OPERATOR == token.type) {
                parent.add(buildOperator(token, tokens));
            } else {
                parent.add(buildValue(token));
            }
            token = tokens.pop();
            isBuilding = isNotJsonOperator(token, JSON_ARRAY_END);
            if (isBuilding && isNotJsonOperator(token, JSON_VALUE_SEP)) {
                throw new JsonParseException("Found unexpected token building array: " + token);
            }
        }

        return parent;
    }

    private JsonObjectBuilder buildObject(LinkedList<JsonToken> tokens) {
        JsonObjectBuilder parent =  new JsonObjectBuilder(JsonObject.Type.OBJECT);
        JsonToken token;
        boolean isBuilding = true;
        while (isBuilding) {
            token = tokens.pop();
            if (TokenType.STRING != token.type){
                throw new JsonParseException("Expected string token but found " + token);
            }
            String name = token.token;
            // parse the assign statement
            token = tokens.pop();
            if (isNotJsonOperator(token, JSON_ASSIGN)) {
                throw new JsonParseException("Expected assignment for mapping in object type but found " + token);
            }
            token = tokens.pop();
            // parse the value
            Object value;
            if (TokenType.OPERATOR == token.type) {
                value = (buildOperator(token, tokens));
            } else {
                value = buildValue(token);
            }
            parent.add(name, value);
            token = tokens.pop();
            isBuilding = isNotJsonOperator(token, JSON_OBJECT_END);
            if (isBuilding && isNotJsonOperator(token, JSON_VALUE_SEP)) {
                throw new JsonParseException("Found unexpected token building array: " + token);
            }
        }
        return parent;
    }

    private Object buildValue(JsonToken token) {
        if (TokenType.STRING == token.type) {
            return token.token;
        } else if (TokenType.LITERAL == token.type) {
            switch (token.token) {
                case JSON_LITERAL_FALSE:
                    return false;
                case JSON_LITERAL_TRUE:
                    return true;
                case JSON_LITERAL_NULL:
                    return null;
            }
            boolean isDouble = token.token.contains(".") || token.token.contains("e");
            try {
                if (isDouble) {
                    return Double.parseDouble(token.token);
                }
                return Integer.parseInt(token.token);
            } catch (NumberFormatException e) {
                throw new JsonParseException("Invalid literal value: " + token.token);
            }
        }
        throw new JsonParseException("Tried to build value from Json operator: " + token);
    }

    private boolean isNotJsonOperator(JsonToken token, char operator) {
        return TokenType.OPERATOR != token.type || token.token.charAt(0) != operator;
    }

    private LinkedList<JsonToken> tokenize(Reader reader) throws IOException {
        LinkedList<JsonToken> tokens = new LinkedList<>();
        while (reader.ready()) {
            int c = reader.read();
            if (-1 == c) {
                throw new IllegalStateException("Hit EOF while reader was ready");
            }
            if (!Character.isWhitespace(c)) {
                if (jsonOperators.contains((char)c)) {
                    tokens.addLast(new JsonToken("" + (char)c, TokenType.OPERATOR));
                } else if (JSON_STRING_SEP == c) {
                    tokens.addLast(new JsonToken(ParserUtil.readString(reader, JSON_STRING_SEP, JSON_ESCAPE), TokenType.STRING));
                } else {
                    tokens.addLast(new JsonToken((char)c + ParserUtil.readLiteral(reader, literalSeparators), TokenType.LITERAL));
                }
            }
        }
        return tokens;
    }

}

