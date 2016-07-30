package parser;

import java.io.OutputStream;

public interface Writer<Output> {
    void write(Output output, OutputStream stream);
}
