package com.example.planit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.example.planit.util.CloudTextRecognitionProcessor;
import com.example.planit.util.GraphicOverlay;
import com.example.planit.util.VisionImageProcessor;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.FileInputStream;

public class TextPreviewActivity extends AppCompatActivity {

    private ImageView preview;
    private GraphicOverlay graphicOverlay;
    private Bitmap bitmapForDetection;
    private VisionImageProcessor imageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_preview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preview = findViewById(R.id.previewPane);

        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bitmapForDetection = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        preview.setImageBitmap(bitmapForDetection);

        graphicOverlay = findViewById(R.id.previewOverlay);
        graphicOverlay.clear();
        imageProcessor = new CloudTextRecognitionProcessor();
        imageProcessor.process(bitmapForDetection, graphicOverlay);

    }

}
