/**
 * 
 */
package com.hesine.util;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeWriter;

/**
 * @author pineapple
 *
 */
public class JSONTool {
	public static String getJSONStringExceptField(Object object, String... excludeFields){
		if(excludeFields!=null){
			final List<String> excludeList = Arrays.asList(excludeFields);
			PropertyFilter filter = new PropertyFilter(){
				@Override
				public boolean apply(Object source, String name, Object value) {
					if(excludeList.contains(name)){
						return false;
					}
					return true;
				}
				
			};
			SerializeWriter sw = new SerializeWriter();
			JSONSerializer serializer = new JSONSerializer(sw);
			serializer.getPropertyFilters().add(filter);
			serializer.write(object);
			return sw.toString();
		}
		return JSON.toJSONString(object);
	}
}
