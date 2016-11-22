package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import softwareChange.ClassChangeIndicator;
import softwareMeasurement.measure.MeasureObjectKind;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ18ÈÕ
 * @version 1.0
 */
public class ClassChangeIndicatorCreator {

	public static void main(String[] args) {
		String systemName = "ant";
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		int mainVersionIndex = versions.length - 1;		// The last version as the main version!

		String reportFile = QualitasPathsManager.getTestingResultFile();

		PrintWriter output = null;
		PrintWriter writer = new PrintWriter(System.out);
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			
			writer = new PrintWriter(new FileOutputStream(new File(reportFile)));
			
			Debug.setStart("Begin to generate indicator files.....");
			generateChangeIndicatorsForMainVersion(systemName, versions, mainVersionIndex);
			Debug.time("End....");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		writer.close();
		output.close();
	}
	
	public static void generateChangeIndicatorsForMainVersion(String systemName, String[] versions, int mainVersionIndex) throws IOException {
		if (mainVersionIndex < 0 || mainVersionIndex >= versions.length) return;
		for (int j = 0; j < versions.length; j++) {
			if (j == mainVersionIndex) continue;		// We do not generate indicator for two same versions
			String indicatorFile = QualitasPathsManager.getMeasureIndicatorFile(MeasureObjectKind.MOK_CLASS, systemName, versions[mainVersionIndex], versions[j]);
			generateChangeIndicator(systemName, versions[mainVersionIndex], versions[j], indicatorFile);
		}
	}
	
	public static void generateAllChangeIndicators(String systemName, String[] versions) throws IOException {
		for (int i = 0; i < versions.length; i++) {
			for (int j = 0; j < versions.length; j++) {
				if (j == i) continue;		// We do not generate indicator for two same versions
				String indicatorFile = QualitasPathsManager.getMeasureIndicatorFile(MeasureObjectKind.MOK_CLASS, systemName, versions[i], versions[j]);
				generateChangeIndicator(systemName, versions[i], versions[j], indicatorFile);
			}
		}
	}
	
	
	public static ClassChangeIndicator generateChangeIndicator(String systemName, String versionOne, String versionTwo, String indicatorFile) throws IOException {
		String pathOne = QualitasPathsManager.getSystemPath(systemName, versionOne);
		SourceCodeFileSet parserOne = new SourceCodeFileSet(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parserOne);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		
		String pathTwo = QualitasPathsManager.getSystemPath(systemName, versionTwo);
		SourceCodeFileSet parserTwo = new SourceCodeFileSet(pathTwo);
		creator = new NameDefinitionCreator(parserTwo);

		Debug.setStart("Begin creating system, path = " + pathTwo);
		NameTableManager managerTwo = creator.createNameTableManager();
		Debug.time("End creating.....");
		
		Debug.setStart("Begin generate change indicator for [" + versionOne + "] vs. [" + versionTwo + "]");
		ClassChangeIndicator indicator = ClassChangeIndicator.generateClassChangeIndicator(versionOne, managerOne, versionTwo, managerTwo, indicatorFile);
		indicator.write();
		Debug.time("End generating....");
		return indicator;
	}

	public static void generateIndicatorCheckReportForMainVersion(String systemName, String[] versions, int mainVersionIndex, PrintWriter report) throws IOException {
		if (mainVersionIndex < 0 || mainVersionIndex >= versions.length) return;
		for (int j = 0; j < versions.length; j++) {
			if (j == mainVersionIndex) continue;		// We do not generate indicator for two same versions
			String indicatorFile = QualitasPathsManager.getMeasureIndicatorFile(MeasureObjectKind.MOK_CLASS, systemName, versions[mainVersionIndex], versions[j]);
			generateIndicatorCheckReport(systemName, versions[mainVersionIndex], versions[j], indicatorFile, report);
		}
	}
	
	public static void generateIndicatorCheckReport(String systemName, String versionOne, String versionTwo, String indicatorFile, PrintWriter reporter) throws IOException {
		String pathOne = QualitasPathsManager.getSystemPath(systemName, versionOne);
		SourceCodeFileSet parserOne = new SourceCodeFileSet(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parserOne);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		
		String pathTwo = QualitasPathsManager.getSystemPath(systemName, versionTwo);
		SourceCodeFileSet parserTwo = new SourceCodeFileSet(pathTwo);
		creator = new NameDefinitionCreator(parserTwo);

		Debug.setStart("Begin creating system, path = " + pathTwo);
		NameTableManager managerTwo = creator.createNameTableManager();
		Debug.time("End creating.....");

		Debug.setStart("Begin generate report for [" + versionOne + "] vs. [" + versionTwo + "]");
		ClassChangeIndicator indicator = ClassChangeIndicator.getClassChangeIndicatorInstance(indicatorFile, versionOne, versionTwo); 
		indicator.generateCheckReport(managerOne, managerTwo, reporter);
		indicator.generateMatchedRatioReport(reporter);
		Debug.time("End generating....");
		reporter.flush();
	}
	
	public static void generateAllIndicatorsCheckReport(String systemName, String[] versions, PrintWriter reporter) throws IOException {
		for (int i = 0; i < versions.length; i++) {
			for (int j = 0; j < versions.length; j++) {
				if (j == i) continue;		// We do not generate indicator for two same versions
				String indicatorFile = QualitasPathsManager.getMeasureIndicatorFile(MeasureObjectKind.MOK_CLASS, systemName, versions[i], versions[j]);
				generateIndicatorCheckReport(systemName, versions[i], versions[j], indicatorFile, reporter);
			}
		}
	}
	
}
