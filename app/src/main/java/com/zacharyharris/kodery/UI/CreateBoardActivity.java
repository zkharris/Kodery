package com.zacharyharris.kodery.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.R;

public class CreateBoardActivity extends AppCompatActivity {
    Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);

        if(getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            board = (Board)extras.get("board");
        }

        final EditText boardTitle = (EditText) findViewById(R.id.boardname_edit);
        boardTitle.setText(board.getName());
    }
}
