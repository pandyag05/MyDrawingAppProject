package android.drawingapp;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainDrawingApp extends Activity {

    private CanvasView canvasView;
    private SensorManager sensorManager;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private AtomicBoolean dialogDisplay = new AtomicBoolean();
    private Dialog currentDialog;

    private static final int colorMenuID = Menu.FIRST;
    private static final int widthMenuID = Menu.FIRST + 1;
    private static final int eraseMenuID = Menu.FIRST + 2;
    private static final int clearMenuID = Menu.FIRST + 3;
    private static final int saveMenuID = Menu.FIRST + 4;

    private static final int accelerationThreshold = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawing_app);

        canvasView = (CanvasView) findViewById(R.id.drawingView);
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        enableAcceleroListening();
    }

    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
            sensorManager = null;
        }
    }

    private void enableAcceleroListening() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sEvent) {

            if (!dialogDisplay.get()) {
                float x = sEvent.values[0];
                float y = sEvent.values[1];
                float z = sEvent.values[2];

                lastAcceleration = currentAcceleration;

                currentAcceleration = x * x + y * y + z * z;

                acceleration = currentAcceleration * (currentAcceleration - lastAcceleration);

                if (acceleration > (float) accelerationThreshold) {

                    AlertDialog.Builder cBuilder = new AlertDialog.Builder(MainDrawingApp.this);

                    cBuilder.setMessage(R.string.message_erase);
                    cBuilder.setCancelable(true);

                    cBuilder.setPositiveButton(R.string.button_erase, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialogDisplay.set(false);
                            canvasView.clear();
                        }
                    });


                    cBuilder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialogDisplay.set(false);
                            dialog.cancel();
                        }
                    });


                }
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, colorMenuID, Menu.NONE, R.string.menuitem_color);
        menu.add(Menu.NONE, widthMenuID, Menu.NONE, R.string.menuitem_line_width);
        menu.add(Menu.NONE, clearMenuID, Menu.NONE, R.string.menuitem_clear);
        menu.add(Menu.NONE, eraseMenuID, Menu.NONE, R.string.menuitem_erase);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case colorMenuID:
                showColorDialog();
                return true;
            case widthMenuID:
                showLineWidth();
                return true;
            case clearMenuID:
                canvasView.clear();
                return true;
            case eraseMenuID:
                canvasView.setDrawingColor(Color.WHITE);
                return true;


        }

        return false;
    }

    private void showColorDialog() {
        currentDialog = new Dialog(this);
        currentDialog.setContentView(R.layout.choose_color_dialog);
        currentDialog.setTitle(R.string.title_color_dialog);
        currentDialog.setCancelable(true);
        final SeekBar alphaBar = (SeekBar) currentDialog.findViewById(R.id.alphaSeekBar);
        final SeekBar redBar = (SeekBar) currentDialog.findViewById(R.id.redSeekBar);
        final SeekBar greenBar = (SeekBar) currentDialog.findViewById(R.id.greenSeekBar);
        final SeekBar blueBar = (SeekBar) currentDialog.findViewById(R.id.blueSeekBar);

        alphaBar.setOnSeekBarChangeListener(colorSeekBarChange);
        redBar.setOnSeekBarChangeListener(colorSeekBarChange);
        greenBar.setOnSeekBarChangeListener(colorSeekBarChange);
        blueBar.setOnSeekBarChangeListener(colorSeekBarChange);

        final int mColor = canvasView.getDrawingColor();

        alphaBar.setProgress(Color.alpha(mColor));
        redBar.setProgress(Color.red(mColor));
        greenBar.setProgress(Color.green(mColor));
        blueBar.setProgress(Color.blue(mColor));

        Button mColorButton = (Button) currentDialog.findViewById(R.id.setColorButton);

        mColorButton.setOnClickListener(setColorButton);

        dialogDisplay.set(true);
        currentDialog.show();

    }

    private void showLineWidth() {
        currentDialog = new Dialog(this);
        currentDialog.setContentView(R.layout.width_line_dialog);
        currentDialog.setTitle(R.string.title_line_width_dialog);
        currentDialog.setCancelable(true);

        SeekBar widthSeekBar = (SeekBar) currentDialog.findViewById(R.id.widthSeekBar);
        widthSeekBar.setOnSeekBarChangeListener(widthSeekBarChange);

        int mWidthLine = canvasView.getLineWidth();
        widthSeekBar.setProgress(mWidthLine);

        Button setWidthButton = (Button) currentDialog.findViewById(R.id.widthLineButton);
        setWidthButton.setOnClickListener(setWidthLineButton);
        dialogDisplay.set(true);
        currentDialog.show();
    }

    private OnSeekBarChangeListener colorSeekBarChange = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            SeekBar alphaBar = (SeekBar) currentDialog.findViewById(R.id.alphaSeekBar);
            SeekBar redBar = (SeekBar) currentDialog.findViewById(R.id.redSeekBar);
            SeekBar greenBar = (SeekBar) currentDialog.findViewById(R.id.greenSeekBar);
            SeekBar blueBar = (SeekBar) currentDialog.findViewById(R.id.blueSeekBar);
            View colorView = (View) currentDialog.findViewById(R.id.colorView);

            colorView.setBackgroundColor(Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));
           /* dialogDisplay.set(false);
            currentDialog.dismiss();
            currentDialog = null;*/

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
    private OnSeekBarChangeListener widthSeekBarChange = new OnSeekBarChangeListener() {

        Bitmap imageBitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(imageBitmap);


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView widthImageView = (ImageView) currentDialog.findViewById(R.id.widthImageView);

            Paint drawPaint = new Paint();
            int color = canvasView.getDrawingColor();
            drawPaint.setColor(color);
            drawPaint.setStrokeCap(Paint.Cap.ROUND);
            drawPaint.setStrokeWidth(progress);

            imageBitmap.eraseColor(Color.WHITE);
            mCanvas.drawLine(30, 50, 370, 50, drawPaint);
            widthImageView.setImageBitmap(imageBitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private OnClickListener setColorButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SeekBar alphaBar = (SeekBar) currentDialog.findViewById(R.id.alphaSeekBar);
            SeekBar redBar = (SeekBar) currentDialog.findViewById(R.id.redSeekBar);
            SeekBar greenBar = (SeekBar) currentDialog.findViewById(R.id.greenSeekBar);
            SeekBar blueBar = (SeekBar) currentDialog.findViewById(R.id.blueSeekBar);

            canvasView.setDrawingColor(Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(),
                    blueBar.getProgress()));
            dialogDisplay.set(false);
            currentDialog.dismiss();
            currentDialog = null;
        }
    };

    private OnClickListener setWidthLineButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SeekBar widthSeekBar = (SeekBar) currentDialog.findViewById(R.id.widthSeekBar);
            int widthGetProgress = widthSeekBar.getProgress();

            canvasView.setLineWidth(widthGetProgress);
            dialogDisplay.set(false);
            currentDialog.dismiss();
            currentDialog = null;
        }
    };
}
