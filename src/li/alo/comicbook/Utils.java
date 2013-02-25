package li.alo.comicbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.opengl.GLES20;
import android.opengl.GLU;
import android.util.Log;


public class Utils{

	// Automagically turns a float array into a float buffer
	public static FloatBuffer makeFloatBuffer(float array[]){
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        FloatBuffer buffer;
        bb.order(ByteOrder.nativeOrder());
        buffer = bb.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
	}
	
	// Automagically turns a short array into a short buffer
	public static ShortBuffer makeShortBuffer(short array[]){
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 2);
        ShortBuffer buffer;
        bb.order(ByteOrder.nativeOrder());
        buffer = bb.asShortBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
	}
	
	// Check for GL errors, and throw an exception upon errors
	public static void checkGlError(String op){
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
        	String errorStr = GLU.gluErrorString(error);
        	Log.e("ComicBook", op + ": glError " + error + ": " + errorStr);
            throw new RuntimeException(op + ": glError " + error + ": " + errorStr);
        }
    }
	
	// Print an exception to the log
	public static void printException(Exception e){
		Log.e("ComicBook", e.toString());
		StackTraceElement[] trace = e.getStackTrace();
		for(int i = 0; i < trace.length; i++){
			Log.e("ComicBook", "    "+trace[i].toString());
		}
	}
	
	// Read an entire file to a string
	public static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			
			// Instead of using default, pass in a decoder.
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}
    
	// Read an input stream into a string
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
}
