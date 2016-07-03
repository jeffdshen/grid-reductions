package postprocessor;

public interface PostProcessor<Input, Output> {
    Output process(Input input);
}
