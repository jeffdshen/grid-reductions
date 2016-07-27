package transform;

public interface Processor<Input, Output> {
    Output process(Input input);
}
