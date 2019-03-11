package com.example.gerard.socialapp.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gerard.socialapp.GlideApp;
import com.example.gerard.socialapp.R;
import com.example.gerard.socialapp.model.Post;
import com.example.gerard.socialapp.view.fragment.PostsFragment;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.time.format.DateTimeFormatter;

public class ShowActivity extends AppCompatActivity {
    //public ImageView foto;
    public TextView author;
    public TextView content;
    public ImageView image;
    public TextView date;
    Post post;
    private DatabaseReference buscar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        ImageView foto = (ImageView) findViewById(R.id.foto);
        TextView autor = (TextView) findViewById(R.id.author);
        TextView content = (TextView) findViewById(R.id.content);
        TextView fecha = (TextView) findViewById(R.id.fecha);
        ImageView media = (ImageView) findViewById(R.id.image);


        //String id = getIntent().getStringExtra("postid");
        GlideApp.with(ShowActivity.this).load((getIntent().getStringExtra("fotoautor"))).into(foto);
        autor.setText(getIntent().getStringExtra("nombreautor"));
        content.setText(getIntent().getStringExtra("contenido"));
        fecha.setText(getIntent().getStringExtra("data"));
        GlideApp.with(ShowActivity.this).load((getIntent().getStringExtra("uri"))).into(media);
        media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowActivity.this, MediaActivity.class);
                intent.putExtra("mediaUrl", getIntent().getStringExtra("uri"));
                intent.putExtra("mediaType", getIntent().getStringExtra("type"));
                startActivity(intent);
            }
        });









    }
}
