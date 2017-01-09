package analyzer.qualitas;

import java.io.IOException;

import analyzer.dataTable.DataTableManager;
import analyzer.dataTable.DataTableUtil;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2016Äê12ÔÂ29ÈÕ
 * @version 1.0
 *
 */
public class ClassOutlierSelector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		findOutlierLines();
	}

	public static void findOutlierLines() {
		String rootPath = QualitasPathsManager.defaultResultPath;
		String version = "jhotdraw-7.5.1";
		String classMetricFile = rootPath + "class" + QualitasPathsManager.pathSeparator + version + ".txt";

		String outlierFile = rootPath + QualitasPathsManager.pathSeparator + version + "-cohesion-outlier.txt";
		String resultFile = rootPath + QualitasPathsManager.pathSeparator + version + "-cohesion-outlier-class.txt";
		
		
		DataTableManager dataManager = new DataTableManager("data");
		DataTableManager outlierIndicator = new DataTableManager("OutlierIndicator");
		try {
			dataManager.read(classMetricFile, true);
			Debug.println("Data: line number = " + dataManager.getLineNumber() + ", column number = " + dataManager.getColumnNumber());
			
			outlierIndicator.read(outlierFile, true);
			
			DataTableManager resultManager = DataTableUtil.findOutlierLinesWithReason(dataManager, outlierIndicator);
			Debug.println("Result: line number = " + resultManager.getLineNumber() + ", column number = " + resultManager.getColumnNumber());
			resultManager.write(resultFile);
			
//			PrintWriter writer = new PrintWriter(new FileOutputStream(resultFile));
//			DataTableUtil.writeDataLinesAsLatexTableLines(writer, manager, null);
//			writer.close();
		} catch (Exception exec) {
			exec.printStackTrace();
		}
	}
	
}
