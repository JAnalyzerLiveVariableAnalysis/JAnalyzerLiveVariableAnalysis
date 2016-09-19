package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import nameTable.NameDefinitionVisitor;
import nameTable.NameTableFilter;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.EnumTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.SystemScope;
import util.Debug;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class NameTableTester {
	public static void main(String[] args) {
		testNameTableCreator();
//		testFileContents();
	}
	
	public static void testNameTableCreator() {
		String result = QualitasPathsManager.defaultRootPath + "QualitasError.txt";
		PrintWriter writer = new PrintWriter(System.out);
		
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
			Debug.setWriter(writer);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		String[] systemNames = QualitasPathsManager.getSystemNames();
		int[] errorUnits = new int[systemNames.length];
		Debug.setScreenOn();
		for (int index = 0; index < systemNames.length; index++) {
			errorUnits[index] = testNameTableCreator(systemNames[index]);
		}
		Debug.println("The following are systems which have compiling errors!");
		for (int index = 0; index < errorUnits.length; index++) {
			if (errorUnits[index] > 0) {
				Debug.println("System " + systemNames[index] + ", " + errorUnits[index] + " errors!");
			}
		}
		Debug.flush();
		writer.close();
	}
	
	public static int testNameTableCreator(String systemName) {
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		int totalErrorUnitNumber = 0;
		
		for (int index = 0; index < versions.length; index++) {
			String path = QualitasPathsManager.getSystemPath(systemName, versions[index]);
			String errorFileName = path + "error.txt";
			String typeListFileName = path + "typelist.txt"; 
			
			SourceCodeParser parser = new SourceCodeParser(path);
			NameTableCreator creator = new NameDefinitionCreator(parser);

			Debug.println("System, path = " + path);
			try {
				PrintWriter writer = new PrintWriter(new File(errorFileName));
				writer.println("File\tMessage");
				creator.setErrorReporter(writer);
				NameTableManager manager = creator.createNameTableManager(false);
				int errorUnitNumber = creator.getErrorUnitNumber();
				if (errorUnitNumber > 1) {
					Debug.println("There are " + errorUnitNumber + " ERROR compilation unit files!");
				} else if (errorUnitNumber > 0) {
					Debug.println("There is " + errorUnitNumber + " ERROR compilation unit file!");
				} else {
					Debug.println("There is no error compilation unit file!");
				}
				totalErrorUnitNumber += errorUnitNumber;
				writer.close();
				writer = new PrintWriter(new File(typeListFileName));
				printNameDefinition(manager, writer);
				writer.close();
			} catch (AssertionError error) {
				String message = error.getMessage();
				Debug.println(message);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			Debug.flush();
		}
		return totalErrorUnitNumber;
	}
	
	public static void printNameDefinition(NameTableManager manager, PrintWriter writer) {
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new NameDefinitionFilter());
		SystemScope rootScope = manager.getRootScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		writer.println("Type\tSourceFile\tTopLevel");
		for (NameDefinition definition : definitionList) {
			TypeDefinition type = (TypeDefinition)definition;
			CompilationUnitScope unitScope = manager.getEnclosingCompilationUnitScope(type);
			String topLevel = "nontop";
			if (type.isPackageMember()) {
				if (type.isPublic()) topLevel = "public";
				else topLevel = "top-nonpublic";
			}
			writer.println(type.getFullQualifiedName() + "\t" + unitScope.getUnitFullName() + "\t" + topLevel);
		}
	}
	
	private static int currentLineNumber = 0;
	
	public static void testFileContents() {
		String debugFile = QualitasPathsManager.defaultDebugPath + "debug.txt";
		try {
			PrintWriter output = new PrintWriter(new FileOutputStream(new File(debugFile)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			String systemPath = QualitasPathsManager.getSystemStartPath(systemName);
			String result = systemPath + "fileInfo.txt";
			PrintWriter writer = new PrintWriter(System.out);
			try {
				writer = new PrintWriter(new FileOutputStream(new File(result)));
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			testFileContents(systemName, writer);
			writer.close();
		}
	}
	
	public static void testFileContents(String systemName, PrintWriter writer) {
		System.out.println("Check system " + systemName);
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		for (int index = versions.length-1; index >= versions.length-1; index--) {
			int totalFiles = 0;
			int problemFiles = 0;
			String path = QualitasPathsManager.getSystemPath(systemName, versions[index]);
			System.out.println("Check path " + path);
			Debug.println("Scan files in path = " + path);
			writer.println("Path: " + path);
			
			SourceCodeParser parser = new SourceCodeParser(path);
			parser.toGetFirstParsedFile();
			while (parser.hasParsedFileInfo()) {
				String fileName = parser.getCurrentUnitFullName();
				int lineNumber = parser.getCurrentFileLineNumber();
				long spaces = parser.getCurrentFileSpaces();
				File file = parser.getCurrentFile();
				long size = file.length();
				
				totalFiles++;
				
				writer.println("\tFile: " + fileName + ", line: " + lineNumber + ", spaces: " + spaces + ", size: " + size);
				currentLineNumber = 0;
				String contents = loadFile(file);
				System.out.println("Load file " + fileName + " ...");
				writer.println("\t\tScanning file, line: " + currentLineNumber + ", spaces: " + contents.length());
				if (currentLineNumber < lineNumber) {
					Debug.println("\tFile: " + fileName + ", load line: " + lineNumber + ", scan line: " + currentLineNumber);
					problemFiles++;
				}
				
				parser.toGetNextParsedFile();
			}
			Debug.println("Total files: " + totalFiles + ", problem files: " + problemFiles);
			Debug.flush();
			writer.println("Total files: " + totalFiles + ", problem files: " + problemFiles);
			writer.flush();
		}
	}

	public static String loadFile(File file) {
		if (file == null) return null;
		String fileContent = "";
		
		try {
			Scanner scanner = new Scanner(file);
			StringBuffer buffer = new StringBuffer(); 
			while (scanner.hasNextLine()) {
				currentLineNumber++;
				String line = scanner.nextLine();
				buffer.append(line + "\n");
			}
			scanner.close();
			fileContent = buffer.toString();
		} catch (IOException exc) {
			return fileContent;
		}
		return fileContent;
	}
	
	
}

class NameDefinitionFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isTypeDefinition()) return false;
		TypeDefinition type = (TypeDefinition)definition;
		if (type.isDetailedType() || type.isEnumeration()) return true;	
		return false;
	}
}
