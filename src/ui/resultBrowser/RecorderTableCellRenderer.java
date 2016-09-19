package ui.resultBrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.BitSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class RecorderTableCellRenderer extends DefaultTableCellRenderer implements
		MouseInputListener {

	JTabbedPane tabbedPane;
	// 鼠标事件所在的行
	private int row = -1;
	// 鼠标事件所在的列
	private int col = -1;
	// 当前监听的Table
	private JTable table = null;
	
	
	public RecorderTableCellRenderer(JTabbedPane pane) {
		tabbedPane = pane;
	}
	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// 恢复默认状态
		this.table = table;
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		this.setForeground(Color.black);
		table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.setText((String)value);
		// 如果当前需要渲染器的单元格就是鼠标事件所在的单元格
		if (row == this.row && column == this.col) {
			// 第四列是显示超链接的列
			if (column == 5) {
				// 改变前景色(文字颜色)
				this.setForeground(Color.blue);
				// 改变鼠标形状
				table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//				this.setText("<html><u>" + value + "</u></html>");
			}
			setBackground(table.getSelectionBackground());
		} else if (isSelected) {
			// 如果单元格被选中,则改变前景色和背景色
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			// 其他情况下恢复默认背景色
			setBackground(Color.white);
		}
		return this;
	}

	/**
	 * 鼠标移出事件
	 * 
	 * @param e
	 */
	public void mouseExited(MouseEvent e) {
		if (table != null) {
			int oldRow = row;
			int oldCol = col;
			// 鼠标移出目标表格后,恢复行列数据到默认值
			row = -1;
			col = -1;
			// 当之前的行列数据有效时重画相关区域
			if (oldRow != -1 && oldCol != -1) {
				Rectangle rect = table.getCellRect(oldRow, oldCol, false);
				table.repaint(rect);
			}
		}
	}

	/**
	 * 鼠标拖动事件
	 * 
	 * @param e
	 */
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	/**
	 * 鼠标移动事件
	 * 
	 * @param e
	 */
	public void mouseMoved(MouseEvent e) {
		if (table != null) {
			Point p = e.getPoint();
			int oldRow = row;
			int oldCol = col;
			row = table.rowAtPoint(p);
			col = table.columnAtPoint(p);
			// 重画原来的区域
			if (oldRow != -1 && oldCol != -1) {
				Rectangle rect = table.getCellRect(oldRow, oldCol, false);
				table.repaint(rect);
			}
			// 重画新的区域
			if (row != -1 && col != -1) {
				Rectangle rect = table.getCellRect(row, col, false);
				table.repaint(rect);
			}
		}
	}

	/**
	 * 鼠标单击事件
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 1) {
			// 获取事件所在的行列坐标信息
			Point p = e.getPoint();
			int c = table.columnAtPoint(p);
			if (c != 5) {// 第5列是结果文件绝对路径
				return;// 单击时如果不是单击第5列就不做处理
			}
			int r = table.rowAtPoint(p);
			showSelectedRowResultFile(r);
		} else if(e.getClickCount() == 2){// 双击时无论是哪一列都打开该行对应的结果文件
			Point p = e.getPoint();
			int r = table.rowAtPoint(p);
			showSelectedRowResultFile(r);
		}
	}

	private void showSelectedRowResultFile(int row) {
		String filePath = (String)table.getValueAt(row, 5);
		File file = new File(filePath);
		if(!file.exists()) {
			JOptionPane.showMessageDialog(null, "文件" + filePath + "不存在！");
		}
		
		if(file.isFile()) {
			ResultFileParser parser = new ResultFileParser(file);
			ResultTable table = new ResultTable(parser);
			table.setTextArea(tabbedPane);
			
			JFrame frame = new ResultTableFrame(table);
			frame.setTitle(file.getName());
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
		}
	}
	
	/**
	 * 鼠标按下事件
	 * 
	 * @param e
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * 鼠标释放事件
	 * 
	 * @param e
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * 鼠标进入事件
	 * 
	 * @param e
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * 测试方法
	 * 
	 * @param args
	 */
//	public static void main(String[] args) {
//		
//		File file = new File("result/record.txt");
//		// 构建表格数据模型
//		TableModel model = new RecordParser(file);
//		// 创建表格对象
//		JTable table = new JTable(model);
//		// 创建单元格渲染器暨鼠标事件监听器
//		RecorderTableCellRenderer renderer = new RecorderTableCellRenderer(null);
//		// 注入渲染器
//		table.setDefaultRenderer(Object.class, renderer);
//		// 注入监听器
//		table.addMouseListener(renderer);
//		table.addMouseMotionListener(renderer);
//
//		// 为表格增加爱滚动窗格
//		JScrollPane sp = new JScrollPane(table);
//		// 创建窗口程序
//		JFrame f = new JFrame("JTable 单元格超链接测试");
//		f.getContentPane().add(sp, BorderLayout.CENTER);
//		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		f.setSize(800, 600);
//		f.setLocationRelativeTo(null);
//		// 显示窗口
//		f.setVisible(true);
//	}
}