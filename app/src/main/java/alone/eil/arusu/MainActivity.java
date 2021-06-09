package alone.eil.arusu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.eil.arusu.R;

public class MainActivity extends Activity {
	public final static int REQUEST_CODE = 100;
	public static String characterName = "";
	public static MainActivity now;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		this.setContentView(R.layout.activity_main);
		this.playOverlay();

		//piece of shit code
		now = this;

		if (this.checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
				!= PackageManager.PERMISSION_GRANTED) {
			this.requestPermissions(
					new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
					0
			);
		}
		//invoke service
		this.startService(new Intent(this, Overlay.class));
		//это подготовка
		LinearLayout background = new LinearLayout(this);
		ScrollView scrollView = new ScrollView(this);
		scrollView.setLayoutParams(
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT)
		);
		LinearLayout foreground = new LinearLayout(this);
		foreground.setPadding(4, 8, 4, 0);
		foreground.setOrientation(LinearLayout.VERTICAL);
		foreground.setGravity(Gravity.CENTER_HORIZONTAL);

		//saved settings
		SharedPreferences sharedPreferences = getSharedPreferences("saved", Context.MODE_PRIVATE);

		//beautiful font, Nya~
		Typeface dalsp = Typeface.createFromAsset(getAssets(), "dalsp.ttf");

		characterName = sharedPreferences.getString("characterName", "");

		//on/off overlay
		CheckBox showOverlay = new CheckBox(this);
		showOverlay.setTextSize(24);
		showOverlay.setTypeface(dalsp);
		showOverlay.setText(getString(R.string.overlay_show));
		showOverlay.setTextColor(0xffe3e3e3);
		showOverlay.setGravity(Gravity.CENTER);
		showOverlay.setChecked(true);
		showOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.now.setWindow(isChecked);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isShowingOverlay", isChecked);
			editor.apply();
		});
		showOverlay.setChecked(sharedPreferences.getBoolean("isShowingOverlay", true));

		//text
		TextView textView = new TextView(this);
		textView.setTextSize(24);
		textView.setTypeface(dalsp);
		textView.setText(getString(R.string.size));
		textView.setTextColor(0xffe3e3e3);
		textView.setGravity(Gravity.CENTER);

		//handle sizes
		SeekBar size = new SeekBar(this);
		size.setMax(220);
		size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			//on seekbar changes
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				Overlay.now.setSizes(
						(short) (128f * ((float) (progress + 20) / 50f)),
						(short) (160f * ((float) (progress + 20) / 50f))
				);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("size", progress);

				//for saving settings
				editor.apply();
			}
		});

		//TODO: вкл/выкл молнии
		CheckBox sowLighting = new CheckBox(this);
		sowLighting.setText(getString(R.string.lightning));
		sowLighting.setTypeface(dalsp);
		sowLighting.setOnCheckedChangeListener((buttonView, isChecked)
				-> Overlay.now.character.isLightShowing = isChecked);
		sowLighting.setGravity(Gravity.CENTER);
		sowLighting.setTextSize(24);

		//TODO: настройка выключения блютуза
		CheckBox manageBluetooth = new CheckBox(this);
		manageBluetooth.setText(getString(R.string.bt_setting));
		manageBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.isResetsBluetooth = isChecked;
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isResetsBluetooth", isChecked);
			editor.apply();
		});
		manageBluetooth.setChecked(sharedPreferences.getBoolean("isResetsBluetooth", true));

		//TODO: настройка очистки памяти
		CheckBox manageMemory = new CheckBox(this);
		manageMemory.setText(getString(R.string.mc_setting));
		manageMemory.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.isClearsMemory = isChecked;
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isClearsMemory", isChecked);
			editor.apply();
		});
		manageMemory.setChecked(sharedPreferences.getBoolean("isClearsMemory", true));

		//TODO: настройка звука
		CheckBox manageVolume = new CheckBox(this);
		manageVolume.setText(getString(R.string.ns_setting));
		manageVolume.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.isPlayingNyaa = isChecked;
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("ns", isChecked);
			editor.apply();
		});
		manageVolume.setChecked(sharedPreferences.getBoolean("isPlayingNyaa", true));

		//TODO: настройка движения по синусу
		CheckBox moveBySine = new CheckBox(this);
		moveBySine.setText(getString(R.string.sm_setting));
		moveBySine.setChecked(sharedPreferences.getBoolean("isSineMotion", true));
		moveBySine.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.now.character.isSineMotion = isChecked;
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isSineMotion", isChecked);
			editor.apply();
		});

		//TODO: имена всех встроенных персонажей
		String[] names = new String[]{
				getString(R.string.Maria),
				getString(R.string.ITohka),
				getString(R.string.Tohka),
				getString(R.string.Yoshino),
				getString(R.string.Kotori),
				getString(R.string.Kurumi),
				getString(R.string.Origami),
				getString(R.string.IOrigami)
		};
		//TODO: их айдишники в ресурсах
		int[] ids = new int[]{
				R.drawable.maria,
				R.drawable.i_tohka,
				R.drawable.tohka,
				R.drawable.yoshino,
				R.drawable.kotori,
				R.drawable.kurumi,
				R.drawable.origami,
				R.drawable.i_origami
		};

		//for character selection
		Spinner character = new Spinner(this, Spinner.MODE_DIALOG);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				this,
				android.R.layout.simple_spinner_item,
				names
		);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		character.setAdapter(adapter);
		character.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Overlay.now.character.characterId = (byte) pos;
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("character", pos);
				editor.putString("characterName", "");
				editor.apply();
				Overlay.now.character.update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				int spos = sharedPreferences.getInt("character", 0);
				Overlay.now.character.characterId = (byte) ids[spos];
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("characterName", "");
				editor.apply();
				Overlay.now.character.update();
			}
		});
		int spos = sharedPreferences.getInt("character", 0);
		character.setSelection(spos);

		//TODO: измнение прозрачности
		TextView opacityText = new TextView(this);
		opacityText.setText(getString(R.string.opacity));
		opacityText.setTextSize(24);
		opacityText.setTypeface(dalsp);
		opacityText.setTextColor(0xffe3e3e3);
		opacityText.setGravity(Gravity.CENTER);
		SeekBar manageOpacity = new SeekBar(this);
		manageOpacity.setMax(70);
		manageOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			//on seek bar changed
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress() + 30 - 1;
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt("opacity", progress);
				editor.apply();
				Overlay.now.character.alpha = progress * 256 / 100;

				Log.d("Maria", Integer.toString(progress)
						+ " " + Integer.toString(Overlay.now.character.alpha)
						+ " " + Float.toString(Overlay.now.character.getAlpha()));
			}
		});
		int opacityProgress = sharedPreferences.getInt("opacity", 70);
		manageOpacity.setProgress(opacityProgress);

		//to put your own picture
		Button customPictureButton = new Button(this);
		customPictureButton.setOnClickListener(v -> {
			Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
			//works only with png
			chooseFile.setType("image/png");
			chooseFile = Intent.createChooser(chooseFile, "Choose picture");
			//waiting for selection
			startActivityForResult(chooseFile, 1);
		});
		customPictureButton.setText(getString(R.string.custom_character));

		//на всякий случай
		background.setBackgroundColor(0xff2f2f2f);
		scrollView.setBackgroundColor(0xff2f2f2f);
		foreground.setBackgroundColor(0xff2f2f2f);

		//добавляем все, что создали выше
		foreground.addView(showOverlay);
		foreground.addView(moveBySine);
		foreground.addView(textView);
		foreground.addView(size);
		foreground.addView(opacityText);
		foreground.addView(manageOpacity);
		foreground.addView(sowLighting);
		foreground.addView(character);
		foreground.addView(manageBluetooth);
		foreground.addView(manageMemory);
		foreground.addView(manageVolume);
		foreground.addView(customPictureButton);

		//встраиваем это
		scrollView.addView(foreground);
		background.addView(scrollView);

		//и отображаем
		setContentView(background);
	}

	private void playOverlay(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			this.StartPlayingOverlay();
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public void StartPlayingOverlay() {
		if (!Settings.canDrawOverlays(this)) {
			Intent request = new Intent(
					Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + this.getPackageName())
			);
			this.startActivityForResult(request, REQUEST_CODE);
		} else {
			Intent intent = new Intent(this, Overlay.class);
			this.startService(intent);
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		//check if received result code
		//is equal our requested code for draw permission
		if (_requestCode == REQUEST_CODE) {
			this.StartPlayingOverlay();
		}
	}
}
