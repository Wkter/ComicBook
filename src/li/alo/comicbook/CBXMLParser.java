package li.alo.comicbook;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class CBXMLParser{
	
	private XPath xpath = XPathFactory.newInstance().newXPath();
	private Document dom;
	private Node root;
	
	// Default constructor
	public CBXMLParser(){
	}
	
	// Constructor that parses a file
	public CBXMLParser(String filename) throws IOException{
		loadFile(filename);
	}
	
	// Parse a file
	public void loadFile(String filename) throws IOException{
		dom = getDomElement(filename);
		try {
			root = (Node)xpath.evaluate("/", dom, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			root = null;
		}
	}
	
	// Return the root node of the document
	public Node getRootNode(){
		return root;
	}
	
	// Return a node that matches the expression, or null on error 
	public Node getNode(String expression, Node node){
		try {
			return (Node)xpath.evaluate(expression, node, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Return a list of nodes that matches the expression, or null on error
	public NodeList getNodeList(String expression, Node node){
		try {
			return (NodeList)xpath.evaluate(expression, node, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Return the the text of a node matching the expression, or an empty string on error
	public String getText(String expression, Node node){
		try {
			Node result = (Node)xpath.evaluate(expression, node, XPathConstants.NODE);
			if(result != null){
				String str = result.getTextContent();
				if(str != null)
					return str.trim();
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	// Return a DOM from an XML file
	private Document getDomElement(String path) throws IOException{
		// Set up the parser
		String xml = Utils.readFile(path);
		Document dom = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// Parse
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			dom = db.parse(is); 
		} catch (ParserConfigurationException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			return null;
		}
		
		// Return DOM tree
		return dom;
	}
}
