# AI-Based-Leaf-Plant-and-Fruit-Disease-Doctor-
AI Based Leaf, Plant and Fruit Disease Detection System

## Project Overview
AI Based Leaf, Plant and Fruit Disease Doctor is an Android-based AI application that detects plant, leaf, and fruit diseases from images. Users can upload or capture an image, and the system predicts the disease using a TensorFlow Lite model.

## Team Members and Roles
| Member | Role |
| Nisarg Soni | Project Leader |
| Samruddhi Parmar | AI Engineer |
| Meet Vaghela | Backend Developer |
| Santosh Thadakale | QA Engineer |
| Surepally Pavan Kumar | UI/UX Developer |

## Project Structure

project-root/
├── android-app/
│   └── DiseaseApp/
├── model/
│   ├── plant_disease_model.tflite
│   └── plant_labels.txt
├── week1/
├── week2/
├── week3/
├── week4/
├── docs/
└── README.md

## Tools and Technologies

* Android Studio
* Java
* XML
* TensorFlow Lite
* Google Colab
* Kaggle Dataset
* GitHub

## Environment Setup

Android App Setup
1. Install Android Studio.
2. Clone or download this repository.
3. Open the Android project folder in Android Studio.
4. Sync Gradle files.
5. Make sure the following files are inside the Android assets folder:
    plant_disease_model.tflite
    plant_labels.txt

### Android App Setup

1. Install Android Studio.
2. Clone or download this repository.
3. Open the Android project folder in Android Studio.
4. Sync Gradle files.
5. Make sure the following files are inside the Android assets folder:

   * `plant_disease_model.tflite`
   * `plant_labels.txt`
## How to Run the Android App

1. Open the project in Android Studio.
2. Connect an Android device or start an emulator.
3. Click Run.
4. Login/Register in the app.
5. Upload or capture a plant image.
6. View the disease prediction result.

## How to Train the Model

1. Open Google Colab.
2. Download dataset from Kaggle.
3. Preprocess images by resizing and normalizing.
4. Train CNN model.
5. Convert the trained model to TensorFlow Lite.

## Basic training flow:

Kaggle Dataset → Google Colab → Preprocessing → CNN Training → TFLite Conversion

## How to Run Demo

1. Open Android app in emulator or real device.
2. Upload a plant/leaf/fruit image.
3. The app loads the TensorFlow Lite model.
4. The model predicts disease class.
5. The result is displayed in the app.

## Important Integration Fix

During integration, the TensorFlow Lite model returned output shape `[1, 15]`, meaning the model predicted probabilities for 15 classes. The Android classifier was updated to handle all 15 outputs and select the class with the highest confidence.

The `plant_labels.txt` file was also corrected because extra newline formatting caused label mapping issues.

## Testing

Testing was done for:

* Dataset validation
* Image preprocessing
* Android UI flow
* Image upload
* TFLite model prediction
* Result display

## Limitations

* Model accuracy can improve with more balanced dataset
* More real-world images are needed for testing
* Real-device testing should be expanded
* Cure suggestions can be improved further

## Future Work

* Multi-language support
* Better disease cure recommendation
* Offline detection improvement
* More plant and fruit disease classes
* Real-time camera detection
