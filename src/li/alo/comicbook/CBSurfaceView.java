package li.alo.comicbook;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class CBSurfaceView extends GLSurfaceView {

    public CBSurfaceView(Context context){
        super(context);
        
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        
        // Better pixel depth
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888); 

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(new CBRenderer());
    }
}