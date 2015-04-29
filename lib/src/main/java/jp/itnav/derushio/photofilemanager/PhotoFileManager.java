package jp.itnav.derushio.photofilemanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ShareCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by nakashionarumi on 2014/06/26.
 * ファイルにアクセスするためのマネージャー
 */

public class PhotoFileManager {

	private Context mContext;

	public PhotoFileManager(Context context) {
		mContext = context;
	}

	public File getCacheDir() {
		return mContext.getExternalFilesDir("cache");
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

		if (outputImageDir.exists() == false) {
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
			MediaScannerConnection.scanFile(mContext, new String[]{outputFile.getPath()}, new String[]{"image/jpg"}, null);
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
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	// シェア
}
