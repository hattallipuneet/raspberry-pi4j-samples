package rest;

import adafruit.io.rest.HttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PostSwitch {

	private static String key = "";

	private String ONOFF_FEED = "onoff";
	private boolean DEBUG = true;

	private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Prompt the user for input, from stdin. Completed on [Return]
	 * @param prompt The prompt
	 * @return the user's input.
	 */
	public static String userInput(String prompt) {
		String retString = "";
		System.out.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e);
			String s;
			try {
				s = userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}

	public void setSwitch(String key, String switchPos) throws Exception {
		String url = "https://io.adafruit.com/api/feeds/" + ONOFF_FEED + "/data";
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-AIO-Key", key);
		JSONObject value = new JSONObject();
		value.put("value", switchPos);
	  System.out.println("Sending " + value.toString(2));
		int httpCode = HttpClient.doPost(url, headers, value.toString());
		if (DEBUG)
			System.out.println("POST Ret:" + httpCode);
	}

	public PostSwitch() {
	}

	public static void main(String... args) {

		PostSwitch.key = System.getProperty("aio.key", "").trim();

		if (PostSwitch.key.trim().isEmpty()) {
			throw new RuntimeException("Require the key as System variables (-Daio.key)");
		}

		boolean switchPos = true;

		try {
			PostSwitch postSwitch = new PostSwitch();

			System.out.println("Hit return to toggle the switch, Q to exit.");
			boolean go = true;
			while (go) {
				String str = userInput("Hit [Return] ");
				if ("Q".equalsIgnoreCase(str)) {
					go = false;
					System.out.println("Bye.");
				} else {
					String data = switchPos ? "ON" : "OFF";
					postSwitch.setSwitch(PostSwitch.key, data);
					switchPos = !switchPos;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
