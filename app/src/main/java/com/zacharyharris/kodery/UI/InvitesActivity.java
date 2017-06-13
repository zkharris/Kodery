package com.zacharyharris.kodery.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.zacharyharris.kodery.Model.Update;
import com.zacharyharris.kodery.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.zacharyharris.kodery.UI.BoardMembersActivity.root;

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
        android.support.v7.app.ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#245a7a")));

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        boardList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.inviteRecycleView);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new RecycleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference(root + "/invites/" + mFirebaseUser.getUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boardList.clear();
                        for(DataSnapshot data : dataSnapshot.getChildren()) {
                            findBoard(String.valueOf(data.getKey()));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Kodery", "getUser:onCancelled", databaseError.toException());
                    }
                });

        Log.w(TAG, "Invites: " + String.valueOf(boardList));

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

        Log.w(TAG, "Invites: " + String.valueOf(boardList));

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
                    Toast.makeText(v.getContext(), "Accepted invite to: " +
                            boardList.get(position).getName(), Toast.LENGTH_SHORT).show();
                    acceptInvite(boardList.get(position), mFirebaseUser.getUid());

                }
                if (v.getId() == decline.getId()) {
                    Toast.makeText(v.getContext(), "Declined invite to: " +
                            boardList.get(position).getName(), Toast.LENGTH_SHORT).show();
                    declineInvite( boardList.get(position), mFirebaseUser.getUid());
                }
            }
        }
    }

    private void acceptInvite(Board board, String uid) {
        mDatabase.child(root).child("boards").child(board.getBoardKey()).child("peeps").child(uid).setValue(true);
        mDatabase.child(root).child("invites").child(uid).child(board.getBoardKey()).removeValue();

        boardList.remove(board);
        adapter.notifyDataSetChanged();

        String updateText = (mFirebaseUser.getDisplayName() + " has joined");
        update(board, updateText);

        Intent intent = new Intent(this, SingleBoardActivity.class);
        intent.putExtra("board", board);
        startActivity(intent);
        finish();
    }

    private void update(Board board, String updateText) {
        String key = mDatabase.child(root).child("updates").child(board.getBoardKey()).push().getKey();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Log.w(TAG, currentDateTimeString);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        String dateString = format.format(calendar.getTime());

        Update update = new Update();
        update.setText(updateText);
        update.setBoard(board.getBoardKey());
        update.setKey(key);
        update.setDate(dateString);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(root + "/updates/" + board.getBoardKey() + "/" + key, update.toFirebaseObject());
        mDatabase.updateChildren(childUpdates);
    }

    private void declineInvite(Board board, String uid) {
        mDatabase.child(root).child("invites").child(uid).child(board.getBoardKey()).removeValue();

        boardList.remove(board);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
