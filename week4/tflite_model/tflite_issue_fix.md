# TensorFlow Lite Issue Fix (Week 4)

## Issue Faced
During Android integration, the following error occurred:

Cannot copy from a TensorFlowLite tensor with shape [1, 15] to a Java object with shape [1, 1].

## Reason
The model was returning probabilities for 15 classes, but the Android classifier code was expecting only one output value.

## Fix Applied
The classifier logic was updated to handle output shape `[1, 15]`.

Instead of expecting one value, the app now reads all 15 probabilities and selects the class with the highest confidence score.

## Label File Issue
Another issue was found in `plant_labels.txt`. The file had extra newline formatting, which affected label mapping.

## Fix Applied for Labels
The label file was corrected so that each class name appears properly on a separate line.

## Result
After fixing output handling and label formatting, the model worked correctly in the Android application.

## Status
Resolved.
