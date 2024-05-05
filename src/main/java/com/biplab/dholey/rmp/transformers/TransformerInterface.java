package com.biplab.dholey.rmp.transformers;

public interface TransformerInterface<S, T> {
    T transform(S source);
}