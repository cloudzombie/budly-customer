package com.budly.android.CustomerApp.driver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.budly.BaseActivity;
import com.budly.R;
import com.budly.android.CustomerApp.beans.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.budly.android.CustomerApp.td.utils.PreferenceHelper;
import com.budly.android.CustomerApp.td.utils.UploadFile;

public class ProfilePictureActivity extends BaseActivity implements OnClickListener{
	
	Button btn_next, btn_add_photo;
	Uri selectedImageUri; // Global Variable
	String  selectedPath; // Global Variable
	ImageView img_profile;
	PreferenceHelper preferenceHelper;
	User mUser;
	private static final String IMAGE_DIRECTORY_NAME = "budly";
	
	// Khai bao options display image
	DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(false).cacheOnDisc(true).build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.driver_activity_profile_picture);
		preferenceHelper = new PreferenceHelper(this);
		mUser = preferenceHelper.getUserInfo();
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_add_photo = (Button) findViewById(R.id.btn_add_photo_profile);
		img_profile = (ImageView) findViewById(R.id.img_profile);
		btn_next.setOnClickListener(this);
		btn_add_photo.setOnClickListener(this);
		
	}
	
    public void pickPhoto() {
    	Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO);
	}
    
    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();	
    }
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if(requestCode == CAMERA_CAPTURE) {
				performCrop();
			} else if(requestCode == PICK_PHOTO) {
				try {
					File ff = new File(getPath(data.getData()));
					crop_path = Uri.fromFile(ff);
					performCrop();
				} catch (Exception e) { }
			} else if(requestCode == PIC_CROP) {
				crop_path = null;
				Bundle extras = data.getExtras();
				Bitmap thePic = extras.getParcelable("data");
				FileOutputStream out = null;
				try {
				    out = new FileOutputStream(getOutputMediaFile());
				    thePic.compress(Bitmap.CompressFormat.PNG, 100, out);
				    Log.e("Tuan", getOutputMediaFileUri().toString());
				    img_profile.setImageBitmap(thePic);
				    img_profile.invalidate();
				} catch (Exception e) {
				    e.printStackTrace();
				} finally {
				    try {
				        if (out != null) {
				            out.close();
				        }
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
				}
			}
		}
	}
	
	private void performCrop(){
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP"); 
			cropIntent.setDataAndType(getOutputMediaFileUri(), "image/*");
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			cropIntent.putExtra("outputX", 400);
			cropIntent.putExtra("outputY", 400);
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, PIC_CROP);
		}
		catch(ActivityNotFoundException anfe){
		    String errorMessage = "Whoops - your device doesn't support the crop action!";
		    Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
		    toast.show();
		}
	}
	
	ProgressDialog progressDialog;
	Handler mHandler = new Handler(); 
	
	void uploadImage() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				progressDialog = ProgressDialog.show(ProfilePictureActivity.this, "", "uploading...");
				progressDialog.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
						// TODO Auto-generated method stub
						return true;
					}
				});
			}
		});
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				try {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							try {
								progressDialog.dismiss();
							} catch (Exception e) { }
						}
					});
					
				} catch (Exception e) { }
			}
		}, 30000);
		if(getOutputMediaFileUri()!=null)
			selectedPath = getOutputMediaFileUri().toString().replace("file://", "");
		if(selectedPath!=null && !selectedPath.equals("")) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
					params.put("user_id", String.valueOf(mUser.id));
					params.put("type", "profile");
					String re = UploadFile.uploadFile("http://mybudly.com/DAPP/api/user/image", selectedPath, ProfilePictureActivity.this, params);
					Log.i("Tuan", re);
					try {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								try {
									progressDialog.dismiss();
								} catch (Exception e) { }
							}
						});
					} catch (Exception e) { }
					try {
						JSONObject data = new JSONObject(re);
						int status = data.getInt("status");
						if(status==200) {
							mUser.image = data.getString("data");
							preferenceHelper.setUserInfo(mUser);
							try {
								runOnUiThread(new Runnable() {
									public void run() {
										try {
											ImageLoader.getInstance().loadImage(mUser.image, options, null);
										} catch (Exception e) { }
									}
								});
							} catch (Exception e) { }
							startActivity(new Intent(ProfilePictureActivity.this, AddSupplierActivity.class));
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(ProfilePictureActivity.this, "Can not upload this image", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).start();
		} else {
			Toast.makeText(this, "Select image failed", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_next:
			uploadImage();
			break;
			
		case R.id.btn_add_photo_profile:
			chooseOption();
			break;

		default:
			break;
		}
	}
	int CAMERA_CAPTURE = 0x1;
	int PICK_PHOTO = 0x2;
	int PIC_CROP = 0x3;
	Uri crop_path = null;
	void chooseOption() {
		crop_path = null;
		final String[] time = { "Take a photo", "Select from gallery" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilePictureActivity.this);
        builder.setTitle("Choose one");
        builder.setItems(time, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int positon) {
				
				try {
					File of = getOutputMediaFile();
					if(of.exists()) {
						of.delete();
					}
				} catch (Exception e) { e.printStackTrace(); }

				if (positon == 0) {
					captureImage();
				} else {
					pickPhoto();
				}
			}
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.create().show();
	}
	
    private static File getOutputMediaFile() {
    	try {
    		File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    IMAGE_DIRECTORY_NAME);
         
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                            + IMAGE_DIRECTORY_NAME + " directory");
                    return null;
                }
            }
            File mediaFile;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator  + "IMG_driver_tmp.jpg");
            return mediaFile;	
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public void captureImage() {
        Intent t = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    t.putExtra(MediaStore.EXTRA_OUTPUT, getOutputMediaFileUri());
	    startActivityForResult(t, CAMERA_CAPTURE);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(StatusActivity.IS_FIRST) finish();
	}
	
    public Uri getOutputMediaFileUri() {
    	if(crop_path!=null) return crop_path;
    	try {
    		return Uri.fromFile(getOutputMediaFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
        
    }
     
}
