package parser;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

public interface Parser<Type> {
    /**
     * Parses a given type from a file.
     * @param file The file
     * @return An object of the given type.
     */
    Type parse(File file);

    /**
     * Parses a given type from a stream. Closing the stream is not handled by the parser, so should be done by
     * the caller.
     * @param stream The input stream
     * @param id an id associated with the input stream for reporting errors
     * @return An object of the given type
     */
    Type parse(InputStream stream, String id);
}
