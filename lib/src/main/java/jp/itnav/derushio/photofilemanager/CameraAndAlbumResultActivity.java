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

	private int mCropSizeX = 1000;
	private int mCropSizeY = 1000;

	private static final int REQUEST_CAMERA = 0;
	private static final int REQUEST_GALLERY = 1;
	private static final int REQUEST_CROP = 2;

	private PhotoFileManager mPhotoFIleManager = new PhotoFileManager(this);

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case REQUEST_GALLERY:
					try {
						mPhotoFIleManager.outputImage(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()), mPhotoFIleManager.getCacheDir(), CACHE_PHOTO, false);
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
					Bitmap bitmap = BitmapFactory.decodeFile(mPhotoFIleManager.getCacheFile(CACHE_CROP).getPath());
					Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, mCropSizeX, mCropSizeY, false);
					try {
						mPhotoFIleManager.outputImage(resizeBitmap, mPhotoFIleManager.getCacheDir(), CACHE_CROP, false);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

					onCropFinished();
					break;
			}
		}
	}
	// カメラ画面等から戻ってきた時の動作

	protected void startCamera() {
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFIleManager.getCacheFile(CACHE_PHOTO)));
		startActivityForResult(intent, REQUEST_CAMERA);
	}
	// カメラを起動

	protected void startAlbum() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_GALLERY);
	}
	// アルバムを起動

	protected void startCrop() {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(mPhotoFIleManager.getCacheFile(CACHE_PHOTO)), "image/*");
		intent.putExtra("outputX", mCropSizeX);
		intent.putExtra("outputY", mCropSizeY);
		intent.putExtra("aspectX", mCropSizeX);
		intent.putExtra("aspectY", mCropSizeY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFIleManager.getCacheFile(CACHE_CROP)));

		startActivityForResult(intent, REQUEST_CROP);
	}
	// 切り抜きを開始

	abstract protected void onCropFinished();
	// 切り抜きが終わった時の処理を設定

	protected class pictureActionDialog extends Dialog {
		private Context context;

		public pictureActionDialog(Context context) {
			super(context);
			this.context = context;

			setTitle("写真を選ぶ");
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
	// 写真を撮る、選択する操作を開始

	public void setCropSizeX(int cropSizeX) {
		this.mCropSizeX = cropSizeX;
	}
	// 切り抜き幅を設定

	public void setCropSizeY(int cropSizeY) {
		this.mCropSizeY = cropSizeY;
	}
	// 切り抜き高さを設定
}
