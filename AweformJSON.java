package com.aweform;

import java.util.ArrayList;
import java.util.List;

//
// AweformJSON
// This is a tiny JSON parser used to parse a JSON string into a simple
// in memory representation. This "library" is optimally used by just adding
// this single file to your Java project
// NOTE: that this parser has only basic syntax error handling
// (c) 2021 - Aweform - https://aweform.com/
////////////////////////////////////////////////////////////////////////////////////

public class AweformJSON {

    public class InvalidSyntaxException extends Exception {

        public InvalidSyntaxException(String message) {

            super(message);
        }
    }

    private class ParseContext {

        public char[] chars;
        public int index;
        public int peekIndex;
        public StringBuilder parseStringStringBuilder;
    }

    public enum Token {

        EndOrUnknown,
        ObjectStart,
        ObjectEnd,
        ArrayStart,
        ArrayEnd,
        Colon,
        Comma,
        String,
        Number,
        True,
        False,
        Null
    }

    public enum ElementType {

        String,
        Number,
        Object,
        Array,
        Boolean,
        Null
    }

    public class Ref<T> {

        T value;

        public Ref(T value) {

            this.value = value;
        }
    }

    public class Element {

        public ElementType type;
        public String name;
        public String value;						// String, Number, true, false, null as a String
        public List<Element> elements;				// Attributes if "Object", Items if "Array"

        public Element(ElementType type, String value) {
            
            this.type = type;
            this.elements = null;
            this.value = value;
        }

        public void setAttribute(String name, String value) throws Exception {

            getOrCreateAttributeOfType(name, (value == null)? ElementType.Null : ElementType.String).value = value;
        }

        public void setAttribute(String name, Integer value) throws Exception {

            getOrCreateAttributeOfType(name, ElementType.Number).value = value.toString();
        }

        public void setAttribute(String name, Long value) throws Exception {

            getOrCreateAttributeOfType(name, ElementType.Number).value = value.toString();
        }

        public void setAttribute(String name, Float value) throws Exception {

            getOrCreateAttributeOfType(name, ElementType.Number).value = value.toString();
        }

        public void setAttribute(String name, Double value) throws Exception {

            getOrCreateAttributeOfType(name, ElementType.Number).value = value.toString();
        }

        public void setAttribute(String name, Boolean value) throws Exception {

            getOrCreateAttributeOfType(name, ElementType.Boolean).value = value.toString();
        }

        private Element getOrCreateAttributeOfType(String name, ElementType type) throws Exception {

            if (this.type != ElementType.Object) { throw new Exception("You cannot set an attribute on a none Object type Element"); }

            Element attribute = getAttribute(name);

            if (attribute == null) {

                attribute = new Element(type, "");
                attribute.name = name;

                elements.add(attribute);

            } else {

                attribute.type = type;
            }

            return attribute;
        }

        public String getAttributeAsString(String name) {

            return getAttributeAsString(name, "");
        }

        public String getAttributeAsString(String name, String defaultValue) {

            Element attribute = getAttribute(name);

            if (attribute == null || attribute.value == null) {

                return defaultValue;
            }

            return attribute.value;
        }

        public Boolean getAttributeAsBoolean(String name) {

            return getAttributeAsBoolean(name, false);
        }

        public Boolean getAttributeAsBoolean(String name, Boolean defaultValue) {

            Element attribute = getAttribute(name);

            if (attribute == null || attribute.value == null) {

                return defaultValue;
            }

            try {

                return Boolean.parseBoolean(attribute.value);

            } catch (NumberFormatException ex) {

                return defaultValue;
            }
        }

        public Integer getAttributeAsInt(String name) {

            return getAttributeAsInt(name, 0);
        }

        public Integer getAttributeAsInt(String name, int defaultValue) {

            Element attribute = getAttribute(name);

            if (attribute == null || attribute.value == null) {

                return defaultValue;
            }

            try {

                return Integer.parseInt(attribute.value);

            } catch (NumberFormatException ex) {

                return defaultValue;
            }
        }

