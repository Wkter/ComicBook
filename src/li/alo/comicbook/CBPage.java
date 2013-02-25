package li.alo.comicbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.opengl.GLES20;
import android.util.Log;

// This class holds all the panels of a given page,
// and a pre-rendered texture used for caching.
// The textures can be loaded and destroyed at any time by calling
// load() and unload() when the reader is likely to read the page.
public class CBPage {
	
	// Dimensions of the page
	private int width;
	private int height;
	
	private boolean isLoaded = false;
	
	private CBEngine engine;
	private CBRenderElement pageCache;
	private CBFBO fbo;
	private int FBOSize;
	private ArrayList<CBPanel> panels = new ArrayList<CBPanel>();
	
	private String title = "";
	private String path = "";
	
	// Default constructor
	public CBPage(CBEngine engine){
		this.engine = engine;
		initialize();
	}
	
	// Constructor that also parses a page package descriptor
	public CBPage(CBEngine engine, File descriptor){
		this.engine = engine;
		path = descriptor.getParentFile().getAbsolutePath();
		try {
			// Parse the page XML and get the root node
			CBXMLParser xml = new CBXMLParser(descriptor.getAbsolutePath());
			Node root = xml.getRootNode();
			
			// Get the title  
			String title = xml.getText("/page/title", root);
			if(!title.isEmpty()){
				this.title = title;
			}
			
			// Get the dimensions of the page
			try{
				width = Integer.parseInt(xml.getText("/page/width", root));
				height = Integer.parseInt(xml.getText("/page/height", root));
			} catch(NumberFormatException e){
				Log.e("ComicBook", title+": Failed to parse dimensions!");
				
				// TODO: Guess dimensions based on panel sizes.
				//       Even if the definition is broken, we should do our best
				//       to correct it... And yell at the bad maintainers!
			}
			
			// Get panels
			NodeList panelList = xml.getNodeList("/page/panel", root);
			int panelSize = panelList.getLength();
			for(int i = 0; i < panelSize; i++){
				panels.add(new CBPanel(engine, this, xml, panelList.item(i)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		initialize();
	}
	
	// <^-^>
	public void initialize(){
        // Get the smallest power-of-two needed for the FBO
        int a = width > height ? width : height;
        FBOSize = 1 << (a == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(a - 1));
		Log.i("ComicBook", "Texture size: "+FBOSize);
	}
	
	public void load(){
		Iterator<CBPanel> i = panels.iterator();
		while(i.hasNext())
			i.next().load();
		
		// Make an FBO object for the page
		fbo = new CBFBO(engine, FBOSize);
		preRender();
		
		isLoaded = true;
	}
	
	// Free all textures allocated by this page
	public void unload(){
		Iterator<CBPanel> i = panels.iterator();
		while(i.hasNext())
			i.next().unload();
		
		// Destroy the old FBO object
		fbo.destroy();
		fbo = null;
		
		isLoaded = false;
	}
	
	// Has textures loaded?
	public boolean isLoaded(){
		return isLoaded;
	}
	
	// Return the path to the page directory
	public String getPath(){
		return path;
	}
	
	// Return the title of the page
	public String getTitle(){
		return title;
	}
	
	// Cache the page into the FBO
	private void preRender(){
		// Render to the FBO
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		fbo.bind();
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		// Draw all the panels
		drawPanels();
		
		// Release the FBO
		fbo.release();

		// Create the page cache render element
		pageCache = new CBRenderElement(engine);
		pageCache.setTexture(fbo.getTexture(), fbo.getSize());
		pageCache.setSize(width, height);
		pageCache.setTextureCrop(0, 0, width, height);
		pageCache.shader = engine.shader;
        
		// Scale to fit the screen
        float scale = 1;
        if(width > height){
        	scale = ((float)engine.screenHeight / (float)height)*100.0f;
        }else{
        	scale = ((float)engine.screenWidth / (float)width)*100.0f;
        }
        
        pageCache.scaleX = scale;
        pageCache.scaleY = scale;
        Log.i("ComicBook", "                scaleX: "+pageCache.scaleX+", scaleY:"+pageCache.scaleY);
	}
	
	// Draw all the panels
	public void drawPanels(){
		Iterator<CBPanel> i = panels.iterator();
		while(i.hasNext())
			i.next().draw();
	}
	
	// Draw the (cached) page
	public void draw(){
		if(isLoaded)
			pageCache.draw();
	}
}
