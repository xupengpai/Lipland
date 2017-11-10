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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.qihoo.plugin.bean.LibInfo;
import com.qihoo.plugin.core.Log;

/**
 * Apk相关的工具类
 * @author xupengpai
 * @date 2015年12月3日 下午5:28:32
 */
public class ApkUtil {
	
	private final static String TAG = InternalUtil.class
			.getSimpleName();



	public static boolean copy(InputStream in, String target)
			throws IOException {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(new File(target));
			byte[] buf = new byte[1024 * 4];
			int c = 0;
			while ((c = in.read(buf)) > 0) {

				// for(int i=0;i<c;i++)
				// buf[i] += 20;

				out.write(buf, 0, c);
			}
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					Log.e("unzipApkLibs", "copy exception", e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return true;
	}

	private static void copyFile(InputStream in, String targetPath)
			throws IOException {
		File target = new File(targetPath);
		target.getParentFile().mkdirs();
		IO.copy(in, targetPath);
	}

	private static String getFileName(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

	// public static void unzipApkLibs(String path,String tmpPath,String
	// libPath, String abi) {
	//
	// FileInputStream fis = null;
	// new File(libPath).mkdirs();
	//
	// try {
	// List<ZipEntry> soEntryList = new ArrayList<ZipEntry>();
	// List<ZipEntry> v7aSoEntryList = new ArrayList<ZipEntry>();
	// ZipEntry entry = null;
	// fis = new FileInputStream(path);
	// ZipInputStream zis = new ZipInputStream(
	// new BufferedInputStream(fis));
	//
	// boolean isv7aAbi = abi.equals("armeabi-v7a");
	//
	// while ((entry = zis.getNextEntry()) != null) {
	// if (!entry.isDirectory()){
	// String name = entry.getName();
	// if(isv7aAbi && name.startsWith("lib/armeabi-v7a/")) {
	// copyFile(zis,tmpPath + "/" + name);
	// v7aSoEntryList.add(entry);
	// }else if(name.startsWith("lib/armeabi/")){
	// copyFile(zis,tmpPath + "/" + name);
	// soEntryList.add(entry);
	// }
	// }
	//
	// }
	// Log.e("unzipApkLibs", "path="+path);
	// Log.e("unzipApkLibs", "tmpPath="+tmpPath);
	// Log.e("unzipApkLibs", "libPath="+libPath);
	// Log.e("unzipApkLibs", "abi="+abi);
	// Log.e("unzipApkLibs", "isv7aAbi="+isv7aAbi);
	//
	// for(ZipEntry soEntry: soEntryList){
	// entry = soEntry;
	// if(isv7aAbi){
	// for(ZipEntry v7aSoEntry: v7aSoEntryList){
	// String v7aFileName = getFileName(v7aSoEntry.getName());
	// String fileName = getFileName(entry.getName());
	//
	// if(fileName.equals(v7aFileName)){
	// entry = v7aSoEntry;
	// break;
	// }
	// }
	// }
	//
	// String name = entry.getName();
	//
	// File srcFile = new File(tmpPath + "/" + name);
	// File libFile = new File(libPath + "/" + srcFile.getName());
	//
	// Log.e("unzipApkLibs", "srcFile="+srcFile.getAbsolutePath());
	// Log.e("unzipApkLibs", "libFile="+libFile.getAbsolutePath());
	// Log.e("unzipApkLibs", "libFile.length()="+libFile.length());
	// Log.e("unzipApkLibs", "srcFile.length()="+srcFile.length());
	// if(libFile.length() != srcFile.length()){
	// IO.copy(srcFile.getAbsolutePath(), libFile.getAbsolutePath());
	// System.out.println(name);
	// }
	// }
	//
	// zis.close();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }finally{
	// if(fis != null){
	// try {
	// fis.close();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	private static boolean findZipEntry(List<ZipEntry> list, ZipEntry zipEntry) {
		return list.contains(zipEntry);
	}

	// 要临时目录，混合so版本
	public static void unzipApkLibs(String path, String tmpPath,
			String libPath, String abi) {

		FileInputStream fis = null;
		new File(libPath).mkdirs();

		try {
			List<ZipEntry> soEntryList = new ArrayList<ZipEntry>();
			List<ZipEntry> v7aSoEntryList = new ArrayList<ZipEntry>();
			ZipEntry entry = null;
			fis = new FileInputStream(path);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));

			boolean isv7aAbi = abi.equals("armeabi-v7a");

			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String name = entry.getName();
					if (isv7aAbi && name.startsWith("lib/armeabi-v7a/")) {

						copyFile(zis, tmpPath + "/" + name);
						v7aSoEntryList.add(entry);
					} else if (name.startsWith("lib/armeabi/")) {
						copyFile(zis, tmpPath + "/" + name);
						soEntryList.add(entry);
					}
				}

			}
			Log.i("unzipApkLibs", "path=" + path);
			Log.i("unzipApkLibs", "tmpPath=" + tmpPath);
			Log.i("unzipApkLibs", "libPath=" + libPath);
			Log.i("unzipApkLibs", "abi=" + abi);
			Log.i("unzipApkLibs", "isv7aAbi=" + isv7aAbi);
			Log.i("unzipApkLibs",
					"v7aSoEntryList.size()=" + v7aSoEntryList.size());

			if (isv7aAbi) {
				for (ZipEntry v7aSoEntry : v7aSoEntryList) {
					String name = v7aSoEntry.getName();

					File srcFile = new File(tmpPath + "/" + name);
					File libFile = new File(libPath + "/" + srcFile.getName());

					Log.i("unzipApkLibs",
							"isv7aAbi-srcFile=" + srcFile.getAbsolutePath());
					Log.i("unzipApkLibs",
							"isv7aAbi-libFile=" + libFile.getAbsolutePath());
					if (libFile.length() != srcFile.length()) {
						Log.i("unzipApkLibs",
								"-----------------------------------------");
						InputStream in = new FileInputStream(srcFile);
						copyFile(in, libFile.getAbsolutePath());
						in.close();
						// IO.copy(srcFile.getAbsolutePath(),
						// libFile.getAbsolutePath());
					}
				}
			}

			for (ZipEntry soEntry : soEntryList) {
				if ((isv7aAbi && !findZipEntry(v7aSoEntryList, soEntry))
						|| !isv7aAbi) {
					String name = soEntry.getName();

					File srcFile = new File(tmpPath + "/" + name);
					File libFile = new File(libPath + "/" + srcFile.getName());

					Log.i("unzipApkLibs",
							"srcFile=" + srcFile.getAbsolutePath());
					Log.i("unzipApkLibs",
							"libFile=" + libFile.getAbsolutePath());
					Log.i("unzipApkLibs",
							"srcFile.length()=" + srcFile.length());
					Log.i("unzipApkLibs",
							"libFile.length()=" + libFile.length());

					if (libFile.length() != srcFile.length()) {
						Log.i("unzipApkLibs",
								"---------------------------------copy");
						InputStream in = new FileInputStream(srcFile);
						copyFile(in, libFile.getAbsolutePath());
						// IO.copy(srcFile.getAbsolutePath(),
						// libFile.getAbsolutePath()+".jpg");
						Log.i("unzipApkLibs",
								"----------copy-srcFile.exists()="
										+ srcFile.exists());
						Log.i("unzipApkLibs",
								"----------copy-libFile.exists()="
										+ libFile.exists());
					}
				}
			}

			// for(ZipEntry soEntry: soEntryList){
			// entry = soEntry;
			// if(isv7aAbi){
			// for(ZipEntry v7aSoEntry: v7aSoEntryList){
			// String v7aFileName = getFileName(v7aSoEntry.getName());
			// String fileName = getFileName(entry.getName());
			//
			// if(fileName.equals(v7aFileName)){
			// entry = v7aSoEntry;
			// break;
			// }
			// }
			// }
			//
			// String name = entry.getName();
			//
			// File srcFile = new File(tmpPath + "/" + name);
			// File libFile = new File(libPath + "/" + srcFile.getName());
			//
			// Log.e("unzipApkLibs", "srcFile="+srcFile.getAbsolutePath());
			// Log.e("unzipApkLibs", "libFile="+libFile.getAbsolutePath());
			// Log.e("unzipApkLibs", "libFile.length()="+libFile.length());
			// Log.e("unzipApkLibs", "srcFile.length()="+srcFile.length());
			// if(libFile.length() != srcFile.length()){
			// IO.copy(srcFile.getAbsolutePath(), libFile.getAbsolutePath());
			// System.out.println(name);
			// }
			// }

			zis.close();

		} catch (Exception e) {
			Log.e("unzipApkLibs", "copy exception", e);
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					Log.e("unzipApkLibs", "copy exception", e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// 不要临时目录，完全分开so版本
	public static void unzipApkLibs2(String path, String libPath) {

		FileInputStream fis = null;
		new File(libPath).mkdirs();

		try {
			ZipEntry entry = null;
			fis = new FileInputStream(path);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));

			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String name = entry.getName();

					// 不允许此类相对路径，避免系统的安全漏洞被利用
					if (name.indexOf("../") >= 0) {
						continue;
					}

					if (name.startsWith("lib/armeabi-v7a/")
							|| name.startsWith("lib/armeabi/")) {
						String libFile = libPath + "/" + name;// .replace("-v7a",
																// "");
						copyFile(zis, libFile);
						Log.e("unzipApkLibs", "----------copy-libPath="
								+ libFile);
						Log.e("unzipApkLibs",
								"----------copy-libFile.exists()="
										+ new File(libFile).exists());
					}
				}

			}
		} catch (Exception e) {
			Log.e(e);
			e.printStackTrace();
		}
	}

	
	private static boolean libExists(Map<String,Map<String,LibInfo>> libs,String name){
		if(libs != null){
			for(Map<String,LibInfo> infos : libs.values()){
				for(LibInfo info : infos.values()){
					if(TextUtils.equals(info.name, name)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// 不要临时目录，完全分开so版本
	/**
	 * 解压插件的so文件，如果有重名so文件，则用tag重命名，避免无法加载
	 * @param tag
	 * @param path
	 * @param libPath
	 */
	public static void unzipApkLibs3(Map<String,Map<String,LibInfo>> libs,String tag,String path, String libPath,boolean overwrite) {

		FileInputStream fis = null;
		new File(libPath).mkdirs();
		
		try {
			ZipEntry entry = null;
			fis = new FileInputStream(path);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));

			Map<String,LibInfo> infos = new HashMap<String, LibInfo>();
			
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String name = entry.getName();

					// 不允许此类相对路径，避免系统的安全漏洞被利用
					if (name.indexOf("../") >= 0) {
						continue;
					}

					if (name.startsWith("lib/armeabi-v7a/")
							|| name.startsWith("lib/armeabi/")) {
						String fileName = name.substring(name.lastIndexOf('/')+1);
						//去掉lib开头，.so结尾
						String libName = fileName.substring(3, fileName.length()-3);
						LibInfo info = new LibInfo();
						info.name = libName;
						info.mappingName = libName;
						if(libExists(libs,libName)){
							info.mappingName = tag+"_"+libName;
							name = name.substring(0,name.lastIndexOf('/')+1)+"lib"+info.mappingName+".so";
						}
						info.fileName = name;
						
						String libFile = libPath + "/" + name;// .replace("-v7a",
																// "");

						if(!new File(libFile).exists() || overwrite) {
							copyFile(zis, libFile);
						}
						
						info.path = libFile;
						info.tag = tag;
						
						infos.put(libName, info);
						
//						Log.e("unzipApkLibs", "----------copy-libPath="
//								+ libFile);
//						Log.e("unzipApkLibs",
//								"----------copy-libFile.exists()="
//										+ new File(libFile).exists());
					}
				}

			}
			libs.put(tag, infos);
		} catch (Exception e) {
			Log.e(e);
			e.printStackTrace();
		}finally {
			if(fis != null){
				try {
					fis.close();
				} catch (IOException ignored) {
					Log.e(ignored);
				}
			}
		}
	}
	
	private static Package collectCertificates(Class PackageParser_class,Object parser,PackageParser.Package pkg){

		Method m_collectCertificates = null;
		Method m_collectManifestDigest = null;
		
		try{
			m_collectCertificates = PackageParser_class.getMethod("collectCertificates", PackageParser.Package.class,int.class);
		}catch(Throwable thr){
			thr.printStackTrace();
		}

		try{
			m_collectManifestDigest = PackageParser_class.getMethod("collectManifestDigest", PackageParser.Package.class);
		}catch(Throwable thr){
			thr.printStackTrace();
		}

		if(m_collectCertificates != null){
			try {
				m_collectCertificates.invoke(parser, pkg,0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(m_collectManifestDigest != null){
			try {
				m_collectManifestDigest.invoke(parser, pkg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pkg;
	}
	public final static int PARSE_COLLECT_CERTIFICATES = 1<<8;
	private static Package parseApkInfoForV5_0(String path) {

		Class<?> PackageParser_class;
		try {
			PackageParser_class = Class
					.forName("android.content.pm.PackageParser");
			Constructor<?> constructor = PackageParser_class
					.getDeclaredConstructor(new Class[] {});
			Object packageParser = constructor.newInstance();
			PackageParser parser = (PackageParser) packageParser;

			PackageParser.Package pkg = (PackageParser.Package) RefUtil
					.callDeclaredMethod(parser, PackageParser_class,
							"parsePackage", new Class[] { File.class, int.class },
							new Object[] { new File(path), 0 });
			
			collectCertificates(PackageParser_class,packageParser,pkg);
/*
 public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
3130        final PackageParser parser = new PackageParser();
3131        final File apkFile = new File(archiveFilePath);
3132        try {
3133            PackageParser.Package pkg = parser.parseMonolithicPackage(apkFile, 0);
3134            if ((flags & GET_SIGNATURES) != 0) {
3135                parser.collectCertificates(pkg, 0);
3136                parser.collectManifestDigest(pkg);
3137            }

2131        if ((flags & GET_SIGNATURES) != 0) {
2132            packageParser.collectCertificates(pkg, 0);

3138            PackageUserState state = new PackageUserState();
3139            return PackageParser.generatePackageInfo(pkg, null, flags, 0, 0, null, state);
3140        } catch (PackageParserException e) {
3141            return null;
3142        }
3143    }
 
 */
			// try {
			// PackageParser.Package pkg = parser.parseMonolithicPackage(apkFile,
			// 0);
			// if ((flags & GET_SIGNATURES) != 0) {
			// parser.collectCertificates(pkg, 0);
			// parser.collectManifestDigest(pkg);
			// }
			return pkg;
		} catch (Exception e) {
			Log.e(TAG, e);
		}

		return null;

	}

	private static Package parseApkInfoV4_0(String path) {

		/*
		1    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
2122        PackageParser packageParser = new PackageParser(archiveFilePath);
2123        DisplayMetrics metrics = new DisplayMetrics();
2124        metrics.setToDefaults();
2125        final File sourceFile = new File(archiveFilePath);
2126        PackageParser.Package pkg = packageParser.parsePackage(
2127                sourceFile, archiveFilePath, metrics, 0);
2128        if (pkg == null) {
2129            return null;
2130        }
2131        if ((flags & GET_SIGNATURES) != 0) {
2132            packageParser.collectCertificates(pkg, 0);
2133        }
2134        return PackageParser.generatePackageInfo(pkg, null, flags, 0, 0);
2135    }
2136
		 */
		PackageParser packageParser = new PackageParser(path);
		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		final File sourceFile = new File(path);
		PackageParser.Package pkg = packageParser.parsePackage(sourceFile,
				path, metrics, 0);

		collectCertificates(PackageParser.class,packageParser,pkg);
		
		return pkg;

	}

	public static Package parseApkInfo(String path) {
		try{
			//大於或等於5.0版本
			if(VERSION.SDK_INT >= 21){
				return parseApkInfoForV5_0(path);
			}else{
				return parseApkInfoV4_0(path);
			}
		}catch(Throwable thr){
			Log.e(TAG, thr);
		}
		return null;
		
	}

    private static boolean copyNeeded(int flags, Package p, Bundle metaData) {
//        if (p.mSetEnabled != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
//            boolean enabled = p.mSetEnabled == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
//            if (p.applicationInfo.enabled != enabled) {
//                return true;
//            }
//        }
//        if ((flags & PackageManager.GET_META_DATA) != 0
//                && (metaData != null || p.mAppMetaData != null)) {
//            return true;
//        }
//        if ((flags & PackageManager.GET_SHARED_LIBRARY_FILES) != 0
//                && p.usesLibraryFiles != null) {
//            return true;
//        }
//        return false;
    	return false;
    }
    
    public static final ActivityInfo generateActivityInfo(android.content.pm.PackageParser.Activity a,ApplicationInfo appInfo,
            int flags) {
        if (a == null) return null;
//        if (!copyNeeded(flags, a.owner, a.metaData)) {
//            return a.info;
//        }
//        // Make shallow copies so we can store the metadata safely
//        ActivityInfo ai = new ActivityInfo(a.info);
//        ai.metaData = a.metaData;
//        ai.applicationInfo = appInfo;
//        return ai;
        return a.info;
    }
    
    public static PackageInfo generatePackageInfo(PackageParser.Package p,int flags) {

        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.versionCode = p.mVersionCode;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = p.applicationInfo;
//        pi.signatures = p.mSignatures;

        if ((flags&PackageManager.GET_CONFIGURATIONS) != 0)
        {
            int N = p.configPreferences.size();
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                p.configPreferences.toArray(pi.configPreferences);
            }
            N = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N > 0) {
                pi.reqFeatures = new FeatureInfo[N];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
        }
        if ((flags&PackageManager.GET_ACTIVITIES) != 0) 
        {
            int N = p.activities.size();
            if (N > 0) {
                if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) 
                {
                    pi.activities = new ActivityInfo[N];
                } 
                else 
                {
                    int num = 0;
                    for (int i=0; i<N; i++) {
                        if (p.activities.get(i).info.enabled) num++;
                    }
                    pi.activities = new ActivityInfo[num];
                }
                for (int i=0, j=0; i<N; i++) {
                    final android.content.pm.PackageParser.Activity activity = p.activities.get(i);
                    if (activity.info.enabled
                        || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                        pi.activities[j++] = generateActivityInfo(p.activities.get(i),p.applicationInfo, flags);
                    }
                }
            }
        }
        if ((flags&PackageManager.GET_RECEIVERS) != 0) {
            int N = p.receivers.size();
            if (N > 0) {
                if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                    pi.receivers = new ActivityInfo[N];
                } else {
                    int num = 0;
                    for (int i=0; i<N; i++) {
                        if (p.receivers.get(i).info.enabled) num++;
                    }
                    pi.receivers = new ActivityInfo[num];
                }
                for (int i=0, j=0; i<N; i++) {
                    final android.content.pm.PackageParser.Activity activity = p.receivers.get(i);
                    if (activity.info.enabled
                        || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                        pi.receivers[j++] = generateActivityInfo(p.receivers.get(i),p.applicationInfo, flags);
                    }
                }
            }
        }
        if ((flags&PackageManager.GET_SERVICES) != 0) {
            int N = p.services.size();
            if (N > 0) {
                if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                    pi.services = new android.content.pm.ServiceInfo[N];
                } else {
                    int num = 0;
                    for (int i=0; i<N; i++) {
                        if (p.services.get(i).info.enabled) num++;
                    }
                    pi.services = new android.content.pm.ServiceInfo[num];
                }
                for (int i=0, j=0; i<N; i++) {
                    final android.content.pm.PackageParser.Service service = p.services.get(i);
                    if (service.info.enabled
                        || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                        pi.services[j++] = p.services.get(i).info;//generateServiceInfo(p.services.get(i), flags);
                    }
                }
            }
        }
        if ((flags&PackageManager.GET_PROVIDERS) != 0) {
            int N = p.providers.size();
            if (N > 0) {
                if ((flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                    pi.providers = new android.content.pm.ProviderInfo[N];
                } else {
                    int num = 0;
                    for (int i=0; i<N; i++) {
                        if (p.providers.get(i).info.enabled) num++;
                    }
                    pi.providers = new android.content.pm.ProviderInfo[num];
                }
                for (int i=0, j=0; i<N; i++) {
                    final android.content.pm.PackageParser.Provider provider = p.providers.get(i);
                    if (provider.info.enabled
                        || (flags&PackageManager.GET_DISABLED_COMPONENTS) != 0) {
                        pi.providers[j++] = p.providers.get(i).info;//generateProviderInfo(p.providers.get(i), flags);
                    }
                }
            }
        }
        if ((flags&PackageManager.GET_INSTRUMENTATION) != 0) {
            int N = p.instrumentation.size();
            if (N > 0) {
                pi.instrumentation = new android.content.pm.InstrumentationInfo[N];
                for (int i=0; i<N; i++) {
                    pi.instrumentation[i] = p.instrumentation.get(i).info;//generateInstrumentationInfo(p.instrumentation.get(i), flags);
                }
            }
        }
        if ((flags&PackageManager.GET_PERMISSIONS) != 0) {
            int N = p.permissions.size();
            if (N > 0) {
                pi.permissions = new android.content.pm.PermissionInfo[N];
                for (int i=0; i<N; i++) {
                    pi.permissions[i] = p.permissions.get(i).info;//generatePermissionInfo(p.permissions.get(i), flags);
                }
            }
            N = p.requestedPermissions.size();
            if (N > 0) {
                pi.requestedPermissions = new String[N];
                for (int i=0; i<N; i++) {
                    pi.requestedPermissions[i] = p.requestedPermissions.get(i);
                }
            }
        }
        if ((flags&PackageManager.GET_SIGNATURES) != 0) {
           int N = (p.mSignatures != null) ? p.mSignatures.length : 0;
           if (N > 0) {
                pi.signatures = new Signature[N];
                System.arraycopy(p.mSignatures, 0, pi.signatures, 0, N);
            }
        }
        return pi;
    }
    
    /**
     * 获取apk文件的包名
     * 该函数主要是用来保证apk都能得到自身正确的包名，便于后续查询和操作
     * 该函数解析一个apk，目的只为了得到一个包名，而这个apk其他地方也会解析，所以相对来说，很浪费且效率较低，能不用就不用
     * 如果可以保证插件配置(plugins.xml)和更新配置(update.xml)都能正确的填写包名的话，此处代码注释掉，直接修改为返回null，可以提高效率
     * 
     * @param context
     * @param path
     * @return
     */
    public static String getApkPackageName(Context context,String path){
//    	try{
//	    	PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(path, 0);
//	    	if(pi != null){
//	    		return pi.packageName;
//	    	}
//    	}catch(Exception e){
//    		e.printStackTrace();
//    	}
    	return null;
    }
	
}
