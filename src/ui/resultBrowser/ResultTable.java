package ui.resultBrowser;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ui.mainFrame.SrcTabbedPane;
import ui.resultBrowser.chart.BarChart;
import ui.resultBrowser.chart.BoxChart;
import ui.resultBrowser.chart.ClassMeasureData;
import ui.resultBrowser.chart.HistChart;
import ui.resultBrowser.chart.LineChart;
import ui.resultBrowser.chart.MeasureDistribution;
import ui.resultBrowser.chart.MeasurementValue;

public class ResultTable extends JTable {

	private JTabbedPane tabbedPane;
	static Searcher searcher;
	static int tableCount = 0;

	final static String MENU_DES_STATISTICS_STR = "描述性统计";
	
	final static String MENU_METRIC_CHART_STR = "统计图形";
	final static String MENU_BAR_CHART_STR = "条形图";
	final static String MENU_HIST_CHART_STR = "直方图";
	final static String MENU_BOX_CHART_STR = "盒图";
	final static String MENU_LINE_CHART_STR = "折线图";

	final static String MENU_NUMERICAL_CHARAC_STR = "数值特征";
	
	final static String MENU_VARIANCE_STR = "方差"; 
	
	final static String MENU_OPEN_SRC_STR = "查看源代码";
	
	final static String MENU_SEARCH_STR = "搜索";

