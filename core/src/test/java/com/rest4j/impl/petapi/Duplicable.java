package com.rest4j.impl.petapi;

import javax.annotation.Nonnull;

public interface Duplicable<SubT> {
    @Nonnull
    SubT duplicate();
}
