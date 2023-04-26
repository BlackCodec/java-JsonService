package it.icapito.json;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import it.icapito.json.JsonService.*;

public class Examples {
	
	// change this value to true to append standard java console logger to json service in verbose mode 
	public static final boolean ENABLE_LOGGER = false;
	
	public static void main(String[] args) {
		if (ENABLE_LOGGER) {
			// Create formatter for log output
			SimpleFormatter formatter = new SimpleFormatter() {
					private static final String FORMAT = "<%s> [%s] %s.%s(): %s %n";
					@Override
					public synchronized String format(LogRecord logRecord) {
						return String.format(FORMAT,new java.util.Date(),logRecord.getLevel().getName(),
								logRecord.getSourceClassName(), logRecord.getSourceMethodName(),
								logRecord.getMessage());
					}
			};
			// create a new instance of logger
			Logger logger = Logger.getLogger("Example");
			logger.setLevel(Level.FINEST);
			Handler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.FINEST);
			consoleHandler.setFormatter(formatter);
			logger.addHandler(consoleHandler);
			JsonService.appendLogger(logger);
		}
		System.out.println("*** Example 1 *** \nConvert a string to JsonObject\n\n");
		testOne();
		System.out.println("\n\n*** Example 2 *** \nBuild a JsonObject and convert to string\n\n");
		testTwo();
		System.out.println("\n\n*** Example 3 *** \nParse a string to JsonArray\n\n");
		testThree();
	}
	
	private static final String ELEMENT_FORMAT = "- %s => (%s) %s";
	
	private static void testOne() {
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
				+ "{\"sk1\":\"sv2\",\"sk2\":1.2, \"sk3\": \"true\"}], \"keynum\" : 100"
				+ "}";
		System.out.println("Json string:\n" + jsonString);
		JsonObject parsed = JsonService.parse(jsonString);
		for (Map.Entry<String, JsonElement> r: parsed.childs().entrySet())
				System.out.println(String.format(ELEMENT_FORMAT, r.getKey(),r.getValue().type().name(), r.getValue().value()));
		System.out.println("\n* Get key5 * \n");
		JsonArray x = (JsonArray) parsed.get("key5");
		for (JsonElement e: x.childs()) 
			System.out.println("\t"+e.type().name() + " : " + e.value());
		System.out.println("\n* Get key8 * \n");
		x = (JsonArray) parsed.get("key8");
		for (JsonElement e: x.childs()) {
			System.out.println("\t"+e.type().name() + " : " + e.value());
		JsonObject z = (JsonObject) e;
		for (Map.Entry<String, JsonElement> r: z.childs().entrySet())
			System.out.println("\t\t"+String.format(ELEMENT_FORMAT, r.getKey(),r.getValue().type().name(),r.getValue().value()));
		}
	}
	
	public static void testTwo() {
		JsonObject root = new JsonObject();
		JsonObject obj = new JsonObject();
		obj.add("key1", new JsonElement(JsonElement.Types.JSON_STRING, "value1"));
		obj.add("key2", new JsonElement(JsonElement.Types.JSON_BOOLEAN,"false"));
		obj.add("key3", new JsonElement(JsonElement.Types.JSON_NUMBER,"2.1"));
		root.add("root1", obj);
		obj = new JsonObject();
		obj.add("key1", new JsonElement(JsonElement.Types.JSON_STRING,"value2"));
		obj.add("key2", new JsonElement(JsonElement.Types.JSON_BOOLEAN,"true"));
		obj.add("key3", new JsonElement(JsonElement.Types.JSON_NUMBER,"31"));
		root.add("root2", obj);
		root.add("root3",new JsonElement(JsonElement.Types.JSON_STRING,"this is a test string with \\\"escapes\\\""));
		JsonArray array = new JsonArray();
		JsonElement record = new JsonElement(JsonElement.Types.JSON_NUMBER,"1.1");
		array.add(record);
		array.add(new JsonElement(JsonElement.Types.JSON_NUMBER,"2.1"));
		array.add(new JsonElement(JsonElement.Types.JSON_NUMBER,"2.2"));
		array.add(new JsonElement(JsonElement.Types.JSON_NUMBER,"1.4"));
		root.add("root4", array);
		array = new JsonArray();
		array.add(new JsonElement(JsonElement.Types.JSON_STRING,"av1 with escapes\\\""));
		array.add(new JsonElement(JsonElement.Types.JSON_NUMBER,"55"));
		array.add(new JsonElement(JsonElement.Types.JSON_BOOLEAN,"false"));
		root.add("root4", array);
		System.out.println("Root:\n" + root.toString());
	}
	
	public static void testThree() {
		String jsonString = "{ \"elements\": [ \"a\",\"b\",\n"
				+ "   \"c\",\"d\"] }";
		System.out.println("Source json: " + jsonString);
		JsonArray parsed = (JsonArray) ((JsonObject) JsonService.parse(jsonString)).get("elements");
		System.out.println("JsonService -> parse string -> get \"elements\" as json array and print");
		if (parsed != null && parsed.type() == JsonElement.Types.JSON_ARRAY) {
			System.out.println(parsed.toString());
			for (JsonElement e: parsed.childs())
				System.out.println(e.type().name() + " : " + e.value());
		}
	}
}
