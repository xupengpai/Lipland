/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.qihoo.plugin.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.qihoo.plugin.core.Log;

/**
 * 反射工具类
 * @author xupengpai
 *
 */
public class RefUtil {
	
	private final static String TAG = RefUtil.class.getSimpleName();
	
	public static boolean instanceOf(Object obj,Class<?> clz){
		
		if(obj == null || clz == null)
			return false;
		
		if(clz.equals(Object.class))
			return true;
		
		Class<?> c = obj.getClass();
		while(!c.equals(clz) && !c.equals(Object.class)){
			c = c.getSuperclass();
		}
		return !c.equals(Object.class);
	}
	
	public static Object callDeclaredMethod(Object obj,String name,Class[] parameterTypes,Object... args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		if(parameterTypes == null){
			parameterTypes = new Class[]{};
			args = new Object[]{};
		}
		
		Method method = null;
		if (method == null) {
			Class<?> clz = obj.getClass();
			do {
				try {
					method = clz
							.getDeclaredMethod(name, parameterTypes);
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				clz = clz.getSuperclass();
			} while (method == null && !clz.equals(Object.class));
		}
		if (method == null) {
			throw new NoSuchMethodException("method:" + name + "(" + parameterTypes + ") dont exists in " + obj.getClass()
					+ " and it's super classes");
		}
		method.setAccessible(true);
		return method.invoke(obj, args);
	}
	
	public static Method getDeclaredMethod(Class<?> clz,String name,Class... parameterTypes) {
		try {
			return clz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e);
		}
		return null;
	}
	
