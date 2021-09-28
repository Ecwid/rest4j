package com.rest4j.impl.petapi;

import com.rest4j.Cloner;

public class TestCloner implements Cloner {
    @Override
    public <T> T clone(T object) {
        if (object instanceof Duplicable) {
            return (T) ((Duplicable<?>) object).duplicate();
        } else {
            throw new IllegalStateException("class " + object.getClass() + " is not duplicable");
        }
    }
}
