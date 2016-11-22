package gui.softwareMeasurement.codeBrowser;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import gui.softwareMeasurement.mainFrame.JsMetricFrame;

public class SearchDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JButton button;
	private JButton cancelButton;
	private JLabel content;
	private JCheckBox checkBox;
	private JTextField textField;
	
	/*搜索时的相关辅助标志*/
	int offset = 0;
	int lastSearchStrLength = 0;
	int preLength = 0;
	boolean isFind = false;

	public SearchDialog(final Component viewer) {
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

		setVisible(true);
		
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				// 焦点从搜索窗口转移到主窗口，则设置搜索窗口为always on top
				if(e.getOppositeWindow() instanceof JsMetricFrame) {
					setAlwaysOnTop(true);
				} else {// 转移到其他的窗口，则不设置为always on top
					setAlwaysOnTop(false);
				}
			}
			
		});
		
		setSearchViewer(viewer);
	}

	public void setSearchViewer(final Component textViewer) {
		removeAllListener();
		offset = 0;
		lastSearchStrLength = 0;
		preLength = 0;
		isFind = false;
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				search(textViewer);
				button.requestFocus();
			}
		});
		
		textField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					search(textViewer);
					textField.requestFocus();
				} else {
					isFind = false;
					offset = 0;
					preLength = 0;
					lastSearchStrLength = 0;
				}
			}
		});
	}
	
	private void search(final Component textViewer) {
		if(textViewer != null) {
			FocusAdapter adapter = new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					isFind = false;
					offset = 0;
					preLength = 0;
					lastSearchStrLength = 0;
				}
			};
			checkBox.addFocusListener(adapter);

			String contentStr = textField.getText();
			if (contentStr == null || contentStr.trim().equals("")) {
				JOptionPane.showMessageDialog(SearchDialog.this,
						"请输入要查找的内容！");
			} else {
				if(textViewer instanceof JSrcViewer) {
					JSrcViewer viewer = (JSrcViewer) textViewer;
					offset = viewer.setSelectedString(offset, contentStr,
							checkBox.isSelected());
					if (offset != -1) {// 找到了内容
						isFind = true;
					}
					if (!isFind && offset == -1) {// 这段内容未找到则弹出对话框
						viewer.setCaretPosition(0);
						JOptionPane.showMessageDialog(SearchDialog.this,
								"未找到要查找的内容！");
					}
					if (isFind && offset == -1) {// 找到过一次内容之后直到最后都没有找到的话则不弹出对话框
						offset = viewer.setSelectedString(0, contentStr,
								checkBox.isSelected());
					}
				} else {
					JTextArea viewer = (JTextArea) textViewer;
					String fileContents = viewer.getText().substring(preLength);
					if(checkBox.isSelected()) {
						offset = fileContents.indexOf(contentStr);
					} else {
						String lowerFileContents = fileContents.toLowerCase();
						String lowerContentStr = contentStr.toLowerCase();
						offset = lowerFileContents.indexOf(lowerContentStr);
					}
					
					if (offset != -1) {// 找到了内容
						isFind = true;
					}
					if (!isFind && offset == -1) {// 这段内容未找到则弹出对话框
						viewer.setCaretPosition(0);
						JOptionPane.showMessageDialog(SearchDialog.this,
								"未找到要查找的内容！");
					}
					if (isFind && offset != -1) {// 找到过一次内容之后直到最后都没有找到的话则不弹出对话框
						viewer.setSelectionStart(preLength + offset);
						viewer.setSelectionEnd(preLength + offset + contentStr.length());
						viewer.requestFocus();
					}
					lastSearchStrLength = contentStr.length();
					preLength += offset + lastSearchStrLength;

					if(isFind && offset == -1) {
						// 找到了文本的最后，所有标志恢复
						isFind = false;
						offset = 0;
						preLength = 0;
						lastSearchStrLength = 0;
					}
				}
			}
		} 
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
