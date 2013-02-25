package li.alo.comicbook;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


public class CBRenderElement{

	// The engine to fetch useful information from
	CBEngine engine;
	public CBSimpleShader shader;

	// Dimensions of the quad
	public int width;
	public int height;
	
	// Dimensions of the texture
	public int textureWidth;
	public int textureHeight;
	public int textureSize;
	
	// Scale values
	public float scaleX = 100.0f;
	public float scaleY = 100.0f;
	
	// Coordinates
	public int x;
	public int y;
	
	// UV crop dimensions and coordinates
	public int cropX;
	public int cropY;
	public int cropWidth;
	public int cropHeight;
	
	// Texture
	public int textureID;
	
	// Buffers to pass to GL
	private FloatBuffer vertices;
	private FloatBuffer uv;
	private ShortBuffer indecies;
	
	// Matrices (Allocated here for performance)
	final private float modelMatrix[] = new float[16];
	final private float scaleMatrix[] = new float[16];
	
	// Default constructor with no texture loading
	CBRenderElement(CBEngine engine){
		this.engine = engine;
		initialize();
	}
	
	// Constructor with file loading
	CBRenderElement(CBEngine engine, String filename){
		this.engine = engine;
		initialize();
		loadTextureFromFile(filename);
	}
	
	// Initialize the element from the constructor
	private void initialize(){
		// The index list is fairly static for this, so we just create a generic one upfront
		short indecies[] = { 0, 1, 2, 0, 2, 3 };
		this.indecies = Utils.makeShortBuffer(indecies);
	}
	
	// Free all memory held by GL and reset all values for reuse of this object or safe destroying
	public void destroy(){
		// Free memory held by GL
		GLES20.glDeleteTextures(1, new int[]{ textureID }, 0);
		
		// Reset vars
		width = height = 
		textureWidth = textureHeight =
		textureSize = 0;
		
		// Scale values
		scaleX = scaleY = 100.0f;
		
		// Coordinates
		x = y = 0;
		
		// UV crop dimensions and coordinates
		cropX = cropY = cropWidth = cropHeight = 0;
	}
	
	// Set texture ID
	public void setTexture(int id, int size){
		textureID = id;
		textureSize = size;
		textureWidth = size;
        textureHeight = size;
		
		// Create default UV buffer
		cropX = 0;
		cropY = 0;
		cropWidth = textureWidth;
		cropHeight = textureHeight;
		refreshUVBuffer();
	}
	
	// Create default vertex buffer
	public boolean setSize(int width, int height){
		this.width = width;
		this.height = height;
		refreshVertexBuffer();
		return true;
	}
	
	// Set the rectangle used for cropping the texture
	public boolean setTextureCrop(int x, int y, int width, int height){
		// Create a new UV buffer
		cropX = x;
		cropY = y;
		cropWidth = width;
		cropHeight = height;
		refreshUVBuffer();
		return true;
	}
	
	// Refresh the UV buffer to reflect the class' internal state
	private void refreshUVBuffer(){
		float pixelSize = (1.0f/(float)textureSize);
		float _x1 = pixelSize*(float)cropX;
		float _x2 = pixelSize*(float)cropWidth;
		float _y1 = pixelSize*(float)cropY;
		float _y2 = pixelSize*(float)cropHeight;
		float uv[] = {
				_x1,     _y1,     // top left
				_x1,     _y1+_y2, // bottom left
				_x1+_x2, _y1+_y2, // bottom right
				_x1+_x2, _y1      // top right 
		};
		this.uv = Utils.makeFloatBuffer(uv);
	}
	
	// Refresh the vertex buffer to reflect the class' internal state
	private void refreshVertexBuffer(){
		float vertices[] = { 
	    	         0f,     0f, 0.0f, // top left
	                 0f, height, 0.0f, // bottom left
	              width, height, 0.0f, // bottom right
	              width,     0f, 0.0f  // top right
		};
		this.vertices = Utils.makeFloatBuffer(vertices);
	}
	
	// Load a texture from the asset of the APK into GL
	public boolean loadTextureFromAsset(String filename){
		// Get the file stream
		try {
			// Load the texture for the APK
			Bitmap bitmap;
			bitmap = BitmapFactory.decodeStream(engine.context.getAssets().open(filename));
			if(bitmap == null){
				Log.e("ComicBook", "Something went wrong with Java's nasty decoder...");
				return false;
			}
			
			// Load the texture to GL
			return loadTextureFromBitmap(bitmap);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// Load a texture from a file into GL
	public boolean loadTextureFromFile(String filename){
		Log.i("ComicBook", "Attempting to load: "+filename);
		
		// Load the texture
		File file = new File(filename);
		if(file.exists()){
			
			// Load the texture for the disk
			Bitmap bitmap;
	        bitmap = BitmapFactory.decodeFile(filename);
	        if(bitmap == null){
				Log.e("ComicBook", "Something went wrong with Java's nasty decoder...");
				return false;
			}
	        
	        // Load the texture to GL
			return loadTextureFromBitmap(bitmap);
		}else{
			Log.e("ComicBook", "File doesn't exist: "+filename);
			return false;
		}
	}
	
	// Load a texture from a bitmap stored in memory into GL
	public boolean loadTextureFromBitmap(Bitmap bitmap){
		if(bitmap == null){
			Log.e("ComicBook", "Cannot load texture from null, obviously...");
			return false;
		}
		
    	// Variables 
        int[] textureID = new int[1];
        
        // Make a power-of-two byte buffer
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
			Log.w("ComicBook", "Loaded non-power of two texture "+bitmap.getWidth()+"x"+bitmap.getHeight()+"px into a "+textureSize+"x"+textureSize+"px texture.");

		// Load bytes into GL
        byteBuffer.position(0);
        GLES20.glGenTextures(1, textureID, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureSize, textureSize, 0,
                            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        
        // Save dimensions
        textureWidth = bitmap.getWidth();
        textureHeight = bitmap.getHeight();
		this.textureSize = textureSize;
		this.textureID = textureID[0];
		
		// Create default vertex buffer
		width = textureWidth;
		height = textureHeight;
		refreshVertexBuffer();
		
		// Create default UV buffer
		cropX = 0;
		cropY = 0;
		cropWidth = textureWidth;
		cropHeight = textureHeight;
		refreshUVBuffer();
        
        // Return
		return true;
	}
	
	// Draw this element
	public void draw(){
		if(shader == null){
			Log.e("ComicBook", "No shader, no fun");
			return;
		}
		
		Matrix.setIdentityM(modelMatrix, 0);
    	Matrix.translateM(modelMatrix, 0, x, y, 1.0f);
    	Matrix.scaleM(modelMatrix, 0, scaleX*0.01f, scaleY*0.01f, 1.0f);
    	Matrix.multiplyMM(scaleMatrix, 0, engine.getProjectionMatrix(), 0, modelMatrix, 0);
		
    	// Push values to the shader
    	shader.start();
		shader.setVertexBuffer(vertices);
		shader.setUVBuffer(uv);
		shader.setMatrix(scaleMatrix);
		shader.setTexture(textureID);
		
		// Draw the object
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indecies);
		
		// Release the shader
		shader.end();
	}
}
