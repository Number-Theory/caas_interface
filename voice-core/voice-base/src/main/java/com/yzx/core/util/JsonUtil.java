package com.yzx.core.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
	public static String toJsonStr(Object obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(obj);
	}

	public static List<Map<String, Object>> jsonStrToArray(String str) {
		JSONArray array = toJsonArray(str);
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		Map<String, Object> data = null;
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = JsonUtil.toJsonObj(array.get(i).toString());
			data = new HashMap<String, Object>();
			Set<String> set = json.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				data.put(key, json.get(key));
			}
			dataList.add(data);
		}
		return dataList;
	}

	public static Map<String, Object> jsonStrToMap(String str) {
		if (str == null) {
			return null;
		}
		JSONObject json = toJsonObj(str);
		Map<String, Object> data = new HashMap<String, Object>();
		Set<String> set = json.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			data.put(key, json.get(key));
		}
		return data;
	}

	public static Object fromJson(String num, Object o) {
		Gson gson = new Gson();
		return gson.fromJson(num, o.getClass());
	}

	public static <T> T fromJson(String num, Type o) {
		Gson gson = new Gson();
		return gson.fromJson(num, o);
	}

	public static JSONObject toJsonObj(String str) throws JSONException {
		JSONObject jb = JSONObject.parseObject(str);
		return jb;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String ArrayToJsonStr(List list) {
		String jsonStr = null;
		if (list != null && list.size() > 0) {
			JSONArray json = new JSONArray(list);
			jsonStr = json.toString();
		}
		return jsonStr;
	}

	public static JSONArray toJsonArray(String str) {
		if (str == null) {
			return null;
		}
		JSONArray array = JSONArray.parseArray(str);
		return array;
	}
}
