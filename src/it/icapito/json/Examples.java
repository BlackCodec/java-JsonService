package it.icapito.json;

import java.util.Map;

import it.icapito.json.JsonService.*;

public class Examples {
	public static void main(String[] args) {
		System.out.println("*** Example 1 *** \nConvert a string to JsonObject\n\n");
		testOne();
		System.out.println("\n\n*** Example 2 *** \nBuild a JsonObject and convert to string\n\n");
		testTwo();
		System.out.println("\n\n*** Example 3 *** \nParse a string to JsonArray\n\n");
		testThree();
	}
	
	public static void testOne() {
		String jsonString = "{\n"
				+ "  \"key1\": \"value as string\",\n"
				+ "  \"key2\": \"second string\",\n"
				+ "  \"key3\": {\n"
				+ "    \"key3.1\": \"value 3.1\",\n"
				+ "    \"key3.2\": \"value 3.2 con fake \\\" escape char!\\\"\"\n"
				+ "  },\n"
				+ "  \"key4\": [ \"a\",\"b\",\n"
				+ "   \"c\",\"d\"],"
				+ " \"key5\": [ 1,7],\n"
				+ "    \"key6\": false,\n"
				+ "    \"key9\": \"false\",\n"
				+ "\"key7\": 2.0,\n"
				+ "\"key8\": [ {\"sk1\":\"sv1\",\"sk2\":1.0, \"sk3\": false},"
				+ "{\"sk1\":\"sv2\",\"sk2\":1.2, \"sk3\": \"true\"}]"
				+ "}";
		JsonObject parsed = (JsonObject) JsonService.parseString(jsonString, 0);
		if (parsed != null && parsed.getType() != JsonElementType.JSON_NULL) {
			for (Map.Entry<String, JsonElement> r: parsed.entrySet())
				System.out.println(r.getKey() + " ("+r.getValue().getType()+") => " + r.getValue().getValueAsString());
			System.out.println("\n* Get key5 * \n");
			JsonArray x = (JsonArray) parsed.getValue("key5");
			for (JsonElement e: x.values()) 
				System.out.println("\t"+e.getType() + " : " + e.getValueAsString());
			System.out.println("\n* Get key8 * \n");
			x = (JsonArray) parsed.getValue("key8");
			for (JsonElement e: x.values()) {
				System.out.println("\t"+e.getType() + " : " + e.getValueAsString());
				JsonObject z = (JsonObject) e;
				for (Map.Entry<String, JsonElement> r: z.entrySet())
					System.out.println("\t\t"+r.getKey() + " ("+r.getValue().getType()+") => " + r.getValue().getValueAsString());
			}
		}
	}
	
	public static void testTwo() {
		JsonObject root = new JsonObject();
		JsonObject obj = new JsonObject();
		obj.addString("key1", "value1");
		obj.addBoolean("key2", false);
		obj.addNumber("key3", "2.1");
		root.addJsonObject("root1", obj);
		obj = new JsonObject();
		obj.addString("key1", "value2");
		obj.addBoolean("key2", "true");
		obj.addInteger("key3", 31);
		root.addJsonObject("root2", obj);
		root.addString("root3","this is a test string with \\\"escapes\\\"");
		JsonArray array = new JsonArray();
		JsonElement record = new JsonElement(JsonElementType.JSON_NUMBER);
		record.setValue("1.1");
		array.addRecord(record);
		record.setValue("2.1");
		array.addRecord(record);
		record.setValue("2.2");
		array.addRecord(record);
		record.setValue("1.4");
		array.addRecord(record);
		root.addJsonArray("root4", array);
		array.clear();
		array.addString("av1 with escapes\\\"");
		array.addInteger(55);
		array.addBoolean(false);
		array.addBoolean("true");
		root.addJsonArray("root4", array);
		System.out.println("Root:\n" + root.getValueAsString());
	}
	
	public static void testThree() {
		String jsonString = "[ \"a\",\"b\",\n"
				+ "   \"c\",\"d\"]";
		JsonArray parsed = (JsonArray) JsonService.parseString(jsonString, 0);
		if (parsed != null && parsed.getType() != JsonElementType.JSON_NULL) {
			System.out.println(parsed.getValueAsString());
			for (JsonElement e: parsed.values())
				System.out.println(e.getType() + " : " + e.getValueAsString());
		}
	}
}
