package gui.softwareMeasurement.metricBrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.tree.TreeNode;

import gui.softwareMeasurement.mainFrame.Checker;
import gui.softwareMeasurement.structureBrowser.tree.NodeKind;
import gui.softwareMeasurement.structureBrowser.tree.ProjectTreeNode;
import softwareMeasurement.measure.SoftwareMeasure;

/**
 * 一个Recorder对应历史记录中的一行
 * @author Wu zhangsheng
 */
public class Recorder {
	
	String formatDate;
	String formatMetrics;
	String nodesStr;
	NodeKind kind;
	String resultFilePath;
	
	public Recorder(String formatDate, String nodesStr, NodeKind kind,
			String[] metrics, String resultFilePath) {
		this.formatDate = formatDate;
		this.formatMetrics="";
		for(String m : metrics) {
			this.formatMetrics += m+";" ;
		}
		this.nodesStr = nodesStr;
		this.kind = kind;
		this.resultFilePath = resultFilePath;
	}
	

	public void save() {
		File file = new File("result/record.txt");
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(file, true));
			pw.write(formatDate + "\t");
			pw.write(nodesStr+"\t");
			switch(kind) {
			case PROJECT_NODE:
				pw.write("系统"+"\t");
				break;
			case VERSION_NODE:
				pw.write("版本"+"\t");
				break;
			case PACKAGE_NODE:
				pw.write("包"+"\t");
				break;
			case CLASS_NODE:
				pw.write("类"+"\t");
				break;
			case METHOD_NODE:
				pw.write("方法"+"\t");
				break;
			default:
				pw.write("\t");
				break;
			}
			pw.write(formatMetrics+"\t");
			pw.write(resultFilePath+"\r\n");
			pw.flush();
			
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
}
