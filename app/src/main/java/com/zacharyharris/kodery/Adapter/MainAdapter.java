package com.zacharyharris.kodery.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.R;

import java.util.ArrayList;

/**
 * Created by AlexLue on 5/24/17.
 */

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
                .inflate(R.layout.row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MainAdapter.ViewHolder holder, int position) {
        holder.mBoard.setText(boardsList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return boardsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mBoard;

        public ViewHolder(View itemView) {
            super(itemView);
            mBoard =(TextView) itemView.findViewById(R.id.board);
        }

    }
}
