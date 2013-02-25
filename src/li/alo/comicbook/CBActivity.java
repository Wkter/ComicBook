package li.alo.comicbook;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class CBActivity extends Activity {

    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.w("ComicBook", "Activity Started.");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new CBSurfaceView(this);
        setContentView(mGLView);
    }
    
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.w("ComicBook", "Menu button pressed");
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }
    
    public boolean onTouchEvent(MotionEvent e){
         int x = (int) e.getX();
         int y = (int) e.getY();
         
         Log.d("ComicBook", "X: "+x+", Y: "+y);
         
         return true;
    }
}