	public static Object callDeclaredMethod(Object obj,Class<?> clz,String name,Class[] parameterTypes,Object[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		if(parameterTypes == null){
			parameterTypes = new Class[]{};
			args = new Object[]{};
		}
		
		Method method = clz.getDeclaredMethod(name, parameterTypes);
		method.setAccessible(true);
		return method.invoke(obj, args);
	}
	
	public static Object callMethod(Object obj,Class<?> clz,String name,Class[] parameterTypes,Object[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		if(parameterTypes == null){
			parameterTypes = new Class[]{};
			args = new Object[]{};
		}
		
		Method method = clz.getMethod(name, parameterTypes);
		method.setAccessible(true);
		return method.invoke(obj, args);
	}

	public static Object getFieldValue(Object obj, String fieldName){
		if(obj == null)
			return null;
		return getFieldValue(obj,obj.getClass(),fieldName);
	}

	public static Object getFieldValue(Object obj, Class<?> clz,String fieldName){
		if(obj == null)
			return null;
		Field field = getField(clz, fieldName);
		if(field != null){
			field.setAccessible(true);
			try {
				return field.get(obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static Object getFieldValue(Field field,Object obj, String fieldName){
		if(field == null)
			return null;
		if(field != null){
			field.setAccessible(true);
			try {
				return field.get(obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static Object getFieldValue(Class cls,String fieldName){
		Field field = getField(cls, fieldName);
		if(field != null){
			field.setAccessible(true);
			try {
				return field.get(cls);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public static boolean copyField(Class<?> clz,String fieldName,Object src,Object dest){
		Field field = getField(clz, fieldName);
		if(field != null){
			field.setAccessible(true);
			
			try {
				Object value = field.get(src);
				field.set(dest, value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e);
				return false;
			}
			return true;
		}
		return false;
	}

	public static boolean setFieldValue(Object obj, Class<?> clz,String fieldName, Object value){
		if(obj == null)
			return false;
		Field field = getField(clz, fieldName);
		if(field != null){
			field.setAccessible(true);
			try {
				field.set(obj, value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static boolean setDeclaredFieldValue(Object obj,String fieldName, Object value){
		if(obj == null)
			return false;
		return setDeclaredFieldValue(obj,obj.getClass(),fieldName,value);
	}

	public static boolean setDeclaredFieldValue(Object obj, Class<?> clz,String fieldName, Object value){
		if(obj == null)
			return false;
		Field field = getDeclaredField(clz, fieldName);
		if(field != null){
			field.setAccessible(true);
			try {
				field.set(obj, value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 保持兼容
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public static boolean setFieldValue(Object obj, String fieldName, Object value){
//		if(obj == null)
//			return false;
//		Field field = getField(obj.getClass(), fieldName);
//		if(field != null){
//			field.setAccessible(true);
//			try {
//				field.set(obj, value);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return false;
//			}
//			return true;
//		}
//		return false;
		return setDeclaredFieldValue(obj,fieldName,value);
	}

	public static void setFieldValueDeep(Object obj, String fieldName, Object value){
		if(obj == null)
			return;
		Class c = obj.getClass();
		while (!c.equals(Object.class)) {
			try {
				Field field = c.getDeclaredField(fieldName);
				if(field != null){
					field.setAccessible(true);
					try {
						field.set(obj, value);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
			}finally{
				c = c.getSuperclass();
			}
		}
	}

	/**
	 * 设置一个对象字段值，包括未继承的父类的值
	 * @param cls
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public static boolean setFieldValue(Class cls, String fieldName, Object value){
		Field field = getField(cls, fieldName);
		if(field != null){
			field.setAccessible(true);
			try {
				field.set(cls, value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 保持兼容，应当废弃
	 * @param c
	 * @param fieldName
	 * @return
	 */
	public static Field getField(Class<?> c, String fieldName){
//		while (!c.equals(Object.class)) {
//			try {
//				return c.getDeclaredField(fieldName);
//			} catch (Exception e) {
//			}finally{
//				c = c.getSuperclass();
//			}
//		}
//		return null;
		return getDeclaredField(c,fieldName);
	}
	
	public static Field getDeclaredField(Class<?> c, String fieldName){
		while (!c.equals(Object.class)) {
			try {
				return c.getDeclaredField(fieldName);
			} catch (Exception e) {
			}finally{
				c = c.getSuperclass();
			}
		}
		return null;
	}



	public static void cloneObject(Object src,Object target){
		cloneObject(src,target,src.getClass());
	}

/**
 * 只克隆空值
 * @param src
 * @param target
 */
	public static void cloneObjectIfNull(Object src,Object target){
		cloneObject(src,target,src.getClass());
	}



	public static void cloneObject(Object src,Object target,Class clz){
		try {
		    do{
		    	Field[] fields = clz.getDeclaredFields();
				for(Field field : fields){
					field.setAccessible(true);
					Object value = field.get(src);
					try {
						field.set(target, value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				clz = clz.getSuperclass();
			}while(!clz.equals(Object.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



//	public static void cloneObject2(Object src,Object target,Class clz){
//		try {
//		    do{
//		    	Field[] fields = clz.getFields();
//				for(Field field : fields){
//					field.setAccessible(true);
//					Object value = field.get(src);
//					try {
//						field.set(target, value);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				clz = clz.getSuperclass();
//			}while(!clz.equals(Object.class));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
	public static void printFields(Class cls){
		Field[] fs = cls.getFields();
		System.out.println("-----------Fields-------------");
		for(Field f : fs){
			System.out.println(f.getName());
		}
		System.out.println("-----------DeclaredFields-------------");
		fs = cls.getDeclaredFields();
		for(Field f : fs){
			System.out.println(f.getName());
		}
		
	}
	
	public static Class<?>[] getInterfaces(Class<?> clz){
		List<Class<?>> list = new ArrayList<>();
		
		while(!clz.equals(Object.class)){
			for(Class<?> c : clz.getInterfaces()){
				list.add(c);
			}
			clz = clz.getSuperclass();
		}
		return list.toArray(new Class<?>[]{});
	}
	
//	public static Object cloneObjectDeep(Object obj){
//		Class cls = obj.getClass();
//		if(!cls.equals(int.class)
//				&&!cls.equals(byte.class)
//				&&!cls.equals(boolean.class)
//				&&!cls.equals(short.class)
//				&&!cls.equals(long.class)
//				&&!cls.equals(Long.class)
//				&&!cls.equals(String.class)
//				&&!cls.equals(Object.class)	
//				&&!cls.equals(Boolean.class)	
//				)
//		try {
//		    do{
//		    	Field[] fields = clz.getDeclaredFields();
//				for(Field field : fields){
//					field.setAccessible(true);
//					Object value = field.get(src);
//					field.set(target, value);
//				}
//				clz = clz.getSuperclass();
//			}while(!clz.equals(Object.class));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
