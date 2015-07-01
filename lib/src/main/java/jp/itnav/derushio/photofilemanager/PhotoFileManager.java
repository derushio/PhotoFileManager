package jp.itnav.derushio.photofilemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ShareCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by nakashionarumi on 2014/06/26.
 * ファイルにアクセスするためのマネージャー
 */

public class PhotoFileManager {

	private Activity mActivity;
	// intentを制御するためにActivityを保持

	public PhotoFileManager(Activity activity) {
		mActivity = activity;
	}

	public File getCacheDir() {
		return mActivity.getExternalFilesDir("cache");
	}
	// キャッシュするためのPathを取得

	public File getCacheFile(String fileName) {
		return new File(getCacheDir().getPath(), fileName + ".jpg");
	}
	// キャッシュするためのファイルのUri

	public File getOutputImageDir(String dirName) {
		File outputImageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures");
		if (dirName != null || !dirName.equals("")) {
			outputImageDir = new File(outputImageDir.getPath(), dirName);
		}

		if (!outputImageDir.exists()) {
			outputImageDir.mkdirs();
		}

		return outputImageDir;
	}

	public File getOutputImageDir() {
		return getOutputImageDir(null);
	}
	// イメージ出力用のパスを取得

	public File outputImage(Bitmap bitmap, File dir, String name, boolean scan) throws FileNotFoundException {
		if (dir == null) {
			dir = getOutputImageDir();
		}

		if (name == null || name.equals("")) {
			name = System.currentTimeMillis() + ".jpg";
		} else {
			name += ".jpg";
		}

		File outputFile = new File(dir.getPath(), name);
		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

		if (scan) {
			MediaScannerConnection.scanFile(mActivity, new String[]{outputFile.getPath()}, new String[]{"image/jpg"}, null);
			// メディアスキャンをかける
		}

		return outputFile;
	}
	// イメージを出力し、ライブラリDBに登録

	public boolean shareImage(Bitmap bitmap, File dir, Activity activity, String message, String subject) {
		try {
			File outputFile = outputImage(bitmap, dir, null, true);
			ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(activity);
			builder.setType("image/*");
			builder.setStream(Uri.fromFile(outputFile));
			builder.setText(message);
			builder.setSubject(subject);
			builder.startChooser();
			// シェアのダイアログを表示
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	// シェア

	public static final String CACHE_PHOTO = "cache_image";
	public static final String CACHE_CROP = "cache_crop";

	private boolean isCrop = true;
	private int mCropSizeX = 1000;
	private int mCropSizeY = 1000;

	private static final int REQUEST_CAMERA = 0;
	private static final int REQUEST_GALLERY = 1;
	private static final int REQUEST_CROP = 2;

	private OnTakePicFinished mOnTakePicFinished = new OnTakePicFinished() {
		@Override
		public void onTakePicFinished(File file) {
		}
	};

	public void showPictureActionDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);

		dialog.setTitle("写真を選ぶ");
		dialog.setPositiveButton("アルバム", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startAlbum();
			}
		});
		dialog.setNegativeButton("カメラ", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startCamera();
			}
		});
		dialog.setNeutralButton("キャンセル", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
	// 写真を撮る、選択する操作を開始

	public void startCamera() {
		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCacheFile(CACHE_PHOTO)));
		mActivity.startActivityForResult(intent, REQUEST_CAMERA);
	}
	// カメラを起動

	public void startAlbum() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		mActivity.startActivityForResult(intent, REQUEST_GALLERY);
	}
	// アルバムを起動

	public void setCropExecute(boolean isCrop) {
		this.isCrop = isCrop;
	}

	public void setCropSizeX(int cropSizeX) {
		this.mCropSizeX = cropSizeX;
	}
	// 切り抜き幅を設定

	public void setCropSizeY(int cropSizeY) {
		this.mCropSizeY = cropSizeY;
	}
	// 切り抜き高さを設定

	public void startCrop(File file) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(file), "image/*");
		intent.putExtra("outputX", mCropSizeX);
		intent.putExtra("outputY", mCropSizeY);
		intent.putExtra("aspectX", mCropSizeX);
		intent.putExtra("aspectY", mCropSizeY);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCacheFile(CACHE_CROP)));

		mActivity.startActivityForResult(intent, REQUEST_CROP);
	}
	// 切り抜きを開始

	public void startCrop() {
		startCrop(getCacheFile(CACHE_PHOTO));
	}
	// 切り抜きを開始（キャッシュから始める場合のショートカット）

	public void onPhotoActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case REQUEST_GALLERY:
					try {
						outputImage(MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), data.getData()), getCacheDir(), CACHE_PHOTO, false);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (isCrop) {
						startCrop();
					} else {
						mOnTakePicFinished.onTakePicFinished(getCacheFile(CACHE_PHOTO));
					}
					break;
				case REQUEST_CAMERA:
					if (isCrop) {
						startCrop();
					} else {
						mOnTakePicFinished.onTakePicFinished(getCacheFile(CACHE_PHOTO));
					}
					break;
				case REQUEST_CROP:
					Bitmap bitmap = BitmapFactory.decodeFile(getCacheFile(CACHE_CROP).getPath());
					Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, mCropSizeX, mCropSizeY, false);
					try {
						outputImage(resizeBitmap, getCacheDir(), CACHE_CROP, false);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

					mOnTakePicFinished.onTakePicFinished(getCacheFile(CACHE_CROP));
					break;
			}
		}
	}

	public void setOnTakePicFinished(OnTakePicFinished onTakePicFinished) {
		mOnTakePicFinished = onTakePicFinished;
	}

	public interface OnTakePicFinished {
		public void onTakePicFinished(File file);
	}
}
