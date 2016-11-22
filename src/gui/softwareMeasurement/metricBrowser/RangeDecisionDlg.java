package gui.softwareMeasurement.metricBrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RangeDecisionDlg extends JDialog {
	
	JPanel contentPane = new JPanel();
	
	JLabel label;
	JComboBox<String> rangeBox;
	MetricCalculationDlg mDlg;
	private ItemChoosenPane choosenPane;

	public RangeDecisionDlg(JDialog dlg) {
		mDlg = (MetricCalculationDlg)dlg;
		
		init();
		
		this.add(contentPane, BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.add(buttonPane, BorderLayout.SOUTH);
		
		JButton lastStepBtn = new JButton("上一步");
		lastStepBtn.setActionCommand("Last Step");
		JButton okButton = new JButton("下一步");
		okButton.setActionCommand("OK");
		JButton cancelButton = new JButton("取消");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(lastStepBtn);
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);
		
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if(command.equals("OK")) {
					List nodes = getSelectedNodes();
					if(nodes == null || nodes.isEmpty()) {
						JOptionPane.showMessageDialog(null, "请确定被度量的实体所处范围！");
					} else {
						EntitySelectionDlg dlg = new EntitySelectionDlg(mDlg, RangeDecisionDlg.this);
						setVisible(false);
						dlg.setVisible(true);
					}
				}
				if(command.equals("Cancel")) {
					dispose();
					mDlg.dispose();
				}
				if(command.equals("Last Step")) {
					RangeDecisionDlg.this.dispose();
					mDlg.setVisible(true);
				}
			}
		};
		okButton.addActionListener(buttonListener);
		cancelButton.addActionListener(buttonListener);
		lastStepBtn.addActionListener(buttonListener);
		
		add(buttonPane, BorderLayout.SOUTH);
	}

	private void init() {
		setModal(true);
		
		setTitle("被度量实体所处范围");
		setMinimumSize(new Dimension(700,400));
		setLocationRelativeTo(null);
		
		contentPane.setLayout(null);
		label = new JLabel("度量范围：");
		rangeBox = new JComboBox<String>();
		rangeBox.addItem("系统");
		
		List<Item> projectNodes = mDlg.manager.getAllProjectNodes();
		String[] columnNames = {"", "系统","路径"};
		choosenPane = new ItemChoosenPane(projectNodes, columnNames, true);
		
		contentPane.add(choosenPane);
		if(mDlg.metricGranularity.equals("包")) {
			rangeBox.addItem("版本");
		}
		if(mDlg.metricGranularity.equals("类")) {
			rangeBox.addItem("版本");
			rangeBox.addItem("包");
		}
		
		rangeBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					contentPane.remove(choosenPane);
					contentPane.validate();
					if(e.getItem().equals("系统")) {
						List<Item> projectNodes = mDlg.manager.getAllProjectNodes();
						String[] columnNames = {"", "系统","路径"};
						choosenPane = new ItemChoosenPane(projectNodes, columnNames, true);
						contentPane.add(choosenPane);
						repaint();
					}
					if(e.getItem().equals("版本")) {
						List<Item> projectNodes = mDlg.manager.getAllVersionNodes();
						String[] columnNames = {"", "版本","所属系统", "路径"};
						choosenPane = new ItemChoosenPane(projectNodes, columnNames, true);
						contentPane.add(choosenPane);
						repaint();
					}
					if(e.getItem().equals("包")) {
						List<Item> projectNodes = mDlg.manager.getAllPackageNodes();
						String[] columnNames = {"", "包", "所属版本","所属系统"};
						choosenPane = new ItemChoosenPane(projectNodes, columnNames, true);
						contentPane.add(choosenPane);
						repaint();
					}
				}
				
			}
		});
		
		
		contentPane.add(label);
		contentPane.add(rangeBox);
		
		contentPane.add(choosenPane);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		
		int contentPaneWidth = contentPane.getWidth();
		int fontSize = getFont().getSize();
		label.setBounds(50, 30, fontSize * 7, fontSize + 6);
		rangeBox.setBounds(label.getX() + label.getWidth(), label.getY() - 1, 
				contentPaneWidth - 2*label.getX() - label.getWidth(), fontSize + 8);
		choosenPane.setBounds(label.getX(), label.getY() + label.getHeight() + 20,
				contentPaneWidth - 2*label.getX(), contentPane.getHeight()-70);
	}
	
	public List getSelectedNodes() {
		return choosenPane.getSelectedItems();
	}
}
