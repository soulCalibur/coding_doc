/*
 * @Project Name: coding_doc
 * @File Name: Install.java
 * @Package Name: com.ht.install
 * @Date: 2017-4-19涓婂崍11:57:32
 * @Creator: bb.h
 * @line------------------------------
 * @淇敼浜�
 * @淇敼鏃堕棿:
 * @淇敼鍐呭:
 */

package com.ht.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * @description TODO
 * @author bb.h
 * @date 2017-4-19涓婂崍11:57:32
 * @see
 */
public class Install {
	private static boolean debug=true;
	public static void main(String[] args) throws Exception {
		String profile = "profile.properties";
		if (args != null && args.length == 1) {
			profile = args[0];
		}
		Properties properties=new LinkedProperties();
		properties.load(Install.class.getClassLoader().getResourceAsStream(profile));
		String home=(String)properties.get("HOME");
		if("false".equals(properties.get("DEBUG"))){
			debug=false;
		}
		if(home!=null){
			if(!home.endsWith("\\")){
				home=home+"\\";
			}
		}
		List<String> changesPath=new ArrayList<>();
		LinkedHashMap<String,String> sets=new LinkedHashMap<>();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			if(((String)entry.getKey()).equals("HOME")){
					continue;
			}else if(((String)entry.getKey()).startsWith("PATH.")){
				changesPath.add(((String)entry.getValue()));
			}else {
				sets.put(((String)entry.getKey()),(String)entry.getValue());
			}
		}
		
		for (Entry<String, String> kv : sets.entrySet()) {
			String value=kv.getValue();
			if(home!=null&&!value.startsWith("%")){
				value=home+value;
			}
			System.out.println(setingConfig(kv.getKey(),value));
		}
		System.out.println(setingConfig("path", reloads(changesPath.toArray(new String[0]))));
		run("taskkill /im explorer.exe /f ");
		Runtime.getRuntime().exec("cmd /c start c:\\windows\\explorer.exe");
		System.exit(0);
	}

	private static String reloads(String[] pathsAddOrChange) {
		String path = getSetingConfig("path");
		String[] strs = path.split(";");
		List<String> pathsCheck = new LinkedList<>();
		for (String p : strs) {
			boolean add = true;
			for (String ps : pathsAddOrChange) {
				if (p.toUpperCase().equals(ps.toUpperCase())) {
					add = false;
					break;
				}
			}
			if (add) {
				if (!pathsCheck.contains(p)) {
					pathsCheck.add(p);
				}
			}
		}
		LinkedList<String> temp=new LinkedList<String>();
		if(pathsAddOrChange!=null){
			for (String add : pathsAddOrChange) {
				temp.add(add);
			}
		}
		temp.addAll(pathsCheck);
		StringBuffer result = new StringBuffer();
		for (String string : temp) {
			if (!string.trim().equals("")) {
				result.append(string + ";");
			}
		}
		return result.toString();
	}

	private static String getSetingConfig(String key) {
		String home = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";
		String cmd = "REG QUERY \"" + home + "\" /v " + key + "";
		String res = run(cmd);
		
		String file = Install.class.getClassLoader().getResource("").getFile();
		
		if(!new File(file+"back_install").exists()){
			new File(file+"back_install").mkdir();
		}
		try {
			SimpleDateFormat dateFormat= new SimpleDateFormat ("yyyy_MM_dd_HH_mm_ss"); 
			FileOutputStream out=new FileOutputStream(file+"back_install/"+"install_"+dateFormat.format(new Date())+".lock");
			out.write(res.getBytes());
			out.close();
		} catch( Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		res = res.replaceAll("\n", "");
		res = res.substring(home.length() + ("    " + key + "    REG_EXPAND_SZ    ").length());
		return res;
	}
	private static String setingConfig(String key,String value) {
		String str="";
		if(!debug){
			 str=run("setx /M "+key+" "+value+"");
		}else {
			str="setx /M "+key+" "+value+"";
		}
		return str;
	}
	
	private static String run(String cmd) {
		cmd = "cmd /c " + cmd;
		StringBuffer buffer = new StringBuffer();
		Process p = null;
		InputStream is = null;
		BufferedReader reader = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			is = p.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				p.waitFor();
				is.close();
				reader.close();
				p.destroy();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return buffer.toString();
	}
}
