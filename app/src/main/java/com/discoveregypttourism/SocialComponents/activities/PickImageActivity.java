/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.discoveregypttourism.SocialComponents.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.discoveregypttourism.R;
import com.discoveregypttourism.SocialComponents.Constants;
import com.discoveregypttourism.SocialComponents.utils.LogUtil;
import com.discoveregypttourism.SocialComponents.utils.ValidationUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

public abstract class PickImageActivity extends BaseActivity {
    private static final String TAG = PickImageActivity.class.getSimpleName();
    protected static final int MAX_FILE_SIZE_IN_BYTES = 10485760;   //10 Mb
    private static final String SAVED_STATE_IMAGE_URI = "RegistrationActivity.SAVED_STATE_IMAGE_URI";
    private final int VIDEO_REQUEST = 70 ;

    protected Uri imageUri;

    protected abstract ImageView getImageView();

    protected abstract ImageView getDefaultImage();

    protected abstract VideoView getVideoView();

    protected abstract void onImagePikedAction();

    @SuppressLint("NewApi")
    public void onSelectImageClick(View view) {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    protected void loadImageToImageView(String image_video) {
        if (imageUri == null) {
            return;
        }
        if (image_video.equals("image")) {
            final ProgressDialog progressDialog = new ProgressDialog(this , R.style.MyAlertDialogStyle);
            progressDialog.setTitle(getResources().getString(R.string.uploading));
            progressDialog.setCancelable(false);
            progressDialog.show();

            Glide.with(getApplicationContext())
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressDialog.dismiss();
                            LogUtil.logDebug(TAG, "Glide Success Loading image from uri : " + imageUri.getPath());
                            return false;
                        }
                    })
                    .into(getImageView());
            getDefaultImage().setVisibility(View.VISIBLE);
        }
        else {
            try{
                getVideoView().setVideoURI(imageUri);
                getVideoView().start();
            }catch(Exception e){
                e.printStackTrace();
            }

               getDefaultImage().setVisibility(View.GONE);
               getImageView().setImageDrawable(getResources().getDrawable(R.drawable.ic_image_black_24dp));
        }
    }

    protected void chooseVideo() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this , R.style.MyAlertDialogStyle);
        alertDialogBuilder.setTitle(getResources().getString(R.string.upload_video));
        alertDialogBuilder.setMessage(getResources().getString(R.string.choose_video));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.choose_from_camera),  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
               Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent , VIDEO_REQUEST);

            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.open_gallary), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent , VIDEO_REQUEST);
                dialog.dismiss();
            }

        });
        alertDialogBuilder.create().show();


    }

    protected boolean isImageFileValid(Uri imageUri) {
        int message = R.string.error_general;
        boolean result = false;

        if (imageUri != null) {
            if (ValidationUtil.isImage(imageUri, this)) {
                File imageFile = new File(imageUri.getPath());
                if (imageFile.length() > MAX_FILE_SIZE_IN_BYTES) {
                    message = R.string.error_bigger_file;
                } else {
                    result = true;
                }
            } else {
                message = R.string.error_incorrect_file_type;
            }
        }

        if (!result) {
            showSnackBar(message);
            progressDialog.dismiss();
        }

        return result;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            if (isImageFileValid(imageUri)) {
                this.imageUri = imageUri;
            }

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted
                onImagePikedAction();
            }
        }

        else if(requestCode == VIDEO_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            imageUri = data.getData();
                onImagePikedAction();

            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS granted");
                CropImage.startPickImageActivity(this);
            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS not granted");
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (imageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS granted");
//                if (isImageFileValid(imageUri)) {
                onImagePikedAction();
//                }
            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS not granted");
            }
        }
    }

    protected void handleCropImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (ValidationUtil.checkImageMinSize(result.getCropRect())) {
                    imageUri = result.getUri();
                    loadImageToImageView("image");
                } else {
                    showSnackBar(R.string.error_smaller_image);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                LogUtil.logError(TAG, "crop image error", result.getError());
                showSnackBar(R.string.error_fail_crop_image);
            }
        }
    }

    protected void startCropImageActivity() {
        if (imageUri == null) {
            return;
        }

        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setMinCropResultSize(Constants.Profile.MIN_AVATAR_SIZE, Constants.Profile.MIN_AVATAR_SIZE)
                .setRequestedSize(Constants.Profile.MAX_AVATAR_SIZE, Constants.Profile.MAX_AVATAR_SIZE)
                .start(this);
    }
}

