package gui.softwareMeasurement.resultBrowser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableCellRenderer;

public class ResultTableCellRenderer extends DefaultTableCellRenderer implements MouseInputListener {

	JTable table;
	int row = -1;
	int col = -1;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		this.table = table;
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		this.setForeground(Color.black);
		// 如果当前需要渲染器的单元格就是鼠标事件所在的单元格
		if (row == this.row && column == this.col) {
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

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
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

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
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


}
