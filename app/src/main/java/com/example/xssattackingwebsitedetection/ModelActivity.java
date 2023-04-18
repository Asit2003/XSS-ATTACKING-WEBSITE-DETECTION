package com.example.xssattackingwebsitedetection;

// Import statements
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.tensorflow.lite.Interpreter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModelActivity extends AppCompatActivity {

    private static final String TAG = "PredictionActivity";

    private EditText urlInput;
    private Button predictButton;
    private TextView resultText;
    TextView textView;

    private Interpreter tfliteInterpreter;

    // Firebase Realtime Database references
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        // Initialize the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("predictions");

        // Load the TFLite model from assets
        try {
            tfliteInterpreter = new Interpreter(loadModelFile(), null);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load TFLite model: " + e.getMessage());
        }

        urlInput = findViewById(R.id.url_input);
        predictButton = findViewById(R.id.predict_button);
        resultText = findViewById(R.id.result_text);
        textView = findViewById(R.id.main);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input
                String url = urlInput.getText().toString();

                // Extract features from the URL using a separate Python code
                List<Float> features = extractFeatures(url);

                // Make a prediction using the TFLite model
                double[][] input = new double[][]{features.stream().mapToDouble(f -> f).toArray()};
                float[][] output = new float[1][1];
                tfliteInterpreter.run(input, output);

                // Display the result
                float prediction = output[0][0];
                String result = prediction > 0.5 ? "XSS detected" : "No XSS detected";
                resultText.setText("Result: " + result);

                // Store the input, extracted features, and output in the Firebase Realtime Database
                storePrediction(url, features, prediction);
            }

            private void storePrediction(String url, List<Float> features, float prediction) {
            }
        });
    }

    private List<Float> extractFeatures(String url) {
        List<Float> features = null;

        // Execute a separate Python code to extract features from the URL
        try {
            Process process = new ProcessBuilder()
                    .command("python", "extract_features.py", url)
                    .start();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            features = Arrays.stream(reader.readLine().split(","))
                    .map(Float::parseFloat)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Log.e(TAG, "Failed to extract features: " + e.getMessage());
        }

        return features;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();

        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}