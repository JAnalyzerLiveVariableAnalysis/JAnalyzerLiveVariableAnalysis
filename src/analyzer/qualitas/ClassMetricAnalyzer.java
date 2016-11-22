package analyzer.qualitas;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import util.Debug;
import analyzer.dataTable.DataTableManager;
import analyzer.dataTable.DataTableUtil;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê1ÔÂ13ÈÕ
 * @version 1.0
 */
public class ClassMetricAnalyzer {

	public static void main(String[] args) {
//		renameQualitasRecentMeasureDescriptiveFile();
//		groupQualitasRecenMeasureDescriptivesByMetric();
//		groupQualitasRecenMeasureDescriptivesByIndex();
		
//		groupQualitasRecentMeasureModelsByMetric();
		calculateQualitasRecentMeasureModelsPValue();
		groupQualitasRecentMeasureModelsByIndex();
//		groupQualitasRecentMeasureTestsByIndex();

//		groupQualitasRecentMeasurePowerlawTestsByIndex(0);
		
//		groupQualitasRecentMeasureTestsByMetric();
//		String[] indexArray = {"LogADPval", "LogCVMPval", "LogLKSPval", "LogPCPval", "LogSFPval", "LogSWPval"};
//		String[] metricArray = {"ELOC", "FLD", "MTHD", "LCOM2", "TCC", "SCC", "CBO", "RFC", "MPC", "DIT", "NOA", "NOD"};
//		selectQualitasRecentMeasureTests(0.05, metricArray, indexArray);
		
//		groupQualitasRecentMeasureCorrelationsByMetricAndIndex();
//		summaryQualitasRecentMeasureCorrelationsGroupBySystem(true);
//		summaryQualitasRecentMeasureCorrelationsGroupByMetric(true);
//		categorizedQualitasRecentMeasureCorrelationsGroupBySystem(false);
//		categorizedQualitasRecentMeasureCorrelationsGroupByMetric();
		
//		summaryQualitasRecentMeasureCorrelations();
		
//		groupQualitasRecenMeasureOutlierTestsByMetric();
//		groupQualitasRecenMeasureOutlierTestsByIndex();
		
//		selectQualitasRecentMeasureOutlierTests(0.05);
		
//		selectQualitasRecentMeasureOutlierTestRejects(8);
		
//		findQualitasMaxValueNumberOfAllMetrics(0.15);
//		calculateQualitasMaxValueNumberRatio();
		
//		countQualitasRangeAndPositionOfAllMetrics(0.05, 0.3);
		
//		extractQualitasMeasureTime();	
		
//		metricDistributionKSTest();
		
	}

