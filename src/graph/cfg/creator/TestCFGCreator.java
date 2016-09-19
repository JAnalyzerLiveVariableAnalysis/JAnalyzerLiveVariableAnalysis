package graph.cfg.creator;

import graph.basic.GraphNode;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import util.Debug;
import util.SourceCodeParser;

public class TestCFGCreator {

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String rootPath = "E:\\";
		
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";
		String path1 = rootPath + "ZxcTools\\debug\\package\\print_tokens2\\";
		String path2 = rootPath + "ZxcTools\\debug\\package\\replace\\";
		String path3 = rootPath + "ZxcWork\\ProgramAnalysis\\src\\";
		String path4 = rootPath + "ZxcTools\\EclipseSource\\org\\";
		String path5 = rootPath + "ZxcWork\\ToolKit\\src\\sourceCodeAsTestCase\\RECompiler.java";
		String path8 = "E:\\ZxcTools\\JDKSource\\";

/*
		String fileName = "NRGMethodInvocation.java";
		
		SourceCodeParser parser = new SourceCodeParser(path3 + fileName);
		CompilationUnit root = parser.findCompilationUnitByFileSimpleName(fileName);
*/
		
		PrintWriter output = null;
		PrintWriter writer = null;
		try {
			output = new PrintWriter(new FileOutputStream(result));
		} catch (Exception exc) {
			exc.printStackTrace();
			System.exit(-1);
		}

		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			writer = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(writer);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		testMatchASTNode(path8, output);
		writer.close();
		output.close();
		
		
//		testLoadFile();
	}
	
	public static void testMatchASTNode(String path, PrintWriter output) {

		SourceCodeParser parser = new SourceCodeParser(path);
		parser.toGetFirstParsedFile();
		while (parser.hasParsedFileInfo()) {
			String fileName = parser.getCurrentUnitFullName();
			
			System.out.println("Scan file: " + fileName);
//			if (!fileName.contains("RECompiler")) {
//				parser.toGetNextParsedFile();
//				continue;
//			}
//			String fileContent = parser.getCurrentFileContent();
			CompilationUnit root = parser.getCurrentCompilationUnit();
		
			CFGCreator creator = new CFGCreator(fileName, root);
			List<ControlFlowGraph> cfgs = creator.create();
			
			
			for (ControlFlowGraph cfg : cfgs) {
//				try {
//					cfg.simplyWriteToDotFile(output);
//				} catch (Exception exc) {
//					exc.printStackTrace();
//				}
				
				List<GraphNode> nodes = cfg.getAllNodes();
				if (nodes != null) {
					for (GraphNode node : nodes) {
						ExecutionPoint point = (ExecutionPoint)node;
						
						ASTNode astNode = point.getAstNode();
						ASTNode matchedNode = creator.matchASTNode(point);
						
						if (astNode == matchedNode) {
							Debug.println("Matched AST node for execution point [" + point.getDescription() + "] at [" + point.getStartPosition() + "]!");
						} else {
							Debug.println("DO NOT matched AST node for execution point [" + point.getDescription() + "]!");
							if (astNode != null) Debug.println("\tAST Node from point: " + astNode.toString());
							else Debug.println("\tAST node is null!");
							if (matchedNode != null) output.println("\tAST node matched: " + matchedNode.toString());
							else Debug.println("\tMatched node is null!");
						}
					}
				}
			}
			
			parser.releaseCurrentCompilatinUnits();
			parser.releaseCurrentFileContents();
			parser.toGetNextParsedFile();
		}
		
		output.close();
	}
	
	public static void testLoadFile() {
		File file = new File("E:\\ZxcWork\\ProgramAnalysis\\data\\MethodVerifier15.java");
		String fileContent = null;
		
		try {
			final Scanner in = new Scanner(file);
			StringBuilder buffer = new StringBuilder(); 
			while (in.hasNextLine()) {
				String line = in.nextLine();
				Debug.println("Read line: " + line);
				buffer.append(line + "\r\n");
			}
			in.close();
			fileContent = buffer.toString();
			
			Debug.println("File content:");
			Debug.println(fileContent);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

}
