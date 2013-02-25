package li.alo.comicbook;

import java.util.ArrayList;

import org.w3c.dom.Node;

import android.util.Log;

public class CBPanel {
	
	private CBEngine engine;
	private CBPage parent;
	private CBRenderElement panel;
	private boolean isLoaded = false;
	
	private String file;
	private int x;
	private int y;
	public ArrayList<String> dialogue = new ArrayList<String>();
	
	// Constructor
	public CBPanel(CBEngine engine, CBPage parent){
		this.engine = engine;
		this.parent = parent;
	}
	
	// Initializing constructor
	public CBPanel(CBEngine engine, CBPage parent, CBXMLParser xml, Node node){
		this.engine = engine;
		this.parent = parent;
		parseXML(xml, node);
	}
	
	// Parse the panel XML
	public void parseXML(CBXMLParser xml, Node node){
		String file = xml.getText("file", node);
		Log.i("ComicBook", "                panel file: "+file);
		this.file = file;
		try{
			x = Integer.parseInt(xml.getText("x", node));
			y = Integer.parseInt(xml.getText("y", node));
			Log.i("ComicBook", "                X: "+x+", Y:"+y);
		} catch(NumberFormatException e){
			Log.e("ComicBook", "Panel "+file+": Failed to parse coordinates!");
		}
	}
	
	public void load(){
		// Load textures
		panel = new CBRenderElement(engine, parent.getPath()+"/"+file);
		panel.x = x;
		panel.y = y;
		panel.shader = engine.shader;
		
		isLoaded = true;
	}
	
	public void unload(){
		// Destroy GL memory
		panel.destroy();
		
		isLoaded = false;
	}
	
	// Draw
	public void draw(){
		// Make sure we're loaded
		if(!isLoaded)
			return;
		
		// Draw
		panel.draw();
	}

}
