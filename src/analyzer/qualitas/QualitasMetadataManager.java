package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.metric.size.CodeLineCounterMetric;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;
import analyzer.dataTable.DataTableManager;
import util.Debug;

/**
 * A single instance class to manager the properties and meta data of qualitas system given by the Qualitas team!
 * @author Zhou Xiaocong
 * @since 2016Äê3ÔÂ17ÈÕ
 * @version 1.0
 */
public class QualitasMetadataManager {
	private DataTableManager systemProperties = null;
	private DataTableManager currentMetadata = null;
	private String currentSystem = null;
	private String currentVersion = null;
	
	private static QualitasMetadataManager uniqueObject = null;
	
	/**
	 * The client need not to construct object
	 */
	private QualitasMetadataManager() {
	}
	
	/**
	 * The client use this method to get the unique instance of the class
	 */
	public static QualitasMetadataManager getInstance() {
		if (uniqueObject == null) uniqueObject = new QualitasMetadataManager();
		return uniqueObject;
	}
	
	public DataTableManager getSystemPropertiesDataTable() {
		return systemProperties;
	}
	
	public DataTableManager getCurrentSystemMetadata() {
		return currentMetadata;
	}
	
	public void generateAllSystemMetadata() {
		String[] systemNames = QualitasPathsManager.getSystemNamesInOriginalPath();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			String[] versions = QualitasPathsManager.getSystemVersionsInOriginalPath(systemNames[i]);
			for (int j = 0; j < versions.length; j++) {
				loadSystemMetadata(systemName, versions[j]);
			}
		}
	}
	
	public void loadSystemMetadata(String systemName, String version) {
		if (currentSystem != null && currentVersion != null && currentMetadata != null) {
			if (systemName.equals(systemName) && version.equals(currentVersion)) return;
		}
		
		currentSystem = systemName;
		currentVersion = version;
		currentMetadata = new DataTableManager("metadata");
		String dataFileName = QualitasPathsManager.getSystemStartPath(systemName) + version + ".mtd";
		File dataFile = new File(dataFileName);
		if (dataFile.exists()) {
			try {
				System.out.println("Read system meta data from file: " + dataFileName);
				currentMetadata.read(dataFileName, true);
				currentMetadata.setKeyColumnIndex("TypeName");
				return;
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		String[] columnNames = {"TypeName", "BinLocation", "SrcLocation", "InSrcPkg", "InBinSrc", "Distributed", "TopLevel", "LOC", "NCLOC"};
		currentMetadata.setColumnNames(columnNames);
		currentMetadata.setKeyColumnIndex("TypeName");

		String metaFileName = QualitasPathsManager.getSystemQualitasMetadataFile(systemName, version);
		File metaFile = new File(metaFileName);
		
		if (!metaFile.exists()) {
			System.out.println("Can not find meta file for system " + systemName + ", version " + version);
			return;
		}
		try {
			Scanner scanner = new Scanner(metaFile);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				line = line.trim();
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;

				String[] lineArray = line.split("\t");
				if (lineArray.length != columnNames.length) {
					scanner.close();
					throw new AssertionError("Error when read meta data file " + metaFileName + ", line: [" + line + "]!");
				}
				currentMetadata.appendLine(lineArray);
			}
			scanner.close();
			currentMetadata.write(dataFileName);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	/**
	 * Load system properties from all .properties file in Qualitas original path!
	 * @return If success, return true, else return false
	 */
	public void loadSystemProperties() {
		systemProperties = new DataTableManager("System Properties");
		
		String dataFileName = QualitasPathsManager.defaultRootPath + "Qualitas.properties";
		File dataFile = new File(dataFileName);
		if (dataFile.exists()) {
			try {
				System.out.println("Read system properties from file:" + dataFileName);
				systemProperties.read(dataFileName, true);
				systemProperties.setKeyColumnIndex("Version");
				return;
			} catch (Exception exc) {
				exc.printStackTrace();
				// We will load the properties again!!
			}
		}
		
		String[] columnNames = {"System", "Version", "Domain", "ReleaseDate", "SourcePackages", "BinaryClassNum", "ClassNum", 
				"FileNum", "TopBinClass", "TopClassNum", "LOC", "NCLOC", "JREVersion", "Fullname", "VersionCount", "Status", 
				"Distribution", "Description", "License", "URL"
		};
		systemProperties.setColumnNames(columnNames);
		systemProperties.setKeyColumnIndex("Version");
		
		String[] systemNames = QualitasPathsManager.getSystemNamesInOriginalPath();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			String[] versions = QualitasPathsManager.getSystemVersionsInOriginalPath(systemNames[i]);
			for (int j = 0; j < versions.length; j++) {
				String version = versions[j];
				
				String propertyFileName = QualitasPathsManager.getSystemQualitasPropertyFile(systemName, version);
				File propertyFile = new File(propertyFileName);
				
				if (!propertyFile.exists()) {
					System.out.println("Can not find property file for system " + systemName + ", version " + version);
					continue;
				}
				try {
					QualitasSystemPropertyStructure properties = new QualitasSystemPropertyStructure();
					Scanner scanner = new Scanner(propertyFile);
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						setSystemPropertyByLine(properties, line);
					}
					appendSystemPropertiesToDataTable(systemProperties, systemName, version, properties);
					scanner.close();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
		try {
			systemProperties.write(dataFileName);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	
	private void setSystemPropertyByLine(QualitasSystemPropertyStructure properties, String line) {
		String[] lineArray = line.split("=");
		String key = lineArray[0].trim();
		String value = lineArray[1].trim();
		if (key.equals("system")) properties.system = value;
		else if (key.equals("sysver")) properties.sysver = value;
		else if (key.equals("domain")) properties.domain = value;
		else if (key.equals("releasedate")) properties.releasedate = value;
		else if (key.equals("sourcepackages")) properties.sourcepackages = value;
		else if (key.equals("fullname")) properties.fullname = value;
		else if (key.equals("status")) properties.status = value;
		else if (key.equals("sysvercount")) properties.sysvercount = value;
		else if (key.equals("description")) properties.description = value;
		else if (key.equals("jreversion")) properties.jreversion = value;
		else if (key.equals("license")) properties.license = value;
		else if (key.equals("distribution")) properties.distribution = value;
		else if (key.equals("n_bin")) properties.n_bin = value;
		else if (key.equals("n_both")) properties.n_both = value;
		else if (key.equals("n_files")) properties.n_files = value;
		else if (key.equals("n_top(bin)")) properties.n_topbin = value;
		else if (key.equals("loc(both)")) properties.locboth = value;
		else if (key.equals("ncloc(both)")) properties.nclocboth = value;
		else if (key.equals("url")) properties.url = value;
	}
	
	private void appendSystemPropertiesToDataTable(DataTableManager table, String systemName, String version, QualitasSystemPropertyStructure properties) {
		String[] line = new String[table.getColumnNumber()];
		line[0] = properties.system;
		line[1] = properties.sysver;
		line[2] = properties.domain;
		line[3] = properties.releasedate;
		line[4] = DataTableManager.replaceSpaceWithSemicolon(properties.sourcepackages);
		line[5] = properties.n_bin;
		line[6] = properties.n_both;
		line[7] = properties.n_files;
		line[8] = properties.n_topbin;
		line[9] = "0";
		line[10] = properties.locboth;
		line[11] = properties.nclocboth;
		line[12] = properties.jreversion;
		line[13] = properties.fullname;
		line[14] = properties.sysvercount;
		line[15] = properties.status;
		line[16] = properties.distribution;
		line[17] = DataTableManager.replaceSpaceWithLaTeXSpace(properties.description);
		line[18] = DataTableManager.replaceSpaceWithLaTeXSpace(properties.license);
		line[19] = properties.url;
		table.appendLine(line);
	}
	
	public String[] getSourcePackagesBySystemVersion(String version) {
		String cellString = systemProperties.getCellValueAsString(version, "SourcePackages");
		String[] packageArray = cellString.split(";");
		return packageArray;
	}
	
	public String[] getSourcePackagesBySystemIndex(int index) {
		String cellString = systemProperties.getCellValueAsString(index, "SourcePackages");
		String[] packageArray = cellString.split(";");
		return packageArray;
	}
	
	public void checkSystemPropertyAndMetadata() throws IOException {
		String reportFileName = QualitasPathsManager.defaultOriginalPath + "property.check";
		PrintWriter reportWriter = new PrintWriter(new File(reportFileName));
		
		if (systemProperties == null) loadSystemProperties();
		int systemNumber = systemProperties.getLineNumber();
		boolean hasChange = false;
		for (int systemIndex = 0; systemIndex < systemNumber; systemIndex++) {
			String systemName = systemProperties.getCellValueAsString(systemIndex, "System");
			String version = systemProperties.getCellValueAsString(systemIndex, "Version");
			String[] sourcePackages = getSourcePackagesBySystemIndex(systemIndex);
//			if (!systemName.equals("antlr")) continue;

			System.out.println("Check system: " + systemName + ", version: " + version);

			loadSystemMetadata(systemName, version);

			int lineNumber = currentMetadata.getLineNumber();
			int nbin = 0;
			int nboth = 0;
			int nfiles = 0;
			int ntopbin = 0;
			int ntopnum = 0;
			int locboth = 0;
			int nclocboth = 0;
			Set<String> sourceFileSet = new TreeSet<String>();
			
			System.out.println("Meta data line number: " + lineNumber + ", source packages number " + sourcePackages.length);
			DataTableManager typeTable = new DataTableManager("type");
			String[] columnNames = {"Type", "SourceFile", "Location", "TopLevel", "Distributed", "LOC", "NCLOC"};
			typeTable.setColumnNames(columnNames);
			int flagError = 0;
			for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
				String[] lineArray = currentMetadata.getLineAsStringArray(lineIndex);
				if (isTypeInSourcePackages(lineArray[0], sourcePackages) != isFlagInSourcePackages(lineArray[3])) {
					reportWriter.println("\tFlag = " + lineArray[3] + ", type: " + lineArray[0]);
					flagError++;
				}
				
				if (isFlagInSourcePackages(lineArray[3])) {
					if (isFlagInBinaryFile(lineArray[4])) {
						String[] typeLineArray = new String[columnNames.length];
						typeLineArray[0] = lineArray[0];
						typeLineArray[1] = lineArray[2];
						
						nbin++;

						if (isFlagInSourceFile(lineArray[4])) {
							sourceFileSet.add(lineArray[2]);

							nboth++;
							if (isFlagTopLevelType(lineArray[6])) ntopnum++;
							
							locboth += Integer.parseInt(lineArray[7]);
							nclocboth += Integer.parseInt(lineArray[8]);
							
							typeLineArray[2] = "source";
						} else typeLineArray[2] = "binary";
						
						if (isFlagTopLevelType(lineArray[6])) {
							ntopbin++;
							if (isFlagTopLevelPublic(lineArray[6])) typeLineArray[3] = "public";
							else typeLineArray[3] = "top-nonpublic";
						} else typeLineArray[3] = "nontop";
						
						if (isFlagDistributed(lineArray[5])) typeLineArray[4] = "distributed";
						else typeLineArray[4] = "non-distributed";
						
						typeLineArray[5] = lineArray[7];
						typeLineArray[6] = lineArray[8];
						typeTable.appendLine(typeLineArray);
					}
				}
			}
			nfiles = sourceFileSet.size();
			String fileListName = QualitasPathsManager.getSystemPath(systemName, version, QualitasPathsManager.defaultOriginalPath) + "filelist.txt";
			String typeListFileName = QualitasPathsManager.getSystemPath(systemName, version, QualitasPathsManager.defaultOriginalPath) + "typelist.txt";
			try {
				PrintWriter fileListWriter = new PrintWriter(new FileOutputStream(new File(fileListName)));
				for (String fileName : sourceFileSet) fileListWriter.println(fileName);
				fileListWriter.close();
				typeTable.write(typeListFileName);
				typeTable.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			
			int binaryClassNum = systemProperties.getCellValueAsInt(systemIndex, "BinaryClassNum");
			int classNum = systemProperties.getCellValueAsInt(systemIndex, "ClassNum");
			int fileNum = systemProperties.getCellValueAsInt(systemIndex, "FileNum");
			int topBinClassNum = systemProperties.getCellValueAsInt(systemIndex, "TopBinClass");
			int topClassNum = systemProperties.getCellValueAsInt(systemIndex, "TopClassNum");
			int loc = systemProperties.getCellValueAsInt(systemIndex, "LOC");
			int ncloc = systemProperties.getCellValueAsInt(systemIndex, "NCLOC");

			boolean hasError = false;
			if (nbin != binaryClassNum) {
				reportWriter.println("\tProperty n_bin[" + binaryClassNum + "] != binary class num[" + nbin + "]!");
				hasError = true;
			}
			if (nboth != classNum) {
				reportWriter.println("\tProperty n_both[" + classNum + "] != class num[" + nboth + "]!");
				hasError = true;
			}
			if (nfiles != fileNum) {
				reportWriter.println("\tProperty n_files[" + fileNum + "] != file num[" + nfiles + "]!");
				hasError = true;
			}
			if (ntopbin != topBinClassNum) {
				reportWriter.println("\tProperty n_top(bin)[" + topBinClassNum + "] != top bin class num[" + ntopbin + "]!");
				hasError = true;
			}
			
			if (topClassNum <= 0) {
				systemProperties.setCellValue(systemIndex, "TopClassNum", ntopnum);
				hasChange = true;
			} else if (topClassNum > 0 && ntopnum != topClassNum) {
				reportWriter.println("\tProperty n_top(src)[" + topClassNum + "] != top class num[" + ntopnum + "]!");
				systemProperties.setCellValue(systemIndex, "TopClassNum", ntopnum);
				hasChange = true;
				hasError = true;
			} 
			if (locboth != loc) {
				reportWriter.println("\tProperty loc(both)[" + loc + "] != loc[" + locboth + "]!");
				hasError = true;
				systemProperties.setCellValue(systemIndex, "LOC", locboth);
				hasChange = true;
			}
			if (nclocboth != ncloc) {
				reportWriter.println("\tProperty ncloc(both)[" + ncloc + "] != ncloc[" + nclocboth + "]!");
				hasError = true;
				systemProperties.setCellValue(systemIndex, "NCLOC", nclocboth);
				hasChange = true;
			}
			if (flagError > 0) {
				reportWriter.println("\tHave in source package error: " + flagError);
				hasError = true;
			}
			if (hasError) reportWriter.println("Checked system: " + systemName + ", version: " + version);
		}
		reportWriter.close();
		if (hasChange) {
			String dataFileName = QualitasPathsManager.defaultRootPath + "Qualitas.properties";
			systemProperties.write(dataFileName);
		}
	}
	
	private boolean isTypeInSourcePackages(String fullTypeName, String[] sourcePackages) {
		for (int i = 0; i < sourcePackages.length; i++) {
			if (fullTypeName.startsWith(sourcePackages[i])) return true;
		}
		return false;
	}
	
	private boolean isFlagInSourcePackages(String flag) {
		return flag.charAt(0) == '0';
	}
	
	private boolean isFlagTopLevelType(String flag) {
		return flag.charAt(0) != '2'; // || flag.charAt(0) == '4' || flag.charAt(0) == '1';
	}
	
	private boolean isFlagTopLevelPublic(String flag) {
		return flag.charAt(0) == '0'; 
	}

	private boolean isFlagInBinaryFile(String flag) {
		return flag.charAt(0) == '0' || flag.charAt(0) == '1';
	}
	
	private boolean isFlagInSourceFile(String flag) {
		return flag.charAt(0) == '0' || flag.charAt(0) == '2';
	}

	private boolean isFlagDistributed(String flag) {
		return flag.charAt(0) == '0';
	}
	
	public void compareMetadataAndNameTable() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String reportFileName = QualitasPathsManager.defaultDebugPath + "QualitasVsOur.txt";
		DataTableManager reportTable = new DataTableManager("");
		String[] columnNames = {"System", "Version", "QualitasFile", "OurFile", "OurError"};
		reportTable.setColumnNames(columnNames);

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
			
			if (!systemName.equals("jchempaint")) continue;
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++) {
				try {
					Debug.println("Check system " + systemName + ", version " + versions[j]);
					compareMetadataAndNameTable(reportTable, systemName, versions[j]);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
		try {
			reportTable.write(reportFileName);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		Debug.time("End....");
	}
	
	public void compareMetadataAndNameTable(DataTableManager reportTable, String systemName, String version) throws IOException {
		loadSystemProperties();
		int nfiles = systemProperties.getCellValueAsInt(version, "FileNum");

		loadSystemMetadata(systemName, version);
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String reportFileName = QualitasPathsManager.getSystemStartPath(systemName) + "compare-" + version + ".txt";
		String typeListFileName =  systemPath + "typelist-incheck" + ".txt";
		DataTableManager errorTable = null;
		
		int errorNumber = 0;
		try {
			String errorFileName = systemPath + "error.txt";
			errorTable = new DataTableManager("Error");
			errorTable.read(errorFileName, true);
		} catch (Exception exc) {
			exc.printStackTrace();
			errorTable = null;
		}
		
		PrintWriter reportWriter = new PrintWriter(new File(reportFileName));
		PrintWriter typeListWriter = new PrintWriter(new File(typeListFileName));
		
		SourceCodeFileSet parser = new SourceCodeFileSet(systemPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		System.out.println("Begin creating system, path = " + systemPath);
		NameTableManager nameTable = creator.createNameTableManager();
		SoftwareStructManager structManager = new SoftwareStructManager(nameTable);
		CodeLineCounterMetric metric = new CodeLineCounterMetric();
		metric.setSoftwareStructManager(structManager);

		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new AllTypesFilter());
		SystemScope rootScope = nameTable.getSystemScope();
		rootScope.accept(visitor);
		List<NameDefinition> allTypes = visitor.getResult();
		
		typeListWriter.println("Type\tSourceFile\tLocation\tTopLeve\tDistributed\tLOC\tNCLOC");
		Set<CompilationUnitScope> usedUnitScopes = new TreeSet<CompilationUnitScope>();
		int lineNumber = currentMetadata.getLineNumber();
		for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
			String[] lineArray = currentMetadata.getLineAsStringArray(lineIndex);
			String fullTypeName = lineArray[0];
			String memberTypeName = lineArray[0].replace("$", ".");
//			System.out.println("\tCheck type in meta data: " + fullTypeName);
			
//			boolean isAnonymous = isAnonymousTypeName(fullTypeName);
			boolean inBinary = false;
			boolean isTopLevel = false;
			boolean inBoth = false;
			boolean isPublic = false;
			boolean isDistributed = false;
			int locInMetadata = 0;
			int nclocInMetadata = 0;
			if (isFlagTopLevelType(lineArray[6])) isTopLevel = true;
			if (isFlagTopLevelPublic(lineArray[6])) isPublic = true;
			if (isFlagDistributed(lineArray[5])) isDistributed = true;
			if (isFlagInSourcePackages(lineArray[3])) {
				if (isFlagInBinaryFile(lineArray[4])) {
					inBinary = true;
					if (isFlagInSourceFile(lineArray[4])) {
						inBoth = true;
						locInMetadata = Integer.parseInt(lineArray[7]);
						nclocInMetadata += Integer.parseInt(lineArray[8]);
					}
				}
			}
			
			if (!isDistributed) continue;
			
			TypeDefinition foundType = null;
			for (NameDefinition type : allTypes) {
				String qualifiedName = type.getFullQualifiedName();
				if (qualifiedName.equals(fullTypeName) || qualifiedName.equals(memberTypeName)) {
					foundType = (TypeDefinition)type;
					break;
				}
			}
			
			if (foundType != null) {
				if (isTopLevel != foundType.isPackageMember()) {
					reportWriter.println("\tType " + fullTypeName + " top level flag is not consistent with package member in name table!");
				}
				boolean isPublicType = foundType.isPublic();
				if (isTopLevel && isPublic != isPublicType) {
					reportWriter.println("\tType " + fullTypeName + " public is not consistent with public member in name table!");
				}
				
				// If add all units include found type, and then all units in name table can be found in meta data file!
//				CompilationUnitScope unitScope = nameTable.getEnclosingCompilationUnitScope(foundType);
//				usedUnitScopes.add(unitScope);
				
//				typeListWriter.println("Type\tSourceFile\tLocation\tTopLeve\tDistributed\tLOC\tNCLOC");
				if (inBinary) {
					StringBuffer typeListContentBuffer = new StringBuffer();
					typeListContentBuffer.append(foundType.getFullQualifiedName() + "\t");
					// We just want to find what compilation units include the types in source packages and in binary files!!
					// And then those name table units only in source packages can not be found in meta file!  
					CompilationUnitScope unitScope = nameTable.getEnclosingCompilationUnitScope(foundType);
					usedUnitScopes.add(unitScope);
					int locInUnitScope = 0;
					int nclocInUnitScope = 0;
					if (isTopLevel && inBoth && isPublic) {
						// We check if the LOC and NCLOC of top level class in meta data are consistent with the data in name table 
						metric.setMeasuringObject(unitScope);
						SoftwareMeasure measure = new SoftwareMeasure("LOPT");
						metric.calculate(measure);
						locInUnitScope = (int)measure.getValue();
						measure = new SoftwareMeasure("ELOC");
						metric.calculate(measure);
						nclocInUnitScope = (int)measure.getValue();
						
						if (locInMetadata != locInUnitScope) {
							// Our ELOC may be not equal to ncloc in meta file, since some line with notation '@', but actually an 
							// effective line which will be not include in our ELOC.
							reportWriter.println("\tType " + fullTypeName + " in meta data [loc, ncloc] is [" + locInMetadata + ", " + nclocInMetadata + "], but in unit scope [" + unitScope.getUnitName() + "] is [" + locInUnitScope + ", " + nclocInUnitScope + "]!");
						}
					} 

					typeListContentBuffer.append(unitScope.getUnitName() + "\t");
					if (inBoth) typeListContentBuffer.append("source\t");
					else typeListContentBuffer.append("binary\t");
					if (foundType.isPackageMember()) {
						if (isPublicType) typeListContentBuffer.append("public\t");
						else typeListContentBuffer.append("top-nonpublic\t");
					} else typeListContentBuffer.append("nontop\t");
					typeListContentBuffer.append("distributed\t" + locInUnitScope + "\t" + nclocInUnitScope);
					typeListWriter.println(typeListContentBuffer.toString());
				}
			} else if (isTopLevel){
				if (inBoth) {
					boolean isErrorUnit = false;
					if (errorTable != null) {
						String sourceLocation = lineArray[2].replace('/', '\\');
						int errorLineNumber = errorTable.getLineNumber();
						for (int errorIndex = 0; errorIndex < errorLineNumber; errorIndex++) {
							String unitFileName = errorTable.getCellValueAsString(errorIndex, "File");
							if (sourceLocation.endsWith(unitFileName) || unitFileName.endsWith(sourceLocation)) {
								isErrorUnit = true;
								break;
							}
						}
					}
					if (isErrorUnit) {
						reportWriter.println("\tIn both binary and source file top level type " + fullTypeName + " is in error unit!");
						errorNumber++;
					} else reportWriter.println("\tIn both binary and source file top level type " + fullTypeName + " can NOT be found in name table!");
				} else if (inBinary) {
					reportWriter.println("\tIn binary file top level type " + fullTypeName + " can NOT be found in name table!");
				}
			}
		}
		
		int usedUnitNumber = usedUnitScopes.size();
		reportWriter.println("File number in property " + nfiles + ", and used unit " + usedUnitNumber + ", and in error unit " + errorNumber);
		Debug.println("\tFile number in property " + nfiles + ", and used unit " + usedUnitNumber + ", and in error unit " + errorNumber);
	
		String fileListName = QualitasPathsManager.getSystemPath(systemName, version) + "filelist.txt";
		try {
			PrintWriter fileListWriter = new PrintWriter(new FileOutputStream(new File(fileListName)));
			for (CompilationUnitScope scope: usedUnitScopes) fileListWriter.println(scope.getUnitName());
			fileListWriter.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		String[] lineArray = new String[5];
		lineArray[0] = systemName;
		lineArray[1] = version;
		lineArray[2] = nfiles+"";
		lineArray[3] = usedUnitNumber+"";
		lineArray[4] = errorNumber+"";
		reportTable.appendLine(lineArray);
		
		reportWriter.close();
		typeListWriter.close();
	}
	
/*
	private boolean isTypeInSourcePackages(TypeDefinition type, String[] sourcePackages) {
		PackageDefinition packageDef = type.getEnclosingPackage();
		if (packageDef != null) return isTypeInSourcePackages(packageDef.getFullQualifiedName(), sourcePackages);
		else return false;
	}

	private boolean isUnitInSourcePackages(CompilationUnitScope scope, String[] sourcePackages) {
		PackageDefinition packageDef = scope.getPackage();
		return isTypeInSourcePackages(packageDef.getFullQualifiedName(), sourcePackages);
	}
	
	private boolean isAnonymousTypeName(String name) {
		if (!name.contains("$")) return false;
		char ch = name.charAt(name.length()-1);
		return (ch >= '0' && ch <= '9');
	}
*/	
	public static void main(String[] args) {
		QualitasMetadataManager manager = QualitasMetadataManager.getInstance();
		
		try {
//			manager.checkSystemPropertyAndMetadata();
			manager.compareMetadataAndNameTable();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}

class QualitasSystemPropertyStructure {
	String system = "NA";
	String sysver = "NA";
	String fullname = "NA";
	String domain = "NA";
	String status = "NA";
	String sysvercount = "NA";
	String description = "NA";
	String jreversion = "NA";
	String license = "NA";
	String distribution = "NA";
	String releasedate = "NA";
	String sourcepackages = "NA";
	String n_bin = "NA";
	String n_both = "NA";
	String n_files = "NA";
	String n_topbin = "NA";
	String locboth = "NA";
	String nclocboth = "NA";
	String url = "NA";
}

// Columns in meta data
//#1. Fully-qualified type name
//#2. location in bin
//#3. location in src
//#4. 0=in src pkg (user-defined), 1=not in src pkg. See 'Source Packages' above
//#5. 0=both bin and src, 1=bin only, 2=src only
//#6. 0=distributed, 1 = not distributed. Source code in 'Distributed' above
//#7. 0=top-level public, 4=top-level non-public, 1=top-level different name, 2=nested, 3=probably haven't seen source, -1 no source, or not parsed
//#8. LOC - physical lines in file (for public type only)
//#9. NCLOC - non-commented lines of code in file (for public type only)

class AllTypesFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isTypeDefinition()) return false;
		TypeDefinition type = (TypeDefinition)definition;
		if (type.isDetailedType() || type.isEnumType()) return true;	
		return false;
	}
}