        public long getAttributeAsLong(String name) {

            return getAttributeAsLong(name, 0);
        }

        public long getAttributeAsLong(String name, long defaultValue) {

            Element attribute = getAttribute(name);

            if (attribute == null || attribute.value == null) {

                return defaultValue;
            }

            try {
                
                return Long.parseLong(attribute.value);

            } catch (NumberFormatException ex) {

                return defaultValue;
            }
        }

        public Float getAttributeAsFloat(String name) {

            return getAttributeAsFloat(name, 0.f);
        }

        public Float getAttributeAsFloat(String name, Float defaultValue) {

            Element attribute = getAttribute(name);

            if (attribute == null || attribute.value == null) {

                return defaultValue;
            }

            try {

                return Float.parseFloat(attribute.value);

            } catch (NumberFormatException ex) {

                return defaultValue;
            }
        }

        public Double getAttributeAsDouble(String name) {

            return getAttributeAsDouble(name, 0.0);
        }

        public Double getAttributeAsDouble(String name, Double defaultValue) {

            Element attribute = getAttribute(name);

            if (attribute == null || attribute.value == null) {

                return defaultValue;
            }

            try {

                return Double.parseDouble(attribute.value);

            } catch (NumberFormatException ex) {

                return defaultValue;
            }
        }

        public Element getAttribute(String name) {

            if (type != ElementType.Object) {

                return null;
            }

            for (Element value : elements) {

                if (value.name.equals(name)) {

                    return value;
                }
            }

            return null;
        }

        public List<Element> getAttributes() {

            if (type != ElementType.Object) {

                return null;
            }

            return elements;
        }

        public List<Element> getItems() {

            if (type != ElementType.Array) {

                return null;
            }

            return elements;
        }

        public String toJSON() {

            if (type == ElementType.Null) {

                return "null";

            } else if (type == ElementType.Boolean) {

                return value;

            } else if (type == ElementType.Number) {

                return value;

            } else if (type == ElementType.String) {

                return "\"" + encodeStringValue(value) + "\"";
            }

            StringBuilder sb = new StringBuilder();

            if (type == ElementType.Object) {

                sb.append("{");

                Boolean isFirst = true;

                for (Element attribute : elements) {

                    if (isFirst) { isFirst = false; } else { sb.append(", "); }

                    sb.append("\"" + encodeStringValue(attribute.name) + "\": ");
                    sb.append(attribute.toJSON());
                }

                sb.append("}");

            } else if (type == ElementType.Array) {

                sb.append("[");

                Boolean isFirst = true;

                for (Element value : elements) {

                    if (isFirst) { isFirst = false; } else { sb.append(", "); }
                    sb.append(value.toJSON());
                }

                sb.append("]");
            }

            return sb.toString();
        }
    }

    public Element parse(String json) throws InvalidSyntaxException {

        if (json == null) { throw new InvalidSyntaxException("Cannot parse a null string"); }

        ParseContext parseContext = new ParseContext();
        parseContext.chars = json.toCharArray();
        parseContext.parseStringStringBuilder = new StringBuilder();

        return parseElement(parseContext);
    }

    private Element parseElement(ParseContext parseContext) throws InvalidSyntaxException {

        Token nextToken = peekToken(parseContext);

        if (nextToken == Token.ObjectStart) {

            return parseObjectElement(parseContext);

        } else if (nextToken == Token.ArrayStart) {

            return parseArrayElement(parseContext);

        } else if (nextToken == Token.String) {

            parseContext.index = parseContext.peekIndex - 1;
            return new Element(ElementType.String, parseString(parseContext));

        } else if (nextToken == Token.Number) {

            return parseNumberElement(parseContext);

        } else if (nextToken == Token.True) {

            parseContext.index = parseContext.peekIndex;
            return new Element(ElementType.Boolean, "true");

        } else if (nextToken == Token.False) {

            parseContext.index = parseContext.peekIndex;
            return new Element(ElementType.Boolean, "false");

        } else if (nextToken == Token.Null) {

            parseContext.index = parseContext.peekIndex;
            return new Element(ElementType.Null, "null");

        } else {

            throw new InvalidSyntaxException("Unexpected token (" + nextToken + ") at char " + parseContext.index);
        }
    }

