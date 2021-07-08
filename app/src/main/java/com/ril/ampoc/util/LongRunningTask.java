package com.ril.ampoc.util;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class LongRunningTask<Input, Return> implements Callable<Return> {

    private final Function<Input, Return> longRunningFunction;
    private final Input input;
    public LongRunningTask(Function<Input, Return> function, Input input) {
        longRunningFunction = function;
        this.input = input;
    }

    @Override
    public Return call() {
        return longRunningFunction.apply(input);
    }
}
