package util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Zhou Xiaocong
 * @since 2013/12/28
 * @version 1.0
 * 
 * For parsing all java source files in a path. The client can use the class as follows.
 * 		SourceCodeParser parser = new SourceCodeParser("E:\\user\\project\\src\\");
 * 		parser.toGetFirstParsedFile();
 * 		while (parser.hasParsedFileInfo()) {
 * 			CompilationUnit root = parser.getCurrentCompilationUnit();
 * 			... ...
 * 			parser.toGetNextParsedFile();
 * 		}
 * 
 * 	If the client want to get a parsed compilation unit again, it can call parser.toGetFirst(), and scan all files again.
 *  Also, the client can use find...() methods to find file contents or compilation unit after call parser.loadAllJavaFiles()!
 * 		 
 */
public class SourceCodeParser {
	public final String pathSeparator = "\\";
	
	private String systemPath = null;
	private String rootFile = null;
	private ParsingFileInfo[] allInfo = null;
	private int current = 0;
	
	public SourceCodeParser(String rootFile) {
		this.rootFile = rootFile;
		File dir = new File(rootFile);
		if (dir.isFile()) {
			if (dir.getParent() == null) this.systemPath = "";
			else this.systemPath = dir.getParent() + pathSeparator;
		} else this.systemPath = rootFile;
		
		// Load all java files to the array allInfo.
		loadAllJavaFiles();
	}
	
	/**
	 * Reset the current file index to 0 for getting the information (file, file content, and compilation unit of the file) of the first file
	 */
	public void toGetFirstParsedFile() {
		current = 0;
	}
	
	/**
	 * Test if there is parsed file information
	 */
	public boolean hasParsedFileInfo() {
		if (allInfo.length <= current) return false;
		if (allInfo[current].getFile() == null) return false;
		return true;
	}

	/**
	 * To increase the index of the file for getting the information (file, file content, and compilation unit of the file) of the first file
	 */
	public void toGetNextParsedFile() {
		current = current + 1;
	}
	
	/**
	 * Get the File information of the current file. Before call this method, the caller should have called hasNext() at first. 
	 * 
	 */
	public File getCurrentFile() {
		if (current < 0 || current >= allInfo.length) return null;
		return allInfo[current].getFile();
	}
	
	/**
	 * Get the full name for the compilation unit scope in name table. Before call this method, the caller 
	 * should have called hasNext() at first. 
	 * 
	 */
	public String getCurrentUnitFullName() {
		if (current < 0 || current >= allInfo.length) return null;
		return getUnitFullName(allInfo[current].getFileFullName());
	}
	
	/**
	 * Get the file content of the current file. Before call this method, the caller should have called hasNext() at first. 
	 * 
	 */
	public String getCurrentFileContent() {
		if (current < 0 || current >= allInfo.length) return null;
		if (allInfo[current].getFileContent() == null) {
			
		}
		return allInfo[current].getFileContent();
	}
	
	/**
	 * Get the compilation unit (i.e. AST root) of the current file. Before call this method, the caller should have called hasNext() at first. 
	 * 
	 */
	public CompilationUnit getCurrentCompilationUnit() {
		if (current < 0 || current >= allInfo.length) return null;
		return allInfo[current].getASTRoot();
	}

	public boolean hasParseErrorInCurrentCompilationUnit() {
		if (current < 0 || current >= allInfo.length) return false;
		return allInfo[current].hasParseError();
	}

	public String getCurrentParseErrorMessage() {
		if (current < 0 || current >= allInfo.length) return null;
		return allInfo[current].getParseErrorMessage();
	}
	
	public int getCurrentFileLineNumber() {
		if (current < 0 || current >= allInfo.length) return -1;
		return allInfo[current].getTotalLines();
	}
	
	public long getCurrentFileSpaces() {
		if (current < 0 || current >= allInfo.length) return -1;
		return allInfo[current].getTotalSpaces();
	}


	public long getTotalLineNumbersOfAllFiles() {
		long result = 0;
		for (int index = 0; index < allInfo.length; index++) result += allInfo[index].getTotalLines();
		
		return result;
	}
	
