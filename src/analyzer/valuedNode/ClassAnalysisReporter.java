package analyzer.valuedNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import softwareChange.ClassChangeIndicator;
import softwareChange.NodeChangeIndicator;
import sourceCodeAST.SourceCodeFileSet;
import sourceCodeAST.SourceCodeLocation;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * A tools for generating many kinds of valued node (manager)
 * @author Zhou Xiaocong
 * @since 2014/1/25
 * @version 1.0
 */
public class ClassAnalysisReporter {
	private String systemPath = null;
	private String[] versionPaths = null;

	private String firstValuePath = null;
	private String[] firstValueFilePostfix = null;
	
	private String secondValuePath = null;
	private String[] secondValueFilePostfix = null;

	private String indicatorPath = null;
	private String indicatorFilePostfix = null;
	
	private String reportFilePath = null;
	private String reportFilePostfix = null;
	
	public ClassAnalysisReporter(String systemPath, String[] versionPaths) {
		this.systemPath = systemPath;
		this.versionPaths = versionPaths;
	}

	/**
	 * Set the path and file name post-fix of the class change indicator
	 */
	public void setIndicatorInformation(String indicatorPath, String indicatorFilePostfix) {
		this.indicatorPath = indicatorPath;
		this.indicatorFilePostfix = indicatorFilePostfix;
	}
	
	/**
	 * Set the path and file name post-fix of the first and second values. We can use a file to store the node labels and corresponding values, also 
	 * we can use two files, one store the node labels, and the other one store the corresponding values. 
	 */
	public void setValueInformation(String firstValuePath, String[] firstValueFilePostfix, String secondValuePath, String[] secondValueFilePostfix) {
		this.firstValuePath = firstValuePath;
		this.firstValueFilePostfix = firstValueFilePostfix;
		
		if (secondValuePath != null) {
			this.secondValuePath = secondValuePath;
			this.secondValueFilePostfix = secondValueFilePostfix;
		} else {
			this.secondValuePath = firstValuePath;
			this.secondValueFilePostfix = firstValueFilePostfix;
		}
	}

	/**
	 * Set the path and file name post-fix of the report 
	 */
	public void setReportInformation(String reportFilePath, String reportFilePostfix) {
		this.reportFilePath = reportFilePath;
		this.reportFilePostfix = reportFilePostfix;
	}
	
	/**
	 * Generate all value comparison report files for all program versions. We use an indicator file to match the node label 
	 * in two different program versions, and write those matched nodes' two values to the report file for each program version pair. 
	 *
	 * @param threshold : only those node whose value are greater than or equal to the threshold are used to do comparison!
	 *  
	 * @precondition : set all file informations, i.e. set indicator file information, value file information and report file information 
	 */
	public void generateAllValueComparisonReportFiles(double threshold) throws IOException {
		generateAllComparisonReportFiles(threshold, true, false);
	}

	/**
	 * Generate all original rank comparison report files for all program versions. We use an indicator file to match the node label 
	 * in two different program versions, and calculate their original rank (i.e. the rank include those unmatched nodes), and then 
	 * write all nodes' two ranks to the report file for each program version pair. 
	 *
	 * @param threshold : only those node whose value are greater than or equal to the threshold are used to do comparison!
	 *  
	 * @precondition : set all file informations, i.e. set indicator file information, value file information and report file information 
	 */
	public void generateAllOriginalRankComparisonReportFiles(double threshold) throws IOException {
		generateAllComparisonReportFiles(threshold, false, true);
	}
	

