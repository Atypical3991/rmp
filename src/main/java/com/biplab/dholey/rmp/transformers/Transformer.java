package com.biplab.dholey.rmp.transformers;

public interface Transformer<S, T> {
    T transform(S source);
}