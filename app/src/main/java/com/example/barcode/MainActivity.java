package com.example.barcode;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView resultTextView;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean shouldEnableFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = findViewById(R.id.scanButton);
        resultTextView = findViewById(R.id.resultTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "Sensor de luz não disponível!", Toast.LENGTH_SHORT).show();
            shouldEnableFlash = true; // Se não houver sensor de luz, ative o flash por padrão
        }

        scanButton.setOnClickListener(v -> {
            // Iniciar a leitura do código de barras com a atividade personalizada
            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan a barcode");
            integrator.setCameraId(0);  // Use a câmera traseira
            integrator.setOrientationLocked(true); // Bloqueia a orientação para a atual (vertical)
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(false);
            integrator.setTorchEnabled(shouldEnableFlash); // Liga ou desliga o flash com base na luz ambiente
            integrator.setCaptureActivity(CustomCaptureActivity.class); // Usa a atividade personalizada
            integrator.initiateScan();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            // Define um limite de luz para ativar o flash
            shouldEnableFlash = lux < 50; // Por exemplo, ativa o flash se a luz for menor que 10 lux
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não necessário para o sensor de luz
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Exibir o código de barras no TextView
                resultTextView.setText(result.getContents());

                // Exibir um Toast de sucesso
                Toast.makeText(this, "Código lido com sucesso", Toast.LENGTH_SHORT).show();
            } else {
                // Caso o resultado esteja vazio
                Toast.makeText(this, "Falha ao ler o código de barras", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
