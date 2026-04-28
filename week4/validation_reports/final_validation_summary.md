## Validation Summary

| Validation Area | Tool / Method Used | Observation | Improvement Needed |
|---|---|---|---|
| Dataset Validation | Manual checking | Dataset was mostly clean and correctly labelled | More balanced dataset can improve accuracy |
| Preprocessing Validation | Google Colab | Image resizing and normalization worked properly | More edge-case testing can be added |
| UI Flow Testing | Android Studio Emulator | App navigation was smooth | Real-device testing should be done |
| Image Input Testing | Android Emulator / Manual testing | Image upload/capture flow worked | Better error message for invalid images can be added |
| Result Validation | Manual testing | Output was displayed clearly | Result explanation can be more user-friendly |
| Model Integration | Android Studio | TFLite model worked after output shape fix | More real-world testing needed |

## Final Status
The system is stable and ready for final demonstration.

## Future Improvements
- Test on real Android devices
- Improve result explanation
- Add more disease classes
- Improve model accuracy using more balanced data