	public ResultTable(TableModel model) {
		super(model);
		tableCount ++;
		ResultTableCellRenderer renderer = new ResultTableCellRenderer();
		setDefaultRenderer(Object.class, renderer);
		
		addMouseListener(renderer);
		addMouseMotionListener(renderer);

		int colNum = model.getColumnCount();
		for(int i = 0; i < colNum; i++) {
			TableColumn columnObj = getColumnModel().getColumn(i);
			columnObj.setMinWidth(80);
		}
		
		setColumnSelectionAllowed(true);
		
		final JTableHeader header = getTableHeader();
		header.setResizingAllowed(true);

		header.setReorderingAllowed(false);
		header.addMouseListener(new MouseAdapter() {
			/** lastColumn是用来记录按下shift之前最后点击的那一列 **/
			int lastColumn = 0;
			boolean shiftMask = false;
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					int column = header.columnAtPoint(e.getPoint());
					if (column > 0) {
						if (e.isShiftDown()) {
							clearSelection();
							addColumnSelectionInterval(lastColumn, column);
							addRowSelectionInterval(0, getRowCount() - 1);
							shiftMask = true;
						} else {
							shiftMask = false;
							if (e.isControlDown()) {// 按下了ctrl键
								if(!isColumnSelected(column)) {// 当前列没被选中，则全选当前的列
									addColumnSelectionInterval(column, column);
									addRowSelectionInterval(0, getRowCount() - 1);
								} else {// 当前列如果被选中，则移除当前列
									removeColumnSelectionInterval(column, column);
								}
							} else {
								clearSelection();
								if(!isColumnSelected(column)) {// 当前列没被选中，则全选当前的列
									addColumnSelectionInterval(column, column);
									addRowSelectionInterval(0, getRowCount() - 1);
								}
							}
						}
						if(!shiftMask)// 没有按下shift的时候，改变lastColumn的值
							lastColumn = column;
					}
				}
			}
		});

		MouseAdapter adapter = createMouseAdapter();
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		
	}
	
	private MouseAdapter createMouseAdapter() {
		MouseAdapter adapter = new MouseAdapter() {
			int originRow = -1;// 用来记录鼠标在行上的拖拽，按下时所在的那一行
			/** shiftOriginRow 用来记录shift按下的时候，鼠标点击过的最后一行 **/
			int shiftOriginRow = -1;
			boolean shiftMask = false;
			boolean isButton1Down;
			/** 由于每次选中第零列的某一行时,isRowSelected(row)必然得到的是true,因此使用这个BitSet来记录之前被选中的行 **/
			BitSet lastState = new BitSet();

			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					isButton1Down = true;
				}
				int col = columnAtPoint(e.getPoint());
				originRow = rowAtPoint(e.getPoint());
				if(isButton1Down && col == 0) {// 右键在第零列按下
					if (e.isShiftDown()) {// 按下了shift,就选中两行之间的所有行
						clearSelection();
						lastState.clear();
						if(shiftOriginRow > -1) {
							int endRow = rowAtPoint(e.getPoint());
							addRowSelectionInterval(shiftOriginRow, endRow);
							addColumnSelectionInterval(0, getColumnCount() - 1);
							// 将选中的行记录到BitSet中
							int min = shiftOriginRow > endRow ? endRow : shiftOriginRow;
							int max = shiftOriginRow > endRow ? shiftOriginRow : endRow;
							for(int i = min; i <= max; i++) {
								lastState.set(i);
							}
						}
						shiftMask = true;
					} else {
						shiftMask = false;
						if(e.isControlDown()) {
							int row = rowAtPoint(e.getPoint());
							if(lastState.get(row)) {// 按下ctrl时，如果之前已经选择过了这行，则删除
													// 且移除这行在BitSet中对应的标志
								removeRowSelectionInterval(row, row);
								lastState.clear(row);
							} else {
								addRowSelectionInterval(row, row);
								addColumnSelectionInterval(0, getColumnCount() - 1);
								lastState.set(row);
							}
							
						} else {// 如果shift没有按下，且ctrl也没有按下
							selectRow(e);
						}
					}
				}
				if (e.getButton() == MouseEvent.BUTTON3) {// 响应右键，弹出菜单
					// 被选中的行和列
					int[] selectedRows = getSelectedRows();
					int[] selectedCols = getSelectedColumns();
					if (selectedRows != null && selectedRows.length > 0
							&& selectedCols != null && selectedCols.length > 0) {
						showMenu(e);
					}
				}
				if(!shiftMask) {
					shiftOriginRow = rowAtPoint(e.getPoint());
				}
			}

			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					int[] rows = getSelectedRows();
					ResultFileParser parser = (ResultFileParser) getModel();
					for (int row : rows) {
						String filePath = parser.getFilePath(row);
						File file = new File(filePath);
						if (file.isFile()) {
							showSrc(filePath);
						} else if(file.isDirectory()){
							JOptionPane.showMessageDialog(null, file.getName() + 
									"是一个文件夹！", "提示", JOptionPane.YES_OPTION);
						} else {
							if(getColumnName(0).equals("包")) {
								JOptionPane.showMessageDialog(null, getValueAt(row, 0)+
										"是文件夹，无法打开！");
							} else {
								JOptionPane.showMessageDialog(null, "文件" + file.getName() + 
										"不存在！", "提示", JOptionPane.YES_OPTION);
							}
						}
					}
				}
			}
			
			public void mouseDragged(MouseEvent e) {
				if(isButton1Down) {
					selectRow(e);
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					isButton1Down = false;
				}
			}
			
			private void selectRow(MouseEvent e) {
				int col = columnAtPoint(e.getPoint());
				if (col == 0) {// 点击的是第零列
					if(!e.isControlDown()) {
						clearSelection();
						lastState.clear();
					}
					if (originRow > -1) {
						int endRow = rowAtPoint(e.getPoint());
						if(endRow > -1 && endRow <= getRowCount() - 1) {
							addRowSelectionInterval(originRow, endRow);
							addColumnSelectionInterval(0, getColumnCount() - 1);
							int min = originRow > endRow ? endRow : originRow;
							int max = originRow > endRow ? originRow : endRow;
							for(int i = min; i <= max; i++) {
								lastState.set(i);
							}
						}
					} 
				}
			}
		};
		return adapter;
	}

	private void showMenu(MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();
		JMenu mnDes = new JMenu(MENU_DES_STATISTICS_STR);
		JMenu mnMetricChart = new JMenu(MENU_METRIC_CHART_STR);
		JMenuItem mntmBarChart = new JMenuItem(MENU_BAR_CHART_STR);
		JMenuItem mntmHistChart = new JMenuItem(MENU_HIST_CHART_STR);
		JMenuItem mntmBoxChart = new JMenuItem(MENU_BOX_CHART_STR);
		JMenuItem mntmLineChart = new JMenuItem(MENU_LINE_CHART_STR);

		mnDes.add(mnMetricChart);
		mnMetricChart.add(mntmBarChart);
		mnMetricChart.add(mntmHistChart);
		mnMetricChart.add(mntmBoxChart);
		mnMetricChart.add(mntmLineChart);

		JMenu mnNumCha = new JMenu(MENU_NUMERICAL_CHARAC_STR);
		JMenuItem mntmQ1 = new JMenuItem("下四分位数");
		mnNumCha.add(mntmQ1);
		JMenuItem mntmQ2 = new JMenuItem("中位数");
		mnNumCha.add(mntmQ2);
		JMenuItem mntmQ3 = new JMenuItem("上四分位数");
		mnNumCha.add(mntmQ3);
		JMenuItem mntmMean = new JMenuItem("均值");
		mnNumCha.add(mntmMean);
		JMenuItem mntmVari =  new JMenuItem(ResultTable.MENU_VARIANCE_STR);
		mnNumCha.add(mntmVari);
		JMenuItem mntmSw = new JMenuItem("偏度");
		mnNumCha.add(mntmSw);
		JMenuItem mntmKw = new JMenuItem("峰度");
		mnNumCha.add(mntmKw);
		JMenuItem mntmCV = new JMenuItem("变异系数");
		mnNumCha.add(mntmCV);
		JMenuItem mntmGini = new JMenuItem("基尼指数");
		mnNumCha.add(mntmGini);
		
		JMenuItem mntmOpenFile = new JMenuItem(MENU_OPEN_SRC_STR);
		
		mnDes.add(mnNumCha);
		if(getSelectedRowCount() == 1) {
			mnDes.add(mntmOpenFile);
		}
		
		
//		ResultFileParser parser = (ResultFileParser)getModel();
//		int[] rows = getSelectedRows();
//		for (int row : rows) {
//			String filePath = parser.getFilePath(row);
//			File file = new File(filePath);
//			if (!file.isFile()) {
//				mntmOpenFile.setEnabled(false);
//				break;
//			}
//		}
		menu.add(mnDes);

		ActionListener listener = new TableMenuListener(this);

		mntmBarChart.addActionListener(listener);
		mntmHistChart.addActionListener(listener);
		mntmBoxChart.addActionListener(listener);
		mntmOpenFile.addActionListener(listener);
		mntmLineChart.addActionListener(listener);
		
		mntmQ1.addActionListener(listener);
		mntmQ2.addActionListener(listener);
		mntmQ3.addActionListener(listener);
		mntmVari.addActionListener(listener);
		mntmMean.addActionListener(listener);
		mntmSw.addActionListener(listener);
		mntmKw.addActionListener(listener);
		mntmCV.addActionListener(listener);
		mntmGini.addActionListener(listener);

		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	void showSrc(String filePath) {
		((SrcTabbedPane) tabbedPane).showSrc(filePath);
	}

	public String getToolTipText(MouseEvent e) {
		Point point = e.getPoint();
		int column = columnAtPoint(point);
		int row = rowAtPoint(point);
		String s = (String) getValueAt(row, column);
		return s;
	}

	public void setTextArea(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

//	public boolean isCellSelected(int row, int col) {
//		if (col == 0)
//			return false;
//		return super.isCellSelected(row, col);
//	}

	public void dispose() {
		tableCount--;
		for(MouseListener l : getTableHeader().getMouseListeners()) {
			getTableHeader().removeMouseListener(l);
			l = null;
		}
		for(MouseListener l : getMouseListeners()) {
			removeMouseListener(l);
			l = null;
		}
	}
}
