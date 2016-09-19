package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import analyzer.dataTable.DataTableManager;
import analyzer.valuedNode.ValuedClassNode;
import analyzer.valuedNode.ValuedNodeManager;
import nameTable.NameDefinitionVisitor;
import nameTable.NameTableFilter;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameScope.SystemScope;
import softwareMeasurement.ClassMeasurement;
import softwareMeasurement.measure.MeasureObjectKind;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.SoftwareStructManager;
import util.Debug;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class ClassMetricCollector {
	

	public static void main(String[] args) {
		collectAllQualitasDetailedTypeMeasure(true);
//		summaryQualitasRecentSystemsMeasureIntoOneFile();
//		combineQualitasRecentSystemsMeasureIntoOneFile();
	}

	
	public static void collectAllQualitasDetailedTypeMeasure(boolean recollect) {
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
		Debug.disable();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			
//			if (systemName.equals("jre") || systemName.equals("eclipse_SDK") || systemName.equals("netbeans")) continue;
//			if (!systemName.equals("eclipse_SDK")) continue;
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++) {
				collectQualitasDetailedTypeMeasure(systemName, versions[j], recollect);
			}
		}
		Debug.enable();
		Debug.time("End....");
		output.close();
	}
	
	public static void modifyAllQualitasDetailedTypeMeasure() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		List<SoftwareMeasure> measureList = getRecollectDetailedTypeMeasureList();

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
		Debug.disable();
		for (int i = 0; i < systemNames.length; i++) {
			String systemName = systemNames[i];
			
//			if (systemName.equals("jre") || systemName.equals("eclipse_SDK") || systemName.equals("netbeans")) continue;
			if (!systemName.equals("eclipse_SDK")) continue;
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			for (int j = 0; j < versions.length; j++) {
				modifyQualitasDetailedTypeMeasure(systemName, versions[j], measureList);
			}
		}
		Debug.enable();
		Debug.time("End....");
		output.close();
	}
	
	public static void modifyQualitasDetailedTypeMeasure(String systemName, String version, List<SoftwareMeasure> measureList) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_CLASS, systemName, version, false);

		File resultFile = new File(result);
		if (!resultFile.exists()) return;

		DataTableManager manager = new DataTableManager("metric");

		try {
			manager.read(result, true);
			manager.setKeyColumnIndex("Lable");
			modifyQualitasDetailedTypeMeasure(systemPath, manager, measureList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void modifyQualitasDetailedTypeMeasure(String systemPath, DataTableManager metricDataManager, List<SoftwareMeasure> measureList) {
		SourceCodeParser parser = new SourceCodeParser(systemPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.time("Begin creating system, path = " + systemPath);
		Debug.disable();
		NameTableManager manager = creator.createNameTableManager(true);
		Debug.enable();
		Debug.time("End creating.....");
		Debug.flush();
		
		Debug.time("Begin creating structure, path = " + systemPath);
		Debug.disable();
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		structManager.readOrCreateSoftwareStructure();
//		structManager.createSoftwareStructure();
		Debug.enable();
		Debug.time("End creating.....");
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeFilter());
		SystemScope rootScope = manager.getRootScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		Debug.time("Begin scan class....");
//		Debug.disable();
		int counter = 1;
		int totalCounter = definitionList.size();
		List<DetailedTypeDefinition> allTypes = rootScope.getAllDetailedTypeDefinition();
		
		System.out.println("total = " + totalCounter + ", all types size = " + allTypes.size());
		
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			int lineIndex = metricDataManager.getLineIndex(type.getUniqueId());
			
			Debug.time("Scan class " + counter + " : " + type.getFullQualifiedName() + ", total " + totalCounter);
			
			if (lineIndex > 0) {
				ClassMeasurement measurement = new ClassMeasurement(type, structManager);
				measurement.getMeasureList(measureList);

				for (SoftwareMeasure measure : measureList) {
					metricDataManager.setCellValue(lineIndex, measure.getIdentifier(), measure.valueString());
				}
			}

			Debug.time("End scan, remain " + (totalCounter - counter) + " classes .....");
			counter = counter + 1;
		}
		Debug.enable();
		Debug.time("End scan.....");
	}
	

	public static void summaryQualitasRecentSystemsMeasureIntoOneFile() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Qualits(cls).txt" ;
		
		DataTableManager dataTable = new DataTableManager("Qualitas(cls)");
		String[] columnNameArray = {"System", "CLS", "ELOC", "BLOC", "CLOC", "NLOC", "LOC", "LOPT", "FLD", "MTHD", "PARS", "LOCV", "STMN", "BYTE", "WORD", "CHAR", "Notes"};
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Begin....");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_CLASS, systemName, version, false);

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				
				String[] lineArray = new String[columnNameArray.length];
				lineArray[0] = version;
				lineArray[1] = "" + tempTable.getLineNumber();
				lineArray[lineArray.length-1] = result;
				for (int columnIndex = 2; columnIndex < columnNameArray.length-1; columnIndex++) {
					String columnName = columnNameArray[columnIndex];
					Debug.println("\tGet column: " + columnName);
					double sum = 0.0;
					double[] columnValues = tempTable.getColumnAsDoubleArray(columnName);
					for (int valueIndex = 0; valueIndex < columnValues.length; valueIndex++) sum += columnValues[valueIndex];
					lineArray[columnIndex] = "" + sum;
					Debug.println("\tGet column: " + columnName + ", sum value = " + sum);
				}
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End....");
	}
	
	public static void combineQualitasRecentSystemsMeasureIntoOneFile() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Qualits-class-all.txt" ;
		
		DataTableManager dataTable = new DataTableManager("class all");
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Begin....");
		
		boolean first = true;
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_CLASS, systemName, version, false);

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				if (first) {
					dataTable.read(result, true);
					first = false;
					continue;
				}
				
				tempTable.read(result, true);
				int lineNumber = tempTable.getLineNumber();
				for (int i = 0; i < lineNumber; i++) {
					String[] lineArray = tempTable.getLineAsStringArray(i);
					dataTable.appendLine(lineArray);
				}
				tempTable.close();
			}
			dataTable.write(info);
			dataTable.close();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End....");
	}
	

	public static void collectQualitasDetailedTypeMeasure(String systemName, String version, boolean recollect) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_CLASS, systemName, version, false);

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
		collectQualitasDetailedTypeMeasure(systemPath, writer);
		writer.close();
	}

	
	public static void collectQualitasDetailedTypeMeasure(String systemPath, PrintWriter resultWriter) {
		List<SoftwareMeasure> measureList = getAvailableDetailedTypeMeasureList();

		SourceCodeParser parser = new SourceCodeParser(systemPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.time("Begin creating system, path = " + systemPath);
		Debug.disable();
		NameTableManager manager = creator.createNameTableManager(true);
		Debug.enable();
		Debug.time("End creating.....");
		Debug.flush();
		
		Debug.time("Begin creating structure, path = " + systemPath);
		Debug.disable();
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		structManager.readOrCreateSoftwareStructure();
//		structManager.createSoftwareStructure();
//		structManager.writeSoftwareStructure();
		Debug.enable();
		Debug.time("End creating.....");
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeFilter());
		SystemScope rootScope = manager.getRootScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		resultWriter.print("Class");
		for (SoftwareMeasure measure : measureList) resultWriter.print("\t" + measure.getIdentifier());
		resultWriter.println("\tPackage\tLabel");
		
		Debug.time("Begin scan class....");
		Debug.disable();
		int counter = 1;
		int totalCounter = definitionList.size();
		List<DetailedTypeDefinition> allTypes = rootScope.getAllDetailedTypeDefinition();
		
		Debug.println("total = " + totalCounter + ", all types size = " + allTypes.size());
		
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			
			Debug.println("Scan class " + counter + " : " + type.getFullQualifiedName() + ", total " + totalCounter);
			ClassMeasurement measurement = new ClassMeasurement(type, structManager);
			measurement.getMeasureList(measureList);

			resultWriter.print(type.getSimpleName());
			for (SoftwareMeasure measure : measureList) resultWriter.print("\t" + measure.valueString());
			resultWriter.println("\t" + type.getEnclosingPackage().getSimpleName() + "\t" + type.getUniqueId());

			Debug.println("End scan, remain " + (totalCounter - counter) + " classes .....");
			counter = counter + 1;
		}
		Debug.enable();
		Debug.time("End scan.....");
	}

	public static ValuedNodeManager collectQualitasDetailedTypeMeasure(String systemName, String version, SoftwareMeasure measure) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		return collectQualitasDetailedTypeMeasure(systemPath, measure);
	}
		
	public static ValuedNodeManager collectQualitasDetailedTypeMeasure(String systemPath, SoftwareMeasure measure) {
		SourceCodeParser parser = new SourceCodeParser(systemPath);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + systemPath);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new DetailedTypeFilter());
		SystemScope rootScope = manager.getRootScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		ValuedNodeManager nodeManager = new ValuedNodeManager();
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
			String id = type.getFullQualifiedName();
			String label = type.getSimpleName() + "@" + type.getLocation().toFullString(); 
			ValuedClassNode node = new ValuedClassNode(id, label);
			node.setDefinition(type);
			
			System.out.println("Calculate metric for class " + id + "...");
			
			ClassMeasurement measurement = new ClassMeasurement(type, structManager);
			measure.setUnusable();
			measure = measurement.getMeasure(measure);
			if (measure.isUsable()) node.setValue(measure.getValue());
			nodeManager.addValuedNode(node);
		}
		Debug.time("End scan.....");
		
		return nodeManager;
	}

	
	public static List<SoftwareMeasure> getAvailableDetailedTypeMeasureList() {
		SoftwareMeasure[] measures = {
				// Size metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IMPMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLFLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),


//				new SoftwareMeasure(SoftwareMeasureIdentifier.NEWMTHD),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),

				// Coupling metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBO),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOi),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOe),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFCp),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DAC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DACp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MPC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ACAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ACMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.AMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DMMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMIC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMEC),

				// Cohesion metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2),

				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1p),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2p),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM3),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM4),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CoPrim),

				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM5),
				new SoftwareMeasure(SoftwareMeasureIdentifier.Coh),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCI),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCCp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCCp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCDp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DCIp),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.CAMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICH),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LSCC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOM),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CCp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOMp),
				
				// Inheritance metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.DIT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.AID),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SIX),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOA),

				new SoftwareMeasure(SoftwareMeasureIdentifier.IHMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NEWMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OVMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHFLD),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.SPA),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SP),
			
				new SoftwareMeasure(SoftwareMeasureIdentifier.DPA),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DP),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}

	public static List<SoftwareMeasure> getRecollectDetailedTypeMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.DIT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.AID),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLD),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
}

class DetailedTypeFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isDetailedType()) return false;	
		DetailedTypeDefinition type = (DetailedTypeDefinition)definition;
//		if (!type.isPackageMember() || !type.isPublic()) return false;
		if (!type.isPackageMember() || type.isInterface() || type.isEnumeration()) return false;
		return true;
//		if (definition.getSimpleName().contains("DetailedTypeFilter")) return true;
//		else return false;
	}
}



