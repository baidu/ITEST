package com.baidu.qa.service.test.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import com.baidu.qa.service.test.dto.Constant;


/**
 * 
 * @author xuedawei
 * @date 2013-8-9
 * @classname FileUtil
 * @version 1.0.0
 * @desc File操作的工具类
 */
public class FileUtil {
	static private Log log = LogFactory.getLog(FileUtil.class);

	
	
	public static Map<String,String> getUccookieFromFile(String filepath){
		File file = new File(filepath);

		if (!file.exists()) {
			return null;
		}else{
			String loginfo = readFileByLines(file);
			Map<String,String> map = new HashMap<String, String>();
			map.put("logintime",loginfo.split("\t")[0]);
			map.put("cookie",loginfo.split("\t")[1] );
			return map;
		}
	}
	
	
	
	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */

	public static String readFileByLines(File file) {
		String resp = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			// reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;

			while ((tempString = reader.readLine()) != null) {
				resp = resp + tempString;
				line++;
			}
			reader.close();

		} catch (IOException e) {
			log.info("[load file " + file + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return resp;

	}
	
	
	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */

	public static String readFileByLinesWithBOMFilter(File file) {
		String resp = "";
		BufferedReader reader = null;
		UnicodeReader ur = null;
		
		try {
			FileInputStream fis = new FileInputStream(file);  
			ur = new UnicodeReader(fis, "UTF-8");  
			reader = new BufferedReader(ur);

			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				resp = resp + tempString;
				line++;
			}
			reader.close();
			log.info("[load file]" + file.getPath());

		} catch (IOException e) {
			log.info("[load file " + file + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
					ur.close();
				} catch (IOException e1) {
					log.error("[close reader error]",e1);
				}
			}
		}
		return resp;

	}

	// public static String getFileSuffix(File file){
	// String suffix = "";
	// try{
	// String name = file.getName();
	// String[] n=name.split("\\.");
	// suffix=n[n.length-1];
	// // suffix= name.substring(name.indexOf(".")+1);
	// }catch(Exception e){
	// log.info("[load file "+file+"error]:",e);
	// }
	//    	
	// return suffix;
	// }
	//    

	// public static List<String> getListFromFile(File file) {
	// List<String> list = new ArrayList<String>();
	// BufferedReader reader = null;
	// try {
	// reader = new BufferedReader(new FileReader(file));
	// String tempString = null;
	// int line = 1;
	// // 一次读入一行，直到读入null为文件结束
	// while ((tempString = reader.readLine()) != null) {
	// list.add(tempString);
	// line++;
	// }
	// reader.close();

	public static String getFileSuffix(File file) {
		if (file == null) {
			return "";
		}
		String suffix = "";
		try {
			String name = file.getName();
			String[] n = name.split("\\.");
			suffix = n[n.length - 1];
			// suffix= name.substring(name.indexOf(".")+1);
		} catch (Exception e) {
			log.info("[load file " + file + "error]:", e);
		}

		return suffix;
	}

	public static List<String> getListFromFile(File file) {
		List<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			// reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				list.add(tempString);
				line++;
			}
			reader.close();
			log.info("[load file]" + file.getPath());
			log.info(list);

		} catch (IOException e) {
			log.info("[load file " + file + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return list;

	}
	
	
	/**
	 * 读取文件时，过滤掉BOM
	 * @param file
	 * @return
	 */
	public static List<String> getListFromFileWithBOMFilter(File file) {
		List<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		UnicodeReader ur = null;
		try {
			FileInputStream fis = new FileInputStream(file);  
			ur = new UnicodeReader(fis, "UTF-8");  
			reader = new BufferedReader(ur);

			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				list.add(tempString);
				line++;
			}
			reader.close();
			log.info("[load file]" + file.getPath());
			log.info(list);

		} catch (IOException e) {
			log.info("[load file " + file + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
					ur.close();
				} catch (IOException e1) {
					log.error("[close reader error]",e1);
				}
			}
		}
		return list;

	}
	
	
	public static Map<String,String> getSplitedMapFromFile(File file, String regex) {
		Map<String,String> filemap = new HashMap<String,String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			String tempString = null;
			int line = 1;
			int column = 0;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				String[] row = tempString.split(regex,2);
				if (row.length == 2&&row[1].trim().length()!=0) {
					filemap.put(row[0].trim(),row[1].trim());
				}				
				line++;
			}
			reader.close();

		} catch (IOException e) {
			log.info("[load file " + file.getName() + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return filemap;
	}

	public static List<String[]> getSplitedListFromFile(File file, String regex) {
		List<String[]> filelist = new ArrayList<String[]>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String tempString = null;

			int line = 1;
			int column = 0;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				String[] row = tempString.split(regex);
				if (line == 1) {
					column = row.length;
					if (column == 0) {
						log.error("[column of file is 0]:" + file.getName());
						reader.close();
						return null;
					}
				} else if (row.length != column) {
					log.error("[wrong column of file ]:" + file.getName()
							+ "line:" + line);
					Assert.assertTrue("[wrong column of file ]:"
							+ file.getName() + "line:" + line, false);
					reader.close();
					return null;
				}

				filelist.add(row);

				line++;
			}
			reader.close();

		} catch (IOException e) {
			log.info("[load file " + file.getName() + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return filelist;
	}
	/*
	 * column 从0开始
	 */
	
	public static List<String> getColumnListFromFile(File file,int column,
			String regex) {
		List<String> filelist = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String tempString = null;

			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				String[] row = tempString.split(regex);
				 if (row.length < column) {
					log.error("[wrong column of file ]:" + file.getName()
							+ "line:" + line);
					Assert.assertTrue("[wrong column of file ]:"
							+ file.getName() + "line:" + line, false);
					reader.close();
					return null;
				}

				filelist.add(row[column]);

				line++;
			}
			reader.close();

		} catch (IOException e) {
			log.info("[load file " + file.getName() + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return filelist;
	}

	public static List<String[]> getSplitedListFromFileByFirstRegex(File file,
			String regex) {
		List<String[]> filelist = new ArrayList<String[]>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			String tempString = null;

			int line = 1;
			int column = 0;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				String[] row = tempString.split(regex, 2);
				if (line == 1) {
					column = row.length;
					if (column == 0) {
						log.error("[column of file is 0]:" + file.getName());
						reader.close();
						return null;
					}
				} else if (row.length != column) {
					log.error("[wrong column of file ]:" + file.getName()
							+ "line:" + line);
					Assert.assertTrue("[wrong column of file ]:"
							+ file.getName() + "line:" + line, false);
					reader.close();
					return null;
				}

				filelist.add(row);

				line++;
			}
			reader.close();

		} catch (IOException e) {
			log.info("[load file " + file.getName() + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return filelist;
	}

	static public File writeFile(String filepath, String filename,
			String content) {
		File dir = new File(filepath);
		if (content == null || content.trim().length() == 0) {
			log.error("[write file " + filename
					+ "error]:content is null,can't write");
			return null;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(filepath + "/" + filename);
		if (file.exists()) {

			log.error("[write file " + filename
					+ "error]:file is exist,can't rewrite");

			return null;
		}
		FileWriter fw = null;
		try {
			file.createNewFile();
			fw = new FileWriter(file);
			fw.write(content.trim());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return file;
	}
	
	
	
	static public File appendWriteFile(String filepath, String filename,
			String content) {
		File dir = new File(filepath);
		if (content == null || content.trim().length() == 0) {
			log.error("[write file " + filename
					+ "error]:content is null,can't write");
			return null;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(filepath + "/" + filename);
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file,true);
			fw.write(content.trim()+System.getProperty("line.separator"));
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		return file;
	}

	static public File rewriteFile(String filepath, String filename,
			String content) {
		File dir = new File(filepath);

		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(filepath + "/" + filename);
		FileWriter fw = null;
		try {
			file.createNewFile();
			fw = new FileWriter(file);
			fw.write(content.trim());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return file;
	}
	
	
	
	static public File rewriteFile(String filename,
			String content) {
		File dir = new File(filename);

		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(filename);
		FileWriter fw = null;
		try {
			file.createNewFile();
			fw = new FileWriter(file);
			fw.write(content.trim());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return file;
	}

	static public File[] getFiles(String folderpath) {
		File folder = new File(folderpath);
		if (folder.exists()) {
			return folder.listFiles();
		}
		return null;
	}

	static private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/*
	 * 从第一个前缀中获取tag
	 */
	public static String getPrefixTag(File file) {
		try {
			String name = file.getName();
			String[] n = name.split("\\.");
			if (n == null || n.length == 0) {
				return null;
			}
			String prefix = n[0].trim();
			if (prefix.startsWith("[") && prefix.endsWith("]")) {
				String numstring = prefix.substring(1, prefix.length() - 1)
						.trim();
				if (!isNumeric(numstring)) {
					return numstring;
				}

			}
			return null;
			// suffix= name.substring(name.indexOf(".")+1);
		} catch (Exception e) {
			log.info("[load file PrefixOder" + file + "error]:", e);
		}

		return null;

	}

	public static File[] orderFiles(File[] files) {
		if (files == null || files.length == 0) {
			return files;
		}
		File orderfile = new File(files[0].getParentFile().getPath() + "/"
				+ Constant.FILENAME_ORDERFILE);
		if (!orderfile.exists()) {
			return files;
		}
	//	List<String> order = FileUtil.getListFromFile(orderfile);
		List<String> order=getColumnListFromFile(orderfile, 0,"\t");
		if (order == null || order.size() == 0) {
			return files;
		}
		File[] orderedfiles = new File[files.length];
		ArrayList<File> filelist = new ArrayList<File>();
		for (File fexist : files) {
			filelist.add(fexist);
		}
		int filenum = 0;
		for (String fstr : order) {
			if (fstr.trim().equals("")) {
				continue;
			}
			File f = new File(files[0].getParentFile().getPath() + "/" + fstr);
			if (f.exists()) {
				orderedfiles[filenum++] = f;
				filelist.remove(f);
			}
		}
		for (File fexist : filelist) {
			orderedfiles[filenum++] = fexist;
		}
		return orderedfiles;
	}

	static public String transCoding(String input) {
		try {
			byte[] bs = input.getBytes("ISO-8859-1");
			String output = new String(bs, "utf-8");
			return output;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return input;
		}

	}

	static public String transToUTF8(File f, String input) {

		try {
			FileCharsetDetector det = new FileCharsetDetector();
			String oldcharset = det.guestFileEncoding(f);
			if (oldcharset.equalsIgnoreCase("UTF-8") == true) {
				return input;
			}
			byte[] bs = input.getBytes(oldcharset);
			String output = new String(bs, "utf-8");
			return output;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return input;
		}
	}

	static public String changeCharset(String input, String newCharset) {
		try {
			byte[] bs = input.getBytes();
			String output = new String(bs, newCharset);
			return output;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return input;
		}

	}

	public static void transferFile(File file, String oldcharset,
			String newcharset) throws IOException {
		String line_separator = System.getProperty("line.separator");
		FileInputStream fis = new FileInputStream(file);
		StringBuffer content = new StringBuffer();
		DataInputStream in = new DataInputStream(fis);
		BufferedReader d = new BufferedReader(new InputStreamReader(in,
				oldcharset));// , "UTF-8"
		String line = null;
		while ((line = d.readLine()) != null)
			content.append(line + line_separator);
		d.close();
		in.close();
		fis.close();

		Writer ow = new OutputStreamWriter(new FileOutputStream(file),
				newcharset);
		ow.write(content.toString());
		ow.close();
	}

	public static boolean diff(File expectfile, File actualfile) {

		boolean result = true;
		try {
			// 转码

			FileReader in11 = new FileReader(expectfile);
			FileReader in22 = new FileReader(actualfile);
			BufferedReader in1 = new BufferedReader(in11);
			BufferedReader in2 = new BufferedReader(in22);
			int n1 = 0, n2 = 0;
			int line = 1;
			n2 = in2.read();
			n1 = in1.read();
			int max = (n1 > n2 ? n1 : n2);

			while (line < max) {
				line++;
				String s1, s2;
				s1 = in1.readLine();
				s2 = in2.readLine();
				if (s1 == null && s2 == null) {
					continue;
				}
				if (s1 == null || s2 == null || !s1.equals(s2)) {
					result = false;
					log.error("difference between " + expectfile + " and "
							+ actualfile + " in line:" + line);
					log.error("expectfile:" + transToUTF8(expectfile, s1));
					log.error("actualfile:" + transToUTF8(actualfile, s2));
				}
			}
			in22.close();
			in11.close();
			in2.close();
			in1.close();
		}// end of try
		catch (IOException e) {
			log.error("difffile error", e);

		}// end of catch
		return result;
	}

	public static void copyFile(File srcFile, File descFile) {
		FileInputStream input = null;
		try {
			input = new FileInputStream(srcFile);
			FileOutputStream output = new FileOutputStream(descFile);
			try {
				byte[] buffer = new byte[4096];
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException ioe) {
					// ignore
				}
			}
		} catch (IOException ioe) {
			// ignore
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}

	/**
	 * 删除指定文件夹下除了.svn的所有文件
	 * 
	 * @param FolderName
	 *            文件夹名称
	 */
	public static void deleteInFolder(String FolderName) {
		File fs = new File(FolderName);
		if (!fs.exists()) {
			return;
		}
		File[] files = fs.listFiles();
		for (File f : files) {
			if (f.getName().equalsIgnoreCase(".svn")) {
				continue;
			}
			f.delete();// 删除
		}

	}

	public static Map<String, String> getMapFromFile(File file, String reg) {
		Map<String, String> result = new HashMap<String, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));

			// reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null
					&& tempString.trim().length() != 0) {
				int num = tempString.indexOf(reg);
				if (num < 1 || num >= tempString.length()) {
					continue;
				}
				result.put(tempString.substring(0, num), tempString.substring(
						num + 1, tempString.length()));
				line++;
			}
			reader.close();
			log.info("[load file]" + file.getPath());

		} catch (IOException e) {
			log.info("[load file " + file + "error]:", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return result;

	}
}
