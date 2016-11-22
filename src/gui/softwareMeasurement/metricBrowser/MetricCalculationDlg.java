package gui.softwareMeasurement.metricBrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import gui.softwareMeasurement.mainFrame.Checker;
import gui.softwareMeasurement.structureBrowser.ProjectTreeManager;
import gui.softwareMeasurement.structureBrowser.ScrollComboBox;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
/**
 * 度量计算配置面板
 * @author Wu zhangsheng
 */
public class MetricCalculationDlg extends JDialog {

	private JPanel contentPane = new JPanel();
	JTextField field;
	JTextField resPathField;
	JComboBox<String> comboBox;
	
	JLabel label;
	JLabel metricLabel;
	JLabel pathLabel;
	JButton metricChoosenBtn;
	
	String formatDate;
	String date;
	
	String metricGranularity="系统";
	ProjectTreeManager manager;
	JTabbedPane srcTabbedPane;
	
	public MetricCalculationDlg(ProjectTreeManager manager,JTabbedPane srcTabbedPane) {
		setModal(true);
		setMinimumSize(new Dimension(670, 300));
		setLocationRelativeTo(null);
		this.manager = manager;
		this.srcTabbedPane = srcTabbedPane;

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = (calendar.get(Calendar.MONTH)+1);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		formatDate = year +"/"+ month +"/"+ day +" "+ hour + ":" + min+ ":"+ second;
		date = year+""+month+""+day+""+hour+""+min+""+second;
		setTitle("度量计算-" + formatDate);

		metricLabel = new JLabel("计算度量：");
		
		field = new JTextField();
		
		label = new JLabel("度量粒度：");
		
		
		contentPane.add(label);
		contentPane.add(metricLabel);
		contentPane.add(field);
		
		metricChoosenBtn = new JButton("选择");
		metricChoosenBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				MetricDescriptionDlg frame = new MetricDescriptionDlg(field);
				frame.setVisible(true);
			}
		});
		
		contentPane.add(metricChoosenBtn);
		
		
		this.setLayout(new BorderLayout());
		this.add(contentPane, BorderLayout.CENTER);
		contentPane.setLayout(null);
		comboBox = new ScrollComboBox<String>();
		comboBox.addItem("系统");
		comboBox.addItem("版本");
		comboBox.addItem("包");
		comboBox.addItem("类");
		
		contentPane.add(comboBox);
		
		
		pathLabel = new JLabel("结果文件路径：");
		resPathField = new JTextField();
		resPathField.setEditable(false);
		resPathField.setText(Checker.getResultDir().getAbsolutePath()+"\\"+"result"+date+".jsm");
		resPathField.setCaretPosition(0);
		
		
		contentPane.add(pathLabel);
		contentPane.add(resPathField);
		
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("下一步");
		okButton.setActionCommand("OK");
		JButton cancelButton = new JButton("取消");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);
		
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if(command.equals("OK")) {
					if(isLegalInput()) {
						if(metricGranularity.equals("系统")) {
							EntitySelectionDlg dlg = new EntitySelectionDlg(MetricCalculationDlg.this, null);
							MetricCalculationDlg.this.setVisible(false);
							dlg.setVisible(true);
						} else {
							RangeDecisionDlg f = new RangeDecisionDlg(MetricCalculationDlg.this);
							MetricCalculationDlg.this.setVisible(false);
							f.setVisible(true);
						}
					}
				}
				if(command.equals("Cancel")) {
					dispose();
				}
			}
		};
		okButton.addActionListener(buttonListener);
		cancelButton.addActionListener(buttonListener);
		
		comboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					metricGranularity = (String)e.getItem();
				}
			}
		});
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		int fontSize = getFont().getSize();
		metricLabel.setBounds(50, 27, fontSize * 7, fontSize + 6);
		
		field.setBounds(metricLabel.getX() + metricLabel.getWidth(), 25, 
				contentPane.getWidth() - 2*metricLabel.getX() - metricLabel.getWidth() - 45, fontSize + 8);
		metricChoosenBtn.setBounds(field.getX() + field.getWidth() + 5, 25, 
				fontSize * 7, field.getHeight());
		
		label.setBounds(metricLabel.getX(), metricLabel.getY() + metricLabel.getHeight()+50,
				fontSize * 7, fontSize + 6);
		comboBox.setBounds(label.getX() + label.getWidth(), label.getY() - 2, 
				field.getWidth(), field.getHeight());
		
		pathLabel.setBounds(label.getX(),  label.getY() + label.getHeight() + 30,
				fontSize * 15, fontSize + 8);
		resPathField.setBounds(pathLabel.getX(), pathLabel.getY() + pathLabel.getHeight() + 5,
				field.getX() + field.getWidth() - pathLabel.getX(), fontSize + 8);
		
	}
	
	private boolean isLegalInput() {
		String[] metrics = field.getText().split(";");
		for(String metricId : metrics){
			if(!SoftwareMeasureIdentifier.hasMeasure(metricId)) {
				JOptionPane.showMessageDialog(null, "要计算的度量输入格式有误，请重新输入！");
				return false;
			}
		}
		return true;
	}
//	
//	public static void main(String args[]) {
//		MetricCalculationDlg frame = new MetricCalculationDlg();
//		frame.setVisible(true);
//	}
}