    private Element parseObjectElement(ParseContext parseContext) throws InvalidSyntaxException {

        parseContext.index = parseContext.peekIndex;

        Element objectElement = new Element(ElementType.Object, "");
        objectElement.elements = new ArrayList<Element>();

        while (true) {

            Token nextToken = peekToken(parseContext);

            if (nextToken == Token.String) {

                String attributeName = parseString(parseContext);

                if (getNextToken(parseContext) != Token.Colon) {

                    throw new InvalidSyntaxException("Unexpected token at char " + parseContext.index + " expected a Colon before the attribute value");
                }

                Element attributeElement = parseElement(parseContext);
                attributeElement.name = attributeName;

                objectElement.elements.add(attributeElement);

            } else if (nextToken == Token.Comma) {

                if (objectElement.elements.size() == 0) {

                    throw new InvalidSyntaxException("Unexpected Comma at the start of an Object at char " + parseContext.index);
                }

                parseContext.index = parseContext.peekIndex;

            } else if (nextToken == Token.ObjectEnd) {

                parseContext.index = parseContext.peekIndex;
                return objectElement;

            } else {

                throw new InvalidSyntaxException("Unexpected token (" + nextToken + ") at char " + parseContext.index);
            }
        }
    }

    private Element parseArrayElement(ParseContext parseContext) throws InvalidSyntaxException {

        parseContext.index = parseContext.peekIndex;

        Element arrayElement = new Element(ElementType.Array, "");
        arrayElement.elements = new ArrayList<Element>();

        while (true) {

            Token nextToken = peekToken(parseContext);

            if (nextToken == Token.EndOrUnknown) {

                throw new InvalidSyntaxException("Unexpected token (" + nextToken + ") at char " + parseContext.index);

            } else if (nextToken == Token.Comma) {

                if (arrayElement.elements.size() == 0) {

                    throw new InvalidSyntaxException("Unexpected Comma at the start of an Array at char " + parseContext.index);
                }

                parseContext.index = parseContext.peekIndex;

            } else if (nextToken == Token.ArrayEnd) {

                parseContext.index = parseContext.peekIndex;
                break;

            } else {

                arrayElement.elements.add(parseElement(parseContext));
            }
        }

        return arrayElement;
    }

    private String parseString(ParseContext parseContext) throws InvalidSyntaxException {

        parseContext.index = parseContext.peekIndex; // move to "

        StringBuilder sb = parseContext.parseStringStringBuilder;
        sb.setLength(0);

        while (true) {

            if (parseContext.index == parseContext.chars.length) {

                break;
            }

            char c = parseContext.chars[parseContext.index++];

            if (c == '"') {

                return sb.toString();

            } else if (c == '\\') {

                if (parseContext.index == parseContext.chars.length) {

                    break;
                }

                c = parseContext.chars[parseContext.index++];

                if (c == '"') {

                    sb.append('"');

                } else if (c == '\\') {

                    sb.append('\\');

                } else if (c == '/') {

                    sb.append('/');

                } else if (c == 'b') {

                    sb.append('\b');

                } else if (c == 'f') {

                    sb.append('\f');

                } else if (c == 'n') {

                    sb.append('\n');

                } else if (c == 'r') {

                    sb.append('\r');

                } else if (c == 't') {

                    sb.append('\t');

                } else if (c == 'u') {

                    int remainingLength = parseContext.chars.length - parseContext.index;

                    if (remainingLength >= 4) {

                        int codePoint = Integer.parseInt(new String(parseContext.chars, parseContext.index, 4), 16);

                        sb.append((char)codePoint);

                        parseContext.index += 4;

                    } else {

                        break;
                    }

                } else {

                    throw new InvalidSyntaxException("Invalid escape sequence at char " + parseContext.index);
                }

            } else {

                sb.append(c);
            }
        }

        throw new InvalidSyntaxException("Found an incomplete string at char " + parseContext.index);
    }

