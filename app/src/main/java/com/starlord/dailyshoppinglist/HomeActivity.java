package com.starlord.dailyshoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.starlord.dailyshoppinglist.model.Data;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FloatingActionButton fab_btn;
    private TextView totalAmount;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    FirebaseRecyclerAdapter<Data,MyViewHolder> adapter;

    //global data variables
    private String type, note;
    private int amount;
    private String post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        totalAmount = findViewById(R.id.total_amount);
        toolbar = findViewById(R.id.home_toolbar);
        toolbar.setTitle("Shopping List");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);

        recyclerView = findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        //Calculating the total amount
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int amount = 0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Data data = snapshot.getValue(Data.class);
                    amount+=data.getAmount();
                    String tAmount = String.valueOf(amount);
                    totalAmount.setText("₹ "+tAmount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        showData();

        fab_btn = findViewById(R.id.fab);
        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog();
            }
        });

    }

    private void customDialog(){
        AlertDialog.Builder myDialog = new  AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.input_data_layout,null);
        final AlertDialog dialog = myDialog.create();
        dialog.setView(view);

        final EditText type = view.findViewById(R.id.edt_type);
        final EditText amount = view.findViewById(R.id.edt_amount);
        final EditText note = view.findViewById(R.id.edt_note);
        Button save = view.findViewById(R.id.btn_save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mType = type.getText().toString().trim();
                String mAmount = amount.getText().toString().trim();
                String mNote = note.getText().toString().trim();

                if (TextUtils.isEmpty(mType)){
                    type.setError("Required field.");
                    return;
                }
                if (TextUtils.isEmpty(mAmount)) {
                    amount.setError("Required field.");
                    return;
                }
                if (TextUtils.isEmpty(mNote)){
                    note.setError("Required field.");
                    return;
                }

                String id = mDatabase.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());
                int amount = Integer.parseInt(mAmount);

                Data data = new Data(mType,amount,mNote,date,id);

                mDatabase.child(id).setValue(data);

                Toast.makeText(HomeActivity.this, "Shopping data added", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    protected void onStart() {
        showData();
        super.onStart();
    }

    protected void onResume() {
        showData();
        super.onResume();
    }

    private void showData(){
        adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(
                Data.class,R.layout.item_data,MyViewHolder.class,mDatabase
        ) {
            @Override
            protected void populateViewHolder(MyViewHolder myViewHolder, final Data data, final int i) {
                myViewHolder.setDate(data.getDate());
                myViewHolder.setType(data.getType());
                myViewHolder.setNote(data.getNote());
                myViewHolder.setAmount(data.getAmount());

                myViewHolder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(i).getKey();
                        type = data.getType();
                        note = data.getNote();
                        amount = data.getAmount();

                        updateData();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View myView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            myView = itemView;
        }

        public void setDate(String date){
            TextView mDate = myView.findViewById(R.id.date);
            mDate.setText(date);
        }
        public void setType(String type){
            TextView mType = myView.findViewById(R.id.type);
            mType.setText(type);
        }
        public void setNote(String note){
            TextView mNote = myView.findViewById(R.id.note);
            mNote.setText(note);
        }
        public void setAmount(int amount){
            TextView mAmount = myView.findViewById(R.id.amount);
            String value = String.valueOf(amount);
            mAmount.setText("₹ "+value);
        }
    }

    public void updateData(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view =  inflater.inflate(R.layout.update_input_data_layout,null);
        final AlertDialog dialog = myDialog.create();
        dialog.setView(view);
        final EditText uType = view.findViewById(R.id.edt_type_upd);
        final EditText uAmount = view.findViewById(R.id.edt_amount_upd);
        final EditText uNote = view.findViewById(R.id.edt_note_upd);

        uType.setText(type);
        uType.setSelection(type.length());
        uNote.setText(note);
        uNote.setSelection(note.length());
        uAmount.setText(String.valueOf(amount));
        uAmount.setSelection(String.valueOf(amount).length());

        Button update = view.findViewById(R.id.btn_update_upd);
        Button delete = view.findViewById(R.id.btn_delete_upd);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mType = uType.getText().toString().trim();
                String mAamount = uAmount.getText().toString().trim();
                String mNote = uNote.getText().toString().trim();
                int intAmount = Integer.parseInt(mAamount);
                String date = DateFormat.getDateInstance().format(new Date());

                if (TextUtils.isEmpty(mType)){
                    uType.setError("Required field.");
                    return;
                }

                if (TextUtils.isEmpty(mNote)){
                    uNote.setError("Required field.");
                    return;
                }

                Data  data = new Data(mType,intAmount,mNote,date,post_key);
                mDatabase.child(post_key).setValue(data);
                Toast.makeText(HomeActivity.this, "Shopping data updated.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child(post_key).removeValue();
                Toast.makeText(HomeActivity.this, "Data deleted.", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.log_out){
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            Toast.makeText(this, "You have successfully Logged out.", Toast.LENGTH_LONG).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
