package reduction.module;

import parser.Parser;
import parser.Writer;
import reduction.ReductionData;
import transform.Processor;

import java.io.OutputStream;

public interface Module<Input, Output> extends Parser<Input>, Processor<Input, Output>, Writer<Output> {
    String name();
    void init(ReductionData data);
}