    private Element parseNumberElement(ParseContext parseContext) {

        parseContext.index = parseContext.peekIndex - 1;

        int lastNumberCharacterIndex;
        String numberCharacters = "0123456789-.eE";

        for (lastNumberCharacterIndex = parseContext.index; lastNumberCharacterIndex < parseContext.chars.length; ++lastNumberCharacterIndex) {

            if (numberCharacters.indexOf(parseContext.chars[lastNumberCharacterIndex]) == -1) {

                break;
            }
        }

        lastNumberCharacterIndex -= 1;

        int charLength = (lastNumberCharacterIndex - parseContext.index) + 1;

        Double number = Double.parseDouble(new String(parseContext.chars, parseContext.index, charLength));

        parseContext.index = lastNumberCharacterIndex + 1;

        Element numberElement = new Element(ElementType.Number, "");
        numberElement.value = number.toString();

        return numberElement;
    }

    private static Token peekToken(ParseContext parseContext) {

        int peekFromIndex = parseContext.index;
        Token peekToken = getNextToken(parseContext);
        parseContext.peekIndex = parseContext.index; // we store the PeekIndex to make things faster in some cases
        parseContext.index = peekFromIndex;

        return peekToken;
    }

    private static Token getNextToken(ParseContext parseContext) {

        skipWhitespace(parseContext);

        if (parseContext.index == parseContext.chars.length) {

            return Token.EndOrUnknown;
        }

        char c = parseContext.chars[parseContext.index++];

        if (c == '{') {

            return Token.ObjectStart;

        } else if (c == '}') {

            return Token.ObjectEnd;

        } else if (c == '[') {

            return Token.ArrayStart;

        } else if (c == ']') {

            return Token.ArrayEnd;

        } else if (c == ':') {

            return Token.Colon;

        } else if (c == ',') {

            return Token.Comma;

        } else if (c == '"') {

            return Token.String;

        } else if ("-0123456789".indexOf(c) != -1) {

            return Token.Number;
        }

        parseContext.index--;

        if (forwardMatch("false", parseContext)) {

            return Token.False;

        } else if (forwardMatch("true", parseContext)) {

            return Token.True;

        } else if (forwardMatch("null", parseContext)) {

            return Token.Null;

        } else {

            return Token.EndOrUnknown;
        }
    }

    private static Boolean forwardMatch(String what, ParseContext parseContext) {

        int remainingLength = parseContext.chars.length - parseContext.index;

        if (remainingLength >= what.length()) {

            for (int i = 0; i < what.length(); ++i) {

                if (parseContext.chars[parseContext.index + i] != what.charAt(i)) {

                    return false;
                }
            }

            parseContext.index += what.length();
            return true;
        }

        return false;
    }

    private static void skipWhitespace(ParseContext parseContext) {

        while (parseContext.index < parseContext.chars.length) {

            char c = parseContext.chars[parseContext.index];

            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {

                break;
            }

            parseContext.index++;
        }
    }

    private static String encodeStringValue(String s) {

        if (s == null || s == "") { return ""; }

        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder();

        for (char c : chars) {

            if (c == '"') {

                sb.append("\\\"");

            } else if (c == '\\') {

                sb.append("\\\\");

            } else if (c == '\b') {

                sb.append("\\b");

            } else if (c == '\f') {

                sb.append("\\f");

            } else if (c == '\t') {

                sb.append("\\t");

            } else if (c == '\n') {

                sb.append("\\n");

            } else if (c == '\r') {

                sb.append("\\r");

            } else if (c >= 32 && c <= 126) {

                sb.append(c);

            } else {

                String hex = Integer.toHexString(c);

                sb.append("\\u");

                for (int i = hex.length(); i < 4; ++i) {

                    sb.append("0");
                }

                sb.append(hex);
            }
        }

        return sb.toString();
    }
}
