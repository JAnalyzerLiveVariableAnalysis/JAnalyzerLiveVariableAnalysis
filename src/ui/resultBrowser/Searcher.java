package ui.resultBrowser;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.TableColumnModel;

import ui.mainFrame.JsMetricFrame;

public class Searcher extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JButton button;
	private JButton cancelButton;
	private JLabel content;
	private JCheckBox checkBox;
	private JTextField textField;
	private Component searchTarget;
	
	
	boolean isFind = false;
	int lastRow = 0;
	int lastCol = -1;
	
	public Searcher(final JTable table) {
		setTitle("搜索");
		setSize(350, 150);
		setLocation(3 * JsMetricFrame.desktopDimension.width / 4 - 175,
				JsMetricFrame.desktopDimension.height / 3 - 75);
		setResizable(false);
		setAlwaysOnTop(true);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 334, 112);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		cancelButton = new JButton();
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		contentPanel.add(cancelButton);

		button = new JButton();
		button.setMnemonic(KeyEvent.VK_ENTER);
		button.setEnabled(false);

		contentPanel.add(button);

		content = new JLabel("内容：");
		contentPanel.add(content);

		textField = new JTextField();
		textField.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				if (textField.getText() == null
						|| textField.getText().equals("")) {
					button.setEnabled(false);
				} else {
					button.setEnabled(true);
				}
			}
		});

		contentPanel.add(textField);
		textField.setColumns(10);

		checkBox = new JCheckBox("区分大小写");
		contentPanel.add(checkBox);

//		addWindowFocusListener(new WindowAdapter() {
//			@Override
//			public void windowLostFocus(WindowEvent e) {
//				// 焦点从搜索窗口转移到主窗口，则设置搜索窗口为always on top
//				if(e.getOppositeWindow().equals(table)) {
//					setAlwaysOnTop(true);
//				} else {// 转移到其他的窗口，则不设置为always on top
//					setAlwaysOnTop(false);
//				}
//			}
//			
//		});

		setSearchViewer(table);
	}

	public void setSearchViewer(final JTable table) {
		if(table != null && !table.equals(searchTarget)) {
			isFind = false;
			lastRow = 0;
			lastCol = -1;
			searchTarget = table;
			removeAllListener();
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					search(table);
					button.requestFocus();
				}
			});
			
			textField.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						search(table);
						textField.requestFocus();
					} else {
						isFind = false;
					}
				}
			});
		}
	}
	
	private void search(final JTable table) {
		if(table != null) {
			table.requestFocusInWindow();
			FocusAdapter adapter = new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					isFind = false;
				}
			};
//			textField.addFocusListener(adapter);
			checkBox.addFocusListener(adapter);

			String contentStr = textField.getText();
			if (contentStr == null || contentStr.trim().equals("")) {
				JOptionPane.showMessageDialog(Searcher.this,
						"请输入要查找的内容！");
			} else {
				int noSuchStrRows = 0;
				table.setColumnSelectionAllowed(true);
				while(!searchRow(table, contentStr)) {
					if(noSuchStrRows == table.getRowCount()) {
						table.clearSelection();
						JOptionPane.showMessageDialog(null, "未搜索到数据！");
						break;
					}
					noSuchStrRows ++;
				}
//				table.setColumnSelectionAllowed(false);
			}
		}
	}

	private void modifySrcollBarLocation(final JTable table) {
		Container c = table.getParent().getParent();
		if(c instanceof JScrollPane) {
			JScrollPane pane = (JScrollPane)c;
			if(pane != null) {
				final JScrollBar hBar = pane.getHorizontalScrollBar();
				final JScrollBar vBar = pane.getVerticalScrollBar();
				
				if(hBar != null) {
					int col = table.getSelectedColumn();
					int colWidth = 0;
					TableColumnModel columnModel = table.getColumnModel();
					for(int i = 0; i < col - 1; i ++) {
						colWidth += columnModel.getColumn(i).getPreferredWidth();
					}
					int extent = hBar.getModel().getExtent();
					if(colWidth > hBar.getMaximum() - extent) {
						colWidth = hBar.getMaximum() - extent;
					}
					final int value = colWidth;
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							hBar.setValue(value);
						}
					});
				}
				
				if(vBar != null) {
					int row = table.getSelectedRow();
					int rowHeight = table.getRowHeight();
					int tmp = row * rowHeight;
					int extent = vBar.getModel().getExtent();
					if(tmp > vBar.getMaximum() - extent) {
						tmp = vBar.getMaximum() - extent;
					}
					final int value = tmp;
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							vBar.setValue(value);
						}
					});
				}
			}
		}
	}
	
	private boolean searchRow(final JTable table, String toSearchStr) {
		int col = table.getColumnCount();
		isFind = false;
		int currentCol = -1;
		// 循环执行完毕之后，说明在本行的所有列中都进行了搜索，
		// 如果搜索到了isFind为true, 没有的话，循环之后,isFind依旧为false;
		for(currentCol = lastCol + 1; currentCol < col; currentCol++) {
			Object o = table.getValueAt(lastRow, currentCol);
			if(o instanceof String) {
				String value = (String)o;
				if(!checkBox.isSelected()) {// 区分大小写
					String lowerCaseValue = value.toLowerCase();
					String lowerCaseToSearch = toSearchStr.toLowerCase();
					if(lowerCaseValue.contains(lowerCaseToSearch)) {
						table.clearSelection();
						table.addRowSelectionInterval(lastRow, lastRow);
						if(currentCol == 0) {
							table.addColumnSelectionInterval(0, table.getColumnCount()-1);
						} else {
							table.addColumnSelectionInterval(currentCol, currentCol);
						}
						lastCol = currentCol;
						isFind = true;
						modifySrcollBarLocation(table);
					}
				} else {
					if(value.contains(toSearchStr)) {
						table.clearSelection();
						table.addRowSelectionInterval(lastRow, lastRow);
						if(currentCol == 0) {
							table.addColumnSelectionInterval(0, table.getColumnCount()-1);
						} else {
							table.addColumnSelectionInterval(currentCol, currentCol);
						}
						lastCol = currentCol;
						isFind = true;
						modifySrcollBarLocation(table);
					}
				}
				if(isFind) break;
			}
		}
		if(currentCol == table.getColumnCount()) { //到了行的最后一列
			lastRow ++;
			if(lastRow == table.getRowCount()) lastRow = 0;
			lastCol = -1;
		}
		return isFind;
	}
	
	private void removeAllListener() {
		ActionListener[] listeners = button.getActionListeners();
		for (ActionListener listener : listeners) {
			button.removeActionListener(listener);
			listener = null;
		}
		KeyListener[] ls = textField.getKeyListeners();
		for (KeyListener l : ls) {
			textField.removeKeyListener(l);
			l = null;
		}
	}
	
	
	public void paint(Graphics g) {
		super.paint(g);
		
		int fontSize = content.getFont().getSize();
		content.setBounds(15, 30,
				fontSize * 3, fontSize + 6);
		textField.setBounds(content.getWidth() + content.getX() + 5, 32, 265,
				21);
		

		checkBox.setBounds(10, 83,
				fontSize * 5 + 50, fontSize + 6);
		
		String searchStr = "查找";
		button.setText(searchStr);
		button.setSize(fontSize * 2 + 45, fontSize + 10);
		button.setLocation(175, checkBox.getY() - (button.getHeight()-checkBox.getHeight())/2);
		
		String cancelStr = "取消";
		cancelButton.setText(cancelStr);
		cancelButton.setBounds(button.getWidth() + button.getX() + 10, button.getY(),
				fontSize * 2 + 45, fontSize + 10);
	}

}
