package jp.itnav.derushio.photofilemanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Created by derushio on 15/02/12.
 */
abstract public class CameraAndAlbumResultActivity extends Activity {
	public static final String CACHE_PHOTO = "cache_image";
	public static final String CACHE_CROP = "cache_crop";

	private int cropSizeX = 1000;
	private int cropSizeY = 1000;

	private static final int REQUEST_CAMERA = 0;
	private static final int REQUEST_GALLERY = 1;
	private static final int REQUEST_CROP = 2;

	private PhotoFileManager photoFileManager = new PhotoFileManager(this);

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_GALLERY:
					try {
						photoFileManager.outputImage(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()), photoFileManager.getCacheDir(), CACHE_PHOTO, false);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				case REQUEST_CAMERA:
					startCrop();
					break;
				case REQUEST_CROP:
					onCropFinished();
					break;
			}
		}
	}

	protected void startCamera() {
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileManager.getCacheFile(CACHE_PHOTO));
		startActivityForResult(intent, REQUEST_CAMERA);
	}

	protected void startAlbum() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_GALLERY);
	}

	protected void startCrop() {

		PackageManager packageManager = this.getPackageManager();
		List<ApplicationInfo> applications = packageManager.getInstalledApplications(0);
		String[] apps = {"com.android.gallery", "com.cooliris.media", "com.google.android.gallery3d"};
		String[] appClasses = {"com.android.camera.CropImage", "com.cooliris.media.CropImage", "com.android.gallery3d.app.CropImage"};

		int classType = -1;
		for (ApplicationInfo ai : applications) {
			String s1 = ai.packageName;
			if (apps[0].equals(s1)) {
				classType = 0;
			}
			if (apps[1].equals(s1)) {
				classType = 1;
			}
			if (apps[2].equals(s1)) {
				classType = 2;
			}
		}

		Intent intent = new Intent();
		if (classType != -1) {

			intent.setClassName(apps[classType], appClasses[classType]);
			intent.setData(Uri.fromFile(photoFileManager.getCacheFile(CACHE_PHOTO)));

			intent.putExtra("outputX", cropSizeX);
			intent.putExtra("outputY", cropSizeY);
			intent.putExtra("aspectX", cropSizeX / (cropSizeX + cropSizeY));
			intent.putExtra("aspectY", cropSizeY / (cropSizeX + cropSizeY));
			intent.putExtra("scale", true);
			intent.putExtra("noFaceDetection", true);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFileManager.getCacheFile(CACHE_CROP)));
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.name());

			startActivityForResult(intent, REQUEST_CROP);
		}
	}

	abstract protected void onCropFinished();

	protected class pictureActionDialog extends Dialog {

		private Context context;

		public pictureActionDialog(Context context) {
			super(context);
			this.context = context;

			setTitle("画像取得方法を選んでください");
			LinearLayout layoutDialog = new LinearLayout(context);
			LayoutInflater.from(context).inflate(R.layout.dialog_photo_action, layoutDialog);

			LinearLayout buttonCamera = (LinearLayout) layoutDialog.findViewById(R.id.action_camera);
			LinearLayout buttonAlbum = (LinearLayout) layoutDialog.findViewById(R.id.action_album);

			buttonCamera.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startCamera();
				}
			});

			buttonAlbum.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startAlbum();
				}
			});
		}
	}

	public void setCropSizeX(int cropSizeX) {
		this.cropSizeX = cropSizeX;
	}

	public void setCropSizeY(int cropSizeY) {
		this.cropSizeY = cropSizeY;
	}
}
