package com.example.tmmf5.onmyway;

import java.io.InputStream;


public interface APIListener {

    void preRequest();
    void requestCompleted(InputStream response);

}