	public long getTotalSpacesOfAllFiles() {
		long result = 0;
		for (int index = 0; index < allInfo.length; index++) result += allInfo[index].getTotalSpaces();
		
		return result;
	}
	
	public int getFileNumber() {
		return allInfo.length;
	}

	
	/**
	 * Get the file content of the file given by the file simple name (i.e. exclude the path string of the file)
	 */
	public String findFileContentByFileSimpleName(String fileSimpleName) {
		if (allInfo == null) return null;
		for (int index = 0; index < allInfo.length; index++) {
			String simpleName = allInfo[index].getFile().getName();
			if (fileSimpleName.equals(simpleName)) {
				current = index;
				return allInfo[index].getFileContent();
			}
		}
		return null;
	}

	/**
	 * Get the compilation unit (i.e. AST Root) of the file given by the file simple name (i.e. exclude the path string of the file)
	 */
	public CompilationUnit findCompilationUnitByFileSimpleName(String fileSimpleName) {
		if (allInfo == null) return null;
		for (int index = 0; index < allInfo.length; index++) {
			String simpleName = allInfo[index].getFile().getName();
			if (fileSimpleName.equals(simpleName)) {
				current = index;
				return allInfo[index].getASTRoot();
			}
		}
		return null;
	}


	/**
	 * Get the file content of the file given by the file full name (i.e. include the path string of the file)
	 */
	public String findFileContentByUnitFullName(String unitFullName) {
		if (allInfo == null) return null;
		for (int index = 0; index < allInfo.length; index++) {
			String unitName = getUnitFullName(allInfo[index].getFileFullName());
			if (unitFullName.equals(unitName)) {
				current = index;
				return allInfo[index].getFileContent();
			}
		}
		return null;
	}

	/**
	 * Get the compilation unit (i.e. AST Root) of the file given by the file full name (i.e. exclude the path string of the file)
	 */
	public CompilationUnit findCompilationUnitByUnitFullName(String unitFullName) {
		if (allInfo == null) return null;
		for (int index = 0; index < allInfo.length; index++) {
			String unitName = getUnitFullName(allInfo[index].getFileFullName());
			if (unitFullName.equals(unitName)) {
				current = index;
				return allInfo[index].getASTRoot();
			}
		}
		return null;
	}


	/**
	 * Release memory of all file contents
	 */
	public void releaseAllFileContents() {
		if (allInfo == null) return;
		for (int index = 0; index < allInfo.length; index++) allInfo[index].releaseFileContent();
	}
	
	/**
	 * Release memory of all compilation units (i.e. AST root)
	 */
	public void releaseAllCompilatinUnits() {
		if (allInfo == null) return;
		for (int index = 0; index < allInfo.length; index++) allInfo[index].releaseASTRoot();
	}
	
	/**
	 * Release memory of all file contents
	 */
	public void releaseCurrentFileContents() {
		if (allInfo == null) return;
		allInfo[current].releaseFileContent();
	}
	
	/**
	 * Release memory of all compilation units (i.e. AST root)
	 */
	public void releaseCurrentCompilatinUnits() {
		if (allInfo == null) return;
		allInfo[current].releaseASTRoot();
	}
	
	public String getSystemPath() {
		return systemPath;
	}

