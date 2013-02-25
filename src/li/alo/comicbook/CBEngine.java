package li.alo.comicbook;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Application;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class CBEngine {
	private static CBEngine instance = null;
	public Context context = null;
	public Application application = null;
	
	// screen size
	public int screenWidth = 0;
	public int screenHeight = 0;
	
	// Global matrices
	public float[] normalProjectionMatrix = new float[16];
	public float[] upsidedownProjectionMatrix = new float[16];
	
	// Stacks
	private ArrayList<float[]> matrixStack = new ArrayList<float[]>();
	private ArrayList<int[]> viewportStack = new ArrayList<int[]>();
	private ArrayList<Integer> fboStack = new ArrayList<Integer>();
	
	// Packages
	private ArrayList<CBComicBook> packages = new ArrayList<CBComicBook>();
	
	// Test elements
	public CBSimpleShader shader;
	
	// FPS log
	private boolean doLogging = false;
	private int frames = 0;
	private float totalTime = 0;
	private float lastTime = 0;
	
	// Exists only to defeat instantiation
	protected CBEngine(){
	}
	
	// Get the engine instance
	public static CBEngine getInstance(){
		if(instance == null)
			instance = new CBEngine();
		return instance;
	}
	
	// Initialize the engine and subsystems
	public void initialize(int width, int height){
		Log.w("ComicBook", "Initializing.");
		shader = new CBSimpleShader(this);
		
		// Save screen dimensions 
		screenWidth = width;
		screenHeight = height;
		
		// Create the projection matrices
		Matrix.orthoM(normalProjectionMatrix, 0, 0, width, height, 0, -1f, 1f);
		Matrix.orthoM(upsidedownProjectionMatrix, 0, 0, width, 0, height, -1f, 1f);
		pushProjectionMatrix(normalProjectionMatrix);
		
		// Push the initial GL values to the stack
		pushViewport(width, height);
		pushFBO(0);
		
		// Get the directories of out application folder
		File files[] = application.getExternalFilesDir(null).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		
		// Look for packages
		String directoryName;
		for(int i = 0; i < files.length; i++){
			// Get the directory name
			directoryName = files[i].getName();
			
			// Check for package descriptor
			String packageDescriptorURI = files[i].getAbsoluteFile()+"/"+directoryName+".xml";
			File packageDescriptor = new File(packageDescriptorURI);
			if(packageDescriptor.exists()){
				// Parse the XML package descriptor
				packages.add(new CBComicBook(this, packageDescriptor));
			}
		}
		
		Log.w("ComicBook", "Loading package 0.");
		packages.get(0).pages.get(0).load();
		
		Log.w("ComicBook", "Initialization Done.");
	}
	
	// Push a matrix to the matrix stack
	public void pushProjectionMatrix(float[] projectionMatrix){
		matrixStack.add(projectionMatrix);
	}
	
	// Pop a matrix from the matrix stack
	public void popProjectionMatrix(){
		if(matrixStack.size() != 0)
			matrixStack.remove(matrixStack.size()-1);
	}
	
	// Get the current matrix from the matrix stack
	public float[] getProjectionMatrix(){
		if(matrixStack.size() != 0)
			return matrixStack.get(matrixStack.size()-1);
		else
			return null;
	}
	
	// Push a viewport to the viewport stack
	public void pushViewport(int width, int height){
		viewportStack.add(new int[]{width, height});
		GLES20.glViewport(0, 0, width, height);
		
	}
	
	// Pop a viewport from the viewport stack
	public void popViewport(){
		if(viewportStack.size() != 0)
			viewportStack.remove(viewportStack.size()-1);
		GLES20.glViewport(0, 0, getViewportWidth(), getViewportHeight());
	}
	
	// Get the width of the current viewport
	public int getViewportWidth(){
		if(viewportStack.size() != 0)
			return viewportStack.get(viewportStack.size()-1)[0];
		else
			return 0;
	}

	// Get the height of the current viewport
	public int getViewportHeight(){
		if(viewportStack.size() != 0)
			return viewportStack.get(viewportStack.size()-1)[1];
		else
			return 0;
	}

	// Push an FBO to the FBO stack
	public void pushFBO(int fbo){
		fboStack.add(fbo);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
	}
	
	// Pop an FBO from the FBO stack
	public void popFBO(){
		if(fboStack.size() != 0)
			fboStack.remove(fboStack.size()-1);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getFBO());
	}
	
	// Returns the current FBO
	public int getFBO(){
		if(fboStack.size() != 0)
			return fboStack.get(fboStack.size()-1);
		else
			return 0;
	}

	// Render a frame
	public void draw(){
		// TODO: Rewrite and provide delta time to the children
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		//testpage.draw();
		packages.get(0).pages.get(0).draw();
		
		if(doLogging){
			totalTime += (System.nanoTime()/1000000)-lastTime;
	    	lastTime = System.nanoTime()/1000000;
	    	frames++;
	    	if(frames == (60*10)){
	    		MemoryInfo mi = new MemoryInfo();
	    		ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
	    		activityManager.getMemoryInfo(mi);
	    		long availableMegs = mi.availMem / 1048576L;
	    		Log.i("ComicBook", "FPS: "+((int)((1000.0f/(totalTime/frames))*100.0f))/100.0f+" - Time: "+(totalTime/frames));
	    		Log.i("ComicBook", "Memory available: "+availableMegs+"MB - Memory used: "+(getUsedMemorySize()/1048576)+"MB");
	    		frames = 0;
	    		totalTime = 0;
	    	}
		}
	}
	
	// Returns the total bytes of memory used by the JVM
	public static long getUsedMemorySize() {
	    long freeSize = 0;
	    long totalSize = 0;
	    long usedSize = -1;
	    try {
	        Runtime info = Runtime.getRuntime();
	        freeSize = info.freeMemory();
	        totalSize = info.totalMemory();
	        usedSize = totalSize - freeSize;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return usedSize;
	}
	
}
