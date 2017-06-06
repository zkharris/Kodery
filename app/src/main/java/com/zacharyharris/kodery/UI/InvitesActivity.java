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
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String root = "testRoot";

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
        database.getReference(root + "/invites/" + mFirebaseUser.getUid()).addValueEventListener(
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
        database.getReference(root + "/boards/" + boardKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Board board = dataSnapshot.getValue(Board.class);
                boardList.add(board);
                adapter.notifyDataSetChanged();
                Log.w(TAG, boardList.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, databaseError.toException());
            }
        });
    }

    class RecycleAdapter extends RecyclerView.Adapter {


        @Override
        public int getItemCount() {
            return boardList.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            viewHolder.position = position;
            Board boards = boardList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(boards.getName());
        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView title;
            Button accept;
            Button decline;
            public int position;

            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.invite_lbl);
                accept = (Button) itemView.findViewById(R.id.accept);
                decline = (Button) itemView.findViewById(R.id.decline);
                accept.setOnClickListener(this);
                decline.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == accept.getId()){
                    acceptInvite(mFirebaseUser.getUid(), boardList.get(position).getBoardKey());
                    Toast.makeText(v.getContext(), "Accepted invite to: " +
                            boardList.get(position).getName(), Toast.LENGTH_SHORT).show();
                    boardList.remove(boardList.get(position));
                }
                if (v.getId() == decline.getId()) {
                    declineInvite(mFirebaseUser.getUid(), boardList.get(position).getBoardKey());
                    Toast.makeText(v.getContext(), "Declined invite to: " +
                            boardList.get(position).getName(), Toast.LENGTH_SHORT).show();
                    boardList.remove(boardList.get(position));
                }
            }
        }
    }

    private void acceptInvite(String uid, String boardKey) {
        mDatabase.child(root).child("boards").child(boardKey).child("peeps").child(uid).setValue(true);
        mDatabase.child(root).child("invites").child(mFirebaseUser.getUid()).child(boardKey).removeValue();
    }

    private void declineInvite(String uid, String boardKey) {
        mDatabase.child(root).child("invites").child(mFirebaseUser.getUid()).child(boardKey).removeValue();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
