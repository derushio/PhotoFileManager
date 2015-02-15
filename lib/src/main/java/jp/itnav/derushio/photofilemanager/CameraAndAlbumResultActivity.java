package jp.itnav.derushio.photofilemanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.IOException;

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
					startCrop();
					break;
				case REQUEST_CAMERA:
					startCrop();
					break;
				case REQUEST_CROP:
					Bitmap bitmap = BitmapFactory.decodeFile(photoFileManager.getCacheFile(CACHE_CROP).getPath());
					Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, cropSizeX, cropSizeY, false);
					try {
						photoFileManager.outputImage(resizeBitmap, photoFileManager.getCacheDir(), CACHE_CROP, false);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

					onCropFinished();
					break;
			}
		}
	}

	protected void startCamera() {
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFileManager.getCacheFile(CACHE_PHOTO)));
		startActivityForResult(intent, REQUEST_CAMERA);
	}

	protected void startAlbum() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_GALLERY);
	}

	protected void startCrop() {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(photoFileManager.getCacheFile(CACHE_PHOTO)), "image/*");
		intent.putExtra("outputX", cropSizeX);
		intent.putExtra("outputY", cropSizeY);
		intent.putExtra("aspectX", cropSizeX);
		intent.putExtra("aspectY", cropSizeY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFileManager.getCacheFile(CACHE_CROP)));

		startActivityForResult(intent, REQUEST_CROP);
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
