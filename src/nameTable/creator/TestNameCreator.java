package nameTable.creator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.PackageDefinition;
import util.Debug;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/21/
 * @version 1.0
 */
public class TestNameCreator {

	public static void main(String args[]) { 
        // 本地的JDK src目录
		String jdkPath = "C:\\ZxcTools\\JDKSource\\";

		SourceCodeParser parser = null;
		NameTableCreator creator = null;
		NameTableManager manager = null;
		for(int i=0; i < 1; i++) {
			System.gc();
			parser = new SourceCodeParser(jdkPath);
			creator = new NameDefinitionCreator(parser);
			manager = creator.createNameTableManager();
			List<DetailedTypeDefinition> definitionList = manager.getRootScope().getAllDetailedTypeDefinition();
			System.out.println("Total class numbers: " + definitionList.size());
		}
	}

	
	public static void testMultipleVersions() {
		String rootPath = "E:\\";

		String systemPath = "E:\\ZxcTools\\jEdit\\"; 
		String[] versionPaths = {"jEdit(3.0)", "jEdit(3.1)", "jEdit(3.2)", "jEdit(4.0)", "jEdit(4.0.2)", "jEdit(4.0.3)", 
				"jEdit(4.1)", "jEdit(4.2)", "jEdit(4.3)", "jEdit(4.3.3)", "jEdit(4.4.1)",  "jEdit(4.4.2)", "jEdit(4.5.0)", 
				"jEdit(4.5.1)", "jEdit(4.5.2)", "jEdit(5.0.0)", "jEdit(5.1.0)",
		};

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
		
		int versionIndex = 7;
		while (versionIndex < versionPaths.length) {
			String path = systemPath + versionPaths[versionIndex] + "\\"; 

			SourceCodeParser parser = new SourceCodeParser(path);
			NameTableCreator creator = new NameDefinitionCreator(parser);

			Debug.setStart("Begin creating system, path = " + path);
			NameTableManager manager = creator.createNameTableManager();
			Debug.time("End creating.....");
			Debug.println("\r\nDetailed type list of the system = " + path);
			
			Debug.println("");
			
			writer.println();
			writer.println("Detailed type list of the system = " + path);
			writeAllDetailedTypes(manager, writer);
			
			versionIndex = versionIndex + 1;
			writer.flush();
			parser.releaseAllCompilatinUnits();
			parser.releaseAllFileContents();
			
			parser = null;
			creator = null;
			manager = null;
		}
		
		writer.close();
		output.close();
	}

	public static void writeAllPackages(NameTableManager manager, PrintWriter report) {
		List<PackageDefinition> definitionList = manager.getAllPackageDefinitions();
		for (NameDefinition definition : definitionList) {
			report.println(definition.getFullQualifiedName());
		}
	}

	public static void writeAllDetailedTypes(NameTableManager manager, PrintWriter report) {
		List<DetailedTypeDefinition> definitionList = manager.getRootScope().getAllDetailedTypeDefinition();
		for (DetailedTypeDefinition definition : definitionList) {
			String locationString = definition.getLocation().toFullString();
			String reportString = definition.getSimpleName() + "@" + locationString;; 
			report.println(reportString);
		}
	}

}
