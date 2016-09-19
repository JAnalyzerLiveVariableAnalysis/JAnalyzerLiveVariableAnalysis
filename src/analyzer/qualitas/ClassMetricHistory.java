package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import analyzer.dataTable.DataTableManager;
import analyzer.valuedNode.ManyValuedNode;
import analyzer.valuedNode.ManyValuedNodeManager;
import analyzer.valuedNode.ValuedNode;
import analyzer.valuedNode.ValuedNodeManager;
import softwareChange.ClassChangeIndicator;
import softwareMeasurement.measure.MeasureObjectKind;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ19ÈÕ
 * @version 1.0
 */
public class ClassMetricHistory {

	public static void main(String[] args) {
		String systemName = "ant";
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);
		int mainVersionIndex = versions.length-1;
		
		List<SoftwareMeasure> measureList = getDetailedTypeHistoryMeasureList();
//		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
//		measureList.add(new SoftwareMeasure("RFC"));
		
		PrintWriter output = null;
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		
		try {
			Debug.setScreenOn();
			Debug.setStart("Begin to calculate metrics ....");
			for (int i = 0; i < versions.length; i++) ClassMetricCollector.collectQualitasDetailedTypeMeasure(systemName, versions[i], false);
			Debug.time("Begin to collect metric history ....");
			collectClassMetricHistory2(systemName, versions, mainVersionIndex, measureList);	
			Debug.time("End....");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		output.close();
	}

	/**
	 * Collect the history of metrics. In this method, we assume that the metrics have been generated and stored into the 
	 * file named by QualitasPathsManager.  
	 */
	public static void collectClassMetricHistory2(String systemName, String[] versions, int mainVersionIndex, List<SoftwareMeasure> measureList) throws IOException {
		final String idColumnNameInMetricFile = "Class"; 			// The column "Class" is equal to the id of each class
		final String labelColumnNameInMetricFile = "Label"; 			// The column "Notes" is equal to the label of each class
		final String idColumnNameInHistoryFile = "id";
		final String labelColumnNameInHistoryFile = "label";
		final String mainVersion = versions[mainVersionIndex];
		
		// Set the column names in the metric history files
		String[] columnNames = new String[versions.length+2];
		columnNames[0] = idColumnNameInHistoryFile;
		columnNames[1] = labelColumnNameInHistoryFile;
		for (int i = 0; i < versions.length; i++) columnNames[i+2] = versions[i];

		Debug.time("Read metric from main version " + mainVersion);
		
		// Read the metrics from the main version 
		String mainVersionMetricFile = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_CLASS, systemName, mainVersion, true);
		DataTableManager mainManager = new DataTableManager(mainVersion);
		mainManager.read(mainVersionMetricFile, true);
		mainManager.setKeyColumnIndex(labelColumnNameInMetricFile);		
		// Read the column "id" and "label" as the class name and label in the main version
		List<String> classIdList = mainManager.getColumnAsStringList(idColumnNameInMetricFile);
		List<String> classLabelList = mainManager.getColumnAsStringList(labelColumnNameInMetricFile);

		Debug.time("Initial result manager list...");
		
		// All metric history will be manager by DataTableManager object in resultManagerList!
		List<DataTableManager> resultManagerList = new ArrayList<DataTableManager>();
		// Store the metric identifier to the array metricIdentifierArray for latter using
		String[] metricIdentifierArray = new String[measureList.size()];
		for (int i = 0; i < measureList.size(); i++) {
			SoftwareMeasure measure = measureList.get(i);
			metricIdentifierArray[i] = measure.getIdentifier();
			// Each metric has a DataTableManager object to manage its history value!
			DataTableManager manager = new DataTableManager(metricIdentifierArray[i]);
			// The column names of the metric history include "id", "label" and all version names
			manager.setColumnNames(columnNames);
			// The column "label" is the key column!
			manager.setKeyColumnIndex(labelColumnNameInHistoryFile);
			// Set the column "id" and "label" of the metric history 
			manager.setColumnValue(idColumnNameInHistoryFile, classIdList);
			manager.setColumnValue(labelColumnNameInHistoryFile, classLabelList);
			// Set values of the column corresponding to the main version to the value read from mainManager (DataTableManager 
			// object of the main version) 
			List<String> metricValueList = mainManager.getColumnAsStringList(metricIdentifierArray[i]);
			manager.setColumnValue(versions[mainVersionIndex], metricValueList);
			
			// Add the DataTableManager object of this metric to the resultManagerList for latter using.
			// And the, the metric history of the metric with index i in measureList will be managed by 
			// the DataTableManager object with index i in resultManagerList! 
			resultManagerList.add(manager);
		}
		
