package postprocessor;

import java.io.File;

public interface PostWriter<Output> {
    void write(Output output, File file);
}
