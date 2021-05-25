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

		if (checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 0);
		}
		startService(new Intent(this, Overlay.class)); //запуск сервиса
		//это подготовка
		LinearLayout main__ = new LinearLayout(this);
		ScrollView main_ = new ScrollView(this);
		main_.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		LinearLayout main = new LinearLayout(this);
		main.setPadding(4, 8, 4, 0);
		main.setOrientation(LinearLayout.VERTICAL);
		main.setGravity(Gravity.CENTER_HORIZONTAL);

		//сохраненные настройки
		SharedPreferences sp = getSharedPreferences("saved", Context.MODE_PRIVATE);

		//красивый шрифт
		Typeface dalsp = Typeface.createFromAsset(getAssets(), "dalsp.ttf");

		characterName = sp.getString("characterName", "");

		//вкл/выкл оверлей
		CheckBox so = new CheckBox(this);
		so.setTextSize(24);
		so.setTypeface(dalsp);
		so.setText(getString(R.string.overlay_show));
		so.setTextColor(0xffe3e3e3);
		so.setGravity(Gravity.CENTER);
		so.setChecked(true);
		so.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.now.setWindow(isChecked);
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("isShowingOverlay", isChecked);
			editor.apply();
		});
		so.setChecked(sp.getBoolean("isShowingOverlay", true));

		//текст
		TextView tv = new TextView(this);
		tv.setTextSize(24);
		tv.setTypeface(dalsp);
		tv.setText(getString(R.string.size));
		tv.setTextColor(0xffe3e3e3);
		tv.setGravity(Gravity.CENTER);

		//управление размером
		SeekBar size = new SeekBar(this);
		size.setMax(220);
		size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			//надо
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

			//надо
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			//когда изменили seekbar
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				Overlay.now.setSizes((short) (128f * ((float) (progress + 20) / 50f)), (short) (160f * ((float) (progress + 20) / 50f)));
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("size", progress);
				editor.apply(); //для сохранения настроек
			}
		});

		//вкл/выкл молнии
		CheckBox light = new CheckBox(this);
		light.setText(getString(R.string.lightning));
		light.setTypeface(dalsp);
		light.setOnCheckedChangeListener((buttonView, isChecked)
				-> Overlay.now.character.isLightShowing = isChecked);
		light.setGravity(Gravity.CENTER);
		light.setTextSize(24);

		//настройка выключения блютуза
		CheckBox bto = new CheckBox(this);
		bto.setText(getString(R.string.bt_setting));
		bto.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.isResetsBluetooth = isChecked;
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("isResetsBluetooth", isChecked);
			editor.apply();
		});
		bto.setChecked(sp.getBoolean("isResetsBluetooth", true));

		//настройка очистки памяти
		CheckBox cm = new CheckBox(this);
		cm.setText(getString(R.string.mc_setting));
		cm.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.isClearsMemory = isChecked;
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("isClearsMemory", isChecked);
			editor.apply();
		});
		cm.setChecked(sp.getBoolean("isClearsMemory", true));

		//настройка звука
		CheckBox ns = new CheckBox(this);
		ns.setText(getString(R.string.ns_setting));
		ns.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.isPlayingNyaa = isChecked;
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("ns", isChecked);
			editor.apply();
		});
		ns.setChecked(sp.getBoolean("isPlayingNyaa", true));

		//настройка движения по синусу
		CheckBox sm = new CheckBox(this);
		sm.setText(getString(R.string.sm_setting));
		sm.setChecked(sp.getBoolean("isSineMotion", true));
		sm.setOnCheckedChangeListener((buttonView, isChecked) -> {
			Overlay.now.character.isSineMotion = isChecked;
			SharedPreferences.Editor editor = sp.edit();
			editor.putBoolean("isSineMotion", isChecked);
			editor.apply();
		});

		//имена всех встроенных персонажей
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
		//их айдишники в ресурсах
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

		//для выбора персонажа
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
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("character", pos);
				editor.putString("characterName", "");
				editor.apply();
				Overlay.now.character.update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				int spos = sp.getInt("character", 0);
				Overlay.now.character.characterId = (byte) ids[spos];
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("characterName", "");
				editor.apply();
				Overlay.now.character.update();
			}
		});
		int spos = sp.getInt("character", 0);
		character.setSelection(spos);

		TextView opacity_t = new TextView(this);
		opacity_t.setText(getString(R.string.opacity));
		opacity_t.setTextSize(24);
		opacity_t.setTypeface(dalsp);
		opacity_t.setTextColor(0xffe3e3e3);
		opacity_t.setGravity(Gravity.CENTER);
		SeekBar opacity = new SeekBar(this);
		opacity.setMax(70);
		opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			//надо
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

			//надо
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			//когда изменили seekbar
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress() + 30 - 1;
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("opacity", progress);
				editor.apply();
				Overlay.now.character.alpha = progress * 256 / 100;
			}
		});
		int opacity_ = sp.getInt("opacity", 70);
		opacity.setProgress(opacity_);

		//чтобы ставить свою картинку
		Button cc = new Button(this);
		cc.setOnClickListener(v -> {
			Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
			chooseFile.setType("image/png"); //нам нужны только png
			chooseFile = Intent.createChooser(chooseFile, "Choose picture");
			startActivityForResult(chooseFile, 1); //ждем выбора
		});
		cc.setText(getString(R.string.custom_char));

		//на всякий случай
		main__.setBackgroundColor(0xff2f2f2f);
		main_.setBackgroundColor(0xff2f2f2f);
		main.setBackgroundColor(0xff2f2f2f);

		//добавляем все, что создали выше
		main.addView(so);
		main.addView(sm);
		main.addView(tv);
		main.addView(size);
		main.addView(opacity_t);
		main.addView(opacity);
		main.addView(light);
		main.addView(character);
		main.addView(bto);
		main.addView(cm);
		main.addView(ns);
		main.addView(cc);

		//встраиваем это
		main_.addView(main);
		main__.addView(main_);

		//и отображаем
		setContentView(main__);
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
