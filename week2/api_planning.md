# API Planning

## Week 2 Focus
In Week 2, the backend communication flow was planned.

## Purpose of API
The API will act as a bridge between the Android application and the AI model.

## Planned API Flow
1. User uploads image from Android app
2. App sends image request to backend/API
3. Backend processes the request
4. AI model generates prediction
5. Result is returned to Android app
6. App displays disease name and suggestion

## Expected Request
The Android app will send image data to the backend.

## Expected Response
The backend should return:
- Disease name
- Confidence score
- Cure or suggestion
- Status message

## Example Response Format
```json
{
  "status": "success",
  "disease": "Tomato Leaf Blight",
  "confidence": "92%",
  "suggestion": "Remove infected leaves and use recommended fungicide."
}
