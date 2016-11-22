package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import softwareChange.ClassChangeIndicator;
import softwareChange.NameTableComparator;
import softwareChange.NodeChangeIndicator;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ18ÈÕ
 * @version 1.0
 */
public class SystemSimilarityReport {

	public static void main(String[] args) {
		String systemName = "ant";
		String versionOne = "ant-1.1";
		String systemPathOne = QualitasPathsManager.getSystemPath(systemName, versionOne);

		String versionTwo = "ant-1.2";
		String systemPathTwo = QualitasPathsManager.getSystemPath(systemName, versionTwo);

		String result = QualitasPathsManager.getTestingResultFile();

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;

		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
//		reportSimilarityAtPackageLevel(systemPathOne, systemPathTwo, writer);
		reportSimilarityAtDetailedTypeLevel(systemPathOne, systemPathTwo, writer);
		
		writer.close();
		output.close();
	}
	
	public static void generateChangeIndicator(String versionOne, String pathOne, String versionTwo, String pathTwo, String indicatorFile) throws IOException {
		SourceCodeFileSet parser = new SourceCodeFileSet(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		
		parser = new SourceCodeFileSet(pathTwo);
		creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathTwo);
		NameTableManager managerTwo = creator.createNameTableManager();
		Debug.time("End creating.....");
		
		NodeChangeIndicator indicator = ClassChangeIndicator.generateClassChangeIndicator(versionOne, managerOne, versionTwo, managerTwo, indicatorFile);		
		indicator.write();
	}

	public static void reportSimilarityAtPackageLevel(String pathOne, String pathTwo, PrintWriter report) {
		SourceCodeFileSet parser = new SourceCodeFileSet(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nPackage list of the system = " + pathOne);
		writeAllPackagesToDebugger(managerOne);
		
		parser = new SourceCodeFileSet(pathTwo);
		creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathTwo);
		NameTableManager managerTwo = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nPackage list of the system = " + pathTwo);
		writeAllPackagesToDebugger(managerTwo);
		
		NameTableComparator.calculateSimilarityAtPackageLevel(managerOne, managerTwo, report, 0.3);
	}

	public static void reportSimilarityAtDetailedTypeLevel(String pathOne, String pathTwo, PrintWriter report) {
		SourceCodeFileSet parser = new SourceCodeFileSet(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nDetailed type list of the system = " + pathOne);
		writeAllDetailedTypesToDebugger(managerOne);
		
		parser = new SourceCodeFileSet(pathTwo);
		creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathTwo);
		NameTableManager managerTwo = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nDetailed type list of the system = " + pathTwo);
		writeAllDetailedTypesToDebugger(managerTwo);

		NameTableComparator.calculateSimilarityAtDetailedTypeLevel(managerOne, managerTwo, report, 0.60);
	}

	public static void writeAllPackagesToDebugger(NameTableManager manager) {
		List<PackageDefinition> definitionList = manager.getAllPackageDefinitions();
		for (NameDefinition definition : definitionList) {
			Debug.println(definition.getFullQualifiedName());
		}
	}

	public static void writeAllDetailedTypesToDebugger(NameTableManager manager) {
		List<DetailedTypeDefinition> definitionList = manager.getSystemScope().getAllDetailedTypeDefinitions();
		for (DetailedTypeDefinition definition : definitionList) {
			Debug.println(definition.getFullQualifiedName());
		}
	}

}
