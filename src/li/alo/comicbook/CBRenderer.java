package li.alo.comicbook;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.GLSurfaceView;

// This basically just redirects everything to the CBEngine singleton
public class CBRenderer implements GLSurfaceView.Renderer {

	private CBEngine engine = null;

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		engine = CBEngine.getInstance();
	}

	public void onDrawFrame(GL10 unused) {
		engine.draw();
	}

	public void onSurfaceChanged(GL10 unused, int width, int height) {
		engine.initialize(width, height);
	}
}