package reduction.module;

import parser.Parser;
import reduction.ReductionData;
import transform.Processor;

import java.io.OutputStream;

public interface Module<Input, Output> extends Parser<Input>, Processor<Input, Output> {
    String name();
    void init(ReductionData data);
    void write(Output output, OutputStream stream);
}
