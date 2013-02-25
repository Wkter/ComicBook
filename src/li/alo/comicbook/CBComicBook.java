package li.alo.comicbook;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class CBComicBook{

	// Reference to the engine this package was loaded by
	private CBEngine engine;
	
	// Title of the package
	private String title = "Unnamed";

	// Stuff for loading page packages
	private File pagesDirectory;
	private static final String PAGES_DIRECTORY = "pages";
	private static final String PAGE_XML_FILE = "page.xml";
	
	// Authors who make this comic, and a list of all the pages
	private final ArrayList<CBAuthor> authors = new ArrayList<CBAuthor>();
	public final ArrayList<CBPage>   pages   = new ArrayList<CBPage>();
	
	// Default constructor, taking an engine reference and a file object pointing to a package XML definition.
	public CBComicBook(CBEngine engine, File file){

		// Store the engine reference (Our children will need it for rendering) 
		this.engine = engine;
		
		// Try to load the package
		try {
			// Parse the XML definition and get the root node
			CBXMLParser xml = new CBXMLParser(file.getAbsolutePath());
			Node root = xml.getRootNode();
			
			// Get the title  
			String title = xml.getText("/comicbook/title", root);
			if(!title.isEmpty()){
				this.title = title;
			}
			Log.i("ComicBook", "Opening CB Package:");
			Log.i("ComicBook", "---- "+this.title+" ----");
			
			// Get the comic authors
			NodeList authorsList = xml.getNodeList("/comicbook/author", root);
			int authorsSize = authorsList.getLength();
			for(int i = 0; i < authorsSize; i++){
				CBAuthor author = new CBAuthor(authorsList.item(i));
				Log.i("ComicBook", "Author:");
				Log.i("ComicBook", "    Author: "+author.getName());
				Log.i("ComicBook", "    Email:  "+author.getEmail());
				Log.i("ComicBook", "    Role:   "+author.getRole());
				Log.i("ComicBook", "    URL:    "+author.getURL());
				authors.add(author);
			}
			
			// Make sure this package contains a pages directory
			pagesDirectory = new File(file.getParentFile().getAbsoluteFile()+"/"+PAGES_DIRECTORY);
			if(pagesDirectory.exists()){
				Log.i("ComicBook", "Found pages directory");
			}else{
				Log.e("ComicBook", "No pages directory found!");
			}
			
			// Find all page packages
			File dirs[] = pagesDirectory.listFiles(new FileFilter(){
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			
			// Parse all page packages
			String pageDescriptorURI;
			for(int i = 0; i < dirs.length; i++){
				Log.i("ComicBook", "    Page dir: "+dirs[i].getName());
				pageDescriptorURI = dirs[i].getAbsoluteFile()+"/"+PAGE_XML_FILE;
				File pageDescriptor = new File(pageDescriptorURI);
				if(!pageDescriptor.exists()){
					Log.e("ComicBook", "        Pages descriptor not found for page "+dirs[i].getName()+"!");
				}else{
					Log.i("ComicBook", "        Pages descriptor for page "+dirs[i].getName()+" found.");
					CBPage page = new CBPage(engine, pageDescriptor);
					pages.add(page);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Get comicbook title
	public String getTitle(){
		return title;
	}
	
	// Get the engine refrence
	public CBEngine getEngine(){
		return engine;
	}
	

}
