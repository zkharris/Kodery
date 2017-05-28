package com.zacharyharris.kodery.UI;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zacharyharris.kodery.R;

import java.util.ArrayList;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zacharyharris.kodery.Model.Board;
import com.zacharyharris.kodery.R;

import java.util.ArrayList;

public class InvitesActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "InvitesActivity";

    RecycleAdapter adapter;
    ArrayList<Board> boardList;

    private SharedPreferences mSharedPreferences;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("invites/" + mFirebaseUser.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boardList.clear();
                        Log.w("Kodery", "getUser:onCancelled " + dataSnapshot.toString());
                        Log.w("Kodery", "count = " + String.valueOf(dataSnapshot.getChildrenCount()) + " values " + dataSnapshot.getKey());
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Log.w(TAG, String.valueOf(data.getKey()));
                            findBoard(String.valueOf(data.getKey()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Kodery", "getUser:onCancelled", databaseError.toException());
                    }
                });

        boardList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.inviteRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void findBoard(final String boardKey) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("board").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    Board boards = data.getValue(Board.class);
                    if(boards.getBoardKey().equals(boardKey)){
                        boardList.add(boards);
                        Log.w(TAG, "Boards:" + boards.getBoardKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });
    }

    class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {


        @Override
        public int getItemCount() {
            return boardList.size();
        }

        @Override
        public RecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invite, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            InvitesActivity.RecycleAdapter.ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.position = position;
            Board boards = boardList.get(position);
            ((ViewHolder) holder).title.setText(boards.getName());
        }

/*
        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                //title = (TextView) itemView.findViewById(R.id.myTextView);
            }

            @Override
            public void onClick(View view) {
                acceptInvite(mFirebaseUser.getUid(), boardList.get(position).getBoardKey());
            }
        }
*/
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            public int position;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                acceptInvite(mFirebaseUser.getUid(), boardList.get(position).getBoardKey());
            }
        }
    }

    private void acceptInvite(String uid, String boardKey) {
        mDatabase.child("board").child(boardKey).child("peeps").child(uid).setValue(true);
        mDatabase.child("invites").child(mFirebaseUser.getUid()).child(boardKey).removeValue();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
