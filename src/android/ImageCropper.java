package com.example.imagecropper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageCropper extends CordovaPlugin {
    private static final int CROP_REQUEST_CODE = 200;
    private CallbackContext callbackContext;
    private File tempFile;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("cropImage")) {
            this.callbackContext = callbackContext;
            this.cropImage(args.getJSONObject(0));
            return true;
        }
        return false;
    }

    private void cropImage(final JSONObject options) throws JSONException {
        final String sourceType = options.getString("sourceType");
        final String sourcePath = options.getString("sourcePath");
        final float aspectRatioX = (float) options.getDouble("aspectRatioX");
        final float aspectRatioY = (float) options.getDouble("aspectRatioY");
        final int quality = options.getInt("quality");

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Uri sourceUri;
                    if (sourceType.equals("local")) {
                        sourceUri = Uri.parse(sourcePath);
                    } else {
                        // Download image from URL
                        sourceUri = downloadImageFromUrl(sourcePath);
                    }

                    if (sourceUri != null) {
                        startCropActivity(sourceUri, aspectRatioX, aspectRatioY, quality);
                    } else {
                        callbackContext.error("Failed to process the image");
                    }
                } catch (Exception e) {
                    callbackContext.error("Error: " + e.getMessage());
                }
            }
        });
    }

    private Uri downloadImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
            
            File outputDir = cordova.getActivity().getCacheDir();
            tempFile = File.createTempFile("temp_image", ".jpg", outputDir);
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            
            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startCropActivity(Uri sourceUri, float aspectRatioX, float aspectRatioY, int quality) {
        Activity activity = cordova.getActivity();
        File outputFile = new File(activity.getCacheDir(), "cropped_image.jpg");
        Uri destinationUri = Uri.fromFile(outputFile);

        UCrop.Options cropOptions = new UCrop.Options();
        cropOptions.setCompressionQuality(quality);
        cropOptions.setToolbarTitle("Crop Image");

        UCrop.of(sourceUri, destinationUri)
             .withAspectRatio(aspectRatioX, aspectRatioY)
             .withOptions(cropOptions)
             .start(activity, CROP_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CROP_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    try {
                        Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(cordova.getActivity().getContentResolver(), resultUri);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        JSONObject result = new JSONObject();
                        result.put("image", encodedImage);
                        callbackContext.success(result);
                    } catch (IOException | JSONException e) {
                        callbackContext.error("Error processing cropped image: " + e.getMessage());
                    } finally {
                        // Delete the temporary file
                        if (tempFile != null && tempFile.exists()) {
                            tempFile.delete();
                        }
                    }
                } else {
                    callbackContext.error("Failed to crop image");
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                callbackContext.error(cropError.getMessage());
            }
        }
    }
}