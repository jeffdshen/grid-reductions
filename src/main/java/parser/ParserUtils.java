package parser;

import java.io.ByteArrayOutputStream;

public class ParserUtils {
    public static <T> String asString(Writer<T> writer, T obj) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.write(obj, stream);
        return stream.toString();
    }
}
