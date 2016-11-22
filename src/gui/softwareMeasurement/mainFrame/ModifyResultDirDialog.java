package gui.softwareMeasurement.mainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ModifyResultDirDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JLabel lblNewLabel;
	private JButton btnNewButton;
	
	public ModifyResultDirDialog() {
		setTitle("结果文件存放目录");
		setSize(400, 222);
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		lblNewLabel = new JLabel("当前路径：");
		contentPanel.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setText(Checker.getResultDir().getAbsolutePath());
		contentPanel.add(textField);
		
		btnNewButton = new JButton("...");
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY
						| JFileChooser.OPEN_DIALOG);
				int res = chooser.showOpenDialog(null);
				if (res == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					textField.setText(file.getAbsolutePath());
				}
			}
		});
		contentPanel.add(btnNewButton);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		ActionListener oclistener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if(command.equals("OK")) {
					String text = textField.getText();
					if(text == null || text.trim().equals("")) {
						JOptionPane.showConfirmDialog(ModifyResultDirDialog.this,
								"目录不能为空，请重新输入！", "提示", JOptionPane.YES_OPTION);
					}
					File dir = new File(text);
					if(!dir.isDirectory()) {
						JOptionPane.showConfirmDialog(ModifyResultDirDialog.this,
								"请选择一个文件夹！", "提示", JOptionPane.YES_OPTION);
					} else {
						File file = new File("res/.setting");
						SAXReader reader = new SAXReader();
						FileInputStream fis;
						try {
							fis = new FileInputStream(file);
							Document document = reader.read(fis);
							Element root = document.getRootElement();
							Iterator it = root.elementIterator();
							while (it.hasNext()) {
								Element ele = (Element) it.next();
								if(ele.getName().equals("resultpath")) {
									Attribute attribute = ele.attribute("value");
									attribute.setValue(textField.getText());// 修改属性节点的值
									break;
								}
							}
							OutputFormat format = OutputFormat.createPrettyPrint();
							format.setEncoding("utf8");
							FileWriter writer;
							writer = new FileWriter(file);
							XMLWriter xmlWriter = new XMLWriter(writer, format);
							xmlWriter.write(document);
							xmlWriter.flush();
							xmlWriter.close();
							fis.close();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						dispose();
					}
				}
				if(command.equals("Cancel")) {
					dispose();
				}
			}
		};
		
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		okButton.addActionListener(oclistener);
		cancelButton.addActionListener(oclistener);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		int fontSize = lblNewLabel.getFont().getSize();
		int charWidth = fontSize * 5;
		int charHeight = fontSize + 2;
		lblNewLabel.setBounds(20, contentPanel.getHeight()/2, charWidth, charHeight);
		textField.setBounds(lblNewLabel.getX() + lblNewLabel.getWidth() + 5,
				lblNewLabel.getY(), 230, charHeight + 3);
		btnNewButton.setBounds(textField.getX() + textField.getWidth() + 8,
				textField.getY() - 1, fontSize*3, charHeight + 5);
	}
}
