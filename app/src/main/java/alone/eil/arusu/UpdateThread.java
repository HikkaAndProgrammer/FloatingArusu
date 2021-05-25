package alone.eil.arusu;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.util.Random;

public class UpdateThread extends Thread {
	protected final Random m_random = new Random();
	protected final CharacterView m_parent;
	protected ColorMatrix m_colorMatrix = new ColorMatrix();
	protected Paint[] m_paints = new Paint[100];
	protected byte m_idx = 0;

	public UpdateThread(CharacterView _parent) {
		for (byte i = 0; i != this.m_paints.length; i++) {
			int a = this.getRandom(), b = this.getRandom(), c = this.getRandom();
			float[] cmdt = new float[]{
					1 + a / 160f, b / 120f, c / 180f, 0, 0,
					(a + b) / 160f, 1, (b + c) / 160f, 0, 0,
					(a + c) / 120f, (a - b) / 80f, 1, 0, 0,
					0, 0, 0, 1, 0
			};
			this.m_colorMatrix.set(cmdt);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(this.m_colorMatrix);
			this.m_paints[i] = new Paint();
			this.m_paints[i].setColorFilter(filter);
		}
		this.m_parent = _parent;
	}

	protected int getRandom() {
		return this.m_random.nextInt(41) - 10;
	}

	@Override
	public void run() {
		super.run();
		//end time of loop
		long endTime = System.currentTimeMillis();
		//(startTime)(n + 1) - (endTime)(n)
		long dt;
		//whole time
		long time = 0L;

		while(true) {
			if (this.m_parent.isSineMotion) {
				//calculate dt
				dt = System.currentTimeMillis() - endTime;
				//add dt to whole time
				time += dt;
				//here time - whole time, A - amplitude
				//3 is number of shifts to image
				//so that the image does not exactly go beyond the window
				this.m_parent.yCoordinate = (int) (Math.sin(time / 240d) *
						this.m_parent.amplitude * 2 + 3);

				if (this.m_parent.isLightShowing) {
					this.m_parent.paint = this.m_paints[this.m_idx++];
					this.m_idx %= 100;
				} else
					//to remove effect
					this.m_parent.paint = this.m_parent.defaultPaint;
			}
			this.m_parent.setAlpha(this.m_parent.alpha);
			endTime = System.currentTimeMillis();
		}
	}
};
