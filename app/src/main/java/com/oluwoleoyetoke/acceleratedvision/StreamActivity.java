package com.oluwoleoyetoke.acceleratedvision;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;

import static android.content.Intent.ACTION_VIEW;

public class StreamActivity extends AppCompatActivity implements SurfaceHolder.Callback  {
    //Variables declaration
    static final String DECLARATION_STATE = "STATE_KEY";
    public int toggle=0; //to monitor ON/OFF
    private android.hardware.Camera mCamera;
    private SurfaceHolder holder;
    private int cameraOrientation;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        fab = (FloatingActionButton) findViewById(R.id.fab);


        //Setup camera
        SurfaceView cameraView = (SurfaceView) findViewById(R.id.streamSurfaceView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        cameraOrientation = getCorrectCameraOrientation(cameraInfo, mCamera);
        //Open camera and set orientation
        try {
        mCamera = Camera.open();
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.setDisplayOrientation(cameraOrientation);
                mCamera.getParameters().setRotation(cameraOrientation);
            }
        } catch (Exception exception) {
            Log.e("ERROR", "Camera error on surfaceCreated " + exception.getMessage());
        }



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle == 0) {
                    toggle = 1;
                    fab.setImageResource(android.R.drawable.button_onoff_indicator_on); //change fab colour
                    mCamera.startPreview(); //Ask camera to draw image from camera on surfaceview
                    Toast.makeText(getBaseContext(), getString(R.string.stream_started), Toast.LENGTH_SHORT).show(); //Notify user about starting stream
                } else if (toggle == 1) {
                    toggle = 0;
                    fab.setImageResource(android.R.drawable.button_onoff_indicator_off);
                    mCamera.stopPreview(); //Release camera when we leave the view
                    Toast.makeText(getBaseContext(), getString(R.string.stream_stoped), Toast.LENGTH_SHORT).show(); //Notify user about stream being off
                }
            }
        });

        if(savedInstanceState == null) { //If oncreate has not been run before
            Toast.makeText(getBaseContext(), getString(R.string.instruction_1), Toast.LENGTH_LONG).show();
        }else{
            //do nothing
        }

        // Add back <-- navigation to stream activity toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.streamToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //Save variables before activity kill/screen orientation change
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putInt(DECLARATION_STATE, toggle);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    //Restore saved variables after screen orientation change
    @Override
    public void  onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        toggle = savedInstanceState.getInt(DECLARATION_STATE);
        if(toggle==1){toggle=0;}
        else if(toggle==0){toggle=1;}
        fab.performClick();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
     //Changing the orientation when device orientation changes
       if(holder.getSurface() == null && toggle == 0)//check if the surface is ready to receive camera data
            return;
        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //if camera is not runing
        }

        //Recreate the camera preview
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //Release camera when we leave the view
        mCamera.stopPreview();
        mCamera.release();
    }

    // (c) Method gotten from Official Android Developer Camera Class
    public int getCorrectCameraOrientation(Camera.CameraInfo info, Camera camera) {

        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch(rotation){
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if(info.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }else{
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;//Return the orientation we should set camera to
    }


    //Add menu
    //Inflate Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //When any of the menu is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem menuitem) {
        if(menuitem.getItemId()==R.id.about_options_id){
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);//Go to the about activity page
        }else if(menuitem.getItemId()==R.id.collaborate_options_id){
            Uri uri = Uri.parse(getString(R.string.project_git_hub));
            Intent collaborationIntent = new Intent(ACTION_VIEW, uri); //Use web browser to visit project Git Page
            //Make sure package exists
            if(collaborationIntent.resolveActivity(getPackageManager())!=null){
                startActivity(collaborationIntent);
            }
        }
        return true;
    }

}
