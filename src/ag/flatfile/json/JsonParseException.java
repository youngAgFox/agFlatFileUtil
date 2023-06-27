package ag.flatfile.json;

class JsonParseException extends RuntimeException {
    JsonParseException(String message) {
        super(message);
    }

    JsonParseException(String message, Exception cause) {
        super(message, cause);
    }
}
