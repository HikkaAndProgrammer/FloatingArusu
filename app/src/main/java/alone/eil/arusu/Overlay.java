package alone.eil.arusu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.eil.arusu.R;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class Overlay extends Service {
	//for playing "Nyaa~"
	public final static SoundPool soundPool = new SoundPool.Builder().setAudioAttributes(
		new AudioAttributes.Builder()
				//game works the most efficient
			.setUsage(AudioAttributes.USAGE_GAME)
				//this is works well too
			.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
			.build()
	).build();

	@SuppressLint("StaticFieldLeak")
	public static Overlay now;

	protected static final int track;
	public volatile static boolean isManagingMusic;
	public volatile static boolean isResettingBluetooth;
	public volatile static boolean isClearingMemory;
	//is played "Nyaa~"
	public volatile static boolean isPlayingNyaa;
	//timer for tick every 15 seconds
	public static Timer timer;

	//get sound "Nyaa~"
	static {
		AssetFileDescriptor assetFileDescriptor =
				FloatingArusu.getStaticResources().openRawResourceFd(R.raw.nyaa);
		track = soundPool.load(assetFileDescriptor, 0);
	}

	public volatile int LAYOUT_FLAG;
	public volatile short halfOfWidth;
	public volatile boolean isShowingOverlay;

	public static final ActionPool actions = new ActionPool();

	public final CharacterView character;
	protected LinearLayout m_main;
	//System flag of window
	protected WindowManager.LayoutParams m_layoutParams;
	protected Stack <Pair<Short, Short>> m_updates = new Stack<>();
	protected WindowManager m_windowManager;

	//there are got flag of window, which depends on android version
	//and set start coordinates
	public Overlay() {
		this.character = new CharacterView(FloatingArusu.instance);
		now = this;
	}

	@SuppressLint({"RtlHardcoded", "ClickableViewAccessibility"})
	@Override
	public void onCreate() {
		super.onCreate();
		this.m_windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			this.LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		else
			this.LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
		this.m_layoutParams = new WindowManager.LayoutParams(
				128,
				160,
				this.LAYOUT_FLAG,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT
		);

		this.m_main = new LinearLayout(this);

		this.m_layoutParams.gravity = 51;
		this.m_layoutParams.x = 100;
		this.m_layoutParams.y = 100;

		//load settings
		SharedPreferences arusuSharedPreferences =
				FloatingArusu.instance.getSharedPreferences("saved", Context.MODE_PRIVATE);
		isPlayingNyaa = arusuSharedPreferences.getBoolean("isPlayingNyaa", true);
		isResettingBluetooth = arusuSharedPreferences.getBoolean("isResetsBluetooth", true);
		isClearingMemory = arusuSharedPreferences.getBoolean("isClearsMemory", true);
		isShowingOverlay = arusuSharedPreferences.getBoolean("isShowingOverlay", true);

		//on click character
		this.character.setOnTouchListener(new View.OnTouchListener() {
			public volatile boolean isPlayingSound = false;
			public volatile int rsx, rsy, sx, sy;

			@Override
			public boolean onTouch(View _view, MotionEvent _event) {
				//Log.d("Maria", "Touched Maria!");

				if (_event.getAction() == MotionEvent.ACTION_MOVE) {
					Log.d("Maria", "ActionMove performed!");

					int x = (int) _event.getRawX();
					int y = (int) _event.getRawY();

					//checking how much she moved
					if (Math.sqrt((this.rsx - x) * (this.rsx - x) + (this.rsy - y) * (this.rsy - y))
							< m_layoutParams.width >> 3)
						return true;

					m_layoutParams.x = x - this.sx;
					m_layoutParams.y = y - this.sy;

					m_windowManager.updateViewLayout(m_main, m_layoutParams);

					//sound not to play
					this.isPlayingSound = false;
				} else if (_event.getAction() == MotionEvent.ACTION_DOWN) {
					this.isPlayingSound = true;
					this.rsx = (int) _event.getRawX();
					this.rsy = (int) _event.getRawY();
					this.sx = (int) _event.getX();
					this.sy = (int) _event.getY();
				} else if (_event.getAction() == MotionEvent.ACTION_UP && this.isPlayingSound) {
					Log.d("Maria", "ActionUp performed!");

					//playing sound if necessary
					if (isPlayingNyaa)
						soundPool.play(
								track,
								1,
								1,
								1,
								0,
								1
						);

					//off bluetooth if necessary
					if (isResettingBluetooth)
						try {
							BluetoothManager bluetoothManager = (BluetoothManager)
									getBaseContext().getSystemService(BLUETOOTH_SERVICE);
							bluetoothManager.getAdapter().disable();
						} catch (Exception _e) {
							MainActivity.now.requestPermissions(new String[] {
									Manifest.permission.BLUETOOTH_ADMIN
							}, 0);
						}

					//clears memory if necessary
					if (isClearingMemory) Runtime.getRuntime().gc();

					//continue/pause music if necessary
					if (isManagingMusic) {

					}
				}
				
				return true;
			}
		});

		//add character to window
		this.m_main.addView(this.character, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
				));

		//show window
		this.m_windowManager.addView(this.m_main, this.m_layoutParams);

		//set saved sizes
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"saved",
				Context.MODE_PRIVATE
		);
		this.setSizes(
				(short) (128f * ((float) (sharedPreferences.getInt("size", 20) + 20) / 50f)),
				(short) (160f * ((float) (sharedPreferences.getInt("size", 20) + 20) / 50f))
		);

		timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				System.gc();
			}
		};
		timer.schedule(task, 15000, 15000);
	}

	public void setSizes(short _width, short _height) {
		this.m_layoutParams.width = _width;
		this.m_layoutParams.height = _height;

		this.halfOfWidth = (short) (this.m_layoutParams.width / 2);

		this.character.updateSize(
				(short) this.m_layoutParams.width,
				(short) this.m_layoutParams.height
		);

		if (isShowingOverlay)
			this.m_windowManager.updateViewLayout(this.m_main, this.m_layoutParams);
		else
			this.m_updates.push(new Pair<>(
					(short) this.m_layoutParams.width,
					(short) this.m_layoutParams.height)
			);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.startService(new Intent(this, Overlay.class));
	}

	//just a crutch
	//because of Java says "I need it!"
	@Nullable
	@Override
	public IBinder onBind(Intent intent) { return null; }

	//I have already forgot, why this variable called "_vannessa"
	//May be because of Cytus II
	public void setWindow(boolean _vannessa) {
		if (_vannessa) {
			this.m_windowManager.addView(this.m_main, this.m_layoutParams);
			this.checkUpdates();
		} else
			this.m_windowManager.removeView(this.m_main);
		this.isShowingOverlay = _vannessa;
	}

	public void checkUpdates() {
		if (this.m_updates.empty() || !this.isShowingOverlay)
			return;
		Pair <Short, Short> pair = this.m_updates.pop();
		this.m_updates.clear();
		this.setSizes(pair.first, pair.second);
	}
}
