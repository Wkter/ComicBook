package li.alo.comicbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

class Square{
	
	// Engine
	private CBEngine engine = null;

	// OpenGL related vars
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private static ShortBuffer drawListBuffer = null;
    
    // Simple shader 
    private static int simpleProgram = -1;
	private static int simplePositionHandle = -1;
	private static int simpleModelMatrixHandle = -1;
	private static int simpleTextureHandle = -1;
	private static int simpleTextureCoordinates = -1;

    // Gaussion shader 
	private static boolean gaussianReady = false;
    private static int gaussianProgram[] = new int[2];
	private static int gaussianPositionHandle[] = new int[2];
	private static int gaussianModelMatrixHandle[] = new int[2];
	private static int gaussianTextureHandle[] = new int[2];
	private static int gaussianTextureCoordinates[] = new int[2];
	private static int gaussianBlurSize[] = new int[2];
	
	private static int texture = -1;
	
	// Size of the texture
	private int textureWidth = 0;
	private int textureHeight = 0;
	private int textureSize = 0;   // power of two size of the texture
	
	// Crop area
	private int cropWidth = 0;
	private int cropHeight = 0;
	private int cropX = 0;
	private int cropY = 0;
	
	// Scale
	private float scaleX = 100.0f;
	private float scaleY = 100.0f;
	
	// Position
	public int x = 0;
	public int y = 0;
	
	// Bluring
	private float blurAmount = 0.0f;
	private boolean addBlur = false;
	private int sceneFBO[] = new int[1];
	private int sceneTexture[] = new int[1];
	ByteBuffer pixelBuffer;
	
	// FPS log
	private int frames = 0;
	private float totalTime = 0;
	private float lastTime = 0;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
	
