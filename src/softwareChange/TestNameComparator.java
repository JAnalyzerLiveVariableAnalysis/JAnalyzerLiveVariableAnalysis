package softwareChange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import util.Debug;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/11
 * @version 1.0
 */
public class TestNameComparator {

	public static void main(String[] args) {
		String rootPath = "E:\\";

		String systemPath = "E:\\ZxcTools\\jEdit\\"; 
		String[] versionPaths = {"jEdit(3.0)", "jEdit(3.1)", "jEdit(3.2)", "jEdit(4.0)", "jEdit(4.0.2)", "jEdit(4.0.3)", 
				"jEdit(4.1)", "jEdit(4.2)", "jEdit(4.3)", "jEdit(4.3.3)", "jEdit(4.4.1)",  "jEdit(4.4.2)", "jEdit(4.5.0)", 
				"jEdit(4.5.1)", "jEdit(4.5.2)", "jEdit(5.0.0)", "jEdit(5.1.0)",
		};

//		String path = paths[0];
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		String pathOne = systemPath + versionPaths[0] + "\\"; 
		String pathTwo = systemPath + versionPaths[15] + "\\"; 
				
//		testSimilarityAtPackageLevel(pathOne, pathTwo, writer);
		testSimilarityAtDetailedTypeLevel(pathOne, pathTwo, writer);
		
		writer.close();
		output.close();
	}

	public static void testSimilarityAtPackageLevel(String pathOne, String pathTwo, PrintWriter report) {
		SourceCodeParser parser = new SourceCodeParser(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nPackage list of the system = " + pathOne);
		writeAllPackagesToDebugger(managerOne);
		
		parser = new SourceCodeParser(pathTwo);
		creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathTwo);
		NameTableManager managerTwo = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nPackage list of the system = " + pathTwo);
		writeAllPackagesToDebugger(managerTwo);
		
		NameTableComparator.calculateSimilarityAtPackageLevel(managerOne, managerTwo, report, 0.3);
	}

	public static void testSimilarityAtDetailedTypeLevel(String pathOne, String pathTwo, PrintWriter report) {
		SourceCodeParser parser = new SourceCodeParser(pathOne);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + pathOne);
		NameTableManager managerOne = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.println("\r\nDetailed type list of the system = " + pathOne);
		writeAllDetailedTypesToDebugger(managerOne);
		
		parser = new SourceCodeParser(pathTwo);
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
		List<DetailedTypeDefinition> definitionList = manager.getRootScope().getAllDetailedTypeDefinition();
		for (DetailedTypeDefinition definition : definitionList) {
			Debug.println(definition.getFullQualifiedName());
		}
	}
	
}
