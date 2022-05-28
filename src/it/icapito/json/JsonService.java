/** @Release: 20220528.1825 */
package it.icapito.json;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class JsonService {

	public static class JsonObject extends JsonElement {
		private Map<String, JsonElement> attributes = new TreeMap<>();
		
		public JsonObject() { super(JsonElementType.JSON_OBJECT);}
		
		@Override
		public void addRecord(JsonElement elem) {
			if (elem.getType() != JsonElementType.JSON_NONE)
				this.attributes.put(elem.getKey(),elem);
		}
		@Override
		public String getValueAsString() { 
			StringBuilder b = new StringBuilder("{");
			for (Entry<String, JsonElement> r: this.attributes.entrySet())
				b.append("\""+r.getKey() + "\":" + r.getValue().getValueAsString()).append(",");
			if (b.lastIndexOf(",") == b.length()-1) b.deleteCharAt(b.length()-1);
			b.append("}");
			return b.toString();
		}
		@Override
		public String toString() { return this.getValueAsString(); }
		@Override
		public boolean equals(Object o) {
			if (!super.equals(o))
				return false;
			return ((JsonObject) o).attributes.equals(this.attributes);
		}
		@Override
		public int hashCode() { return this.attributes.hashCode(); }
		public void addString(String key, String value) { this.addRecord(new JsonElement(JsonElementType.JSON_STRING,key,value)); }
		public void addNumber(String key, String value) { this.addRecord(new JsonElement(JsonElementType.JSON_NUMBER,key,value)); }
		public void addInteger(String key, int value) { this.addInteger(key, String.valueOf(value)); }
		public void addInteger(String key, String value) { this.addRecord(new JsonElement(JsonElementType.JSON_INTEGER,key,value)); }
		public void addBoolean(String key, boolean value) { this.addBoolean(key, String.valueOf(value)); }
		public void addBoolean(String key, String value) { this.addRecord(new JsonElement(JsonElementType.JSON_BOOLEAN,key,value)); }
		public void addJsonArray(String key, JsonArray value) { this.attributes.put(key, value); }
		public void addJsonObject(String key, JsonObject value) { this.attributes.put(key, value); }
		public void remove(String key) {  if (this.attributes.containsKey(key)) this.attributes.remove(key); }
		public void clear() { this.attributes.clear(); }
		public boolean containsKey(String key) { return this.attributes.containsKey(key); }
		public boolean isEmpty() { return this.attributes.isEmpty(); }
		public JsonElement getValue(String key) { return this.attributes.get(key); }
		public Set<String> keys() { return this.attributes.keySet(); }
		public Set<Map.Entry<String, JsonElement>> entrySet() { return this.attributes.entrySet(); }
	}
	
	public static class JsonArray extends JsonElement {
		private Set<JsonElement> records = new TreeSet<>();
		
		public JsonArray() { super(JsonElementType.JSON_ARRAY);}
		
		@Override
		public void addRecord(JsonElement elem) {
			if (elem.getType() != JsonElementType.JSON_NONE)
				this.records.add(elem); 
		}
		@Override
		public String getValueAsString() { 
			StringBuilder b = new StringBuilder("[");
			for (JsonElement r: this.records)
				b.append(r.getValueAsString()).append(",");
			if (b.lastIndexOf(",") == b.length()-1) b.deleteCharAt(b.length()-1);
			b.append("]");
			return b.toString();
		}
		@Override
		public String toString() { return this.getValueAsString(); }
		@Override
		public boolean equals(Object o) {
			if (!super.equals(o))
				return false;
			return ((JsonArray) o).records.equals(this.records);
		}
		@Override
		public int hashCode() { return this.records.hashCode(); }
		public JsonElement getValue(int index) { return (JsonElement) (this.records.toArray())[index]; }
		public Set<JsonElement> values() { return this.records; }
		public boolean isEmpty() { return this.records.isEmpty(); }
		public void remove(int index) { 
			JsonElement e = this.getValue(index);
			this.records.remove(e);
		}
		public void clear() { this.records.clear(); }
		public void addString(String value) { this.addRecord(new JsonElement(JsonElementType.JSON_STRING,"",value)); }
		public void addNumber(String value) { this.addRecord(new JsonElement(JsonElementType.JSON_NUMBER,"",value)); }
		public void addInteger(int value) { this.addInteger(String.valueOf(value)); }
		public void addInteger(String value) { this.addRecord(new JsonElement(JsonElementType.JSON_INTEGER,"",value)); }
		public void addBoolean(boolean value) { this.addBoolean(String.valueOf(value)); }
		public void addBoolean(String value) { this.addRecord(new JsonElement(JsonElementType.JSON_BOOLEAN,"",value)); }
		public void addJsonObject(JsonObject value) { this.addRecord(value); }
	}
	
	public static class JsonElement implements Comparable<JsonElement>{
		private int start;
		private int end;
		private JsonElementType type;
		private String key = null;
		private String value = null;
		
		protected JsonElement(JsonElementType type) { this.type = type; }
		protected JsonElement(JsonElementType type,String key, String value) { 
			this(type); 
			this.key = key; 
			this.value=value; 
		}
		public boolean hasKey() { return (this.key != null && !this.key.trim().isEmpty()); }
		public boolean hasValue() { return (this.value != null && !this.value.trim().isEmpty()); }
		public void setStart(int index) { this.start = index; }
		public void setEnd(int index) { this.end = index; }
		public void setKey(String key) { this.key = key; }
		public void setValue(String value) { 
			this.value = value;
			switch(this.value.trim()) {
				case "null":
					this.type = JsonElementType.JSON_NULL;
					break;
				case "true":
				case "false":
					this.type = JsonElementType.JSON_BOOLEAN;
					break;
				default:
					if (this.value.trim().matches("[0-9]*"))
						this.type = JsonElementType.JSON_INTEGER;
					else 
						if (this.value.trim().matches("[0-9.]*"))
							this.type = JsonElementType.JSON_NUMBER;
						else if (!this.value.trim().isEmpty())
							this.type = JsonElementType.JSON_STRING;
					break;
			}
		}
		public void setType(JsonElementType type) { this.type = type; }
		public String getKey() { return this.key; }
		public String getValue() { return this.value; }
		public String getValueAsString() {
			StringBuilder b = new StringBuilder(String.valueOf(this.value));
			if (this.type == JsonElementType.JSON_STRING) {
				b.append('"');
				b.insert(0, '"');
			}
			return b.toString();
		}
		public JsonElementType getType() { return this.type; }
		public int getStartIndex() { return this.start; }
		public int getEndIndex() { return this.end; }
		public void addRecord(JsonElement elem) {
			this.type = elem.getType();
			this.key = elem.getKey();
			this.value = elem.getValue();
		}
		@Override
		public int compareTo(JsonElement o) {
			if (this.hasKey()) return (o.hasKey()?this.getKey().compareTo(o.getKey()):-1);
			if (o.hasKey()) return 1;
			if (this.hasValue()) return (o.hasValue()?this.getValueAsString().compareTo(o.getValueAsString()):-1);
			if (o.hasValue()) return 1;
			return this.getType().compareTo(o.getType());
		}
		@Override
		public boolean equals(Object o) {
			if (o == null || this.getClass() != o.getClass()) return false;
			JsonElement e = (JsonElement) o;
			return (e.getKey().equals(this.getKey()) &&
					e.getType() == this.type && e.getValueAsString().equals(this.getValueAsString()));	
		}
		@Override
		public int hashCode() { return (this.hasKey()?this.key.hashCode():1)*this.type.hashCode()*this.getValueAsString().hashCode(); }
	}
	
	public enum JsonElementType {
		JSON_NONE,
		JSON_STRING,
		JSON_INTEGER,
		JSON_NUMBER,
		JSON_BOOLEAN,
		JSON_NULL,
		JSON_ARRAY,
		JSON_OBJECT;
	}
	
	public static JsonElement parseString(String inputData, int start) {
		JsonElement root = new JsonElement(JsonElementType.JSON_NONE);
		StringBuilder stb = new StringBuilder();
		JsonElement element = new JsonElement(JsonElementType.JSON_NONE);
		String workingData = inputData + " ,";
		boolean isValue = false;
		boolean isString = false;
		boolean isEscape = false;
		while (start < workingData.length()) {
			char currentChar = workingData.charAt(start);
			switch (currentChar) {
				case '\\':
					isEscape = !isEscape;
					stb.append(currentChar);
					break;
				case ':':
					if (isString) stb.append(currentChar);
					else { 
						isValue = true;
						if (stb.length() > 0) stb = new StringBuilder();
					}
					break;
				case ',':
					if (isString) stb.append(currentChar);
					else {
						if (stb.toString().trim().length() > 0 && element.getType() == JsonElementType.JSON_NONE) element.setValue(stb.toString());
						if (element.getType() != JsonElementType.JSON_NONE && element.getType() != JsonElementType.JSON_OBJECT && element.getType() != JsonElementType.JSON_ARRAY) root.addRecord(element);
						element = new JsonElement(JsonElementType.JSON_NONE);
						stb = new StringBuilder();
						isValue = false;
					}
					break;
				case '"':
					if (isEscape) { stb.append(currentChar); isEscape = false; }
					else {
						if (isString) {
							if (root.getType() == JsonElementType.JSON_ARRAY) isValue = true;
							if (isValue) {
								element.setValue(stb.toString());
								element.setType(JsonElementType.JSON_STRING);
								isValue = false;
							}
							else element.setKey(stb.toString());
						} else stb = new StringBuilder();
						isString = !isString;
					}
					break;
				case '{':
					if (isString) stb.append(currentChar);
					else {
						if (root.getType() != JsonElementType.JSON_NONE) {
							JsonElement temp = parseString(inputData, start);
							temp.setKey(element.getKey());
							root.addRecord(temp);
							start = temp.getEndIndex();
						}
						else {
							root = new JsonObject();
							root.setStart(start);
							stb = new StringBuilder();
						}
					}
					break;
				case '}':
					if (isString) stb.append(currentChar);
					else {
						root.addRecord(element);
						root.setEnd(start);
						start = inputData.length();
					}
					break;
				case '[':
					if (isString) stb.append(currentChar);
					else {
						if (root.getType() != JsonElementType.JSON_NONE) {
							JsonElement temp = parseString(inputData, start);
							temp.setKey(element.getKey());
							root.addRecord(temp);
							start = temp.getEndIndex();
						}
						else {
							root = new JsonArray();
							root.setStart(start);
							stb = new StringBuilder();
						}
					}
					break;
				case ']':
					if (isString) stb.append(currentChar);
					else {
						if (stb.length() > 0) element.setValue(stb.toString());
						root.addRecord(element);
						stb = new StringBuilder();
						element = new JsonElement(JsonElementType.JSON_NONE);
						root.setEnd(start);
						start = inputData.length();
					}
					break;
				default:
					if (isString || isValue || ((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9') || currentChar == '.')) stb.append(currentChar);
					break;
			}
			start++;
		}
		return root;
	}	
}