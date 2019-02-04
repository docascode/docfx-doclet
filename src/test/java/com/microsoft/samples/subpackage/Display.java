package com.microsoft.samples.subpackage;

import java.io.Serializable;
import java.util.List;

/**
 * Do you see some <code>First</code> code block?
 * <p>
 * Or this {@code Second} code block?
 */
public interface Display<T, R> extends Serializable, List<Person<T>> {

    void show();

    void hide();
}
