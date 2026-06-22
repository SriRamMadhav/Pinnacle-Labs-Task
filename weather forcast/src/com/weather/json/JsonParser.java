package com.weather.json;

import java.util.*;

/**
 * A simple, lightweight recursive-descent JSON parser.
 * It parses JSON strings into standard Java collections (Map, List, String, Double, Boolean, null).
 */
public class JsonParser {
    private final String src;
    private int pos;

    public JsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    public static Map<String, Object> parse(String json) {
        if (json == null) {
            return null;
        }
        JsonParser parser = new JsonParser(json.trim());
        Object root = parser.parseValue();
        if (root instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) root;
            return map;
        }
        throw new IllegalArgumentException("Root of JSON must be an object");
    }

    private Object parseValue() {
        skipWhitespace();
        if (pos >= src.length()) {
            throw new IllegalArgumentException("Unexpected end of input");
        }

        char c = src.charAt(pos);
        if (c == '{') {
            return parseObject();
        } else if (c == '[') {
            return parseArray();
        } else if (c == '"') {
            return parseString();
        } else if (c == 't' || c == 'f') {
            return parseBoolean();
        } else if (c == 'n') {
            return parseNull();
        } else if (Character.isDigit(c) || c == '-') {
            return parseNumber();
        } else {
            throw new IllegalArgumentException("Unexpected character: '" + c + "' at position " + pos);
        }
    }

    private Map<String, Object> parseObject() {
        match('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();

        if (pos < src.length() && src.charAt(pos) == '}') {
            pos++;
            return map;
        }

        while (true) {
            skipWhitespace();
            if (pos >= src.length() || src.charAt(pos) != '"') {
                throw new IllegalArgumentException("Expected string key at position " + pos);
            }
            String key = parseString();
            skipWhitespace();
            match(':');
            Object val = parseValue();
            map.put(key, val);
            skipWhitespace();

            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                break;
            }
            match(',');
        }
        return map;
    }

    private List<Object> parseArray() {
        match('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();

        if (pos < src.length() && src.charAt(pos) == ']') {
            pos++;
            return list;
        }

        while (true) {
            list.add(parseValue());
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                break;
            }
            match(',');
        }
        return list;
    }

    private String parseString() {
        match('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            } else if (c == '\\') {
                if (pos >= src.length()) {
                    throw new IllegalArgumentException("Unterminated escape sequence in string");
                }
                char next = src.charAt(pos++);
                switch (next) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > src.length()) {
                            throw new IllegalArgumentException("Invalid unicode escape sequence");
                        }
                        String hex = src.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default:
                        sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("Unterminated string starting at position " + pos);
    }

    private Boolean parseBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        } else if (src.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Expected boolean at position " + pos);
    }

    private Object parseNull() {
        if (src.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new IllegalArgumentException("Expected null at position " + pos);
    }

    private Double parseNumber() {
        int start = pos;
        if (pos < src.length() && src.charAt(pos) == '-') {
            pos++;
        }
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
            pos++;
        }
        String numStr = src.substring(start, pos);
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number formats: " + numStr + " at position " + start);
        }
    }

    private void match(char expected) {
        skipWhitespace();
        if (pos >= src.length() || src.charAt(pos) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at position " + pos);
        }
        pos++;
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }
}
