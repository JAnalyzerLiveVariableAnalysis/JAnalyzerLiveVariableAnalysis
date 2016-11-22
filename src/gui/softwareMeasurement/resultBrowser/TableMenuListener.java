package gui.softwareMeasurement.resultBrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import gui.softwareMeasurement.codeBrowser.JSrcViewer;
import gui.softwareMeasurement.mainFrame.Log;
import gui.softwareMeasurement.mainFrame.SrcTabbedPane;
import gui.softwareMeasurement.resultBrowser.chart.BarChart;
import gui.softwareMeasurement.resultBrowser.chart.BoxChart;
import gui.softwareMeasurement.resultBrowser.chart.ClassMeasureData;
import gui.softwareMeasurement.resultBrowser.chart.HistChart;
import gui.softwareMeasurement.resultBrowser.chart.LineChart;
import gui.softwareMeasurement.resultBrowser.chart.MeasureDistribution;
import gui.softwareMeasurement.resultBrowser.chart.MeasurementValue;

public class TableMenuListener implements ActionListener {
	
	
	ResultTable table;
	
	public TableMenuListener(ResultTable table) {
		this.table = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		final int[] rows = table.getSelectedRows();
		final int[] cols = table.getSelectedColumns();
		ResultFileParser parser = (ResultFileParser) table.getModel();
		if (command.equals(ResultTable.MENU_OPEN_SRC_STR)) {
			for (int row : rows) {
				String filePath = parser.getFilePath(row);
				File file = new File(filePath);
				if (file.isFile()) {
					table.showSrc(filePath);
				} else if(file.isDirectory()){
					JOptionPane.showMessageDialog(null, file.getName() + 
							"是一个文件夹！", "提示", JOptionPane.YES_OPTION);
				} else {
					if(table.getColumnName(0).equals("包")) {
						JOptionPane.showMessageDialog(null, table.getValueAt(row, 0)+
								"是文件夹，无法打开！");
					} else {
						JOptionPane.showMessageDialog(null, "文件" + file.getName() + 
								"不存在！", "提示", JOptionPane.YES_OPTION);
					}
				}
			}
		}
		if (command.equals(ResultTable.MENU_BAR_CHART_STR)) {
			new Thread() {
				public void run() {
					List<ClassMeasureData> datas = encapsulateData(
							rows, cols);
					BarChart chart = new BarChart("chart", datas);
					chart.setVisible(true);
				}
			}.start();

		}
		if (command.equals(ResultTable.MENU_HIST_CHART_STR)) {
			new Thread() {
				public void run() {
					List<MeasureDistribution> datas = getAllMeasureDistributions(
							rows, cols);
					for(MeasureDistribution data : datas) {
						HistChart chart = new HistChart("chart", data);
						chart.setVisible(true);
					}
				}
			}.start();
		}
		if (command.equals(ResultTable.MENU_BOX_CHART_STR)) {
			new Thread() {
				public void run() {
					List<MeasureDistribution> datas = getAllMeasureDistributions(
							rows, cols);
					BoxChart chart = new BoxChart("chart", datas);
					chart.setVisible(true);
				}
			}.start();
		}
		if (command.equals(ResultTable.MENU_LINE_CHART_STR)) {
			new Thread() {
				public void run() {
					List<ClassMeasureData> datas = encapsulateData(
							rows, cols);
					LineChart chart = new LineChart("chart", datas);
					chart.setVisible(true);
				}
			}.start();
		}
		if (command.equals(ResultTable.MENU_VARIANCE_STR)) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "方差\r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.variance(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("下四分位数")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "下四分位数 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.littleQuartile(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("中位数")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "中位数 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.median(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("上四分位数")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "上四分位数 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.bigQuartile(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("均值")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "均值 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.mean(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("偏度")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "偏度 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.skewness(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("峰度")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "峰度 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.kurtosis(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("变异系数")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "变异系数 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.CV(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("基尼指数")) {
			int[] selectedCols = table.getSelectedColumns();
			String output = "基尼指数 \r\n  ";
			for(int col : selectedCols) {
				if(col > 0) {
					List<Double> datas = getSelectedNumber(col);
					output += table.getColumnName(col);
					output += ": ";
					output += MetricMath.gini(datas);
					output += ";  ";
				}
			}
			String op = output.replace(";", "\r\n");
			op = "  " + op;
			JOptionPane.showMessageDialog(null, op);
			Log.consoleLog(output);
		}
		if (command.equals("搜索单元格")) {
			if(ResultTable.searcher == null) {
				ResultTable.searcher = new Searcher(table);
			} else {
				ResultTable.searcher.setSearchViewer(table);
			}
			ResultTable.searcher.setVisible(true);
		}
	}
	
	/**
	 * 获取表格中指定列的被选中的数据
	 * @param col
	 * @return
	 */
	private List<Double> getSelectedNumber(int col) {
		List<Double> datas = new ArrayList<Double>();
		for(int i : table.getSelectedRows()) {
			Object o = table.getValueAt(i, col);
			String s = (String)o;
			if(!s.equals("N.A")) {
				double value = Double.valueOf(s);
				datas.add(value);
			}
		}
		return datas;
	}

	private List<ClassMeasureData> encapsulateData(int[] selectedRows,
			int[] selectedCols) {
		ArrayList<ClassMeasureData> datas = new ArrayList<ClassMeasureData>();
		for (int row : selectedRows) {
			ClassMeasureData data = new ClassMeasureData((String) table.getValueAt(
					row, 0));
			for (int col : selectedCols) {
				if (col > 0) {// 第零列是类名，在前面已经取出
					String value = (String) table.getValueAt(row, col);
					data.addMeasure(new MeasurementValue(table.getColumnName(col),
							Double.parseDouble(value)));
				}
			}
			datas.add(data);
		}
		return datas;
	}

	private List<MeasureDistribution> getAllMeasureDistributions(
			int[] selectedRows, int[] selectedCols) {
		List<MeasureDistribution> res = new ArrayList<MeasureDistribution>();
		for (int col : selectedCols) {
			if (col > 0) {// 第一列为类名
				ArrayList<Double> values = new ArrayList<Double>();
				for (int row : selectedRows) {
					String value = (String) table.getValueAt(row, col);
					if (row > 0) {// 第一行为度量名
						values.add(Double.parseDouble(value));
					}
				}
				// 存放的是度量及其分布情况
				MeasureDistribution distribution = new MeasureDistribution(
						table.getColumnName(col), values);
				res.add(distribution);
			}
		}

		return res;
	}
}
