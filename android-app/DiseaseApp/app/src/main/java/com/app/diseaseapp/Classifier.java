package com.app.diseaseapp;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Classifier {

    private final Interpreter interpreter;
    private final List<String> labels;
    private final int inputImageWidth = 224;
    private final int inputImageHeight = 224;
    private final int inputImageChannels = 3;
    private float lastConfidence = 0f;

    public Classifier(Context context) throws IOException {
        ByteBuffer modelBuffer = FileUtil.loadMappedFile(context, "plant_disease_model.tflite");
        interpreter = new Interpreter(modelBuffer);
        labels = loadLabels(context);
    }

    public String classify(Bitmap bitmap) {
        ByteBuffer input = preprocessImage(bitmap);
        float[][] output = new float[1][labels.size()];
        interpreter.run(input, output);

        int maxIndex = 0;
        for (int i = 1; i < labels.size(); i++) {
            if (output[0][i] > output[0][maxIndex]) {
                maxIndex = i;
            }
        }
        lastConfidence = output[0][maxIndex];
        return labels.get(maxIndex);
    }

    public float getLastConfidence() {
        return lastConfidence;
    }

    private ByteBuffer preprocessImage(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputImageWidth * inputImageHeight * inputImageChannels);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[inputImageWidth * inputImageHeight];
        resized.getPixels(intValues, 0, resized.getWidth(), 0, 0, resized.getWidth(), resized.getHeight());

        for (int pixel : intValues) {
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;
            byteBuffer.putFloat(r);
            byteBuffer.putFloat(g);
            byteBuffer.putFloat(b);
        }

        return byteBuffer;
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("plant_labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }
}
