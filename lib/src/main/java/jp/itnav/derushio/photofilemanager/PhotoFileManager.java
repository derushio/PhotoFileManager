package jp.itnav.derushio.photofilemanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by nakashionarumi on 2014/06/26.
 * ファイルにアクセスするためのマネージャー
 */

public class PhotoFileManager {

	private Context context;

	public PhotoFileManager(Context context) {
		this.context = context;
	}

	public File getCacheDir() {
		return context.getExternalFilesDir("cache");
	}
//	キャッシュするためのPathを取得

	public File getCacheFile(String fileName) {
		return new File(getCacheDir().getPath(), fileName + ".jpg");
	}
//	キャッシュするためのファイルのUri

	public File getOutputImageDir() {
		File outputImageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + context.getApplicationInfo().name);
		if (outputImageDir.exists() == false) {
			outputImageDir.mkdirs();
		}

		return outputImageDir;
	}
//	イメージ出力用のパスを取得

	public String outputImage(Bitmap bitmap, File dir, String name, boolean scan) throws FileNotFoundException {
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
			MediaScannerConnection.scanFile(context, new String[]{outputFile.getPath()}, new String[]{"image/jpg"}, null);
		}

		return (outputFile.getPath());
	}
//	イメージを出力し、ライブラリDBに登録
}