	// ---------------------------------------------------------------------------------
	
    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
        	String errorStr = GLU.gluErrorString(error);//GLES20.glGetString(error);
        	Log.e("Square", op + ": glError " + error + ": " + errorStr);
            throw new RuntimeException(op + ": glError " + error + ": " + errorStr);
        }
    }
    
	public static String inputStreamToString(InputStream in) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        sb.append(line).append("\n");
	    }
	    in.close();
	    return sb.toString();
	}
	
	public int compileShader(int type, String shaderName){
	    try {
	    	// Get the shader source and compile it
	    	int shader = GLES20.glCreateShader(type);
	    	checkGlError("glCreateShader");
	    	
			InputStream is = engine.context.getAssets().open(shaderName);
			String shaderCode = inputStreamToString(is);
			GLES20.glShaderSource(shader, shaderCode);
			checkGlError("glShaderSource");
			GLES20.glCompileShader(shader);
			checkGlError("glCompileShader");

			int[] status = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
			checkGlError("glGetShaderiv");
			if(status[0] != GLES20.GL_TRUE){
				// Broken: http://code.google.com/p/android/issues/detail?id=9953
		    	//String error = GLES20.glGetShaderInfoLog(shader);
		    	//String lines[] = error.split("\\r?\\n");
		    	Log.e("Shader", "Could not compile shader");
	            throw new RuntimeException(shaderName + ": Could not compile shader");
		    }
		    return shader;
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return 0;
	}
	
	private FloatBuffer makeFloatBuffer(float array[]){
		// initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        FloatBuffer buffer;
        bb.order(ByteOrder.nativeOrder());
        buffer = bb.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
	}
	
	public void setCrop(int x, int y, int width, int height){
		float squareCoords[] = { 
	    	       0f,     0f, 0.0f,   // top left
	               0f, height, 0.0f,   // bottom left
	            width, height, 0.0f,   // bottom right
	            width,     0f, 0.0f }; // top right
		
		// Texture coordinate buffer
		float _x1 = (1.0f/(float)textureSize)*(float)cropX;
		float _x2 = (1.0f/(float)textureSize)*(float)width;
		float _y1 = (1.0f/(float)textureSize)*(float)cropY;
		float _y2 = (1.0f/(float)textureSize)*(float)height;
		
		float uvCoords[] = {
				_x1,     _y1,     // top left
				_x1,     _y1+_y2, // bottom left
				_x1+_x2, _y1+_y2, // bottom right
				_x1+_x2, _y1 };   // top right
		
		textureBuffer = makeFloatBuffer(uvCoords);	
        vertexBuffer = makeFloatBuffer(squareCoords);
        
        cropWidth = width;
    	cropHeight = height;
    	cropX = x;
    	cropY = y;
    	
    	Log.w("Square", "BlurSize = " + 1.0f/(float)cropWidth);
	}
	
	public void setScale(float x, float y){
		if(x < 0)
			x = 0;
		if( y < 0)
			y = 0;
		scaleX = x;
		scaleY = y;
	}
	
	public int getScaledWidth(){
		return (int) (cropWidth * (scaleX/100.0f));
	}
	
	public int getScaledHeight(){
		return (int) (cropHeight * (scaleY/100.0f));
	}

    public Square(CBEngine engine) {
    	
    	this.engine = engine;
    	
    	InputStream is = null;
		try {
			// Load the texture
			is = engine.context.getAssets().open("twokinds_1_1_1.png");
			texture = loadTexture(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	setCrop(0, 0, textureWidth, textureHeight);

        // Make the draw list buffer, if we haven't already
        if(drawListBuffer == null){
	        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
	        dlb.order(ByteOrder.nativeOrder());
	        drawListBuffer = dlb.asShortBuffer();
	        drawListBuffer.put(drawOrder);
	        drawListBuffer.position(0);
	    }
        
        // Create the shader program if we haven't already
        if(simpleProgram == -1){
	        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, "vertex_simple.glsl");
	        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, "fragment_simple.glsl");
	
	        simpleProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	        GLES20.glAttachShader(simpleProgram, vertexShader);   // add the vertex shader to program
	        GLES20.glAttachShader(simpleProgram, fragmentShader); // add the fragment shader to program
	        GLES20.glLinkProgram(simpleProgram);                  // creates OpenGL ES program executables
	        GLES20.glUseProgram(simpleProgram);
	        
	        // Get the shader locations
	        if(simplePositionHandle == -1)         simplePositionHandle = GLES20.glGetAttribLocation( simpleProgram, "vertexPosition");
	        if(simpleModelMatrixHandle == -1)   simpleModelMatrixHandle = GLES20.glGetUniformLocation(simpleProgram, "modelMatrix");
	        if(simpleTextureHandle == -1)           simpleTextureHandle = GLES20.glGetUniformLocation(simpleProgram, "texture");
	        if(simpleTextureCoordinates == -1) simpleTextureCoordinates = GLES20.glGetAttribLocation( simpleProgram, "textureCoordinates");
        }
        
        // Create gaussion shader program, if we haven't already
        //String programs_vertex[]   = {"gaussian_horizontal_vertex.glsl", "gaussian_vertical_vertex.glsl"};
        //String programs_fragment[] = {"gaussian_fragment.glsl"         , "gaussian_fragment.glsl"};
        String programs_vertex[]   = {"vertex_simple.glsl"     , "vertex_simple.glsl"};
        String programs_fragment[] = {"fragment_gaussian.glsl" , "fragment_gaussian2.glsl"};
        checkGlError("??? 1");
        if(!gaussianReady){
        	checkGlError("??? 2");
        	for(int i = 0; i < 2; i++){
	        	int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, programs_vertex[i]);
	        	int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, programs_fragment[i]);
		
		        gaussianProgram[i] = GLES20.glCreateProgram();             // create empty OpenGL ES Program
		        GLES20.glAttachShader(gaussianProgram[i], vertexShader);   // add the vertex shader to program 
		        GLES20.glAttachShader(gaussianProgram[i], fragmentShader); // add the fragment shader to program
		        GLES20.glLinkProgram(gaussianProgram[i]);                  // creates OpenGL ES program executables
		        int[] status = new int[1];
				GLES20.glGetProgramiv(gaussianProgram[i], GLES20.GL_LINK_STATUS, status, 0);
				if(status[0] != GLES20.GL_TRUE){
					Log.e("Shader", "Could not link shaders "+i);
		            throw new RuntimeException("Could not links shaders "+i);
			    }
		        GLES20.glUseProgram(gaussianProgram[i]);
		        
		        // Get the shader locations
		        gaussianPositionHandle[i]     = GLES20.glGetAttribLocation( gaussianProgram[i], "vertexPosition");
		        gaussianModelMatrixHandle[i]  = GLES20.glGetUniformLocation(gaussianProgram[i], "modelMatrix");
		        gaussianTextureHandle[i]      = GLES20.glGetUniformLocation(gaussianProgram[i], "texture");
		        gaussianTextureCoordinates[i] = GLES20.glGetAttribLocation( gaussianProgram[i], "textureCoordinates");
		        gaussianBlurSize[i]           = GLES20.glGetUniformLocation(gaussianProgram[i], "blurSize");
        	}
	        gaussianReady = true;
        }
    }
    
    public void draw() {
    	// Matrices
    	float[] modelMatrix = new float[16];
    	float[] scaleMatrix = new float[16];
    	
    	// Testing crap
		if(addBlur){
			blurAmount += 0.1f;
			if(blurAmount >= 1.0f)
				addBlur = false;
		}else{
			blurAmount -= 0.1f;
			if(blurAmount <= 0.0f)
				addBlur = true;
		}
      	
    	if(blurAmount > 0){
    		Matrix.setIdentityM(modelMatrix, 0);
	    	Matrix.translateM(modelMatrix, 0, x, y, 1.0f);
	    	Matrix.scaleM(modelMatrix, 0, scaleX*0.01f, scaleY*0.01f, 1.0f);
	    	Matrix.multiplyMM(scaleMatrix, 0, engine.getProjectionMatrix(), 0, modelMatrix, 0);
    		
			// Add program to OpenGL ES environment
			GLES20.glUseProgram(simpleProgram);
			
			// Use the vertex buffer
			vertexBuffer.position(0);
			GLES20.glEnableVertexAttribArray(simplePositionHandle);
			GLES20.glVertexAttribPointer(simplePositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,0, vertexBuffer);
			
			// Set uniforms
			GLES20.glUniformMatrix4fv(simpleModelMatrixHandle, 1, false, scaleMatrix, 0); // Projection matrix uniform
			
			// Bind texture to the fragment shader
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
			GLES20.glUniform1i(simpleTextureHandle, 0);
			
			// Use the UV coordinate buffer
			textureBuffer.position(0);
			GLES20.glEnableVertexAttribArray(simpleTextureCoordinates);
			GLES20.glVertexAttribPointer(simpleTextureCoordinates, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
			
			// Draw the object
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
			
			// Clean up
			GLES20.glDisableVertexAttribArray(simpleTextureCoordinates);
			GLES20.glDisableVertexAttribArray(simplePositionHandle);
    	}else{
			// Use the gaussian blur shader
    		for(int i = 0; i < 2; i++){
    			if(i == 0){
    				// Make the transformation matrix ready
    		    	Matrix.setIdentityM(modelMatrix, 0);
    		    	Matrix.translateM(modelMatrix, 0, 0, 0, 1.0f);
    		    	Matrix.scaleM(modelMatrix, 0, scaleX*0.01f, scaleY*0.01f, 1.0f);
    		    	Matrix.multiplyMM(scaleMatrix, 0, engine.getProjectionMatrix(), 0, modelMatrix, 0);
    				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, sceneFBO[0]);
    				checkGlError("Render");
    			}else{
    				// Make the transformation matrix ready
    		    	Matrix.setIdentityM(modelMatrix, 0);
    		    	Matrix.translateM(modelMatrix, 0, x, y, 1.0f);
    		    	Matrix.scaleM(modelMatrix, 0, 1.0f, 1.0f, 1.0f);
    		    	Matrix.multiplyMM(scaleMatrix, 0, engine.getProjectionMatrix(), 0, modelMatrix, 0);
    				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    				checkGlError("Render");
    			}
				GLES20.glUseProgram(gaussianProgram[i]);
				checkGlError("Render");
				
				// Use the vertex buffer
				vertexBuffer.position(0);
				GLES20.glEnableVertexAttribArray(gaussianPositionHandle[i]);
				GLES20.glVertexAttribPointer(gaussianPositionHandle[i], COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,0, vertexBuffer);
				checkGlError("Render");
				
				// Set uniforms
				GLES20.glUniformMatrix4fv(gaussianModelMatrixHandle[i], 1, false, scaleMatrix, 0); // Projection matrix uniform
				GLES20.glUniform1f(gaussianBlurSize[i], (1.0f/(float)cropWidth)*blurAmount); // Size of one pixel
				
				// Bind texture to the fragment shader
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, i==0?texture:sceneTexture[0]);
				GLES20.glUniform1i(gaussianTextureHandle[i], 0);
				checkGlError("Render");
				
				// Use the UV coordinate buffer
				textureBuffer.position(0);
				GLES20.glEnableVertexAttribArray(gaussianTextureCoordinates[i]);
				GLES20.glVertexAttribPointer(gaussianTextureCoordinates[i], 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
				
				// Draw the object
				GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
				checkGlError("Render");
				
				// Clean up
				GLES20.glDisableVertexAttribArray(gaussianTextureCoordinates[i]);
				GLES20.glDisableVertexAttribArray(gaussianPositionHandle[i]);
    		}
    	}
    	checkGlError("Render");
    	
    	totalTime += (System.nanoTime()/1000000)-lastTime;
    	lastTime = System.nanoTime()/1000000;
    	frames++;
    	if(frames == 60){
    		Log.i("Square", "FPS: "+((int)((1000.0f/(totalTime/frames))*100.0f))/100.0f+" - Time: "+(totalTime/frames));
    		frames = 0;
    		totalTime = 0;
    	}
    	
    }
    
    private int loadTexture(InputStream is){
    	// Notice: Loading bitmaps can take a while!
        int[] textureId = new int[1];
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeStream(is);
		
		int a = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
		int textureSize = 1 << (a == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(a - 1));
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(textureSize * textureSize * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        
        // ARGB -> RGBA 
        byte buffer[] = new byte[4];
		byte empty[] = new byte[(textureSize-bitmap.getWidth())*4];
		byte empty2[] = new byte[textureSize*4];
    	for(int i = 0; i < bitmap.getHeight(); i++){
    		for(int j = 0; j < bitmap.getWidth(); j++){
    			int color = bitmap.getPixel(j, i);
    			buffer[0] = (byte)(color >> 16);
    			buffer[1] = (byte)(color >> 8);
    			buffer[2] = (byte)(color);
    			buffer[3] = (byte)(color >> 24);
    			byteBuffer.put(buffer);
    		}
			byteBuffer.put(empty);
    	}
		for(int i = 0; i < (textureSize-bitmap.getHeight()); i++){
			byteBuffer.put(empty2);
		}
		if(textureSize != a)
			Log.w("Square", "Loaded non-power of two texture "+bitmap.getWidth()+"x"+bitmap.getHeight()+"px into a "+textureSize+"x"+textureSize+"px texture.");

        byteBuffer.position(0);
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureSize, textureSize, 0,
                            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        textureWidth = bitmap.getWidth();
        textureHeight = bitmap.getHeight();
		this.textureSize = textureSize;
        
        bitmap.recycle();
        return textureId[0];
    }

}
