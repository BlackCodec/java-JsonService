package it.icapito.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for manage JSON objects.
 * 
 * @author BlackCodec
 *
 */
public class JsonService {
	
	private static final String REGEX_STRING=".*((\\\"[a-zA-Z0-9_#\\-.]*\\\")[ ]*:[ ]*(\\\".[^\\[\\]\\\"]*(?<!\\\\)\\\")).*";
	private static final String REGEX_ESCAPE_STRING=".*((\\\"[a-zA-Z0-9_\\-.]*\\\")[ ]*:[ ]*(\\\".*(?<!\\\\)\\\")).*";
	private static final String REGEX_NUMBER= ".*((\\\"[a-zA-Z0-9_#\\-.]*\\\")[ ]*:[ ]*([\\d]+[.\\d]+)).*";
	private static final String REGEX_BOOLEAN= ".*((\\\"[a-zA-Z0-9_#\\-.]*\\\")[ ]*:[ ]*(true|false)).*";
	private static final String REGEX_NULL= ".*((\\\"[a-zA-Z0-9_#\\-.]*\\\")[ ]*:[ ]*(null)).*";
	private static final String REGEX_ARRAY= ".*(\\[[^\\[]*?\\]).*";
	private static final String REGEX_OBJECT= ".*(\\{[^\\{]*?\\}).*";
	
	private static final String REGEX_LEAF_MATCH="@leaf-[\\d]+@";
	private static final String REGEX_ARRAY_MATCH="@arr-[\\d]+@";
	private static final String REGEX_OBJECT_MATCH="@obj-[\\d]+@";
	
	private static Map<String,JsonElement> nodes = new HashMap<>();
	private static Map<String,String> keys = new HashMap<>();
	
	private static Logger logger = null;
	
	private JsonService() {}
	
	/**
	 * Append a logger where print messages for debug.
	 * 
	 * @param logger A class that implements java.util.logging.Logger capabilities.
	 */
	public static void appendLogger(Logger logger) { JsonService.logger = logger;}
	
	/**
	 * Return a JsonObject from a string.
	 * The method converts a string in a JSON object.
	 * The string must contains a valid JSON object otherwise UnsupportedOperationException will be raised.
	 * 
	 * @param jsonString	a string that contains the JSON object
	 * @return				the JSON object representation of the string
	 * @see					JsonObject
	 * @throws UnsupportedOperationException if the input string does not represent a valid JSON object
	 */
	public static JsonObject parse(String jsonString) throws UnsupportedOperationException {
		if (logger == null) {
			// set to a void logger
			logger = Logger.getLogger(JsonService.class.getCanonicalName());
			logger.setLevel(Level.OFF);
		}
		logger.entering(JsonService.class.getCanonicalName(),"parse");
		try {
			// match all JSON number
			logger.info("Parsing all numbers... ");
			String parsed = extractLeaf(jsonString, REGEX_NUMBER, JsonElement.Types.JSON_NUMBER);
			// match all JSON boolean
			logger.info("Parsing all booleans ... ");
			parsed = extractLeaf(parsed, REGEX_BOOLEAN, JsonElement.Types.JSON_BOOLEAN);
			// match all JSON null
			logger.info("Parsing all nulls ... ");
			parsed = extractLeaf(parsed, REGEX_NULL, JsonElement.Types.JSON_NULL);
			// match all JSON strings
			logger.info("Parsing all strings ... ");
			parsed = extractLeaf(parsed, REGEX_STRING, JsonElement.Types.JSON_STRING);
			// match all JSON escaped strings
			logger.info("Parsing all escaped strings ... ");
			parsed = extractLeaf(parsed, REGEX_ESCAPE_STRING, JsonElement.Types.JSON_STRING);
			logger.log(Level.FINE,"Current string: {0}",parsed);
			logger.info("Removes end lines and unused spaces ... ");
			// clear the rest of the string, remove all spaces
			parsed = parsed.replace(System.lineSeparator(), "").replace(" ","").replace("\r", "").replace("\n", "").replace("\t", "");
			logger.info("Parsing all JSON arrays... ");
			// parse all JSON arrays
			parsed = extractArray(parsed);
			logger.info("Parsing all JSON objects... ");
			// parse all JSON objects
			parsed = extractObject(parsed);
			// we expected to have a JSON object now
			if (parsed.matches(REGEX_OBJECT_MATCH)) {
				logger.info("Process completed: SUCCESS.");
				return (JsonObject) nodes.get(parsed);
			}
			logger.warning("Process completed: Invalid JSON object");
			return null;
		} finally { logger.exiting(JsonService.class.getCanonicalName(),"parse"); }
	}
	
