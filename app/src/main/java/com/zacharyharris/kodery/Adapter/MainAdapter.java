package com.zacharyharris.kodery.Adapter;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.R;
import com.zacharyharris.kodery.UI.MainActivity;
import com.zacharyharris.kodery.UI.SingleBoardActivity;

import java.util.ArrayList;

import static com.zacharyharris.kodery.R.id.board;

/**
 * Created by AlexLue on 5/24/17.
 */
/*

CURRENTLY NOT IN USE. TRANSFERRED TO MAIN ACTIVITY

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    public static final String TAG = "MainAdapter";
    private ArrayList<Board> boardsList;

    public MainAdapter(ArrayList<Board> boardsList) {
        this.boardsList = boardsList;
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_board, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainAdapter.ViewHolder holder, int position) {
        holder.mBoard.setText(boardsList.get(position).getName());
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return boardsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        public TextView mBoard;
        public int position;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mBoard =(TextView) itemView.findViewById(board);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: Clicked!");
            Toast.makeText(v.getContext(), "Clicked",Toast.LENGTH_SHORT).show();

            Intent i = new Intent(v.getContext(), SingleBoardActivity.class);
            i.putExtra("board", boardsList.get(position));
            v.getContext().startActivity(i);

        }

        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(v.getContext(), "LONG Click",Toast.LENGTH_SHORT).show();

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(v.getContext());
            View mview = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_board, null);
            final EditText mboardname = (EditText) mview.findViewById(R.id.boardnme_edit);
            Button saveleboard = (Button) mview.findViewById(R.id.saveBoard);
            Button delboard = (Button) mview.findViewById(R.id.delBoard);

            saveleboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mboardname.getText().toString().isEmpty()){
                        Toast.makeText(v.getContext(),
                                "Task Created!",
                                Toast.LENGTH_SHORT).show();
                        // Get rid of the pop up go back to main activity
                    }else{
                        Toast.makeText(v.getContext(),
                                "Please name the task.",
                                Toast.LENGTH_SHORT).show();

                    }
                }
            });

            mBuilder.setView(mview);
            AlertDialog dialog = mBuilder.create();
            dialog.show();

            return true;
        }
    }
}

*/