package com.motey.tcfile.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTranslater {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	public static <T> T toObject(String json, Class<T> cls) throws Exception {
      return (T)mapper.readValue(json, cls);
	}
	
	public static String toJson(Object obj) throws Exception {
		String str = mapper.writeValueAsString(obj);
//		str = str.replace("\\\"", "\"");
		return str;
	}
}