	class JavaSourceFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) return true;
			if (pathname.isFile() && pathname.getName().endsWith(".java")) return true;
			return false;
		}
	}

	/**
	 * Load all java files in the path given in the constructor method
	 */
	private void loadAllJavaFiles() {
		ArrayList<File> files = getAllJavaSourceFiles(rootFile);
		allInfo = new ParsingFileInfo[files.size()];
		int index = 0;
		for (File file : files) {
			allInfo[index] = new ParsingFileInfo(file);
			index = index + 1;
		}
		current = -1;
	}

	/**
	 * Use JavaSourceFileFilter to get all java source file in the systemPath
	 */
	private ArrayList<File> getAllJavaSourceFiles(String rootPath) {
		ArrayList<File> files = new ArrayList<File>();
		File dir = new File(rootPath);
		if (dir.isFile()) {
			if (dir.getName().endsWith(".java")) files.add(dir);
			return files;
		}
		
		FileFilter filter = new JavaSourceFileFilter();
		File[] temp = dir.listFiles(filter);
		if (temp != null) {
			for (int index = 0; index < temp.length; index++) {
				if (temp[index].isFile()) files.add(temp[index]);
				if (temp[index].isDirectory()) {
					List<File> tempResult = getAllJavaSourceFiles(temp[index].getAbsolutePath());
					for (File file : tempResult) files.add(file);
				}
			}
		}
		return files;
	}
	
	private String getUnitFullName(String fileFullName) {
		return fileFullName.replace(systemPath, "");
	}

}

class ParsingFileInfo {
	private File file = null;
	private String fileContent = null;
	private CompilationUnit root = null;
	
	private String parseErrorMessage = null;
	private boolean hasParseError = false;
	
	private int totalLines = 0; 		// The line number of the file;
	private int totalSpaces = 0;		// The total spaces of the file  
	
	public ParsingFileInfo(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	public String getFileFullName() {
		return file.getPath();
	}
	
	public String getFileContent() {
		if (fileContent == null) loadFile();
		return fileContent;
	}
	
	public CompilationUnit getASTRoot() {
		if (root == null) parseCode();
		return root;
	}
	
	public int getTotalLines() {
		if (totalLines == 0) loadFile();
		return totalLines;
	}
	
	public int getTotalSpaces() {
		if (totalSpaces == 0) loadFile();
		return totalSpaces;
	}

	/**
	 * Set file content to null for release the memory occupied by it, since file content may use many memories. 
	 */
	public void releaseFileContent() {
		fileContent = null;
	}
	
	/**
	 * Set AST root to null for release the memory occupied by it, since AST root may use many memories. 
	 */
	public void releaseASTRoot() {
		root = null;
		parseErrorMessage = null;
		hasParseError = false;		
	}
	
	private void loadFile() {
		if (file == null) return;
		
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(file));
			String line = reader.readLine();
			StringBuffer buffer = new StringBuffer(); 
			while (line != null) {
				buffer.append(line + "\n");
				totalLines = totalLines + 1;
				totalSpaces += line.length();
				line = reader.readLine();
			}
			reader.close();
			fileContent = buffer.toString();
		} catch (IOException exc) {
			fileContent = null;
		}
	}
	
	@SuppressWarnings({ "deprecation", "rawtypes" })
	private void parseCode() {
		if (file == null) return;
		if (fileContent == null) loadFile();
		if (fileContent == null) return;
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// For parsing the source code in Java 1.5, the compile options must be set!
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);

		parser.setSource(fileContent.toCharArray());
		parseErrorMessage = null;
		hasParseError = false;
		root = (CompilationUnit) parser.createAST(null);
	}

	public boolean hasParseError() {
		if (hasParseError) return true;
		if (root == null) return false;
		
		IProblem[] errors = root.getProblems();
		if (errors != null && errors.length > 0) {
			for (int i=0; i < errors.length; ++i) {
				IProblem problem = errors[i];
				if (problem.isError()) {
					hasParseError = true;
					break;
				}
			}
		}
		return hasParseError;
	}
	
	public String getParseErrorMessage() {
		if (parseErrorMessage != null) return parseErrorMessage;
		
		StringBuilder msg = null;
		if (root == null) return null;
		
		IProblem[] errors = root.getProblems();
		if (errors != null && errors.length > 0) {
			msg = new StringBuilder(); 
			for (int i=0; i < errors.length; ++i) {
				IProblem problem = errors[i];
				if (problem.isError()) hasParseError = true;
				String message = "Line " + problem.getSourceLineNumber() + ": " + problem.getMessage();	
				msg.append(message);
				msg.append("\r\n\t\t");
			}
		}
		if (msg != null) parseErrorMessage = msg.toString();
		else parseErrorMessage = null;
		return parseErrorMessage;
	}
}
