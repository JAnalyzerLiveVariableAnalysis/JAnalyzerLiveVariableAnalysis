package analyzer.valuedNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/20
 * @version 1.0
 */
public class TestClassAnalysisReporter {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String rootPath = "E:\\";
		
		String systemPath = rootPath + "ZxcTools\\jEdit\\"; 
		String[] versionPaths = {"jEdit(3.0)", "jEdit(3.1)", "jEdit(3.2)", "jEdit(4.0)", "jEdit(4.0.2)", "jEdit(4.0.3)", 
				"jEdit(4.1)", "jEdit(4.2)", "jEdit(4.3)", "jEdit(4.3.3)", "jEdit(4.4.1)",  "jEdit(4.4.2)", "jEdit(4.5.0)", 
				"jEdit(4.5.1)", "jEdit(4.5.2)", "jEdit(5.0.0)", "jEdit(5.1.0)",
		};
		String reportFilePath = rootPath + "ZxcWork\\ProgramAnalysis\\data\\jEdit\\Length\\";
		String firstValuePath = rootPath + "zxcWork\\ProgramAnalysis\\data\\jEdit\\Authority\\";
		String secondValuePath = rootPath + "zxcWork\\ProgramAnalysis\\data\\jEdit\\Length\\";
		String indicatorPath = rootPath + "zxcWork\\ProgramAnalysis\\data\\jEdit\\ClassNode\\";
		
		String[] firstValueFilePostfix = {"CDG.net", "CDG.vec"}; 
		String[] secondValueFilePostfix = {"Length.txt", null}; 
		
		String indicatorFilePostfix = "Class.txt";
		String reportFilePostfix = "AuLe.txt";
		
		
		ClassAnalysisReporter reporter = new ClassAnalysisReporter(systemPath, versionPaths);
		reporter.setIndicatorInformation(indicatorPath, indicatorFilePostfix);
		reporter.setReportInformation(reportFilePath, reportFilePostfix);
		reporter.setValueInformation(firstValuePath, firstValueFilePostfix, secondValuePath, secondValueFilePostfix);
		
		PrintWriter output = null;
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);

//			reporter.generateAllDetailedTypeLengthFiles(false);
			
			reporter.generateAllValueComparisonReportFiles(0);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
}
