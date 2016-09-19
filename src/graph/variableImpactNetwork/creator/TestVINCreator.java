package graph.variableImpactNetwork.creator;

import graph.basic.GraphUtil;
import graph.variableImpactNetwork.VariableImpactGraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import util.Debug;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2014/2/2
 * @version 1.0
 */
public class TestVINCreator {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String rootPath = "E:\\";
		String path0 = rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\CNExample.java";
		String path1 = rootPath + "ZxcTools\\debug\\package\\print_tokens2\\";
		String path2 = rootPath + "ZxcTools\\debug\\package\\replace\\";
		String path3 = rootPath + "ZxcWork\\ProgramAnalysis\\src\\";
		String path4 = rootPath + "ZxcTools\\EclipseSource\\org\\";
		String path5 = rootPath + "ZxcDeveloping\\OOPAndJavaExamples\\automata\\src\\";
		String path6 = rootPath + "ZxcTools\\jEdit\\jEdit(5.1.0)\\";
		String path8 = rootPath + "ZxcTools\\JDKSource\\";

		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.dot";
		String resultNet = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.net";

		PrintWriter writer = new PrintWriter(System.out);
		PrintWriter output = null;
		
/*		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
*/		
		
//		String path = path2;
//		String unitFileName = path2 + "Replace.java";
//		String className = "Replace";
//		String methodName = "addstr";
//		testGenerateVIN(path, unitFileName, className, methodName, result, true);

		testGenerateVINForAllSourceCodes(path0, result, true);
	}

	public static void testGenerateVIN(String path, String unitFileName, String className, String methodName, String resultFile, boolean dotOrNetFile) {
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");

		VINCreator vinCreator = new VINCreator(parser, manager);
		Debug.setStart("Begin creating variable impact network, path = " + path);
		VariableImpactGraph vinGraph = null;
	
		if (methodName != null && className != null && unitFileName != null) vinGraph = vinCreator.create(className+methodName, unitFileName, className, methodName);
		else if (className != null && unitFileName != null) vinGraph = vinCreator.create(unitFileName+className, unitFileName, className);
		else if (unitFileName != null) vinGraph = vinCreator.create(unitFileName, unitFileName);
		else vinGraph = vinCreator.create(path);
		Debug.time("End creating.....");

		parser.releaseAllCompilatinUnits();
		parser.releaseAllFileContents();

		if (vinGraph == null) {
			System.out.println("Can not generate VIN for unit = [" + unitFileName + "], class = [" + className + "], method = [" + methodName + "]");
			return;
		}
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(new File(resultFile)));
			Debug.setStart("Begin writing to file " + resultFile);
			if (dotOrNetFile == true) GraphUtil.simplyWriteToDotFile(vinGraph, writer, false);
			else GraphUtil.simplyWriteToNetFile(vinGraph, writer);
			writer.close();
			Debug.time("End writing to file " + resultFile);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
	}
	
	public static void testGenerateVINForAllSourceCodes(String path, String resultFile, boolean dotOrNetFile) {
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");

		VINCreator vinCreator = new VINCreator(parser, manager);
		Debug.setStart("Begin creating variable impact network, path = " + path);
		VariableImpactGraph vinGraph = vinCreator.create(path);
		Debug.time("End creating.....");

		parser.releaseAllCompilatinUnits();
		parser.releaseAllFileContents();

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(new File(resultFile)));
			Debug.setStart("Begin writing to file " + resultFile);
			if (dotOrNetFile == true) GraphUtil.simplyWriteToDotFile(vinGraph, writer, false);
			else GraphUtil.simplyWriteToNetFile(vinGraph, writer);
			writer.close();
			Debug.time("End writing to file " + resultFile);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
	}
}
