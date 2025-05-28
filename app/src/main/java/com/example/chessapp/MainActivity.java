package com.example.chessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    Button btnVsAI, btn2Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setBackgroundResource(R.drawable.background);

        setTitle("Cờ Vua");
        btnVsAI = findViewById(R.id.btnVsAI);
        btn2Player = findViewById(R.id.btn2Player);

        btnVsAI.setOnClickListener(v -> startActivity(new Intent(this, VsAIActivity.class)));
        btn2Player.setOnClickListener(v -> startActivity(new Intent(this, Offline2PlayerActivity.class)));
    }
}
