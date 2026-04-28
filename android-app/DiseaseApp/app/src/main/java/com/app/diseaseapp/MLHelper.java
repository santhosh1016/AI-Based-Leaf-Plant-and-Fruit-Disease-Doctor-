package com.app.diseaseapp;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MLHelper {
    private static List<String> labels;

    public static String predictImage(Context context, Bitmap bitmap) {
        try {
            if (labels == null) {
                labels = loadLabels(context);
            }

            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "plant_disease_model.tflite");
            Interpreter tflite = new Interpreter(tfliteModel);

            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            TensorImage image = TensorImage.fromBitmap(resized);
            float[][] output = new float[1][labels.size()];
            tflite.run(image.getBuffer(), output);

            int maxIdx = 0;
            for (int i = 1; i < labels.size(); i++) {
                if (output[0][i] > output[0][maxIdx]) {
                    maxIdx = i;
                }
            }

            return labels.get(maxIdx);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static List<String> loadLabels(Context context) throws IOException {
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

