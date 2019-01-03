package com.microsoft.samples.subpackage;

import java.io.Serializable;
import java.util.List;

public interface Display<T, R> extends Serializable, List<Person<T>> {

    void show();

    void hide();
}
