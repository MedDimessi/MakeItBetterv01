package com.next.gdg.makeitbetterv01;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class postSharingActivity extends Activity {

    private TextureView textureView;
    private ImageButton switchCamera;
    private ImageButton capturePicture;
    private ImageView savedImage;
    private ImageButton backToCamera;
    public RelativeLayout postCaptureView;
    public RelativeLayout cameraPreviewView;
    private ImageButton addComment;
    private EditText commentEditText;
    private ImageButton uploadPost;

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Size imageDimension;
    private File file;
    private CameraManager manager;
    private LocationManager locationManager;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private int switchValue = 0;
    private LatLng currentLoc;
    private DatabaseReference postsDatabase;
    private FirebaseUser user;
    private FirebaseAuth auth;

    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_sharing);
        postCaptureView = findViewById(R.id.postCaptureView);
        textureView = findViewById(R.id.cameraPreview);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        switchCamera = findViewById(R.id.switchCamera);
        capturePicture = findViewById(R.id.captureCamera);
        savedImage = findViewById(R.id.savedImage);
        backToCamera = findViewById(R.id.backToCamera);
        addComment = findViewById(R.id.addComment);
        commentEditText = findViewById(R.id.commentEditText);
        cameraPreviewView = findViewById(R.id.cameraPreviewView);
        uploadPost = findViewById(R.id.uploadPostButton);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        postsDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        backToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postCaptureView.setVisibility(View.GONE);
                cameraPreviewView.setVisibility(View.VISIBLE);
                addComment.setVisibility(View.VISIBLE);
                commentEditText.setVisibility(View.GONE);
                onWindowFocusChanged(true);
            }
        });

        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchValue == 0) {
                    cameraDevice.close();
                    switchValue = 1;
                    openCamera(switchValue);

                } else {
                    cameraDevice.close();
                    switchValue = 0;
                    openCamera(switchValue);
                }

            }
        });




    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

    }

    private class myAsyncTask extends AsyncTask<File, Void, File> {


        @Override
        protected File doInBackground(File... voids) {
            File f = capturePicture();
            return f;
        }

        @Override
        protected void onPostExecute(File f) {

            String rr = f.getAbsolutePath();
            File g = new File(rr);
            // Toast.makeText(postSharingActivity.this,"onBloodyClick: "+g.toString(),Toast.LENGTH_LONG).show();
            Uri uri = Uri.fromFile(g);
            //Toast.makeText(postSharingActivity.this,"URI :"+uri.toString(),Toast.LENGTH_LONG).show();
            if (isStoragePermissionGranted()) {
                savedImage.setImageURI(uri);
                cameraPreviewView.setVisibility(View.GONE);
                postCaptureView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(postSharingActivity.this, "error", Toast.LENGTH_LONG).show();
            }

        }
    }


    private Location getLocation() {
        Location location = null;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            if ((locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) ) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                }else  if( (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                           return null;

                    }
                    if(location == null){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                }


        }catch (Exception e){

        }
         return location;

    }


    @Override
    protected void onStart() {
        super.onStart();
        capturePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

          myAsyncTask task=new myAsyncTask();
          task.doInBackground();
          task.execute();
          Location location = getLocation();
          if(location == null){
              Toast.makeText(postSharingActivity.this," GPS or Network need to be provided!!",Toast.LENGTH_LONG).show();
          }else{
            currentLoc  =new LatLng(location.getLatitude(),location.getLongitude());

          }



            }

        });
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addComment.setVisibility(View.GONE);
                commentEditText.setVisibility(View.VISIBLE);
            }
        });
        uploadPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (file != null){
                  addPost(currentLoc,user.getEmail(),commentEditText.getText().toString(),file);
              }else{
                  Toast.makeText(postSharingActivity.this,"there's no file to be uploaded",Toast.LENGTH_LONG).show();
              }

            }
        });

    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
        }
        return super.dispatchTouchEvent( event );
    }



    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();
            // The new size we want to scale to
            final int REQUIRED_SIZE=75;
            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file

            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    private void addPost(LatLng coordinates, String user, String comment, File f){
        ArrayList<String> whoLiked = new ArrayList<String>();
        ArrayList<String> whoVolunteered = new ArrayList<>();

        String postId = postsDatabase.push().getKey();
        File compressedOne = saveBitmapToFile(f);
        String picture = convertPicToBASE64(compressedOne);
        Post post = new Post(comment, coordinates.latitude, coordinates.longitude, picture, postId, user, whoLiked, whoVolunteered);
        postsDatabase.child(postId).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(postSharingActivity.this,"SuccessfulUploading",Toast.LENGTH_LONG).show();
                startActivity(new Intent(postSharingActivity.this,MapsActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(postSharingActivity.this,"",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });





    }



    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("PerGrant","Permission is granted");
                return true;
            } else {

                Log.v("PerRevok","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("PerGrant","Permission is granted");
            return true;
        }
    }



    private void createCameraPreview(){
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture !=null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                        cameraCaptureSessions = cameraCaptureSession;
                        updatePreview();

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    private File capturePicture(){
        if(cameraDevice == null)
            return null;
         manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            int width = 1936;
            int height = 2592;

            if(jpegSizes != null && jpegSizes.length>0){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width*8,height*8,ImageFormat.JPEG,1);

            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

              captureBuilder.addTarget(reader.getSurface());

              captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)100);
              int rotation = getWindowManager().getDefaultDisplay().getRotation();
              captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATION.get(rotation));

                 SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
           file = new File("/storage/emulated/0/DCIM/Camera/"+"IMG_"+sdf.format(new Date())+".jpg");

              ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                  @Override
                  public void onImageAvailable(ImageReader imageReader) {
                      Image image=null;
                      try{
                          image = reader.acquireLatestImage();
                          ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                          byte[] bytes =new byte[buffer.capacity()];
                          buffer.get(bytes);
                          save(bytes);


                      }catch (FileNotFoundException e){
                          e.printStackTrace();
                      }
                      catch (IOException e){
                          e.printStackTrace();
                      }
                      finally {
                          if(image!=null)
                              image.close();
                      }
                  }
                private void save(byte[] bytes)throws IOException{
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);

                    }finally {
                        if(outputStream != null)
                            outputStream.close();
                    }

                }
              };
              reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
              final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                  @Override
                  public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                      super.onCaptureCompleted(session, request, result);

                    // Toast.makeText(postSharingActivity.this,"Saved: "+file,Toast.LENGTH_SHORT).show();
                      createCameraPreview();


                  }
              };
              cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                  @Override
                  public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                      try{
                          cameraCaptureSession.capture(captureBuilder.build(),captureCallback,mBackgroundHandler);
                      }catch (CameraAccessException e){
                          e.printStackTrace();
                      }
                  }

                  @Override
                  public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                  }
              },mBackgroundHandler);

        }  catch ( CameraAccessException  e){
            e.printStackTrace();
        }
        return file;
    }



    private void openCamera(int fi) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[fi];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[fi];


            manager.openCamera(cameraId,stateCallBack,null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
              finish();
            }
        }
    }
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                 openCamera(switchValue);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

 @Override
    protected void onResume(){
        super.onResume();
        startBackGroundThread();
        if(textureView.isAvailable())
            openCamera(switchValue);
        else
            textureView.setSurfaceTextureListener(textureListener);
 }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }
    public boolean isFrontCamera(int cameraIndex){
        try{
            if(manager != null){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(String.valueOf(cameraIndex));
                return characteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT;
            }
        }catch (CameraAccessException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return false;
    }



    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void startBackGroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }



    private Size chooseOptimalSize(Size[] outputSizes, int width, int height) {
        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }
        return currentOptimalSize;
    }

    public String convertPicToBASE64(File f){
        String encodedString = null;
         try{
            InputStream inputStream = new FileInputStream(f.getAbsoluteFile());
            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try{
                while((bytesRead = inputStream.read(buffer)) != -1){
                    output.write(buffer,0,bytesRead);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            bytes = output.toByteArray();

            encodedString = Base64.encodeToString(bytes,Base64.DEFAULT);


           }catch(FileNotFoundException e){
            e.printStackTrace();
           }
        return encodedString;
}





}