	public static void metricDistributionKSTest() {
		String rootPath = QualitasPathsManager.defaultResultPath;
		String version = "eclipse_SDK-4.3";
		String classMetricFile = rootPath + "class" + QualitasPathsManager.pathSeparator + version + ".txt";
		
		DataTableManager table = new DataTableManager("data");
		try {
			table.read(classMetricFile, true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		double[] elocData = table.getColumnAsDoubleArray("ELOC");
		int count = 0;
		for (int i = 0; i < elocData.length; i++) {
			if (elocData[i] >= 5) count++;
		}
		
		double[] elocLogData = new double[count];
		int j = 0;
		for (int i = 0; i < elocData.length; i++) {
			if (elocData[i] >= 5) {
				elocLogData[j] = Math.log(elocData[i]);
				j++;
			}
		}
		
		for (int i = 0; i < 5; i++) {
			System.out.println(elocData[i] + "(" + elocLogData[i] + ") ");
		}
		System.out.println();
		
		double mean = StatUtils.mean(elocLogData);
		double sd = Math.sqrt(StatUtils.variance(elocLogData));
		
		NormalDistribution distribution = new NormalDistribution(4.055294, 1.300382);
		KolmogorovSmirnovTest tester = new KolmogorovSmirnovTest();
		double pValue = tester.kolmogorovSmirnovTest(distribution, elocLogData);
		double statistics = tester.kolmogorovSmirnovStatistic(distribution, elocLogData);
		
		double pValue2 = 1d - tester.cdf(0.062004, 41);
				
		System.out.println("Length = " + elocLogData.length + ", Mean = " + mean + ", Sd = " + sd + ", Statistics = " + statistics + ", p-value = " + pValue + ", p-value2 = " + pValue2);
	}
	
	public static void extractQualitasMeasureTime() {
		String timeFile = "C:\\QualitasPacking\\result\\recent\\QualitasTime.txt";
		String resultFile = "C:\\QualitasPacking\\result\\recent\\QualitasTime(table).txt";
		
		DataTableManager table = new DataTableManager("");
		String[] columnNames = {"System", "NameTable", "Structure", "Measure", "Total"};
		table.setColumnNames(columnNames);
		
		try {
			LineNumberReader reader = new LineNumberReader(new FileReader(timeFile));
			String line = reader.readLine();
			long nameTableStart = 0;
			long nameTableEnd = 0;
			long structStart = 0;
			long structEnd = 0;
			long measureStart = 0;
			long measureEnd = 0;

			int lastIndex = -1;
			int lastButOneIndex = -1;
			
			String systemName = null;
			String timeString = null;
			
			boolean inCreatingNameTable = false;
			while (line != null) {
				if (line.contains("creating system")) {
					lastIndex = line.lastIndexOf('\\');
					lastButOneIndex = line.lastIndexOf('\\', lastIndex-1);
					systemName = line.substring(lastButOneIndex+1, lastIndex);
					
					lastIndex = line.lastIndexOf(')');
					lastButOneIndex = line.lastIndexOf('(');
					timeString = line.substring(lastButOneIndex+1, lastIndex);
					nameTableStart = Long.parseLong(timeString);
					inCreatingNameTable = true;
				}
				
				if (line.contains("End creating")) {
					lastIndex = line.lastIndexOf(')');
					lastButOneIndex = line.lastIndexOf('(');
					timeString = line.substring(lastButOneIndex+1, lastIndex);
					if (inCreatingNameTable) nameTableEnd = Long.parseLong(timeString);
					else structEnd = Long.parseLong(timeString);
				}

				if (line.contains("creating structure")) {
					lastIndex = line.lastIndexOf(')');
					lastButOneIndex = line.lastIndexOf('(');
					timeString = line.substring(lastButOneIndex+1, lastIndex);
					structStart = Long.parseLong(timeString);
					inCreatingNameTable = false;
				}
				
				if (line.contains("scan class")) {
					lastIndex = line.lastIndexOf(')');
					lastButOneIndex = line.lastIndexOf('(');
					timeString = line.substring(lastButOneIndex+1, lastIndex);
					measureStart = Long.parseLong(timeString);
				}
				
				if (line.contains("End scan")) {
					lastIndex = line.lastIndexOf(')');
					lastButOneIndex = line.lastIndexOf('(');
					timeString = line.substring(lastButOneIndex+1, lastIndex);
					measureEnd = Long.parseLong(timeString);
					
					String[] lineArray = new String[5];
					lineArray[0] = systemName;
					timeString = Long.toString(nameTableEnd-nameTableStart);
					lineArray[1] = timeString;
					timeString = Long.toString(structEnd-structStart);
					lineArray[2] = timeString;
					timeString = Long.toString(measureEnd-measureStart);
					lineArray[3] = timeString;
					timeString = Long.toString(measureEnd-nameTableStart);
					lineArray[4] = timeString;
					
					table.appendLine(lineArray);
				}
				line = reader.readLine();
			}
			reader.close();
			
			table.write(resultFile);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}
	
	public static void countQualitasRangeAndPositionOfAllMetrics(double rangeThrehold, double positionThrehold) {
		String[] metricArray = getImportantMetric();

		DataTableManager resultTable = new DataTableManager("result");
		String[] columnNames = {"Metric", "RangeNum", "PositionNum"};
		resultTable.setColumnNames(columnNames);
		
		for (int i = 0; i < metricArray.length; i++) {
			System.out.println("Find for metric " + metricArray[i]);
			
			int[] maxValueResultArray = countQualitasRangeAndPositionOfAllMetrics(metricArray[i], rangeThrehold, positionThrehold);
			String[] lineArray = new String[3];
			lineArray[0] = metricArray[i];
			lineArray[1] = Integer.toString(maxValueResultArray[0]);
			lineArray[2] = Integer.toString(maxValueResultArray[1]);
			
			resultTable.appendLine(lineArray);
		}
		
		String resultFile = QualitasPathsManager.defaultDebugPath + "range-position.txt";
		try {
			resultTable.write(resultFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int[] countQualitasRangeAndPositionOfAllMetrics(String metric, double rangeThrehold, double positionThrehold) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String rootPath = QualitasPathsManager.defaultResultPath;
		
		int[] result = new int[2];
		result[0] = 0;
		result[1] = 0;
		DataTableManager classMetricTable = new DataTableManager("class metric");
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			System.out.println("\tScan system " + systemNames[systemIndex]);
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[systemIndex]);
			
			for (int versionIndex = 0; versionIndex < versions.length; versionIndex++) {
				String classMetricFile = rootPath + "class" + QualitasPathsManager.pathSeparator + versions[versionIndex] + ".txt";
				
				try {
					classMetricTable.read(classMetricFile, true);
					String[] metricValueArray = null;
					if (metric.equals("NIHICP")) {
						int lineNumber = classMetricTable.getLineNumber();
						metricValueArray = new String[lineNumber];
						for (int j = 0; j < lineNumber; j++) {
							double ICPValue = classMetricTable.getCellValueAsDouble(j, "ICP");
							double IHICPValue = classMetricTable.getCellValueAsDouble(j, "IHICP");
							double NIHICPValue = ICPValue - IHICPValue;
							metricValueArray[j] = Double.toString(NIHICPValue);
						}
					} else {
						metricValueArray = classMetricTable.getColumnAsStringArray(metric);
					}
					
					DataTableManager valueCountTable = DataTableUtil.countValueNumberDistribution(metricValueArray);
					int valueNumber = valueCountTable.getLineNumber();
					int dataNumber = classMetricTable.getLineNumber();
					double valueDataRatio = (double)valueNumber / dataNumber;
					if (valueDataRatio <= rangeThrehold) result[0] = result[0] + 1; 
					
					double[] fiveNumbers = DataTableUtil.fiveNumberOfDoubleArray(toDoubleArray(metricValueArray));
					double position = 0;
					if (fiveNumbers[4] > fiveNumbers[0]) position = fiveNumbers[2] / (fiveNumbers[4] - fiveNumbers[0]);
					if (position < positionThrehold) result[1] = result[1] + 1;
					
					System.out.println("\t\tValue number " + valueNumber + ", data number " + dataNumber + ", ratio " + valueDataRatio + ", min " + fiveNumbers[0] + ", max " + fiveNumbers[4] + ", median " + fiveNumbers[2] + ", position " + position);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public static double[] toDoubleArray(String[] valueArray) {
		double[] result = new double[valueArray.length];
		for (int i = 0; i < valueArray.length; i++) {
			result[i] = Double.parseDouble(valueArray[i]);
		}
		return result;
	}
	
	public static void calculateQualitasMaxValueNumberRatio() {
		DataTableManager maxNumberValueTable = new DataTableManager("Max Number");
		String resultFile = QualitasPathsManager.defaultDebugPath + "value-number.txt";
		try {
			maxNumberValueTable.read(resultFile, true);
			maxNumberValueTable.setKeyColumnIndex("Metric");
		} catch (IOException exc) {
			exc.printStackTrace();
			return;
		}
		String[] metricArray = getImportantMetric();
		String[] maxNumberValueArray = new String[metricArray.length];
		for (int i = 0; i < metricArray.length; i++) {
			maxNumberValueArray[i] = maxNumberValueTable.getCellValueAsString(metricArray[i], "Value");
		}
		maxNumberValueTable.close();
		maxNumberValueTable = null;
		
		DataTableManager resultTable = new DataTableManager("result");
		String[] columnNames = new String[metricArray.length+1];
		columnNames[0] = "System";
		for (int i = 0; i < metricArray.length; i++) columnNames[i+1] = metricArray[i];
		resultTable.setColumnNames(columnNames);
		resultTable.setKeyColumnIndex("System");
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		DataTableManager classMetricTable = new DataTableManager("class metric");
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			System.out.println("\tScan system " + systemNames[systemIndex]);
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[systemIndex]);
			for (int versionIndex = 0; versionIndex < versions.length; versionIndex++) {
				String classMetricFile = QualitasPathsManager.defaultResultPath + "class" + QualitasPathsManager.pathSeparator + versions[versionIndex] + ".txt";
				
				try {
					classMetricTable.read(classMetricFile, true);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				String[] lineArray = new String[columnNames.length];
				lineArray[0] = versions[versionIndex];
				
				for (int i = 0; i < metricArray.length; i++) {
					String metric = metricArray[i];
					String[] metricValueArray = null;
					if (metric.equals("NIHICP")) {
						int lineNumber = classMetricTable.getLineNumber();
						metricValueArray = new String[lineNumber];
						for (int j = 0; j < lineNumber; j++) {
							double ICPValue = classMetricTable.getCellValueAsDouble(j, "ICP");
							double IHICPValue = classMetricTable.getCellValueAsDouble(j, "IHICP");
							double NIHICPValue = ICPValue - IHICPValue;
							metricValueArray[j] = Double.toString(NIHICPValue);
						}
					} else {
						metricValueArray = classMetricTable.getColumnAsStringArray(metric);
					}
					
					DataTableManager valueCountTable = DataTableUtil.countValueNumberDistribution(metricValueArray);
					int lineNumber = valueCountTable.getLineNumber();

					String frequencyString = "0"; 
					for (int j = 0; j < lineNumber; j++) {
						String value = valueCountTable.getCellValueAsString(j, "Value");
						if (value.equals(maxNumberValueArray[i])) {
							frequencyString = valueCountTable.getCellValueAsString(j, "Frequency");
							break;
						}
					}
					
					lineArray[i+1] = frequencyString;
				}
				resultTable.appendLine(lineArray);
			}
		}

		resultFile = QualitasPathsManager.defaultDebugPath + "max-value-raito.txt";
		try {
			resultTable.write(resultFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void findQualitasMaxValueNumberOfAllMetrics(double threhold) {
		String[] metricArray = getImportantMetric();

		DataTableManager resultTable = new DataTableManager("result");
		String[] columnNames = {"Metric", "Value", "Number", "ThreholdNum"};
		resultTable.setColumnNames(columnNames);
		
		for (int i = 0; i < metricArray.length; i++) {
			System.out.println("Find for metric " + metricArray[i]);
			
			String[] maxValueResultArray = findQualitasMaxValueNumberOfMetric(metricArray[i], threhold);
			String[] lineArray = new String[4];
			lineArray[0] = metricArray[i];
			lineArray[1] = maxValueResultArray[0];
			lineArray[2] = maxValueResultArray[1];
			lineArray[3] = maxValueResultArray[2];
			
			resultTable.appendLine(lineArray);
		}
		
		String resultFile = QualitasPathsManager.defaultDebugPath + "value-number.txt";
		try {
			resultTable.write(resultFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] findQualitasMaxValueNumberOfMetric(String metric, double threhold) {
		String[] result = new String[3];
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String rootPath = QualitasPathsManager.defaultResultPath;
		
		DataTableManager classMetricTable = new DataTableManager("class metric");
		TreeMap<String, int[]> maxValueMap = new TreeMap<String, int[]>();
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			System.out.println("\tScan system " + systemNames[systemIndex]);
			String[] versions = QualitasPathsManager.getSystemVersions(systemNames[systemIndex]);
			
			for (int versionIndex = 0; versionIndex < versions.length; versionIndex++) {
				String classMetricFile = rootPath + "class" + QualitasPathsManager.pathSeparator + versions[versionIndex] + ".txt";
				
				try {
					classMetricTable.read(classMetricFile, true);
					String[] metricValueArray = null;
					if (metric.equals("NIHICP")) {
						int lineNumber = classMetricTable.getLineNumber();
						metricValueArray = new String[lineNumber];
						for (int j = 0; j < lineNumber; j++) {
							double ICPValue = classMetricTable.getCellValueAsDouble(j, "ICP");
							double IHICPValue = classMetricTable.getCellValueAsDouble(j, "IHICP");
							double NIHICPValue = ICPValue - IHICPValue;
							metricValueArray[j] = Double.toString(NIHICPValue);
						}
					} else {
						metricValueArray = classMetricTable.getColumnAsStringArray(metric);
					}
					
					DataTableManager valueCountTable = DataTableUtil.countValueNumberDistribution(metricValueArray);
					int lineNumber = valueCountTable.getLineNumber();
					String maxValue = null;
					int maxValueNumber = 0;
					double frequency = 0.0;
					for (int j = 0; j < lineNumber; j++) {
						String value = valueCountTable.getCellValueAsString(j, "Value");
						int number = valueCountTable.getCellValueAsInt(j, "Number");
						if (number > maxValueNumber) {
							maxValue = value;
							maxValueNumber = number;
							frequency = valueCountTable.getCellValueAsDouble(j, "Frequency");
						}
					}
					int[] valueSystemNumber = maxValueMap.get(maxValue);
					if (valueSystemNumber == null) {
						valueSystemNumber = new int[2];
						valueSystemNumber[0] = 1;
						if (frequency >= threhold) valueSystemNumber[1] = 1;
						else valueSystemNumber[1] = 0;
						maxValueMap.put(maxValue, valueSystemNumber);
					} else {
						valueSystemNumber[0] = valueSystemNumber[0] + 1;
						if (frequency >= threhold) valueSystemNumber[1] = valueSystemNumber[1] + 1;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		String maxValueInAllSystem = null;
		int maxValueNumberInAllSystem = 0;
		int frequencyGreaterThreholdNumber = 0;
		Set<String> maxValueSet = maxValueMap.keySet();
		for (String maxValueString : maxValueSet) {
			int[] maxValueNumber = maxValueMap.get(maxValueString);
			if (maxValueNumber[0] > maxValueNumberInAllSystem) {
				maxValueInAllSystem = maxValueString;
				maxValueNumberInAllSystem = maxValueNumber[0];
				frequencyGreaterThreholdNumber = maxValueNumber[1];
			}
		}
		
		result[0] = maxValueInAllSystem;
		result[1] = Integer.toString(maxValueNumberInAllSystem);
		result[2] = Integer.toString(frequencyGreaterThreholdNumber);
		return result;
	}
	
	public static void selectQualitasRecentMeasureOutlierTestRejects(int threshold) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Outlier-test-rejnum-select.txt";
		String summaryFile = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "outliertest" + QualitasPathsManager.pathSeparator + "Outlier-summary.txt";
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = {"System", "Metric", "Index", "Value"};
		dataTable.setColumnNames(columnNameArray);

		DataTableManager summaryTable = new DataTableManager("summary");
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Begin select.....");
		try {
			summaryTable.read(summaryFile, true);
			summaryTable.setKeyColumnIndex(0);
			
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				
				int outlierNum = summaryTable.getCellValueAsInt(version, "LogSd2Num");
				if (outlierNum < 10) continue;
				
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "outliertest" + QualitasPathsManager.pathSeparator + "Outlier-test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempColumnNameArray = tempTable.getColumnNameArray();
				int lineNumber = tempTable.getLineNumber();
				for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
					String[] tempLineArray = tempTable.getLineAsStringArray(lineIndex);
					for (int colIndex = 0; colIndex < tempColumnNameArray.length; colIndex++) {
						String columnName = tempColumnNameArray[colIndex];
						if (columnName.contains("RejNum")) {
							int value = 0;
							try {
								value = Integer.parseInt(tempLineArray[colIndex]);
							} catch (Exception exc) {
								continue;
							}
							if (value >= threshold) {
								String[] lineArray = new String[columnNameArray.length];
								lineArray[0] = version;
								lineArray[1] = tempLineArray[0];
								lineArray[2] = columnName;
								lineArray[3] = "" + value;
								
								Debug.println("Find system " + version + ", metric " + tempLineArray[0] + ", index " + columnName + ", value " + value);
								dataTable.appendLine(lineArray);
							}
						}
					}
				}
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for select ......");
	}
	
	public static void selectQualitasRecentMeasureOutlierTests(double threshold) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Outlier-test-pval-select.txt";
		String summaryFile = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "outliertest" + QualitasPathsManager.pathSeparator + "Outlier-summary.txt";
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = {"System", "Metric", "Index", "Value"};
		dataTable.setColumnNames(columnNameArray);

		DataTableManager summaryTable = new DataTableManager("summary");
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Begin select.....");
		try {
			summaryTable.read(summaryFile, true);
			summaryTable.setKeyColumnIndex(0);
			
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				
				int outlierNum = summaryTable.getCellValueAsInt(version, "LogSd2Num");
				if (outlierNum < 10) continue;
				
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "outliertest" + QualitasPathsManager.pathSeparator + "Outlier-test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempColumnNameArray = tempTable.getColumnNameArray();
				int lineNumber = tempTable.getLineNumber();
				for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
					String[] tempLineArray = tempTable.getLineAsStringArray(lineIndex);
					for (int colIndex = 0; colIndex < tempColumnNameArray.length; colIndex++) {
						String columnName = tempColumnNameArray[colIndex];
						if (columnName.contains("Min") || columnName.contains("Mean") || columnName.contains("Max")) {
							double value = 0;
							try {
								value = Double.parseDouble(tempLineArray[colIndex]);
							} catch (Exception exc) {
								continue;
							}
							if (value <= threshold) {
								String[] lineArray = new String[columnNameArray.length];
								lineArray[0] = version;
								lineArray[1] = tempLineArray[0];
								lineArray[2] = columnName;
								lineArray[3] = "" + value;
								
								Debug.println("Find system " + version + ", metric " + tempLineArray[0] + ", index " + columnName + ", value " + value);
								dataTable.appendLine(lineArray);
							}
						}
					}
				}
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for select ......");
	}
	
	public static void groupQualitasRecenMeasureOutlierTestsByMetric() {
		String[] metricArray = getImportantMetric();
		for (String metric : metricArray) {
			groupQualitasRecentMeasureOutlierTestsByMetric(metric);
		}
	}

	public static void groupQualitasRecenMeasureOutlierTestsByIndex() {
		String[] indexArray = getImportantOutlierTestIndex();
		for (String index : indexArray) {
			groupQualitasRecentMeasureOutlierTestsByIndex(index);
		}
	}
	
	public static void groupQualitasRecentMeasureOutlierTestsByIndex(String index) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Outlier-test-index-" + index + ".txt" ;

		String summaryFile = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "outliertest" + QualitasPathsManager.pathSeparator + "Outlier-summary.txt";
		DataTableManager summaryTable = new DataTableManager("summary");
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] metricArray = getImportantMetric();
		String[] columnNameArray = new String[metricArray.length + 2];
		columnNameArray[0] = "System";
		columnNameArray[1] = "OutlierNum";
		for (int i = 0; i < metricArray.length; i++) {
			columnNameArray[i+2] = metricArray[i];
		}
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan index " + index + " ......");
		try {
			summaryTable.read(summaryFile, true);
			summaryTable.setKeyColumnIndex(0);
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "outliertest" + QualitasPathsManager.pathSeparator + "Outlier-test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				if (tempTable.getLineNumber() <= 0) continue;
				
				String[] tempArray = tempTable.getColumnAsStringArray(index);
				if (tempArray == null) {
					Debug.println("\tCan not read data of index [" + index + "] in file " + result);
					continue;
				}
				if (tempArray.length != columnNameArray.length-2) {
					Debug.println("\tThe length of line [" + (tempArray.length+2) + " != column length [" + columnNameArray.length + "]!");
					continue;
				}
				
				String[] lineArray = new String[tempArray.length+2];
				lineArray[0] = version;
				lineArray[1] = summaryTable.getCellValueAsString(version, "LogSd2Num");
				for (int i = 0; i < tempArray.length; i++) lineArray[i+2] = tempArray[i];
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for index " + index + " ......");
	}
	
	public static void groupQualitasRecentMeasureOutlierTestsByMetric(String metric) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Outlier-test-metric-" + metric + ".txt" ;

		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = null;
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan metric " + metric + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Outlier-test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				if (tempTable.getLineNumber() <= 0) continue;
				tempTable.setKeyColumnIndex("Metrics");
				String[] lineArray = tempTable.getLineAsStringArray(metric);
				if (lineArray == null) {
					Debug.println("\tCan not read data of metric [" + metric + "] in file " + result);
					continue;
				}
				
				if (systemIndex == 0) {
					columnNameArray = tempTable.getColumnNameArray();
					columnNameArray[0] = "System";
					dataTable.setColumnNames(columnNameArray);
				}
				lineArray[0] = version;
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + metric + " ......");
	}
	
	public static void summaryQualitasRecentMeasureCorrelations() {
		DataTableManager dataTable = new DataTableManager("summary");
		String[] columnNameArray = {"Metric", "PCAll", "PCSize", "PCCohesion", "PCCoupling", "PCInherit", "SCAll", "SCSize", "SCCohesion", "SCCoupling", "SCInherit"};
		dataTable.setColumnNames(columnNameArray);
		dataTable.setKeyColumnIndex(0);

		String[] metricArray = getImportantMetric();
		double threhold = 0.5;
		
		Debug.setStart("Begin....");
		for (int metricIndex = 0; metricIndex < metricArray.length; metricIndex++) {
			String metric = metricArray[metricIndex];
			String[] dataLineArray = new String[columnNameArray.length];
			dataLineArray[0] = metric;
			
			Debug.println("Scan metric " + metric);
			String file = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-summary-" + metric + ".txt";
			DataTableManager temp = new DataTableManager("temp");
			try {
				temp.read(file, true);
			} catch (Exception exc) {
				Debug.println("\tCan not read file " + file);
				dataLineArray[1] = "NA";
				dataLineArray[6] = "NA";
			}
			String pcAll = getMaxThreeCorrelatedMetrics(temp, "PCMean", threhold);
			dataLineArray[1] = pcAll;
			String scAll = getMaxThreeCorrelatedMetrics(temp, "SCMean", threhold);
			dataLineArray[6] = scAll;
			
			file = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-sum(size)-" + metric + ".txt";
			temp = new DataTableManager("temp");
			try {
				temp.read(file, true);
			} catch (Exception exc) {
				Debug.println("\tCan not read file " + file);
				dataLineArray[2] = "NA";
				dataLineArray[7] = "NA";
			}
			String pcSize = getMaxThreeCorrelatedMetrics(temp, "PCMean", threhold);
			dataLineArray[2] = pcSize;
			String scSize = getMaxThreeCorrelatedMetrics(temp, "SCMean", threhold);
			dataLineArray[7] = scSize;

			file = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-sum(coh)-" + metric + ".txt";
			temp = new DataTableManager("temp");
			try {
				temp.read(file, true);
			} catch (Exception exc) {
				Debug.println("\tCan not read file " + file);
				dataLineArray[3] = "NA";
				dataLineArray[8] = "NA";
			}
			String pcCoh = getMaxThreeCorrelatedMetrics(temp, "PCMean", threhold);
			dataLineArray[3] = pcCoh;
			String scCoh = getMaxThreeCorrelatedMetrics(temp, "SCMean", threhold);
			dataLineArray[8] = scCoh;

			file = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-sum(coup)-" + metric + ".txt";
			temp = new DataTableManager("temp");
			try {
				temp.read(file, true);
			} catch (Exception exc) {
				Debug.println("\tCan not read file " + file);
				dataLineArray[4] = "NA";
				dataLineArray[9] = "NA";
			}
			String pcCoup = getMaxThreeCorrelatedMetrics(temp, "PCMean", threhold);
			dataLineArray[4] = pcCoup;
			String scCoup = getMaxThreeCorrelatedMetrics(temp, "SCMean", threhold);
			dataLineArray[9] = scCoup;

			file = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-sum(inh)-" + metric + ".txt";
			temp = new DataTableManager("temp");
			try {
				temp.read(file, true);
			} catch (Exception exc) {
				Debug.println("\tCan not read file " + file);
				dataLineArray[5] = "NA";
				dataLineArray[10] = "NA";
			}
			String pcInh = getMaxThreeCorrelatedMetrics(temp, "PCMean", threhold);
			dataLineArray[5] = pcInh;
			String scInh = getMaxThreeCorrelatedMetrics(temp, "SCMean", threhold);
			dataLineArray[10] = scInh;
			
			dataTable.appendLine(dataLineArray);
		}
		

		String info = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-all-summary.txt";
		try {
			dataTable.write(info);
			Debug.println("\tWrite table to " + info + ", line number = " + dataTable.getLineNumber());
		} catch (IOException exc) {
			Debug.println("\tCan not write table to " + info);
		}
	}
	
	public static String getMaxThreeCorrelatedMetrics(DataTableManager table, String coreffColumn, double threhold) {
		int metricColumnIndex = 0;
		int[] sortedIndex = table.getDecreasingSortedIndexByColumn(coreffColumn);
		
		StringBuffer result = new StringBuffer();
		int lineIndex = sortedIndex[0];
		double value = table.getCellValueAsDouble(lineIndex, coreffColumn);
		if (value >= threhold) {
			String metric = table.getCellValueAsString(lineIndex, metricColumnIndex);
			if (result.length() != 0) result.append(";" + metric);
			else result.append(metric);
		}
		lineIndex = sortedIndex[1];
		value = table.getCellValueAsDouble(lineIndex, coreffColumn);
		if (value >= threhold) {
			String metric = table.getCellValueAsString(lineIndex, metricColumnIndex);
			if (result.length() != 0) result.append(";" + metric);
			else result.append(metric);
		}
		lineIndex = sortedIndex[2];
		value = table.getCellValueAsDouble(lineIndex, coreffColumn);
		if (value >= threhold) {
			String metric = table.getCellValueAsString(lineIndex, metricColumnIndex);
			if (result.length() != 0) result.append(";" + metric);
			else result.append(metric);
		}
		if (result.length() == 0) return "\\quad";
		else return "\\pname{" + result.toString() + "}"; 
	}
	
	public static void categorizedQualitasRecentMeasureCorrelationsGroupByMetric() {
		String[] metricArray = getImportantMetric();
		String[] sizeMetricArray = getImportantSizeMetric();
		String[] cohesionMetricArray = getImportantCohesionMetric();
		String[] couplingMetricArray = getImportantCouplingMetric();
		String[] inheritMetricArray = getImportantInheritMetric();
		
		Debug.setStart("Begin....");
		for (String metric : metricArray) {
			Debug.println("Scan metric " + metric);
			categorizedQualitasRecentMeasureCorrelationsGroupByMetric(metric, sizeMetricArray, "sum(size)");
			categorizedQualitasRecentMeasureCorrelationsGroupByMetric(metric, cohesionMetricArray, "sum(coh)");
			categorizedQualitasRecentMeasureCorrelationsGroupByMetric(metric, couplingMetricArray, "sum(coup)");
			categorizedQualitasRecentMeasureCorrelationsGroupByMetric(metric, inheritMetricArray, "sum(inh)");
		}
		Debug.time("End....");
	}
	
	public static void categorizedQualitasRecentMeasureCorrelationsGroupByMetric(String metric, String[] metricArray, String fileId) {
		DataTableManager dataTable = new DataTableManager("summary");
		String[] columnNameArray = {"Metric", "PCMetric1", "PCMetric2", "PCMetric3", "PCMean", "SCMetric1", "SCMetric2", "SCMetric3", "SCMean"};
		dataTable.setColumnNames(columnNameArray);
		dataTable.setKeyColumnIndex(0);
		String spearman = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-Spearman-" + metric + ".txt";
		String pearson = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-Pearson-" + metric + ".txt";
		DataTableManager pearsonTable = new DataTableManager("pearson");
		DataTableManager spearmanTable = new DataTableManager("spearman");
		try {
			pearsonTable.read(pearson, true);
			spearmanTable.read(spearman, true);
		} catch (IOException exc) {
			Debug.println("\tCan not read spearman or pearson table for the metric " + metric);
			return;
		}
		for (int i = 0; i < metricArray.length; i++) {
			if (metricArray[i].equals(metric)) continue;
			
			String[] dataLineArray = new String[columnNameArray.length];
			for (int j = 0; j < dataLineArray.length; j++) dataLineArray[j] = "";
			dataLineArray[0] = metricArray[i];
			double pcmean = DataTableUtil.meanOfColumn(pearsonTable, metricArray[i]);
			double scmean = DataTableUtil.meanOfColumn(spearmanTable, metricArray[i]);
			String pcmeanString = "NA";
			String scmeanString = "NA";
			if (!Double.isNaN(pcmean)) pcmeanString = "" + pcmean;
			if (!Double.isNaN(scmean)) scmeanString = "" + scmean;
			dataLineArray[4] = pcmeanString;
			dataLineArray[8] = scmeanString;
			dataTable.appendLine(dataLineArray);
		}
		pearsonTable.close();
		spearmanTable.close();
		
		DataTableManager tempTable = new DataTableManager("temp");
		
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			String systemName = systemNames[systemIndex];
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			String version = versions[versions.length-1];
			
			String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-" + fileId + "-" + version + ".txt";
			File resultFile = new File(result);
			if (!resultFile.exists()) continue;
			
			Debug.println("Scan version " + version + ", file " + result);
			try {
				tempTable.read(result, true);
			} catch (IOException exc) {
				Debug.println("\tCan not read file " + result);
				continue;
			}
			tempTable.setKeyColumnIndex("Metric");
			String[] lineArray = tempTable.getLineAsStringArray(metric);
			String pcMetric1 = lineArray[1];
			int pcMetric1Counter = dataTable.getCellValueAsInt(pcMetric1, "PCMetric1") + 1;
			dataTable.setCellValue(pcMetric1, "PCMetric1", pcMetric1Counter);
			String pcMetric2 = lineArray[3];
			int pcMetric2Counter = dataTable.getCellValueAsInt(pcMetric2, "PCMetric2") + 1;
			dataTable.setCellValue(pcMetric2, "PCMetric2", pcMetric2Counter);
			String pcMetric3 = lineArray[5];
			int pcMetric3Counter = dataTable.getCellValueAsInt(pcMetric3, "PCMetric3") + 1;
			dataTable.setCellValue(pcMetric3, "PCMetric3", pcMetric3Counter);

			String scMetric1 = lineArray[7];
			int scMetric1Counter = dataTable.getCellValueAsInt(scMetric1, "SCMetric1") + 1;
			dataTable.setCellValue(scMetric1, "SCMetric1", scMetric1Counter);
			String scMetric2 = lineArray[9];
			int scMetric2Counter = dataTable.getCellValueAsInt(scMetric2, "SCMetric2") + 1;
			dataTable.setCellValue(scMetric2, "SCMetric2", scMetric2Counter);
			String scMetric3 = lineArray[11];
			int scMetric3Counter = dataTable.getCellValueAsInt(scMetric3, "SCMetric3") + 1;
			dataTable.setCellValue(scMetric3, "SCMetric3", scMetric3Counter);
		}

		String info = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-" + fileId + "-" + metric + ".txt";
		try {
			dataTable.write(info);
			Debug.println("\tWrite table to " + info + ", line number = " + dataTable.getLineNumber());
		} catch (IOException exc) {
			Debug.println("\tCan not write table to " + info);
		}
	}
	
	public static void categorizedQualitasRecentMeasureCorrelationsGroupBySystem(boolean redo) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String[] sizeMetricArray = getImportantSizeMetric();
		String[] cohesionMetricArray = getImportantCohesionMetric();
		String[] couplingMetricArray = getImportantCouplingMetric();
		String[] inheritMetricArray = getImportantInheritMetric();
		
		Debug.setStart("Begin....");
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			String systemName = systemNames[systemIndex];
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			String version = versions[versions.length-1];
			categorizedQualitasRecentMeasureCorrelationsGroupBySystem(version, redo, sizeMetricArray, "sum(size)");
			categorizedQualitasRecentMeasureCorrelationsGroupBySystem(version, redo, cohesionMetricArray, "sum(coh)");
			categorizedQualitasRecentMeasureCorrelationsGroupBySystem(version, redo, couplingMetricArray, "sum(coup)");
			categorizedQualitasRecentMeasureCorrelationsGroupBySystem(version, redo, inheritMetricArray, "sum(inh)");
		}
		Debug.time("End....");
	}
	
	public static void categorizedQualitasRecentMeasureCorrelationsGroupBySystem(String version, boolean redo, String[] metricArray, String id) {
		String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-" + version + ".txt";
		String info = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-" + id + "-" + version + ".txt";

		Debug.println("Scan version: " + version + ", file: " + result);
		File resultFile = new File(result);
		if (!resultFile.exists()) return;

		resultFile = new File(info);
		if (resultFile.exists() && !redo) return;
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		try {
			tempTable.read(result, true);
		} catch (IOException exc) {
			Debug.println("\tCan not read table " + result);
			return;
		}
		int lineNumber = tempTable.getLineNumber();
		if (lineNumber <= 0) return;
		
		DataTableManager dataTable = new DataTableManager("summary");
		String[] columnNameArray = {"Metric", "PCMetric1", "PCValue1", "PCMetric2", "PCValue2", "PCMetric3", "PCValue3", 
				"SCMetric1", "SCValue1", "SCMetric2", "SCValue2", "SCMetric3", "SCValue3"};
		dataTable.setColumnNames(columnNameArray);
		
		int lineIndex = 0;
		String lastMetric = "";
		String pc1Metric = "";
		String pc2Metric = "";
		String pc3Metric = "";
		String pc1Value = "NA";
		String pc2Value = "NA";
		String pc3Value = "NA";
		String sc1Metric = "";
		String sc2Metric = "";
		String sc3Metric = "";
		String sc1Value = "NA";
		String sc2Value = "NA";
		String sc3Value = "NA";
		
		while (lineIndex < lineNumber) {
			String[] lineArray = tempTable.getLineAsStringArray(lineIndex);
			lineIndex = lineIndex + 1;

			if (!lastMetric.equals("") && !lineArray[0].equals(lastMetric)) {
				String[] dataLineArray = new String[columnNameArray.length];
				dataLineArray[0] = lastMetric;
				dataLineArray[1] = pc1Metric;
				dataLineArray[2] = pc1Value;
				dataLineArray[3] = pc2Metric;
				dataLineArray[4] = pc2Value;
				dataLineArray[5] = pc3Metric;
				dataLineArray[6] = pc3Value;
				dataLineArray[7] = sc1Metric;
				dataLineArray[8] = sc1Value;
				dataLineArray[9] = sc2Metric;
				dataLineArray[10] = sc2Value;
				dataLineArray[11] = sc3Metric;
				dataLineArray[12] = sc3Value;
				dataTable.appendLine(dataLineArray);
				
				lastMetric = lineArray[0];
				pc1Metric = "";
				pc2Metric = "";
				pc3Metric = "";
				pc1Value = "NA";
				pc2Value = "NA";
				pc3Value = "NA";
				sc1Metric = "";
				sc2Metric = "";
				sc3Metric = "";
				sc1Value = "NA";
				sc2Value = "NA";
				sc3Value = "NA";
			} else if (lastMetric.equals("")) lastMetric = lineArray[0];
			
			String pcValue = lineArray[2];
			String metric = lineArray[1];
			if (!isInStringArray(metric, metricArray)) continue;
			
			if (compareStringAsDoubleValue(pc1Value, pcValue) < 0) {
				pc3Value = pc2Value;
				pc2Value = pc1Value;
				pc3Metric = pc2Metric;
				pc2Metric = pc1Metric;
				pc1Value = pcValue;
				pc1Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(pc2Value, pcValue) < 0) {
				pc3Value = pc2Value;
				pc3Metric = pc2Metric;
				pc2Value = pcValue;
				pc2Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(pc3Value, pcValue) < 0) {
				pc3Value = pcValue;
				pc3Metric = lineArray[1];
			}
			
			String scValue = lineArray[4];
			if (compareStringAsDoubleValue(sc1Value, scValue) < 0) {
				sc3Value = sc2Value;
				sc2Value = sc1Value;
				sc3Metric = sc2Metric;
				sc2Metric = sc1Metric;
				sc1Value = scValue;
				sc1Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(sc2Value, scValue) < 0) {
				sc3Value = sc2Value;
				sc3Metric = sc2Metric;
				sc2Value = scValue;
				sc2Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(sc3Value, scValue) < 0) {
				sc3Value = scValue;
				sc3Metric = lineArray[1];
			}
		}
		if (!lastMetric.equals("")) {
			String[] dataLineArray = new String[columnNameArray.length];
			dataLineArray[0] = lastMetric;
			dataLineArray[1] = pc1Metric;
			dataLineArray[2] = pc1Value;
			dataLineArray[3] = pc2Metric;
			dataLineArray[4] = pc2Value;
			dataLineArray[5] = pc3Metric;
			dataLineArray[6] = pc3Value;
			dataLineArray[7] = sc1Metric;
			dataLineArray[8] = sc1Value;
			dataLineArray[9] = sc2Metric;
			dataLineArray[10] = sc2Value;
			dataLineArray[11] = sc3Metric;
			dataLineArray[12] = sc3Value;
			dataTable.appendLine(dataLineArray);
		}
		try {
			dataTable.write(info);
			Debug.println("\tWrite table to " + info + ", line number = " + dataTable.getLineNumber());
		} catch (IOException exc) {
			Debug.println("\tCan not write table to " + info);
		}
	}
	
	public static boolean isInStringArray(String target, String[] stringArray) {
		if (stringArray == null) return true;
		if (stringArray.length <= 0) return true;
		for (int i = 0; i < stringArray.length; i++) {
			if (target.equals(stringArray[i])) return true;
		}
		return false;
	}
	
	public static void summaryQualitasRecentMeasureCorrelationsGroupByMetric(boolean redo) {
		String[] metricArray = getImportantMetric();
		
		Debug.setStart("Begin....");
		for (String metric : metricArray) {
			Debug.println("Scan metric " + metric);
			summaryQualitasRecentMeasureCorrelationsGroupByMetric(metric, redo);
		}
		Debug.time("End....");
	}
	
	public static void summaryQualitasRecentMeasureCorrelationsGroupByMetric(String metric, boolean redo) {
		DataTableManager dataTable = new DataTableManager("summary");
		String[] columnNameArray = {"Metric", "PCMetric1", "PCMetric2", "PCMetric3", "PCMean", "SCMetric1", "SCMetric2", "SCMetric3", "SCMean"};
		dataTable.setColumnNames(columnNameArray);
		dataTable.setKeyColumnIndex(0);
		String[] metricArray = getImportantMetric();
		String spearman = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-Spearman-" + metric + ".txt";
		String pearson = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-Pearson-" + metric + ".txt";
		DataTableManager pearsonTable = new DataTableManager("pearson");
		DataTableManager spearmanTable = new DataTableManager("spearman");
		try {
			pearsonTable.read(pearson, true);
			spearmanTable.read(spearman, true);
		} catch (IOException exc) {
			Debug.println("\tCan not read spearman or pearson table for the metric " + metric);
			return;
		}
		for (int i = 0; i < metricArray.length; i++) {
			if (metricArray[i].equals(metric)) continue;
			
			String[] dataLineArray = new String[columnNameArray.length];
			for (int j = 0; j < dataLineArray.length; j++) dataLineArray[j] = "";
			dataLineArray[0] = metricArray[i];
			double pcmean = DataTableUtil.meanOfColumn(pearsonTable, metricArray[i]);
			double scmean = DataTableUtil.meanOfColumn(spearmanTable, metricArray[i]);
			String pcmeanString = "NA";
			String scmeanString = "NA";
			if (!Double.isNaN(pcmean)) pcmeanString = "" + pcmean;
			if (!Double.isNaN(scmean)) scmeanString = "" + scmean;
			dataLineArray[4] = pcmeanString;
			dataLineArray[8] = scmeanString;
			dataTable.appendLine(dataLineArray);
		}
		pearsonTable.close();
		spearmanTable.close();
		
		DataTableManager tempTable = new DataTableManager("temp");
		String[] systemNames = QualitasPathsManager.getSystemNames();
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			String systemName = systemNames[systemIndex];
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			String version = versions[versions.length-1];
			String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-summary-" + version + ".txt";
			File resultFile = new File(result);
			if (!resultFile.exists()) continue;
			
			Debug.println("Scan version " + version + ", file " + result);
			try {
				tempTable.read(result, true);
			} catch (IOException exc) {
				Debug.println("\tCan not read file " + result);
				continue;
			}
			tempTable.setKeyColumnIndex("Metric");
			String[] lineArray = tempTable.getLineAsStringArray(metric);
			String pcMetric1 = lineArray[1];
			int pcMetric1Counter = dataTable.getCellValueAsInt(pcMetric1, "PCMetric1") + 1;
			dataTable.setCellValue(pcMetric1, "PCMetric1", pcMetric1Counter);
			String pcMetric2 = lineArray[3];
			int pcMetric2Counter = dataTable.getCellValueAsInt(pcMetric2, "PCMetric2") + 1;
			dataTable.setCellValue(pcMetric2, "PCMetric2", pcMetric2Counter);
			String pcMetric3 = lineArray[5];
			int pcMetric3Counter = dataTable.getCellValueAsInt(pcMetric3, "PCMetric3") + 1;
			dataTable.setCellValue(pcMetric3, "PCMetric3", pcMetric3Counter);

			String scMetric1 = lineArray[7];
			int scMetric1Counter = dataTable.getCellValueAsInt(scMetric1, "SCMetric1") + 1;
			dataTable.setCellValue(scMetric1, "SCMetric1", scMetric1Counter);
			String scMetric2 = lineArray[9];
			int scMetric2Counter = dataTable.getCellValueAsInt(scMetric2, "SCMetric2") + 1;
			dataTable.setCellValue(scMetric2, "SCMetric2", scMetric2Counter);
			String scMetric3 = lineArray[11];
			int scMetric3Counter = dataTable.getCellValueAsInt(scMetric3, "SCMetric3") + 1;
			dataTable.setCellValue(scMetric3, "SCMetric3", scMetric3Counter);
		}

		String info = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-metric-summary-" + metric + ".txt";
		try {
			dataTable.write(info);
			Debug.println("\tWrite table to " + info + ", line number = " + dataTable.getLineNumber());
		} catch (IOException exc) {
			Debug.println("\tCan not write table to " + info);
		}
	}
	

	public static void summaryQualitasRecentMeasureCorrelationsGroupBySystem(boolean redo) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		
		Debug.setStart("Begin....");
		for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
			String systemName = systemNames[systemIndex];
			String[] versions = QualitasPathsManager.getSystemVersions(systemName);
			String version = versions[versions.length-1];
			summaryQualitasRecentMeasureCorrelationsGroupBySystem(version, redo);
		}
		Debug.time("End....");
	}
	
	public static void summaryQualitasRecentMeasureCorrelationsGroupBySystem(String version, boolean redo) {
		String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-" + version + ".txt";
		String info = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-summary-" + version + ".txt";

		Debug.println("Scan version: " + version + ", file: " + result);
		File resultFile = new File(result);
		if (!resultFile.exists()) return;

		resultFile = new File(info);
		if (resultFile.exists() && !redo) return;
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		try {
			tempTable.read(result, true);
		} catch (IOException exc) {
			Debug.println("\tCan not read table " + result);
			return;
		}
		int lineNumber = tempTable.getLineNumber();
		if (lineNumber <= 0) return;
		
		DataTableManager dataTable = new DataTableManager("summary");
		String[] columnNameArray = {"Metric", "PCMetric1", "PCValue1", "PCMetric2", "PCValue2", "PCMetric3", "PCValue3", 
				"SCMetric1", "SCValue1", "SCMetric2", "SCValue2", "SCMetric3", "SCValue3"};
		dataTable.setColumnNames(columnNameArray);
		
		int lineIndex = 0;
		String lastMetric = "";
		String pc1Metric = "";
		String pc2Metric = "";
		String pc3Metric = "";
		String pc1Value = "NA";
		String pc2Value = "NA";
		String pc3Value = "NA";
		String sc1Metric = "";
		String sc2Metric = "";
		String sc3Metric = "";
		String sc1Value = "NA";
		String sc2Value = "NA";
		String sc3Value = "NA";
		
		while (lineIndex < lineNumber) {
			String[] lineArray = tempTable.getLineAsStringArray(lineIndex);
			lineIndex = lineIndex + 1;

			if (!lastMetric.equals("") && !lineArray[0].equals(lastMetric)) {
				String[] dataLineArray = new String[columnNameArray.length];
				dataLineArray[0] = lastMetric;
				dataLineArray[1] = pc1Metric;
				dataLineArray[2] = pc1Value;
				dataLineArray[3] = pc2Metric;
				dataLineArray[4] = pc2Value;
				dataLineArray[5] = pc3Metric;
				dataLineArray[6] = pc3Value;
				dataLineArray[7] = sc1Metric;
				dataLineArray[8] = sc1Value;
				dataLineArray[9] = sc2Metric;
				dataLineArray[10] = sc2Value;
				dataLineArray[11] = sc3Metric;
				dataLineArray[12] = sc3Value;
				dataTable.appendLine(dataLineArray);
				
				lastMetric = lineArray[0];
				pc1Metric = "";
				pc2Metric = "";
				pc3Metric = "";
				pc1Value = "NA";
				pc2Value = "NA";
				pc3Value = "NA";
				sc1Metric = "";
				sc2Metric = "";
				sc3Metric = "";
				sc1Value = "NA";
				sc2Value = "NA";
				sc3Value = "NA";
			} else if (lastMetric.equals("")) lastMetric = lineArray[0];
			
			String pcValue = lineArray[2];
			if (compareStringAsDoubleValue(pc1Value, pcValue) < 0) {
				pc3Value = pc2Value;
				pc2Value = pc1Value;
				pc3Metric = pc2Metric;
				pc2Metric = pc1Metric;
				pc1Value = pcValue;
				pc1Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(pc2Value, pcValue) < 0) {
				pc3Value = pc2Value;
				pc3Metric = pc2Metric;
				pc2Value = pcValue;
				pc2Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(pc3Value, pcValue) < 0) {
				pc3Value = pcValue;
				pc3Metric = lineArray[1];
			}
			
			String scValue = lineArray[4];
			if (compareStringAsDoubleValue(sc1Value, scValue) < 0) {
				sc3Value = sc2Value;
				sc2Value = sc1Value;
				sc3Metric = sc2Metric;
				sc2Metric = sc1Metric;
				sc1Value = scValue;
				sc1Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(sc2Value, scValue) < 0) {
				sc3Value = sc2Value;
				sc3Metric = sc2Metric;
				sc2Value = scValue;
				sc2Metric = lineArray[1];
			} else if (compareStringAsDoubleValue(sc3Value, scValue) < 0) {
				sc3Value = scValue;
				sc3Metric = lineArray[1];
			}
		}
		if (!lastMetric.equals("")) {
			String[] dataLineArray = new String[columnNameArray.length];
			dataLineArray[0] = lastMetric;
			dataLineArray[1] = pc1Metric;
			dataLineArray[2] = pc1Value;
			dataLineArray[3] = pc2Metric;
			dataLineArray[4] = pc2Value;
			dataLineArray[5] = pc3Metric;
			dataLineArray[6] = pc3Value;
			dataLineArray[7] = sc1Metric;
			dataLineArray[8] = sc1Value;
			dataLineArray[9] = sc2Metric;
			dataLineArray[10] = sc2Value;
			dataLineArray[11] = sc3Metric;
			dataLineArray[12] = sc3Value;
			dataTable.appendLine(dataLineArray);
		}
		try {
			dataTable.write(info);
			Debug.println("\tWrite table to " + info + ", line number = " + dataTable.getLineNumber());
		} catch (IOException exc) {
			Debug.println("\tCan not write table to " + info);
		}
	}
	
	public static int compareStringAsDoubleValue(String string1, String string2) {
		double value1 = 0;
		boolean valid1 = true;
		double value2 = 0;
		boolean valid2 = true;
		
		if (string1.equals("NA")) valid1 = false;
		else {
			try {
				value1 = Double.parseDouble(string1);
				value1 = Math.abs(value1);
			} catch (Exception exc) {
				valid1 = false;
			}
		}
		if (string2.equals("NA")) valid2 = false;
		else {
			try {
				value2 = Double.parseDouble(string2);
				value2 = Math.abs(value2);
			} catch (Exception exc) {
				valid2 = false;
			}
		}

		if (valid1 == false && valid2 == false) return 0;
		if (valid1 == false) return -1;
		if (valid2 == false) return 1;
		
		if (value1 < value2) return -1;
		else if (value1 > value2) return 1;
		else return 0;
	}
	
	
	public static void groupQualitasRecentMeasureCorrelationsByMetricAndIndex() {
		String[] metricArray = getImportantMetric();
		String[] indexArray = {"Pearson", "Spearman"};
		for (String metric : metricArray) {
			for (int i = 0; i < indexArray.length; i++) {
				groupQualitasRecentMeasureCorrelationsByMetricAndIndex(metric, indexArray[i]);
			}
		}
	}
	
	public static void groupQualitasRecentMeasureCorrelationsByMetricAndIndex(String metric, String index) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String[] metricArray = getImportantMetric();
		String info = QualitasPathsManager.defaultDebugPath + "Corr-" + index + "-" + metric + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = new String[metricArray.length];
		columnNameArray[0] = "System";
		int columnIndex = 1;
		for (int i = 0; i < metricArray.length; i++) {
			if (!metricArray[i].equals(metric)) {
				columnNameArray[columnIndex] = metricArray[i];
				columnIndex = columnIndex + 1;
			}
		}
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan metric " + metric + ", index " + index + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Corr-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				int metricAColumn = tempTable.getColumnIndex("MetricA");
				int metricBColumn = tempTable.getColumnIndex("MetricB");
				int indexColumn = tempTable.getColumnIndex(index);
				int lineNumber = tempTable.getLineNumber();

				String[] dataLineArray = new String[columnNameArray.length];
				dataLineArray[0] = version;
				
				for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
					String[] lineArray = tempTable.getLineAsStringArray(lineIndex);
					String metricA = lineArray[metricAColumn];
					if (metricA.equals(metric)) {
						String metricB = lineArray[metricBColumn];
						String indexValue = lineArray[indexColumn];
						columnIndex = dataTable.getColumnIndex(metricB);
						dataLineArray[columnIndex] = indexValue;
					}
				}
				dataTable.appendLine(dataLineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + metric + ", index " + index + " ......");
	}

	public static void selectQualitasRecentMeasureTests(double threshold, String[] metricArray, String[] indexArray) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Test-pval-select.txt";
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = {"System", "Metric", "Index", "Value"};
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Begin select.....");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "normtest" + QualitasPathsManager.pathSeparator + "Test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempColumnNameArray = tempTable.getColumnNameArray();
				int lineNumber = tempTable.getLineNumber();
				for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
					String[] tempLineArray = tempTable.getLineAsStringArray(lineIndex);
					if (!isInStringArray(tempLineArray[0], metricArray)) continue;
					
					for (int colIndex = 0; colIndex < tempColumnNameArray.length; colIndex++) {
						String columnName = tempColumnNameArray[colIndex];
						if (!isInStringArray(columnName, indexArray)) continue;
						if (columnName.contains("Pval")) {
							double value = 0;
							try {
								value = Double.parseDouble(tempLineArray[colIndex]);
							} catch (Exception exc) {
								value = 0;
							}
							if (value >= threshold) {
								String[] lineArray = new String[columnNameArray.length];
								lineArray[0] = version;
								lineArray[1] = tempLineArray[0];
								lineArray[2] = columnName;
								lineArray[3] = "" + value;
								
								Debug.println("Find system " + version + ", metric " + tempLineArray[0] + ", index " + columnName + ", value " + value);
								dataTable.appendLine(lineArray);
							}
						}
					}
				}
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for select ......");
	}
	

	public static void groupQualitasRecentMeasureTestsByMetric() {
		String[] metricArray = getImportantMetric();
		for (String metric : metricArray) {
			groupQualitasRecentMeasureTestsByMetric(metric);
		}
	}

	public static void groupQualitasRecentMeasurePowerlawTestsByIndex(int ratioThrehold) {
		String[] indexArray = {"HLKSPVal"};
		for (String index : indexArray) {
			groupQualitasRecentMeasurePowerlawTestsByIndex(index, ratioThrehold);
		}
	}
	
	public static void groupQualitasRecentMeasurePowerlawTestsByIndex(String index, int ratioThrehold) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Test-index-" + index + "(" + ratioThrehold + ")" + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] metricArray = getImportantMetric();
		String[] columnNameArray = new String[metricArray.length + 1];
		columnNameArray[0] = "System";
		for (int i = 0; i < metricArray.length; i++) {
			columnNameArray[i+1] = metricArray[i];
		}
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan index " + index + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "normtest" + QualitasPathsManager.pathSeparator + "Powerlaw-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempArray = tempTable.getColumnAsStringArray(index);
				int[] lengthArray = tempTable.getColumnAsIntArray("Length");
				int[] ntailArray = tempTable.getColumnAsIntArray("PLTNTail");
				if (tempArray == null) {
					Debug.println("\tCan not read data of index [" + index + "] in file " + result);
					continue;
				}
				if (tempArray.length != columnNameArray.length-1) {
					Debug.println("\tThe length of line [" + (tempArray.length+1) + " != column length [" + columnNameArray.length + "]!");
					continue;
				}
				
				String[] lineArray = new String[tempArray.length+1];
				lineArray[0] = version;
				for (int i = 0; i < tempArray.length; i++) {
					double ratio = (double)ntailArray[i] / lengthArray[i];
					if (ratio * 100 >= ratioThrehold) lineArray[i+1] = tempArray[i];
					else lineArray[i+1] = "NA";
				}
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + index + " ......");
	}
	
	public static void groupQualitasRecentMeasureTestsByIndex() {
		String[] indexArray = getImportantTestIndex();
		for (String index : indexArray) {
			groupQualitasRecentMeasureTestsByIndex(index);
		}
	}
	
	public static void groupQualitasRecentMeasureTestsByIndex(String index) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Test-index-" + index + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] metricArray = getImportantMetric();
		String[] columnNameArray = new String[metricArray.length + 1];
		columnNameArray[0] = "System";
		for (int i = 0; i < metricArray.length; i++) {
			columnNameArray[i+1] = metricArray[i];
		}
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan index " + index + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "normtest" + QualitasPathsManager.pathSeparator + "Test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempArray = tempTable.getColumnAsStringArray(index);
				if (tempArray == null) {
					Debug.println("\tCan not read data of index [" + index + "] in file " + result);
					continue;
				}
				if (tempArray.length != columnNameArray.length-1) {
					Debug.println("\tThe length of line [" + (tempArray.length+1) + " != column length [" + columnNameArray.length + "]!");
					continue;
				}
				
				String[] lineArray = new String[tempArray.length+1];
				lineArray[0] = version;
				for (int i = 0; i < tempArray.length; i++) lineArray[i+1] = tempArray[i];
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + index + " ......");
	}
	
	public static void groupQualitasRecentMeasureTestsByMetric(String metric) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Test-metric-" + metric + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = null;
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan metric " + metric + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Test-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				tempTable.setKeyColumnIndex("Metric");
				String[] lineArray = tempTable.getLineAsStringArray(metric);
				if (lineArray == null) {
					Debug.println("\tCan not read data of metric [" + metric + "] in file " + result);
					continue;
				}
				
				if (systemIndex == 0) {
					columnNameArray = tempTable.getColumnNameArray();
					columnNameArray[0] = "System";
					dataTable.setColumnNames(columnNameArray);
				}
				lineArray[0] = version;
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + metric + " ......");
	}
	
	public static void groupQualitasRecentMeasureModelsByMetric() {
		String[] metricArray = getImportantMetric();
		for (String metric : metricArray) {
			groupQualitasRecentMeasureModelsByMetric(metric);
		}
	}

	public static void groupQualitasRecentMeasureModelsByIndex() {
		String[] indexArray = getImportantModelIndex();
		for (String index : indexArray) {
			groupQualitasRecentMeasureModelsByIndex(index);
		}
	}
	
	public static void groupQualitasRecentMeasureModelsByIndex(String index) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Model-index-" + index + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] metricArray = getImportantMetric();
		String[] columnNameArray = new String[metricArray.length + 1];
		columnNameArray[0] = "System";
		for (int i = 0; i < metricArray.length; i++) {
			columnNameArray[i+1] = metricArray[i];
		}
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan index " + index + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Model-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempArray = tempTable.getColumnAsStringArray(index);
				if (tempArray == null) {
					Debug.println("\tCan not read data of index [" + index + "] in file " + result);
					continue;
				}
				if (tempArray.length != columnNameArray.length-1) {
					Debug.println("\tThe length of line [" + (tempArray.length+1) + " != column length [" + columnNameArray.length + "]!");
					continue;
				}
				
				String[] lineArray = new String[tempArray.length+1];
				lineArray[0] = version;
				for (int i = 0; i < tempArray.length; i++) lineArray[i+1] = tempArray[i];
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + index + " ......");
	}

	public static void calculateQualitasRecentMeasureModelsPValue() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		Debug.setScreenOn();
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Model-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				calculateQualitasRecentMeasureModelsPValue(result);
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}
	
	public static void calculateQualitasRecentMeasureModelsPValue(String metricFile) throws IOException {
		DataTableManager tempTable = new DataTableManager("temp");
		File resultFile = new File(metricFile);
		if (!resultFile.exists()) return;

		tempTable.read(metricFile, true);
		String[] originalColumnArray = tempTable.getColumnNameArray();
		int oldColumnNumber = originalColumnArray.length; 
		int lineNumber = tempTable.getLineNumber();
		int powerKSColIndex = tempTable.getColumnIndex("PowerKS");
		int powerNTailColIndx = tempTable.getColumnIndex("PowerNtail");
		int lnormKSColIndex = tempTable.getColumnIndex("LNormKS");
		int lnormNTailColIndex = tempTable.getColumnIndex("LNormNtail");
		
		DataTableManager dataTable = new DataTableManager("data");
		String[] columnNameArray = new String[oldColumnNumber+2];
		for (int i = 0; i < oldColumnNumber; i++) columnNameArray[i] = originalColumnArray[i];
		columnNameArray[oldColumnNumber] = "PowerPVal";
		columnNameArray[oldColumnNumber+1] = "LNormPVal";
		dataTable.setColumnNames(columnNameArray);
		
		for (int lineIndex = 0; lineIndex < lineNumber; lineIndex++) {
			String[] oldLineArray = tempTable.getLineAsStringArray(lineIndex);
			String[] lineArray = new String[oldColumnNumber+2];
			for (int i = 0; i < oldColumnNumber; i++) lineArray[i] = oldLineArray[i];
			
			KolmogorovSmirnovTest tester = new KolmogorovSmirnovTest();

			String powerPValString = "NA";
			if (!oldLineArray[powerKSColIndex].equals("NA")) {
				double powerKS = Double.parseDouble(oldLineArray[powerKSColIndex]);
				int powerNTail = (int)Double.parseDouble(oldLineArray[powerNTailColIndx]);
				double powerPVal = 1d - tester.cdf(powerKS, powerNTail);
				powerPValString = Double.toString(powerPVal);
			}
			String lnormPValString = "NA";
			if (!oldLineArray[lnormKSColIndex].equals("NA")) {
				double lnormKS = Double.parseDouble(oldLineArray[lnormKSColIndex]);
				int lnormNTail = (int)Double.parseDouble(oldLineArray[lnormNTailColIndex]);
				double lnormPVal = 1d - tester.cdf(lnormKS, lnormNTail);
				lnormPValString = Double.toString(lnormPVal);
			}
			lineArray[oldColumnNumber] = powerPValString;
			lineArray[oldColumnNumber+1] = lnormPValString;
			
			dataTable.appendLine(lineArray);
		}
		
		dataTable.write(metricFile);
	}

	
	public static void groupQualitasRecentMeasureModelsByMetric(String metric) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Model-metric-" + metric + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = null;
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan metric " + metric + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Model-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				tempTable.setKeyColumnIndex("Metric");
				String[] lineArray = tempTable.getLineAsStringArray(metric);
				if (lineArray == null) {
					Debug.println("\tCan not read data of metric [" + metric + "] in file " + result);
					continue;
				}
				
				if (systemIndex == 0) {
					columnNameArray = tempTable.getColumnNameArray();
					columnNameArray[0] = "System";
					dataTable.setColumnNames(columnNameArray);
				}
				lineArray[0] = version;
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + metric + " ......");
	}
	
	public static void groupQualitasRecenMeasureDescriptivesByMetric() {
		String[] metricArray = getImportantMetric();
		for (String metric : metricArray) {
			groupQualitasRecentMeasureDescriptivesByMetric(metric);
		}
	}

	public static void groupQualitasRecenMeasureDescriptivesByIndex() {
		String[] indexArray = getImportantDescriptiveIndex();
		for (String index : indexArray) {
			groupQualitasRecentMeasureDescriptivesByIndex(index);
		}
	}
	
	public static void groupQualitasRecentMeasureDescriptivesByIndex(String index) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Desc-index-" + index + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] metricArray = getImportantMetric();
		String[] columnNameArray = new String[metricArray.length + 1];
		columnNameArray[0] = "System";
		for (int i = 0; i < metricArray.length; i++) {
			columnNameArray[i+1] = metricArray[i];
		}
		dataTable.setColumnNames(columnNameArray);
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan index " + index + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Desc-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				String[] tempArray = tempTable.getColumnAsStringArray(index);
				if (tempArray == null) {
					Debug.println("\tCan not read data of index [" + index + "] in file " + result);
					continue;
				}
				if (tempArray.length != columnNameArray.length-1) {
					Debug.println("\tThe length of line [" + (tempArray.length+1) + " != column length [" + columnNameArray.length + "]!");
					continue;
				}
				
				String[] lineArray = new String[tempArray.length+1];
				lineArray[0] = version;
				for (int i = 0; i < tempArray.length; i++) lineArray[i+1] = tempArray[i];
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for index " + index + " ......");
	}
	
	public static void groupQualitasRecentMeasureDescriptivesByMetric(String metric) {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		String info = QualitasPathsManager.defaultDebugPath + "Desc-metric-" + metric + ".txt" ;
		
		DataTableManager dataTable = new DataTableManager(info);
		String[] columnNameArray = null;
		
		DataTableManager tempTable = new DataTableManager("Temp"); 
		Debug.setScreenOn();
		Debug.setStart("Scan metric " + metric + " ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + "Desc-system-" + version + ".txt";

				Debug.println("Scan version: " + version + ", file: " + result);
				File resultFile = new File(result);
				if (!resultFile.exists()) continue;

				tempTable.read(result, true);
				tempTable.setKeyColumnIndex("Metrics");
				String[] lineArray = tempTable.getLineAsStringArray(metric);
				if (lineArray == null) {
					Debug.println("\tCan not read data of metric [" + metric + "] in file " + result);
					continue;
				}
				
				if (systemIndex == 0) {
					columnNameArray = tempTable.getColumnNameArray();
					columnNameArray[0] = "System";
					dataTable.setColumnNames(columnNameArray);
				}
				lineArray[0] = version;
				dataTable.appendLine(lineArray);
			}
			dataTable.write(info);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End for metric " + metric + " ......");
	}

	public static void renameQualitasRecentMeasureDescriptiveFile() {
		String[] systemNames = QualitasPathsManager.getSystemNames();
		
		Debug.setScreenOn();
		Debug.setStart("Begin ......");
		try {
			for (int systemIndex = 0; systemIndex < systemNames.length; systemIndex++) {
				String systemName = systemNames[systemIndex];
				String[] versions = QualitasPathsManager.getSystemVersions(systemName);
				String version = versions[versions.length-1];
				String result = QualitasPathsManager.defaultResultPath + "analysis" + QualitasPathsManager.pathSeparator + version + "-descript.txt";
				String info = QualitasPathsManager.defaultDebugPath + "Desc-system-" + version + ".txt" ;

				DataTableManager dataTable = new DataTableManager("");
				dataTable.read(result, true);
				Debug.println("Scan version: " + version + ", file: " + result);
				dataTable.write(info);
			}
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		Debug.time("End ......");
	}
	
	public static String[] getAvailableDescriptiveIndex() {
		String[] indexArray = {
					"Length", "Min", "Q1",	"Median", "Q3", "Max", "Mean", "Sd", "Sum", "Skew", "Kurt", "CV", "Gini", "V20", "S20", 
					"MedianPos", "ValueNum", "ValueRange", "MinNum", "MaxNum",	"LogLength", "LogMedian", "LogMean", "LogSkew",	 
					"LogKurt", "LogSD", "LogCV", "LogGini"				
		};
		
		return indexArray;
	}

	public static String[] getImportantDescriptiveIndex() {
//		String[] indexArray = {
//					"Min", "Q1", "Median", "Q3", "Max", "Mean", "Sd", "Skew", "Kurt", "CV", "Gini", "V20", "S20", 
//					"ValueNum",	"MinNum", "MaxNum",	"LogLength", "LogMedian", "LogMean" 				
//		};
	
		String[] indexArray = {"Length", "Min", "Q1", "Median", "Q3", "Max", "Mean", "Sd", "Gini", "S20", "MeanPos", "MedianPos", "ValueNum", "ValueRange", "LogLength", "MinNum", "MaxNum"};
		return indexArray;
	}
	
	public static String[] getImportantMetric() {
		String[] metricArray = {
				"TCCp", "LCCp", "DCDp", "DCIp", "CCp", "SCOMp",
		};
//		String[] metricArray = {
//				"MTHD", "IMPMTHD", "FLD", "ELOC",
//				"LCOM1", "LCOM1p", "LCOM2", "LCOM2p", "LCOM5", "Coh", "TCC", "LCC", "DCD", "DCI", "CAMC", "NHD", "ICH", "CC", "LSCC", "SCOM", "SCC",
//		};
//		String[] metricArray = {
//				"MTHD", "ALLMTHD", "IMPMTHD", "FLD", "ALLFLD", "ELOC", "LOPT", "STMN", "PARS", "LOCV",
//				"LCOM1", "LCOM2", "LCOM5", "Coh", "TCC", "LCC", "DCD", "DCI", "CAMC", "NHD", "ICH", "CC", "LSCC", "SCOM", "SCC",
//				"CBO", "RFC", "RFCp", "MPC", "DAC", "DACp", "NIHICP", "IHICP", "ICP", "ACAIC", "ACMIC", "AMMIC", "DCAEC", "DCMEC", "DMMEC", "OCAIC", "OCMIC", "OMMIC", "OCAEC", "OCMEC", "OMMEC",
//				"DIT", "AID", "CLD", "NOC", "NOP", "NOD", "NOA", "IHMTHD", "OVMTHD", "NEWMTHD", "IHFLD", "SIX", "SPA", "SPD", "SP", "DPA", "DPD", "DP", 
//		};
		return metricArray;
	}

	public static String[] getImportantSizeMetric() {
		String[] metricArray = {
				"MTHD", "ALLMTHD", "IMPMTHD", "FLD", "ALLFLD", "ELOC", "LOPT", "STMN", "PARS", "LOCV", 
		};
		return metricArray;
	}
	
	public static String[] getImportantCohesionMetric() {
		String[] metricArray = {
//				"LCOM1", "LCOM1p", "LCOM2", "LCOM2p", "LCOM5", "Coh", "TCC", "LCC", "DCD", "DCI", "CC", "LSCC", "SCOM", "CAMC", "NHD", "ICH", "SCC",
				"LCOM1", "LCOM2", "LCOM5", "Coh", "TCC", "LCC", "DCD", "DCI", "CAMC", "NHD", "ICH", "CC", "LSCC", "SCOM", "SCC",
		};
		return metricArray;
	}

	public static String[] getImportantCouplingMetric() {
		String[] metricArray = {
				"CBO", "RFC", "RFCp", "MPC", "DAC", "DACp", "NIHICP", "IHICP", "ICP", "ACAIC", "ACMIC", "AMMIC", "DCAEC", "DCMEC", "DMMEC", "OCAIC", "OCMIC", "OMMIC", "OCAEC", "OCMEC", "OMMEC",
		};
		return metricArray;
	}

	public static String[] getImportantInheritMetric() {
		String[] metricArray = {
				"DIT", "AID", "CLD", "NOC", "NOP", "NOD", "NOA", "IHMTHD", "OVMTHD", "NEWMTHD", "IHFLD", "SIX", "SPA", "SPD", "SP", "DPA", "DPD", "DP", 
		};
		return metricArray;
	}

	public static String[] getImportantModelIndex() {
		String[] indexArray = {
				"PowerKS", "PowerPVal", "PowerXmin", "PowerAlpha", "PowerNtail", "LNormKS", "LNormPVal", "LnormXmin", "LNormMu", "LNormSigma", "LNormNtail" 
		};
		
		return indexArray;
	}

	public static String[] getImportantTestIndex() {
		String[] indexArray = {
				"SWStat", "SWPval", "SFStat", "SFPval", "ADStat", "ADPval", "CVMStat", "CVMPval", "KSStat", "KSPval", "LKSStat", "LKSPval", "PCStat", "PCPval", 
				"LogSWStat", "LogSWPval", "LogSFStat", "LogSFPval", "LogADStat", "LogADPval", "LogCVMStat", "LogCVMPval", "LogKSStat", "LogKSPval", "LogLKSStat", 
				"LogLKSPval", "LogPCStat", "LogPCPval" 
		};
		
		return indexArray;
	}

	public static String[] getPowerLawTestIndex() {
		String[] indexArray = {
				"PLTCont", "PLTAlpha", "PLTXmin", "PLTNTail", "PLTLogLik", "PLTKSStat", "PLTKSPValue", 
				"HLLength",	"HLValNum",	"HLKSStat",	"HLKSPVal",	"HLADStat",	"HLADPVal",	"HLCVMStat", "HLCVMPVal", "HLLKSStat", "HLLKSPVal"
		};
		
		return indexArray;
	}
	
	public static String[] getImportantOutlierTestIndex() {
		String[] indexArray = {
				"TPMin", "TPMean", "TPMax", "TRejNum", "WPMin", "WPMean", "WPMax", "WRejNum"
		};
		
		return indexArray;
	}
	
}
