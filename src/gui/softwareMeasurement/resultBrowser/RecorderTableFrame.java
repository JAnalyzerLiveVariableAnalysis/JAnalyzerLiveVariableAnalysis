package gui.softwareMeasurement.resultBrowser;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import gui.softwareMeasurement.metricBrowser.Item;
import gui.softwareMeasurement.metricBrowser.ItemChoosenPane;

public class RecorderTableFrame extends JFrame {

	static Searcher searcher;
	static int tableCount;
	ItemChoosenPane choosenPane;
	
	public RecorderTableFrame(final JTabbedPane srcTabbedPane) {
		File file = new File("result/record.txt");
		
		String[] columnNames = {"", "度量时间", "被度量的实体", "粒度", "计算的度量", "结果文件路径"};
		
		RecordParser parser = new RecordParser(file);
		choosenPane = new ItemChoosenPane(parser.getRecorderItems(),
				columnNames, false);
		
		final JTable table = choosenPane.getTable();
		
		RecorderTableCellRenderer renderer = new RecorderTableCellRenderer(srcTabbedPane);
		// 注入渲染器
		table.setDefaultRenderer(Object.class, renderer);
		// 注入监听器
		table.addMouseListener(renderer);
		table.addMouseMotionListener(renderer);
		tableCount ++;
		
		setTitle("记录");
		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);
		JMenu mnFile = new JMenu("文件");
		final JMenuItem mntmOpenFile = new JMenuItem("查看结果文件");
		final JMenuItem mntmDelFile = new JMenuItem("删除结果文件");
		final JMenuItem mntmSearch = new JMenuItem("搜索单元格");
		mntmOpenFile.setEnabled(false);
		mntmDelFile.setEnabled(false);
		
		
		addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				Window window = e.getOppositeWindow();
				if(window != null && window.equals(searcher)) {
					if(searcher != null) {
						searcher.setSearchViewer(table);
					}
				}
			}
		});
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				tableCount--;
				if(tableCount == 0 && searcher != null) {
					searcher.setVisible(false);
				}
			}
		});
		
		
		ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if(command.equals("查看结果文件")) {
					List<Item> items = choosenPane.getSelectedItems();
					for (Item i : items) {
						String filePath = i.getItemString(4);
						File file = new File(filePath);
						if(!file.exists()) {
							JOptionPane.showMessageDialog(null, "文件"
									+file.getAbsolutePath() + "不存在!");
						}
	
						if(file.isFile()) { 
							ResultFileParser parser = new ResultFileParser(file);
							ResultTable table = new ResultTable(parser);
							table.setTextArea(srcTabbedPane);
		
							JFrame frame = new ResultTableFrame(table);
							frame.setTitle(file.getName());
							frame.setLocationRelativeTo(null);
							frame.setVisible(true);
						}
					}
				}
				if(command.equals("删除结果文件")) {
					int[] selectedRows = choosenPane.getSelectedRows();
					for(int i = selectedRows.length - 1; i >= 0; i--) {
						String filePath = (String)table.getValueAt(i, 5);
						File f = new File(filePath);
						f.delete();
						choosenPane.removeRow(selectedRows[i]);
					}
					try {
						saveTable();
					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(null, "文件读取错误，信息未保存！");
						e1.printStackTrace();
					}
				}
				if(command.equals("搜索单元格")) {
					if(searcher == null) {
						searcher = new Searcher(table);
					}
					searcher.setVisible(true);
				}
			}

			private void saveTable() throws FileNotFoundException {
				File file = new File("result/record.txt");
				PrintWriter pw = new PrintWriter(new FileOutputStream(file));
				for(int row = 0; row < table.getRowCount(); row ++) {
					for(int col = 1; col < table.getColumnCount() - 1; col ++) {
						String content = (String)table.getValueAt(row, col);
						pw.write(content+"\t");
					}
					int lastCol = table.getColumnCount()-1;
					pw.write(table.getValueAt(row, lastCol) + "\r\n");
				}
				pw.flush();
				pw.close();
			}
		};
		
		mntmOpenFile.addActionListener(listener);
		mntmDelFile.addActionListener(listener);
		mntmSearch.addActionListener(listener);
		
		bar.add(mnFile);
		mnFile.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (choosenPane.hasSelectedItems()) {
					mntmOpenFile.setEnabled(true);
					mntmDelFile.setEnabled(true);
				} else {
					mntmOpenFile.setEnabled(false);
					mntmDelFile.setEnabled(false);
				}
			}
		});
		mnFile.add(mntmOpenFile);
		mnFile.add(mntmDelFile);
		mnFile.add(mntmSearch);
		
		add(choosenPane);
		
		// 根据表格的内容计算面板大小
		int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		
		int choosenWidth =  choosenPane.getPaneMinWidth();
		int rowCount = choosenPane.getRowCount();
		int rowHeight = choosenPane.getRowHeight();
		
		int choosenHeight = (rowCount+2) * rowHeight;
		if(choosenHeight > 300) {
			choosenHeight = 300;
		}
		if(screenWidth > choosenWidth) {
			this.setSize(choosenWidth, 100 + choosenHeight);
		} else {
			this.setSize(choosenWidth/4 * 3, 100 + choosenHeight);
		}
		setLocationRelativeTo(null);
	}

}
