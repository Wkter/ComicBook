package li.alo.comicbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class CBSimpleShader{
	// Locations
    private int program       = -1;
	private int vertexHandle  = -1;
	private int matrixHandle  = -1;
	private int textureHandle = -1;
	private int uvHandle      = -1;
	
	// Features to use
	private boolean useVertexArray = false;
	private boolean useUVArray     = false;
	
	// Engine instance
	private CBEngine engine;
	
	// Constructor
	CBSimpleShader(CBEngine engine){
		this.engine = engine;
		
		// Compile the shader sources
		int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, "vertex_simple.glsl");
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, "fragment_simple.glsl");
        
        // Make the program
        program = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // creates OpenGL ES program executables
        GLES20.glUseProgram(program);
        
        // Get the shader locations
        if(vertexHandle == -1)   vertexHandle = GLES20.glGetAttribLocation( program, "vertexPosition");
        if(matrixHandle == -1)   matrixHandle = GLES20.glGetUniformLocation(program, "modelMatrix");
        if(textureHandle == -1) textureHandle = GLES20.glGetUniformLocation(program, "texture");
        if(uvHandle == -1)           uvHandle = GLES20.glGetAttribLocation( program, "textureCoordinates");
        
        // Success
        Log.i("ComicBook", "SimpleShader reporting in!");
	}
	
	// Get the shader source and compile it
	public int compileShader(int type, String filename){
		
		// Create a new shader
    	int shader = GLES20.glCreateShader(type);
    	Utils.checkGlError("glCreateShader");
    	
		try {
	    	// Get the shader source code from the assets
			InputStream is = engine.context.getAssets().open(filename);
			String shaderCode = Utils.inputStreamToString(is);
			GLES20.glShaderSource(shader, shaderCode);
			Utils.checkGlError("glShaderSource");
			
			// Compile the shader
			GLES20.glCompileShader(shader);
			Utils.checkGlError("glCompileShader");

			// Check for errors
			int[] status = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
			Utils.checkGlError("glGetShaderiv");
			if(status[0] != GLES20.GL_TRUE){
				// Broken: http://code.google.com/p/android/issues/detail?id=9953
		    	String error = GLES20.glGetShaderInfoLog(shader);
				Log.e("ComicBook", "Could not compile shader");
				Log.e("ComicBook", error);
	            throw new RuntimeException(filename + ": Could not compile shader");
		    }
		} catch (IOException e) {
			// Something went wrong. Let's just clean up for now.
			e.printStackTrace();
			GLES20.glDeleteShader(shader);
			shader = 0;
			// TODO: Throw runtime exception
		}
	    return shader;
	}
	
	// Use the vertex buffer
	public void setVertexBuffer(FloatBuffer vertexBuffer){
		vertexBuffer.position(0);
		GLES20.glEnableVertexAttribArray(vertexHandle);
		GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,0, vertexBuffer);
		useVertexArray = true;
	}
	
	// Use the UV coordinate buffer
	public void setUVBuffer(FloatBuffer uvBuffer){
		uvBuffer.position(0);
		GLES20.glEnableVertexAttribArray(uvHandle);
		GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);
		useUVArray = true;
	}
	
	// Set the matrix
	public void setMatrix(float matrix[]){
		GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0);
	}
	
	// Bind texture to the fragment shader
	public void setTexture(int texture){
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
		GLES20.glUniform1i(textureHandle, 0);
	}
	
	// Add program to OpenGL ES environment
	public void start(){
		GLES20.glUseProgram(program);
	}
	
	// Clean up
	public void end(){
		if(useVertexArray) GLES20.glDisableVertexAttribArray(uvHandle);
		if(useUVArray) GLES20.glDisableVertexAttribArray(vertexHandle);
		useVertexArray = false;
		useUVArray = false;
	}
	
}
