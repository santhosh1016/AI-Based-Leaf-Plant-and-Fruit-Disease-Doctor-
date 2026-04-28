# AI Model Development and Deployment Pipeline

## Work Done by AI Engineer

The AI model was developed through the following process:

1. Dataset collection from Kaggle
2. Dataset setup in Google Colab using `kaggle.json`
3. Image preprocessing
4. CNN model training
5. Model conversion to TensorFlow Lite
6. Android model integration
7. Model testing and validation

## Tools Used
- Kaggle
- Google Colab
- TensorFlow / Keras
- TensorFlow Lite
- Android Studio

## Output Files
- `plant_disease_model.tflite`
- `plant_labels.txt`

## Important Fix
The model output shape was `[1, 15]`, so Android classifier logic was updated to handle 15 class probabilities.

## Status
AI model pipeline completed and integrated.