	/**
	 * Generate all related rank comparison report files for all program versions. We use an indicator file to match the node label 
	 * in two different program versions, and calculate their related rank (i.e. the rank only include those matched nodes), and then 
	 * write all nodes' two ranks to the report file for each program version pair. 
	 *
	 * @param threshold : only those node whose value are greater than or equal to the threshold are used to do comparison!
	 *  
	 * @precondition : set all file informations, i.e. set indicator file information, value file information and report file information 
	 */
	public void generateAllRelatedRankComparisonReportFiles(double threshold) throws IOException {
		generateAllComparisonReportFiles(threshold, false, false);
	}
	
	
	/**
	 * Generate all value or rank comparison report files for all program versions. We use an indicator file to match the node label 
	 * in two different program versions, compare their values or calculate their ranks, and write those matched nodes' two ranks or values to 
	 * the report file for each program version pair. 
	 *
	 * @param threshold : only those node whose value are greater than or equal to the threshold are used to do comparison! 
	 * @param orignalOrRelatedRank : if orignalOrRelatedRank == true, comparing by original rank (i.e. the rank include those unmatched nodes), else 
	 * 		comparing by related rank (i.e. only include those matched nodes)
	 *  
	 * @precondition : set all file informations, i.e. set indicator file information, value file information and report file information 
	 */
	public void generateAllComparisonReportFiles(double threshold, boolean valueOrRank, boolean originalOrRelatedRank) throws IOException {
		for (int firstIndex = 0; firstIndex < versionPaths.length; firstIndex++) {
			String firstValue = firstValuePath + versionPaths[firstIndex] + firstValueFilePostfix[0];
			String firstValueAux = null;
			if (firstValueFilePostfix[1] != null) firstValueAux = firstValuePath + versionPaths[firstIndex] + firstValueFilePostfix[1];

			for (int secondIndex = 0; secondIndex < versionPaths.length; secondIndex++) {
				if (secondIndex == firstIndex) continue;
				String secondValue = secondValuePath + versionPaths[secondIndex] + secondValueFilePostfix[0];
				String secondValueAux = null;
				if (secondValueFilePostfix[1] != null) secondValueAux = secondValuePath + versionPaths[secondIndex] + secondValueFilePostfix[1];

				String reportFile = reportFilePath + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + reportFilePostfix;
				PrintWriter report = new PrintWriter(new FileWriter(new File(reportFile)));

				ValuedNodeManager firstValuedNodeManager = createNodeManager(firstValue, firstValueAux, threshold);
				ValuedNodeManager secondValuedNodeManager = createNodeManager(secondValue, secondValueAux, threshold);
				
				String indicatorFile = indicatorPath + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + indicatorFilePostfix;
				NodeChangeIndicator indicator = new NodeChangeIndicator(indicatorFile);
				indicator.loadBase(versionPaths[firstIndex]);
				indicator.loadContrast(versionPaths[secondIndex]);
				ValuedNodesComparator.setNodeChangerIndicator(indicator);
				
				String title = "Compare " + versionPaths[firstIndex] + " with " + versionPaths[secondIndex];
				System.out.println(title);
				
				if (valueOrRank == true) ValuedNodesComparator.generateValuePairs(title, firstValuedNodeManager, secondValuedNodeManager, report);
				else if (originalOrRelatedRank == true) ValuedNodesComparator.compareByOriginalRank(firstValuedNodeManager, secondValuedNodeManager, report);
				else ValuedNodesComparator.compareByRelatedRank(firstValuedNodeManager, secondValuedNodeManager, report);

				report.close();
			}
		}
	}

	
	/**
	 * Generator change indicator files for all program versions. An indicator file can be used to test if two node label in different versions can be 
	 * regarded as same node (i.e. same entity, such as detailed types, methods, fields, or variables in different program versions).
	 * @param fileNamePostfix : gives the post-fix of the file name of the indicator file for each program version pair
	 *  
	 * @precondition : set indicator file information 
	 */
	public void generateAllClassChangeIndicatorFiles() throws IOException {
		for (int firstIndex = 0; firstIndex < versionPaths.length; firstIndex++) {
			NameTableManager firstManager = generateNameTableManager(firstIndex);
			for (int secondIndex = 0; secondIndex < versionPaths.length; secondIndex++) {
				if (secondIndex == firstIndex) continue;

				NameTableManager secondManager = generateNameTableManager(firstIndex);
				String indicatorFile = indicatorPath + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + indicatorFilePostfix;

				NodeChangeIndicator indicator = ClassChangeIndicator.generateClassChangeIndicator(versionPaths[firstIndex], firstManager, versionPaths[secondIndex], secondManager, indicatorFile);
				indicator.write();
			}
		}
	}

	/**
	 * Generate matched ratio for all indicator files of all program versions. 
	 * @param reportFile : gives the file name for writing the report
	 * @param indicatorFileNamePostfix : gives the post-fix of the file name of the indicator file for each program version pair
	 * 
	 * @precondition : set indicator file information 
	 */
	public void generateAllIndicatorMatchedRatiosReport(String reportFile) throws IOException {
		PrintWriter report = new PrintWriter(new FileWriter(new File(reportFile)));
		
		for (int firstIndex = 0; firstIndex < versionPaths.length; firstIndex++) {
			for (int secondIndex = 0; secondIndex < versionPaths.length; secondIndex++) {
				if (secondIndex == firstIndex) continue;
				
				String indicatorFile = indicatorPath + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + indicatorFilePostfix;
				NodeChangeIndicator indicator = new NodeChangeIndicator(indicatorFile); 
				indicator.loadBase(versionPaths[firstIndex]);
				indicator.loadContrast(versionPaths[secondIndex]);
				indicator.generateMatchedRatioReport(report);
			}
		}
	}
	
