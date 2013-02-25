package li.alo.comicbook;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CBAuthor{
	
	private static final String NAME_TAG = "name";
	private static final String EMAIL_TAG = "email";
	private static final String ROLE_TAG = "role";
	private static final String URL_TAG = "link";
	
	private static final Set<String> TAGS = new HashSet<String>(Arrays.asList(new String[] {
		NAME_TAG, EMAIL_TAG, ROLE_TAG, URL_TAG
	}));
	
	private final HashMap<String, String> map = new HashMap<String, String>();
	
	public CBAuthor(){
	}
	
	public CBAuthor(Node node){
		parseNode(node);
	}
	
	// Fetch all the children and add them to the author set
	public void parseNode(Node node){
		Node child;
		String childName;
		String childText;
		NodeList children = node.getChildNodes();
		
		int childrenSize = children.getLength();
		for(int i = 0; i < childrenSize; i++){
			child = children.item(i);
			childName = child.getNodeName();
			if(TAGS.contains(childName)){
				// TODO: Error checking: If the map already contains this tag, throw a "Bad Author Definition: Duplicate" runtime exception.
				childText = child.getTextContent();
				if(childText != null){
					map.put(childName, childText);
				}
			}
		}
	}
	
	// Get an item from the map, or an empty string
	private String getValue(String key){
		if(map.containsKey(key)){
			return map.get(key);
		}
		return "";
	}
	
	public String getName(){
		return getValue(NAME_TAG);
	}
	
	public String getEmail(){
		return getValue(EMAIL_TAG);
	}
	
	public String getRole(){
		return getValue(ROLE_TAG);
	}
	
	public String getURL(){
		return getValue(URL_TAG);
	}
	
}
