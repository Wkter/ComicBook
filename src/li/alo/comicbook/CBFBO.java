package li.alo.comicbook;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class CBFBO {
	
	// OpenGL IDs
	private int FBO;
	private int texture;
	
	// Used to prevent multiple unbindings
	boolean isBound = false;
    
	// Size of the buffer (Mostly to prevent viewport clipping)
	private int FBOSize;
	
	// Projection matrix of the buffer (To prevent upside-down rendering)
	private float FBOMatrix[] = new float[16];
	
	// Engine refrence
	private CBEngine engine;
	
	// Simple constructor
	public CBFBO(CBEngine engine){
		this.engine = engine;
	}
	
	// Constructor
	public CBFBO(CBEngine engine, int size){
		this.engine = engine;
		makeFBO(size);
	}
	
	// Get the texture of the FBO
	public int getTexture(){
		return texture;
	}
	
	// Get the size of the FBO (and texture)
	public int getSize(){
		return FBOSize;
	}
	
	public void bind(){
		// Prevent rebinding
		if(isBound)
			return;
		isBound = true;
		
		// Push states to the engine
		engine.pushFBO(FBO);
		engine.pushViewport(FBOSize, FBOSize);
		engine.pushProjectionMatrix(FBOMatrix);
	}
	
	public void release(){
		// Only release if we actually are bound
		if(!isBound)
			return;
		isBound = false;
		
		// Pop the states from the engine
		engine.popProjectionMatrix();
		engine.popViewport();
		engine.popFBO();
	}
	
	public void makeFBO(int size){
		// Save the size
		FBOSize = size;
		
		// Because the GLES20 wrapper is retarded an demands an array...
        int FBOArray[] = { 0 };
        int textureArray[] = { 0 };
        
		// Create a framebuffer and texture that can be rendered to
        GLES20.glGenTextures(1, textureArray, 0);
        texture = textureArray[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, FBOSize, FBOSize, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        
        // Generate the framebuffer
     	GLES20.glGenFramebuffers(1, FBOArray, 0);
     	FBO = FBOArray[0];
        bind();
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture, 0);

        // Check for errors
        // TODO: Throw runtime exceptions on errors
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(status != GLES20.GL_FRAMEBUFFER_COMPLETE){
        	Log.e("ComicBook", "Frabebuffer not complete!");
        }
        Log.i("ComicBook", "Framebuffer: "+FBO+" - Texture: "+texture);
        
        // Create the FBO projection matrix
        Matrix.orthoM(FBOMatrix, 0, 0, FBOSize, 0, FBOSize, -1f, 1f);

        // We're done with the FBO, release the FBO
        release();
	}
	
	public void destroy(){
		// Release if bound
		if(isBound)
			release();
		
		// Delete the elements
		GLES20.glDeleteTextures(1, new int[]{texture}, 0);
		GLES20.glDeleteBuffers(1, new int[]{FBO}, 0);
	}
}