	/**
	 * Generate check reports for all indicator files of all program versions. Such report gives the problems in matching two node labels in 
	 * different program versions in the indicator file.
	 * @param reportFileNamePostfix : gives the post-fix of the file name of the report file for each version
	 * @param indicatorFileNamePostfix : gives the post-fix of the file name of the indicator file for each program version pair
	 *
	 * @precondition : set indicator file information and report file information 
	 */
	public void generateAllClassChangeIndicatorCheckReports() throws IOException {
		for (int firstIndex = 0; firstIndex < versionPaths.length; firstIndex++) {
			NameTableManager firstManager = generateNameTableManager(firstIndex);

			for (int secondIndex = 0; secondIndex < versionPaths.length; secondIndex++) {
				if (secondIndex == firstIndex) continue;
				
				NameTableManager secondManager = generateNameTableManager(secondIndex);
				String indicatorFile = indicatorPath + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + indicatorFilePostfix;

				System.out.println("Begin check indicator [" + indicatorFile + "]");
				ClassChangeIndicator indicator = new ClassChangeIndicator(indicatorFile); 
				indicator.loadBase(versionPaths[firstIndex]);
				indicator.loadContrast(versionPaths[secondIndex]);
				
				String reportFile = reportFilePath + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + reportFilePostfix;
				PrintWriter report = new PrintWriter(new FileWriter(new File(reportFile)));
				report.println("\r\nCheck version [" + versionPaths[firstIndex] + "] with [" + versionPaths[secondIndex] + "]");
				indicator.generateCheckReport(firstManager, secondManager, report);
				
				System.out.println("End check indicator....");
			}
		}
	}

	
	/**
	 * Generate detailed type (i.e. class with detailed definition) lengths of all given versions, and write to a file for each version. 
	 * The class length can be regarded as an important scale of the classes, and to generate comparison  
	 * report for class lengths in different versions. 
	 * @param useFirstOrSecondValueFile : use the first value file information or use the second value file information
	 * @precondition : set value file information 
	 */
	public void generateAllDetailedTypeLengthFiles(boolean useFirstOrSecondValueFile) throws IOException {
		for (int index = 0; index < versionPaths.length; index++) {
//			if (index != 0) continue;
			
			String reportFile = firstValuePath + versionPaths[index] + firstValueFilePostfix[0];
			if (useFirstOrSecondValueFile == false) reportFile = secondValuePath + versionPaths[index] + secondValueFilePostfix[0];

			System.out.println("Generate " + reportFile);
			
			String title = "Id\tClass Name\tClass Length"; 
			ValuedNodeManager firstValuedNodeManager = generateDetailedTypeLength(index);
			firstValuedNodeManager.write(reportFile, title, false);
		}
	}
	
	/**
	 * Generate the detailed type lengths of a program version given by the index, and store the data to a valued node manager
	 */
	public ValuedNodeManager generateDetailedTypeLength(int versionIndex) {
		NameTableManager table = generateNameTableManager(versionIndex);

		List<DetailedTypeDefinition> typeList = table.getSystemScope().getAllDetailedTypeDefinitions();
		ValuedNodeManager manager = new ValuedNodeManager();
		
		for (int index = 0; index < typeList.size(); index++) {
			DetailedTypeDefinition type = typeList.get(index);
			SourceCodeLocation start = type.getLocation();
			SourceCodeLocation end = type.getEndLocation();
			String id = "" + (index+1);
			String locationString = start.getUniqueId();
			String label = type.getSimpleName() + "@" + locationString;
			int value = end.getLineNumber() - start.getLineNumber() + 1;
			
			ValuedClassNode node = new ValuedClassNode(id, label);
			node.setDefinition(type);
			node.setValue(value);
			
			manager.addValuedNode(node);
		}
		
		return manager;
	}
	
	
	/**
	 * Generate the name table manager for the program version given by versionIndex.
	 */
	public NameTableManager generateNameTableManager(int versionIndex) {
		String path = systemPath + versionPaths[versionIndex] + "\\";
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);
		NameTableManager table = creator.createNameTableManager();
		parser.releaseAllASTs();
		parser.releaseAllFileContents();

		return table;
	}

	
	private ValuedNodeManager createNodeManager(String valueFile, String valueFileAux, double minimalValue) throws IOException {
		ValuedNodeManager nodeManager = new ValuedNodeManager();
		if (valueFileAux != null) nodeManager.read(valueFile, valueFileAux, ValuedNodeKind.VNK_CLASS);
		else nodeManager.read(valueFile, ValuedNodeKind.VNK_CLASS, true);

		if (minimalValue > 0) nodeManager = nodeManager.getManagerCopy(minimalValue);
		nodeManager.sortNodeByValue(false);
		
		return nodeManager;
	}

}
