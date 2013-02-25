package li.alo.comicbook.gui;

import java.util.ArrayList;

public abstract class CBGuiElement {
	
	// Sign up for messages, allocate memory, etc. here.
	public abstract void initialize(CBGuiElement parent);
	
	// This is called once a frame.
	// Hopefully I can make this spit out a render list instead
	// of actually rendering anything.
	public abstract void draw();
	
	// Handle messages here
	public abstract boolean message();
	
	// Return the list of children
	public ArrayList<CBGuiElement> getChildren(){
		return null;
	}
	
	// Return the width of the element (Used for aligning)
	public abstract void getWidth();
	
	// Return the height of the element (Used for aligning)
	public abstract void getHeight();
	
}