	/**
	 * Private method that parse all JsonArray objects from string and add it to the nodes map
	 * 
	 * @param input string to parse
	 * @return      the string parsed that contains reference to nodes map keys
	 * @see JsonArray
	 * @throws UnsupportedOperationException if the string does not contains a valid json
	 */
	private static String extractArray(String input) throws UnsupportedOperationException {
		logger.entering(JsonService.class.getCanonicalName(),"extractArray");
		try {
			logger.log(Level.FINEST,"Pattern: {0}\nInput: {1}", new String[] {REGEX_ARRAY,input});
			Pattern pattern = Pattern.compile(REGEX_ARRAY,Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(input);
			if (matcher.find()) {
				String values = matcher.group(1); // [@id-1@,@id-2@...]
				String replace = values;
				if (values.startsWith("[")) values = values.substring(1);
				if (values.endsWith("]")) values = values.substring(0,values.length()-1);
				// this is an array so create a new JSON array
				if (values.contains("{")) {
					if (!values.contains("}")) 
						throw new UnsupportedOperationException("Not well formed JsonObject found");
					// is an array objects
					values = extractObject(values);
				}
				// is an array of element, split comma
				JsonArray node = new JsonArray();
				for (String child: values.split(",")) {
					if (child != null && !child.isEmpty()) {
						JsonElement sub = null;
						if (child.matches(REGEX_LEAF_MATCH) || child.matches(REGEX_ARRAY_MATCH) || child.matches(REGEX_OBJECT_MATCH)) {
							sub = nodes.get(child);
						} else {
							// is a value so check if value is a string
							if (child.trim().startsWith("\"")) {
								logger.log(Level.FINEST,"String: {0}", child);
								if (!child.trim().endsWith("\"")) {
									// invalid string
									logger.severe("Invalid string!");
									throw new UnsupportedOperationException("Not well formed JsonString found");
								}
								sub = new JsonElement(JsonElement.Types.JSON_STRING,clearText(child));
							} else {
								// clear the text
								child = clearText(child);
								// if record is true or false is a boolean
								if (child.equals("true") || child.equals("false")) {
									logger.log(Level.FINEST,"Boolean: {0}", child);
									sub = new JsonElement(JsonElement.Types.JSON_BOOLEAN, child);
								} else if (child.matches("[\\d]+")) {
									logger.log(Level.FINEST,"Number: {0}", child);
									sub = new JsonElement(JsonElement.Types.JSON_NUMBER, child);
								} else if (child.equals("null")){
									logger.log(Level.FINEST,"Null: {0}", child);
									sub = new JsonElement(JsonElement.Types.JSON_NULL, null);
								}
							}
						}
						if (sub != null) {
							logger.log(Level.FINEST,"Json child: {0}", sub);
							sub.setParent(node);
							node.add(sub);
						} else 
							throw new UnsupportedOperationException("Invalid JSON type");
					}
				}
				// calculate the is of this object
				String id = String.format("@arr-%d@", nodes.size());
				logger.log(Level.FINEST,"id: {0}", id);
				// add id to nodes only because this element does not have any key
				nodes.put(id, node);
				// now replace the old string and return
				return extractArray(input.replace(replace, id));
			}
			logger.warning("No match!");
			return input;
		} finally { logger.exiting(JsonService.class.getCanonicalName(),"extractArray"); }
	}
	
	/**
	 * Private method that parse all JsonObject objects from string and add it to the nodes map
	 * 
	 * @param input string to parse
	 * @return      the string parsed that contains reference to nodes map keys
	 * @see JsonObject
	 * @throws UnsupportedOperationException if the string does not contains a valid json
	 */
	private static String extractObject(String input) throws UnsupportedOperationException { 
		logger.entering(JsonService.class.getCanonicalName(),"extractObject");
		try {
			logger.log(Level.FINEST,"Pattern: {0}\nInput: {1}", new String[] {REGEX_OBJECT,input});
			Pattern pattern = Pattern.compile(REGEX_OBJECT,Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(input);
			if (matcher.find()) {
				String values = matcher.group(1); 
				String replace = values;
				if (values.startsWith("{")) values = values.substring(1);
				if (values.endsWith("}")) values = values.substring(0,values.length()-1);
				// this is an array so create a new JSON array
				if (values.contains("[")) {
					if (!values.contains("]")) {
						logger.log(Level.FINEST,"Invalid array: {0}", values);
						throw new UnsupportedOperationException("Not well formed JsonArray found");
					}
					// is an array objects
					values = extractArray(values);
				}
				// is an array of element, split comma
				logger.log(Level.FINEST,"Split comma for child value: {0}",values);
				JsonObject node = new JsonObject();
				for (String child: values.split(",")) {
					if (child != null && !child.isEmpty()) {
						JsonElement sub = null;
						String key = null;
						// check for key value pair
						if (child.contains(":")) {
							key = clearText(child.split(":")[0]);
							child = child.split(":")[1];
							logger.log(Level.FINEST,"Key: {0}\nValue: {1}", new String[] {key,child});
						}
						if (child.matches(REGEX_LEAF_MATCH) || child.matches(REGEX_ARRAY_MATCH) || child.matches(REGEX_OBJECT_MATCH)) {
							sub = nodes.get(child);
							if (key == null) key = keys.get(child);
						} 
						if (sub != null && key != null) {
							logger.log(Level.FINEST,"Json child: {0}", sub);
							sub.setParent(node);
							node.add(key,sub);
						} else { 
							logger.log(Level.SEVERE,"Invalid JSON type: {0}.\nKey: {1}\nChild:{2}",new String[] {child, key, (sub!=null?sub.value():"null")});
							throw new UnsupportedOperationException(String.format("Invalid JSON type: %s.%nKey: %s%nChild:%s",child, key, (sub!=null?sub.value():"null")));
						}
					}
				}
				// calculate the is of this object
				String id = String.format("@obj-%d@", nodes.size());
				logger.log(Level.FINEST,"id: {0}", id);
				// add id to nodes only because this element does not have any key
				nodes.put(id, node);
				// now replace the old string and return
				return extractObject(input.replace(replace, id));
			}
			logger.warning("No match!");
			return input;
		} finally { logger.exiting(JsonService.class.getCanonicalName(),"extractObject"); }
	}
	
	/**
	 * Private method that parse all JsonNode objects that are not JsonNode or 
	 * JsonArray object from string and add it to the nodes map.
	 * 
	 * @param input string to parse
	 * @return      the string parsed that contains reference to nodes map keys
	 * @see JsonElement
	 */
	private static String extractLeaf(String input, String regex, JsonElement.Types type) {
		logger.entering(JsonService.class.getCanonicalName(),"extractLeaf");
		try {
			logger.log(Level.FINEST,"Input: {0}\nPattern: {1}\nType: {2}",new String[] {input,regex,type.name()});
			Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(input);
			if (matcher.find()) {
				logger.log(Level.FINEST,"Match: {0}",matcher.group(1));
				String replace = matcher.group(1);
				String key = JsonService.clearText(matcher.group(2));
				String value = JsonService.clearText(matcher.group(3));
				logger.log(Level.FINEST,"Analize: \n - Key: {0}\n - Child:{1}",new String[] {key, value});
				JsonElement node = new JsonElement(type,value);
				String id = String.format("@leaf-%d@", nodes.size());
				logger.log(Level.FINEST,"id: {0}", id);
				nodes.put(id, node);
				keys.put(id, key);
				return extractLeaf(input.replace(replace,id+","),regex,type);
			}
			logger.warning("No match!");
			return input;
		} finally { logger.exiting(JsonService.class.getCanonicalName(),"extractLeaf"); }
	}
	
	/**
	 * Private method for remove the quotation marks from the begin and the end of a string
	 * 
	 * @param input the string to clear
	 * @return      the string without the quotation marks at the begin and at the end
	 */
	private static String clearText(String input) { return input.trim().replaceAll("^\\\"(.*)\\\"$", "$1").trim(); }
	
	/* Json element classes */
	public static class JsonElement {

		public enum Types {

			JSON_NULL,
			JSON_STRING,
			JSON_NUMBER,
			JSON_BOOLEAN,
			JSON_ARRAY,
			JSON_OBJECT;
		}

		private String value;
		private Types type;
		private JsonElement parent = null;
		
		/**
		 * Construct a new JsonNode with specified type and value.
		 * 
		 * @param type type of the JsonNode as specified in Types enumeration
		 * @param value string with the value to be associated to this object
		 */
		public JsonElement(Types type, String value) {
			this.value = value;
			this.type = type;
		}
		
		/**
		 * Return the value of this node as a string.
		 * For JsonObject and JsonArray this is equals to toString method.
		 * @return the value of this JsonNode as a string
		 */
		public String value() { return this.value; }
		
		/**
		 * Return the type of this JsonNode as specified in Types enumeration
		 * 
		 * @return the type of JsonNode
		 */
		public Types type() { return this.type; }
		
		/**
		 * Return true if the value is null or contains an empty string, 
		 * a string with only spaces or a string with value "null".
		 * 
		 * @return true if the value is empty 
		 */
		public boolean isEmpty() { return this.value != null && !this.value.trim().equalsIgnoreCase("null") && this.value.replaceAll("\\s+", "").length() > 0; }
		
		/**
		 * Returns the length of the value this JsonNode. 
		 * The length is equal to the number of Unicode code units in the string.
		 * 
		 * @return the length of the sequence of characters represented by the value of this JsonNode
		 */
		public int size() { return this.isEmpty()?0:this.value.length(); }
		
		/**
		 * Return the value of this node as a integer.
		 * This method is applicable only for JSON_NUMBER types.
		 * 
		 * @return the value of this JsonNode as a integer
		 * @throws UnsupportedOperationException if the JsonNode type is not JSON_NUMBER
		 */
		public int intValue() throws UnsupportedOperationException {
			if (this.type.equals(Types.JSON_NUMBER)) return Integer.parseInt(this.value);
			else throw new UnsupportedOperationException(String.format("Exception type %s but found %s", Types.JSON_NUMBER.name(),this.type.name()));
		}
		
		/**
		 * Return the value of this node as a boolean.
		 * This method is applicable only for JSON_BOOLEAN types.
		 * 
		 * @return the value of this JsonNode as a boolean
		 * @throws UnsupportedOperationException if the JsonNode type is not JSON_BOOLEAN
		 */
		public boolean booleanValue() throws UnsupportedOperationException {
			if (this.type.equals(Types.JSON_BOOLEAN)) return this.value.replaceAll("\\s+","").toLowerCase().equalsIgnoreCase("true");
			else throw new UnsupportedOperationException(String.format("Exception type %s but found %s", Types.JSON_BOOLEAN.name(),this.type.name()));
		}
		
		/**
		 * Return the parent JsonNode object if any, otherwise null.
		 * 
		 * @return the JsonNode parent or null
		 */
		public JsonElement parent() { return this.parent; }
		
		/**
		 * Return true if the JsonNode has a parent associated.
		 * 
		 * @return true if this JsonNode has a parent
		 */
		public boolean hasParent() { return this.parent != null; }
		
		/**
		 * Associate a parent to this JsonNode.
		 * 
		 * @param node the JsonNode parent to associate
		 */
		public void setParent(JsonElement node) { this.parent = node; }
		
		@Override
		public String toString() {
			switch (this.type) {
				case JSON_NULL:
					return "null";
				case JSON_NUMBER:
				case JSON_BOOLEAN:
					return this.value;
				case JSON_STRING:
					return String.format("\"%s\"", this.value);
				case JSON_OBJECT:
					return ((JsonObject) this).toString();
				case JSON_ARRAY:
					return ((JsonArray) this).toString();
				default:
					return "";
			}
		}
	}

	/**
	 * Class that represent a JSON object.
	 * This class extends JsonNode
	 * 
	 * @see JsonElement
	 * 
	 * @author BlackCodec
	 *
	 */
	public static class JsonObject extends JsonElement {

		private Map<String, JsonElement> childs = new TreeMap<>();
		
		/**
		 * Construct an empty JsonObject.
		 */
		public JsonObject() { super(JsonElement.Types.JSON_OBJECT,null); }
		
		/**
		 * Associates the specified JsonNode value with the specified key.
		 * 
		 * @param key key with which the specified value is to be associated
		 * @param value JsonNode object to be associated with the specified key
		 */
		public void add(String key, JsonElement value) { this.childs.put(key, value); }
		
		/**
		 * Returns the JsonNode object to which the specified key is mapped, 
		 * or null if this JsonObject contains no mapping for the key.
		 *  
		 * @param key the key whose associated value is to be returned
		 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
		 * @see Map
		 */
		public JsonElement get(String key) { return this.childs.get(key); }
		
		/**
		 * Removes the mapping for a key from this JsonObject if it is present. 
		 * More formally, if this JsonObject contains a mapping from key k to value v such 
		 * that (key==null ? k==null : key.equals(k)), that mapping is removed. 
		 * (The JsonObject can contain at most one such mapping.)
		 * The JsonObject will not contain a mapping for the specified key once the call returns.
		 * 
		 * @param key key whose mapping is to be removed from the JsonObject
		 */
		public void remove(String key) { 
			if (this.childs.containsKey(key))
				this.childs.remove(key);
		}
		
		/**
		 * Returns a Map that represents all the keys and the associated JsonNode objects, 
		 * or null if this JsonObject contains no JsonNode objects.

		 * @return a Map of keys and JsonNode values contained in JsonObject
		 */
		public Map<String,JsonElement> childs() { return this.childs; }
		
		/**
		 * Returns true if this JsonObject contains a mapping for the specified key. 
		 * More formally, returns true if and only if this JsonObject contains a mapping 
		 * for a key k such that (key==null ? k==null : key.equals(k)). (There can be at most one such mapping.)
		 * 
		 * @param key key whose presence in this JsonObject is to be tested
		 * @return true if this JsonObject contains a mapping for the specified key
		 * @see Map
		 */
		public boolean contains(String key) { return this.childs.containsKey(key); }
		
		/* OVERRIDES */
		/**
		 * Returns true if this JsonObject contains no key-value mappings.
		 * 
		 * @return true if this JsonObject contains no key-value mappings
		 * @see Map
		 */
		@Override
		public boolean isEmpty() { return this.childs != null && !this.childs.isEmpty(); }
		
		/**
		 * Returns the number of key-value mappings in this JsonObject. 
		 * If the JsonObject contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
		 * 
		 * @return the number of key-value mappings in this JsonObject
		 * @see Map
		 */
		@Override
		public int size() { return this.childs.size(); }
		
		@Override
		public String value() { return this.toString(); }
		
		@Override
		public String toString() {
			StringBuilder childBuilder = new StringBuilder();
			for(Entry<String, JsonElement> child: this.childs.entrySet()) {
				childBuilder.append(",").append(String.format("\"%s\"",child.getKey())).append(":").append(child.getValue().toString());
			}
			String child = childBuilder.toString();
			if (child.startsWith(",")) child = child.substring(1);
			return String.format("{%s}", child);
		}
	}

	/**
	 * Class that represent a JSON array.
	 * This class extends JsonNode
	 * 
	 * @see JsonElement
	 * 
	 * @author BlackCodec
	 *
	 */
	public static class JsonArray extends JsonElement {

		private List<JsonElement> childs = new ArrayList<>();
		
		/**
		 * Construct an empty JsonArray.
		 */
		public JsonArray() { super(JsonElement.Types.JSON_ARRAY, null); }
		
		/**
		 * Add a JsonNode object to the array.
		 * 
		 * @param node JsonNode object to add
		 * @see JsonElement
		 */
		public void add(JsonElement node) { this.childs.add(node); }
		
		/**
		 * Add a JsonNode object at specific position.
		 * 
		 * @param index the position
		 * @param node  JsonNode object to add to array
		 * @see ArrayList
		 * @see JsonElement
		 */
		public void add(int index, JsonElement node) { this.childs.add(index, node); }
		
		/**
		 * Return the JsonNode object at specific position in the array.
		 * 
		 * @param index
		 * @return the JsonNode element at the specified position in the list
		 * @see JsonElement
		 * @see ArrayList
		 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
		 */
		public JsonElement get(int index) throws IndexOutOfBoundsException { return this.childs.get(index); }
		
		/**
		 * Return all the JsonNode elements as a list.
		 * 
		 * @return the list of all elements
		 * @see JsonElement
		 * @see List
		 */
		public List<JsonElement> childs() { return this.childs; }
		
		/* OVERRIDES */
		/**
		 * Returns true if the array contains no elements.
		 * 
		 * @return true if this list contains no elements
		 */
		@Override
		public boolean isEmpty() { return this.childs != null && !this.childs.isEmpty(); }
		
		/**
		 * Returns the number of elements in this array. 
		 * If this list contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
		 * 
		 * @return the number of elements in this array
		 * @see List
		 */
		@Override
		public int size() { return this.childs.size(); }
		
		@Override
		public String value() { return this.toString(); }
		
		@Override
		public String toString() {
			StringBuilder childBuilder = new StringBuilder();
			for(JsonElement child: this.childs) {
				childBuilder.append(",").append(child.toString());
			}
			String child = childBuilder.toString();
			if (child.startsWith(",")) child = child.substring(1);
			return String.format("[%s]", child);
		}
	}

}
