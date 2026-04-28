package com.app.diseaseapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QnAResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qna_result);

        TextView questionView = findViewById(R.id.txtQuestion);
        TextView answerView = findViewById(R.id.txtAnswer);
        Button btnClose = findViewById(R.id.btnClose);

        String question = getIntent().getStringExtra("query");
        String answer = getIntent().getStringExtra("answer");

        questionView.setText(question == null || question.trim().isEmpty() ? "Question" : question);
        answerView.setText(answer == null || answer.trim().isEmpty() ? "No response available." : answer.trim());

        btnClose.setOnClickListener(v -> finish());
    }
}
