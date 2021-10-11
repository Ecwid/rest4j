package com.rest4j.impl.petapi;

import com.rest4j.Cloner;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TestCloner implements Cloner {
    @Override
    public <T> T clone(T object) {
        if (object instanceof Duplicable) {
            return (T) ((Duplicable<?>) object).duplicate();
        } else if (object instanceof HashMap) {
            return (T) new HashMap((HashMap) object);
        } else if (object instanceof LinkedHashMap) {
            return (T) new HashMap((LinkedHashMap) object);
        } else {
            throw new IllegalStateException("class " + object.getClass() + " is not duplicable");
        }
    }
}
