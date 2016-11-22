package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import analyzer.dataTable.DataTableManager;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameScope.SystemScope;
import softwareMeasurement.SystemScopeMeasurement;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class SystemMetricCollector {
	
	public static void main(String[] args) {
		collectQualitasRecentSystemsMeasure(true);
		writeQualitasRecentSystemsMeasureIntoOneFile();
	}

	public static void writeQualitasRecentSystemsMeasureIntoOneFile() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "QualitsRecent.txt" ;
		
		DataTableManager dataTable = new DataTableManager("QualitasRecent");
		DataTableManager tempTable = new DataTableManager("Temp"); 

		Debug.setScreenOn();
		Debug.setStart("Begin....");
		try {
			for (int i = 0; i < systemNames.length; i++) {
				String systemName = systemNames[i];
				String result = QualitasPathsManager.getSystemMeasureResultFile(systemName, false);

				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				if (i == 0) dataTable.read(result, true);
				else {
					tempTable.read(result, true);
					if (tempTable.getLineNumber() >= 1) {
						String[] lineArray = tempTable.getLineAsStringArray(0);
						dataTable.appendLine(lineArray);
					}
				}
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End....");
	}
	
	public static void collectQualitasRecentSystemsMeasure(boolean recollect) {
		String[] systemNames = QualitasPathsManager.getSystemNames();

		PrintWriter output = null;
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.setScreenOn();
		Debug.setStart("Begin....");
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
//			if (!systemName.equals("fitlibraryforfitnesse")) continue;
			collectQualitasSystemMeasure(systemName, recollect);
		}
		Debug.time("End....");
		output.close();
	}
	
	public static void collectQualitasSystemMeasure(String systemName, boolean recollect) {
		String result = QualitasPathsManager.getSystemMeasureResultFile(systemName, false);

		File resultFile = new File(result);
		if (!recollect) {
			if (resultFile.exists()) return;
		}
		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(resultFile));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasSystemMeasure(systemName, writer);
		writer.close();
	}
	
	public static void collectQualitasSystemMeasure(String systemName, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableSystemMeasureList();

		writer.print("System");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("\tNotes");

		String[] versionPaths = QualitasPathsManager.getSystemVersions(systemName);
		if (versionPaths.length < 1) return;
		
		for (int index = versionPaths.length-1; index < versionPaths.length; index++) {
			String path = QualitasPathsManager.getSystemPath(systemName, versionPaths[index]);
			
			SourceCodeFileSet parser = new SourceCodeFileSet(path);
			NameTableCreator creator = new NameDefinitionCreator(parser);

			Debug.setStart("Begin creating system, path = " + path);
			NameTableManager manager = creator.createNameTableManager();
			Debug.time("End creating.....");
			Debug.flush();
			SoftwareStructManager structManager = new SoftwareStructManager(manager);
			SystemScope rootScope = manager.getSystemScope();

			Debug.setStart("Begin calculating measures....!");
			SystemScopeMeasurement measurement = new SystemScopeMeasurement(rootScope, structManager);
			measurement.getMeasureList(measureList);
			writer.print(versionPaths[index]);
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + path);
			Debug.time("End calculating....");
		}
		writer.flush();
	}

	public static List<SoftwareMeasure> getAvailableSystemMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FILE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PKG),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.NonTopTYPE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopPubTYPE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopNonPubTYPE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NonTopCLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopPubCLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopNonPubCLS),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
}
