
package com.baker.gateway.common.util;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FastJsonConvertUtil {

	private static final SerializerFeature[] featuresWithNullValue = {
			SerializerFeature.WriteMapNullValue,
			SerializerFeature.WriteNullBooleanAsFalse,
	        SerializerFeature.WriteNullListAsEmpty,
			SerializerFeature.WriteNullNumberAsZero,
			SerializerFeature.WriteNullStringAsEmpty
	};

	/**
	 * 将JSON字符串转换为实体对象
	 *
	 * @param data JSON字符串
	 * @param clzss 转换对象
	 * @return T
	 */
	public static <T> T convertJSONToObject(String data, Class<T> clzss) {
		try {
            return JSON.parseObject(data, clzss);
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertJSONToObject fail ", e);
			return null;
		}
	}
	
	/**
	 * 将JSONObject对象转换为实体对象
	 *
	 * @param data JSONObject对象
	 * @param clzss 转换对象
	 * @return T
	 */
	public static <T> T convertJSONToObject(JSONObject data, Class<T> clzss) {
		try {
            return JSONObject.toJavaObject(data, clzss);
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertJSONToObject fail ", e);
			return null;
		}
	}

	/**
	 * 将JSON字符串数组转为List集合对象
	 *
	 * @param data JSON字符串数组
	 * @param clzss 转换对象
	 * @return List<T>集合对象
	 */
	public static <T> List<T> convertJSONToArray(String data, Class<T> clzss) {
		try {
            return JSON.parseArray(data, clzss);
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertJSONToArray fail ", e);
			return null;
		}
	}
	
	/**
	 * 将List<JSONObject>转为List集合对象
	 *
	 * @param data List<JSONObject>
	 * @param clzss 转换对象
	 * @return List<T>集合对象
	 */
	public static <T> List<T> convertJSONToArray(List<JSONObject> data, Class<T> clzss) {
		try {
			List<T> t = new ArrayList<T>();
			for (JSONObject jsonObject : data) {
				t.add(convertJSONToObject(jsonObject, clzss));
			}
			return t;
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertJSONToArray fail ", e);
			return null;
		}
	}

	/**
	 * 将对象转为JSON字符串
	 *
	 * @param obj 任意对象
	 * @return JSON字符串
	 */
	public static String convertObjectToJSON(Object obj) {
		try {
            return JSON.toJSONString(obj);
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertObjectToJSON fail ", e);
			return null;
		}
	}
	
	/**
	 * 将对象转为JSONObject对象
	 *
	 * @param obj 任意对象
	 * @return JSONObject对象
	 */
	public static JSONObject convertObjectToJSONObject(Object obj){
		try {
            return (JSONObject) JSONObject.toJSON(obj);
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertObjectToJSONObject fail ", e);
			return null;
		}		
	}


	public static String convertObjectToJSONWithNullValue(Object obj) {
		try {
            return JSON.toJSONString(obj, featuresWithNullValue);
		} catch (Exception e) {
			log.error("#FastJsonConvertUtil# convertObjectToJSONWithNullValue fail ", e);
			return null;
		}
	}


}
