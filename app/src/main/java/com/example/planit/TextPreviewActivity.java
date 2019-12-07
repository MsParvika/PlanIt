package com.example.planit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.planit.util.AccessTokenLoader;
import com.example.planit.util.Callback;
import com.example.planit.util.CloudTextRecognitionProcessor;
import com.example.planit.util.GraphicOverlay;
import com.example.planit.util.VisionImageProcessor;
import com.google.android.material.button.MaterialButton;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequest;
import com.google.api.services.language.v1.CloudNaturalLanguageScopes;
import com.google.api.services.language.v1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Entity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public class TextPreviewActivity extends AppCompatActivity implements Callback {

    private static final String TAG = "TextPreviewActivity";

    private ImageView preview;
    private GraphicOverlay graphicOverlay;
    private Bitmap bitmapForDetection;
    private VisionImageProcessor imageProcessor;
    private LinearLayout decisionLinearLayout;
    private MaterialButton looksGoodButton;
    private MaterialButton tryAgainButton;
    private TextView rawDetectedText;

    private static final int LOADER_ACCESS_TOKEN = 1;

    private GoogleCredential mCredential;
    private Thread mThread;
    private Callback mCallback;
    private EntityInfo[] detectedEntities;

    private final BlockingQueue<CloudNaturalLanguageRequest<? extends GenericJson>> mRequests
            = new ArrayBlockingQueue<>(3);

    private CloudNaturalLanguage mApi = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_preview);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        preview = findViewById(R.id.previewPane);
        decisionLinearLayout = findViewById(R.id.decisionContainer);
        looksGoodButton = decisionLinearLayout.findViewById(R.id.buttonLooksGood);
        tryAgainButton = decisionLinearLayout.findViewById(R.id.buttonTryAgain);
        rawDetectedText = decisionLinearLayout.findViewById(R.id.textViewRawDetected);

        mCallback = this;

        looksGoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analyzeEntities(rawDetectedText.getText().toString());
            }
        });

        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        decisionLinearLayout.setVisibility(View.INVISIBLE);

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
        imageProcessor.process(bitmapForDetection, graphicOverlay, decisionLinearLayout);
        prepareApi();

    }

    public void analyzeEntities(String text) {
        try {
            // Create a new entities API call request and add it to the task queue
            mRequests.add(mApi
                    .documents()
                    .analyzeEntities(new AnalyzeEntitiesRequest()
                            .setDocument(new Document()
                                    .setContent(text)
                                    .setType("PLAIN_TEXT"))));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create analyze request.", e);
        }
    }

    private void prepareApi() {
        // Initiate token refresh
        getSupportLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(TextPreviewActivity.this);
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }

    private void setAccessToken(String token) {
        mCredential = new GoogleCredential()
                .setAccessToken(token)
                .createScoped(CloudNaturalLanguageScopes.all());
        startWorkerThread();
    }

    private void startWorkerThread() {
        if (mThread != null) {
            return;
        }
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mThread == null) {
                        break;
                    }
                    try {
                        // API calls are executed here in this worker thread
                        deliverResponse(mRequests.take().execute());
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Interrupted.", e);
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to execute a request.", e);
                    }
                }
            }
        });
        mThread.start();
    }

    private void deliverResponse(GenericJson response) {
        final Activity activity = this;
        if (response instanceof AnalyzeEntitiesResponse) {
            final List<Entity> entities = ((AnalyzeEntitiesResponse) response).getEntities();
            final int size = entities.size();
            detectedEntities = new EntityInfo[size];
            for (int i = 0; i < size; i++) {
                detectedEntities[i] = new EntityInfo(entities.get(i));
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onEntitiesReady(detectedEntities);
                    }
                }
            });
        }
    }

    @Override
    public void onEntitiesReady(EntityInfo[] entities) {

        String locationDetails = "";
        String dateDetails = "";
        String otherDetails = "";
        String eventTitle = "";

        for(EntityInfo entity : entities){
            if(entity.type.equals("LOCATION") || entity.type.equals("ADDRESS")){
                locationDetails += entity.name + " ";
            }else if(entity.type.equals("DATE")){
                dateDetails += entity.name + " ";
            }else if(entity.type.equals("EVENT")){
                eventTitle += entity.name + " ";
            }else{
                otherDetails += entity.name + " ";
            }
        }

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, eventTitle)
                .putExtra(CalendarContract.Events.DESCRIPTION, dateDetails)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, locationDetails);


        startActivity(intent);
    }
}
