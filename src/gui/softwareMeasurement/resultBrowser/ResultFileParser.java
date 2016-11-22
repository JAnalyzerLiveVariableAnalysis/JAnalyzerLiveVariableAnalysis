package gui.softwareMeasurement.resultBrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.table.AbstractTableModel;

public class ResultFileParser extends AbstractTableModel {

	int rowCount = 0;
	int colCount = 0;
	File file;
	String[] columnNames;
	String[][] value;
	List<String> filePaths = new ArrayList<String>();
	

	public ResultFileParser(File file) {
		this.file = file;
		// 扫描两次，第一次得到数据的行列数量且初始化文件路径，第二次填充数据到String数组中去。
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				rowCount++;
				String line = scanner.nextLine();
				if (colCount == 0) {
					columnNames = line.split("\t");
					colCount = columnNames.length;
					--colCount;// 第一列是文件路径，少算一列
				} else {
					filePaths.add(line.split("\t")[0]);
				}
			}
			--rowCount;// 第一行是表头，所以少算一行
			scanner.close();
			
			value = new String[rowCount][colCount];
			scanner = new Scanner(file);
			String line = null;
			int rowIndex = 0;
			scanner.nextLine();//第一行跳过
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				String[] lineValue = line.split("\t");
				for(int i = 0; i < colCount; i++) {
					// 每一行的第一列中放的是文件路径，不需要展示，因此也跳过
					value[rowIndex][i] = lineValue[i+1];
				}
				rowIndex ++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		return colCount;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return value[rowIndex][columnIndex];
	}

	public String getColumnName(int column) {
		return columnNames[column + 1];
	}

	public String getFilePath(int row) {
		if (filePaths != null) {
			return filePaths.get(row);
		}
		return null;
	}

}
