package gui.softwareMeasurement.mainFrame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import gui.softwareMeasurement.structureBrowser.ProjectTreeManager;

public class Checker {

	public static boolean ifFileExists() {
		File file = new File("res/img");
		if(!file.isDirectory()) {
			file.mkdirs();
		}
		file = new File(".info");
		if(!file.isDirectory()) {
			file.mkdir();
		}
		// 载入项目名
		File[] projectNames = file.listFiles();
		if(projectNames != null) {
			for(File f : projectNames) {
				ProjectTreeManager.projectIds.add(f.getName());
			}
		}
		
		file = new File("res/.setting");
		if(!file.exists()) {
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				fw.write("<?xml version=\"1.0\" encoding=\"utf8\"?> \r\n"+
						"<setting>" +
						"<alert value=\"true\" />" +
						"<resultpath value=\"result\" />"+
						"</setting>");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				file.delete();
				return false;
			}
		}
		
		file = new File("res/img/project_icon.jpg");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/version_icon.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/package_icon.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/close.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/close_selected.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/file.jpg");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/field_icon.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/method_icon.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/expand.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/expandBtn.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
		file = new File("res/img/collapse.png");
		if(!file.exists()) {
			System.out.println(file);
			return false;
		}
//		file = new File("res/dir_his");
//		if(!file.exists()) {
//			System.out.println(file);
//			return false;
//		}

		return true;
	}

	public static boolean isDeleteDlgAlert() {
		SAXReader reader = new SAXReader();
		Document document;
		try {
			File file = new File("res/.setting");
			document = reader.read(file);
			Element root = document.getRootElement();// <setting>
			List<Element> children = root.elements();
			for(Element child : children) {
				if(child.getName().equals("alert")) {
					String value = child.attributeValue("value");
					if(value.equals("true")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}catch(Exception e) {
			return true;
		}
		return true;
	}
	
	public static boolean isRecordFileExisting() {
		File file = new File("result/record.txt");
		return file.exists();
	}
	
	public static File getResultDir() {
		SAXReader reader = new SAXReader();
		Document document;
		try {
			File file = new File("res/.setting");
			document = reader.read(file);
			Element root = document.getRootElement();// <setting>
			List<Element> children = root.elements();
			for(Element child : children) {
				if(child.getName().equals("resultpath")) {
					String value = child.attributeValue("value");
					File resultDir = new File(value);
					if(!resultDir.isDirectory()) {
						resultDir.mkdirs();
					}
					return resultDir;
				}
			}
		}catch(Exception e) {
			File resultDir = new File("result");
			if(!resultDir.isDirectory()) {
				resultDir.mkdirs();
			};
			return resultDir;
		}
		File resultDir = new File("result");
		if(!resultDir.isDirectory()) {
			resultDir.mkdirs();
		};
		return resultDir;
	}
}
