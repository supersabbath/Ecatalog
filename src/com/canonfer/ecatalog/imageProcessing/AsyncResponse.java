package com.canonfer.ecatalog.imageProcessing;

import android.graphics.Bitmap;

public interface AsyncResponse {
    void processFinish(Bitmap output);
}