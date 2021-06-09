package alone.eil.arusu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.eil.arusu.R;

public class CharacterView extends SurfaceView implements Runnable {
	//crutch
	protected final Thread m_renderThread;
	protected final Thread m_updateThread;
	public volatile short amplitude = 5, width = 128, height = 160;
	//holder for draw
	public volatile SurfaceHolder surfaceHolder;
	protected Context m_context;
	//character image
	protected Bitmap m_image;
	//standard drawing for remove effect
	public Paint defaultPaint = new Paint();
	public Paint paint = defaultPaint;
	public volatile byte characterId;
	//flag for lighting
	public volatile boolean isLightShowing = false;
	//ids of images
	protected int[] m_ids = new int[] {
			R.drawable.maria,
			R.drawable.i_tohka,
			R.drawable.tohka,
			R.drawable.yoshino,
			R.drawable.kotori,
			R.drawable.kurumi,
			R.drawable.origami,
			R.drawable.i_origami
	};
	public volatile int yCoordinate;
	public volatile boolean isSineMotion;
	public volatile boolean isLoaded;
	protected Canvas m_canvas;
	public int alpha = 255;

	@SuppressLint("UseCompatLoadingForDrawables")
	public CharacterView(Context _context) {
		super(_context);
		this.m_renderThread = new Thread(this);
		this.m_updateThread = new UpdateThread(this);

		this.surfaceHolder = this.getHolder();
		this.m_context = _context;

		//settings
		SharedPreferences sharedPreferences =
				this.m_context.getSharedPreferences("saved", Context.MODE_PRIVATE);
		this.characterId = (byte) sharedPreferences.getInt("characterId", 0);
		this.m_image = this.getImage();
		this.isSineMotion = sharedPreferences.getBoolean("isSineMotion", true);

		//to create background transparent
		this.surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		this.setBackgroundColor(Color.TRANSPARENT);

		this.isLoaded = true;

		this.m_updateThread.start();
		this.m_renderThread.start();
	}

	public static Bitmap getResizedBitmap(Bitmap _bitmap, int _width, int _height) {
		int width = _bitmap.getWidth();
		int height = _bitmap.getHeight();
		float scaleWidth = ((float) _width) / width;
		float scaleHeight = ((float) _height) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap result = Bitmap.createBitmap(
				_bitmap,
				0,
				0,
				width,
				height,
				matrix,
				false
		);
		_bitmap.recycle();
		return result;
	}

	//updates image
	public void update() {
		//for standard character
		if (MainActivity.characterName.equals(""))
			this.m_image = this.getImage();
		//and for custom
		else {
			SharedPreferences sharedPreferences = FloatingArusu.instance.getSharedPreferences(
					"saved",
					Context.MODE_PRIVATE
			);
			@SuppressLint("CommitPrefEdits")
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString("characterName", MainActivity.characterName);
			MainActivity.characterName = "";

			this.m_image = this.getImage();
		}
	}

	//updates sizes
	public void updateSize(short _width, short _height) {
		this.width = _width;
		this.height = _height;
		this.amplitude = (short) (this.height / 32);
		this.m_image = this.getImage();
	}

	private Bitmap getImage() {
		SharedPreferences sharedPreferences =
				this.m_context.getSharedPreferences("saved", Context.MODE_PRIVATE);

		if (sharedPreferences.getString("characterName", "").equals(""))
			return getResizedBitmap(
					BitmapFactory.decodeResource(this.m_context.getResources(),
							this.m_ids[sharedPreferences.getInt("character", 0)]),
					this.width, this.height - this.amplitude * 2);
		else
			return getResizedBitmap(BitmapFactory.decodeFile(sharedPreferences.getString(
							"characterName", "")),
					this.width, this.height - this.amplitude * 2);
	}

	//render thread with crutches
	@Override
	public void run() {
		//wait until loaded
		while(!this.surfaceHolder.getSurface().isValid())
			while(!this.isLoaded);

		while(true) {
			this.m_canvas = this.surfaceHolder.lockCanvas();
			if (this.m_canvas == null)
				continue;

			this.m_canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

			this.m_canvas.drawBitmap(
					this.m_image,
					0,
					this.yCoordinate,
					//there are all need effects
					this.paint
			);

			this.surfaceHolder.unlockCanvasAndPost(this.m_canvas);

			//Log.d("Maria", "posted!");
		}
	}
}
