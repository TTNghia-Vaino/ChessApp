package com.example.chessapp;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.view.ViewGroup;

public class VsAIActivity extends AppCompatActivity {

    GridLayout chessBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_board);
        setTitle("Chơi với máy");
        chessBoard = findViewById(R.id.chessBoard);
        drawChessBoard();
    }

    private void drawChessBoard() {
        int size = 8;
        int cellSize = getResources().getDisplayMetrics().widthPixels / size;

        int lightColor = Color.parseColor("#EEEED2");
        int darkColor = Color.parseColor("#769656");

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                ImageView cell = new ImageView(this);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                params.setMargins(1, 1, 1, 1);

                cell.setLayoutParams(params);
                cell.setBackgroundColor((row + col) % 2 == 0 ? lightColor : darkColor);

                chessBoard.addView(cell);
            }
        }
    }

}
