package jp.itnav.derushio.photofilemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by derushio on 15/02/12.
 */
abstract public class CameraAndAlbumResultActivity extends Activity implements PhotoFileManager.OnTakePicFinished {

	protected PhotoFileManager mPhotoFileManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPhotoFileManager = new PhotoFileManager(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mPhotoFileManager.onPhotoActivityResult(requestCode, resultCode, data);
	}
	// カメラ画面等から戻ってきた時の動作

	protected void startCamera() {
		mPhotoFileManager.startCamera();
	}
	// カメラを起動

	protected void startAlbum() {
		mPhotoFileManager.startAlbum();
	}
	// アルバムを起動

	protected void startCrop() {
		mPhotoFileManager.startCrop();
	}
	// 切り抜きを開始


	public void setCropSizeX(int cropSizeX) {
		mPhotoFileManager.setCropSizeX(cropSizeX);
	}
	// 切り抜き幅を設定

	public void setCropSizeY(int cropSizeY) {
		mPhotoFileManager.setCropSizeY(cropSizeY);
	}
	// 切り抜き高さを設定
}