		for (int versionIndex = 0; versionIndex < versions.length; versionIndex++) {
			if (versionIndex == mainVersionIndex) continue;
			String currentVersion = versions[versionIndex];

			Debug.time("Read metrics from version " + currentVersion);
			
			// Use a DataTableManager object to read metric values from the current version
			String currentVersionMetricFile = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_CLASS, systemName, currentVersion, true);
			DataTableManager currentManager = new DataTableManager(currentVersion);
			currentManager.read(currentVersionMetricFile, true);
			currentManager.setKeyColumnIndex(labelColumnNameInMetricFile);		

			// Use the indicator file to match the class labels in the main version and the current version 
			String indicatorFile = QualitasPathsManager.getMeasureIndicatorFile(MeasureObjectKind.MOK_CLASS, systemName, mainVersion, currentVersion);
			ClassChangeIndicator indicator = ClassChangeIndicator.getClassChangeIndicatorInstance(indicatorFile, mainVersion, currentVersion);
			for (String label : classLabelList) {
				String currentVersionClassLabel = indicator.getContrastNodeLabel(label);
				boolean match = false;
				if (!currentVersionClassLabel.equals("")) {
					// This means that we match the label (in the main version) with the currentVersionClassLabel 
					// (the class label in the current version)
					match = true;	
				} 
				for (int i = 0; i < metricIdentifierArray.length; i++) {
					String value = "NA";
					if (match) {
						// If we match the class label, we get the corresponding metric value from the current version, 
						// otherwise, the value will be "NA"
						value = currentManager.getCellValueAsString(currentVersionClassLabel, metricIdentifierArray[i]);
					}
					// Set the metric value of current version in the metric history DataTableManager object with 
					// the index i in the resultManagerList
					DataTableManager manager = resultManagerList.get(i);
					manager.setCellValue(label, versions[versionIndex], value);
				}
			}
			Debug.time("End for version " + currentVersion);
		}
		
		// Write the metric history for each metric to the file named by QualitasPathsmanager
		for (int i = 0; i < measureList.size(); i++) {
			SoftwareMeasure measure = measureList.get(i);
			DataTableManager manager = resultManagerList.get(i);
			String result = QualitasPathsManager.getMetricHistoryResultFile(MeasureObjectKind.MOK_CLASS, systemName, versions[mainVersionIndex], measure);
			manager.write(result);
		}	
	}
	
	/**
	 * Collect the history of metrics. In this method, the metrics are generated in memory.  
	 */
	public static void collectClassMetricHistory(String systemName, String[] versions, int mainVersionIndex, List<SoftwareMeasure> measureList) throws IOException {
		String title = "id\tlabel";
		for (int i = 0; i < versions.length; i++) {
			title = title + "\t" + versions[i];
		}
		
		for (SoftwareMeasure measure : measureList) {
			String result = QualitasPathsManager.getMetricHistoryResultFile(MeasureObjectKind.MOK_CLASS, systemName, versions[mainVersionIndex], measure);
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(result)));

			System.out.println("Collecting history of metric " + measure.getIdentifier() + ".....");
			Debug.setStart("Begin to collect history of metric " + measure.getIdentifier() + ".....");
			ManyValuedNodeManager manager = collectClassMetricHistory(systemName, versions, mainVersionIndex, measure);
			Debug.time("End.....");
			
			manager.write(result, title, false);
			writer.close();
		}
	}

	
	public static ManyValuedNodeManager collectClassMetricHistory(String systemName, String[] versions, int mainVersionIndex, SoftwareMeasure measure) {
		if (versions.length < 1) return null;
		if (mainVersionIndex < 0 || mainVersionIndex >= versions.length) return null;
		String mainVersion = versions[mainVersionIndex];
		
		ValuedNodeManager mainManager = ClassMetricCollector.collectQualitasDetailedTypeMeasure(systemName, mainVersion, measure);
		List<ValuedNode> mainNodeList = mainManager.getNodeList();

		int valueLength = versions.length; 
		ManyValuedNodeManager resultManager = new ManyValuedNodeManager();
		for (ValuedNode node : mainNodeList) {
			ManyValuedNode mainNode = new ManyValuedNode(node.getId(), node.getLabel(), valueLength);
			if (node.hasUsableValue()) mainNode.setValue(mainVersionIndex, node.getValue());
			resultManager.addValuedNode(mainNode);
		}
		
		for (int index = 0; index < valueLength; index++) {
			if (index == mainVersionIndex) continue;	// We have collected metrics for the main version
			String currentVersion = versions[index];
			System.out.println("Compare main version [" + mainVersion + "] to version [" + currentVersion + "]......");
			
			ValuedNodeManager nodeManager = ClassMetricCollector.collectQualitasDetailedTypeMeasure(systemName, currentVersion, measure);

			List<ValuedNode> currentNodeList = nodeManager.getNodeListCopy();
			String indicatorFile = QualitasPathsManager.getMeasureIndicatorFile(MeasureObjectKind.MOK_CLASS, systemName, mainVersion, currentVersion);
			ClassChangeIndicator indicator = ClassChangeIndicator.getClassChangeIndicatorInstance(indicatorFile, mainVersion, currentVersion);
			
			for (int mainIndex = 0; mainIndex < mainNodeList.size(); mainIndex++) {
				ValuedNode mainNode = mainNodeList.get(mainIndex);
				String mainNodeLabel = mainNode.getLabel();
				
				for (int currentIndex = 0; currentIndex < currentNodeList.size(); currentIndex++) {
					ValuedNode currentNode = currentNodeList.get(currentIndex);
					String currentNodeLabel = currentNode.getLabel();
					
					System.out.println("Compaire main type " + mainIndex + " (of " + mainNodeList.size() + ") to current type " + currentIndex + " (of " + currentNodeList.size() + ")...");
					boolean sameNode = false;
					if (indicator != null) sameNode = indicator.canBindToSameDefinition(mainNodeLabel, currentNodeLabel);
					else sameNode = mainNode.hasBindToSameDefinition(currentNode);
					
					if (sameNode == true) {
						double currentValue = currentNode.getValue();
						ManyValuedNode mainManyValuedNode = resultManager.getNodeList().get(mainIndex);
						mainManyValuedNode.setValue(index, currentValue);
						break;
					}
				}
			}
			
		}
		return resultManager;
	}

	public static List<SoftwareMeasure> getDetailedTypeHistoryMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),

				// Cohesion metrics
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM1p),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM2p),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.LCOM5),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.Coh),

				new SoftwareMeasure(SoftwareMeasureIdentifier.TCC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LCC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DCD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DCI),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.CC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LSCC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.SCOM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.CAMC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICH),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SCC),
				
				// Coupling metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.CBO),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOi),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOe),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CBOp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.RFCp),

				new SoftwareMeasure(SoftwareMeasureIdentifier.DAC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DACp),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ICP),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IHICP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MPC),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.ACAIC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ACMIC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.AMMIC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DCAEC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DCMEC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DMMEC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAIC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMIC),

				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMIC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.OCAEC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.OCMEC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OMMEC),

				// Inheritance metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.AID),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DIT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SIX),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.NOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NOP),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NOA),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.SPA),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.SPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.SP),
				
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DPA),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.DPD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.DP),

				// Size metrics
				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.IHMTHD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NEWMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.OVMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IMPMTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ALLFLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.IHFLD),

				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),

//				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
//				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
	
}
