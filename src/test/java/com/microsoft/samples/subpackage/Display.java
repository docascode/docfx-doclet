package com.microsoft.samples.subpackage;

import java.io.Serializable;

public interface Display<T, R> extends Serializable {

    void show();

    void hide();
}
