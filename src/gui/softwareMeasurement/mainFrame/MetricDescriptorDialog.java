package gui.softwareMeasurement.mainFrame;

import java.awt.FontMetrics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gui.softwareMeasurement.structureBrowser.ScrollComboBox;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;

public class MetricDescriptorDialog extends JDialog {


	public MetricDescriptorDialog() {
		setModal(true);
		setTitle("度量信息");
		setResizable(false);
		setSize(350, 330);
		setLocationRelativeTo(null);
		JLabel label = new JLabel("度量类型：");
		int fontSize = label.getFont().getSize();
		label.setBounds(50, 30, fontSize * 5, fontSize + 2);
		getContentPane().setLayout(null);
		getContentPane().add(label);
		Vector<String> v = new Vector<String>();
		v.addElement("规模");
		v.addElement("内聚");
		v.addElement("耦合");
		v.addElement("继承");
		JComboBox<String> metricTypeBox = new ScrollComboBox<String>(v);
		metricTypeBox.setSize(fontSize * 15, fontSize + 8);
		metricTypeBox.setLocation(label.getX() + 10 + label.getWidth(), 
				label.getY() - (metricTypeBox.getHeight()-label.getHeight())/2);
		getContentPane().add(metricTypeBox);
		
		final JList<String> list = new JList<String>();
		DefaultListModel<String> model = new DefaultListModel<String>();
		model.addElement("FILE");
		model.addElement("PKG");
		model.addElement("CLS");
		model.addElement("INTF");
		model.addElement("ENUM");
		model.addElement("FLD");
		model.addElement("MTHD");
		model.addElement("PARS");
		model.addElement("LOCV");
		
		list.setModel(model);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBounds(label.getX(), label.getY() + label.getHeight() + 20, 
				label.getWidth() + metricTypeBox.getWidth() + 10, 100);
		list.setSelectedIndex(0);
		getContentPane().add(scrollPane);
		
		
		JLabel info = new JLabel("描述：");
		info.setBounds(scrollPane.getX(), scrollPane.getY()+scrollPane.getHeight() + 10,
				fontSize * 3, fontSize + 2);
		getContentPane().add(info);
		
		final JTextArea desArea = new JTextArea();
		desArea.setEditable(false);
		
		JScrollPane pane = new JScrollPane(desArea);
		pane.setBounds(label.getX(), info.getY() + 20,
				scrollPane.getWidth(), 80);
		getContentPane().add(pane);
		String identifier = SoftwareMeasureIdentifier.getDescriptionOfMeasure(SoftwareMeasureIdentifier.FILE);
		desArea.setSize(248, 80);
		showDescription(identifier, desArea);		
		metricTypeBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
					String content = (String)e.getItem();
					model.removeAllElements();
					if(content.equals("规模")) {
						model.addElement("FILE");
						model.addElement("PKG");
						model.addElement("CLS");
						model.addElement("INTF");
						model.addElement("ENUM");
						model.addElement("FLD");
						model.addElement("MTHD");
						model.addElement("PARS");
						model.addElement("LOCV");
					}
					if(content.equals("内聚")) {
						model.addElement("LCOM1");
						model.addElement("LCOM2");
						model.addElement("LCOM3");
						model.addElement("LCOM4");
						model.addElement("LCOM5");
						model.addElement("Co");
						model.addElement("CoPrim");
						model.addElement("ICH");
						model.addElement("Coh");
						model.addElement("TCC");
						model.addElement("LCC");
						model.addElement("OCC");
						model.addElement("PCC");
						model.addElement("CC");
						model.addElement("DCD");
						model.addElement("DCI");
						model.addElement("CC");
						model.addElement("SCOM");
						model.addElement("LSCC");
						model.addElement("CAMC");
						model.addElement("CBMC");
						model.addElement("ICBMC");
						model.addElement("NHD");
						model.addElement("SNHD");
						model.addElement("SCC");
					}
					if(content.equals("耦合")) {
						model.removeAllElements();
						model.addElement("CBO");
						model.addElement("RFC");
						model.addElement("RFCp");
						model.addElement("MPC");
						model.addElement("DAC");
						model.addElement("DACp");
						model.addElement("ICP");
						model.addElement("IHICP");
						model.addElement("ACAIC");
						model.addElement("OCAIC");
						model.addElement("DCAEC");
						model.addElement("OCAEC");
						model.addElement("ACMIC");
						model.addElement("OCMIC");
						model.addElement("DCMEC");
						model.addElement("OCMEC");
						model.addElement("AMMIC");
						model.addElement("OMMIC");
						model.addElement("DMMEC");
						model.addElement("OMMEC");
					}
					if(content.equals("继承")) {
						model.addElement("DIT");
						model.addElement("NOC");
						model.addElement("AID");
						model.addElement("CLD");
						model.addElement("DPA");
						model.addElement("DPD");
						model.addElement("DP");
						model.addElement("SPA");
						model.addElement("SPD");
						model.addElement("SP");
						model.addElement("NOA");
						model.addElement("NOP");
						model.addElement("NOD");
						model.addElement("SIX");
					}
					list.setSelectedIndex(0);
				}
			}
		});
		
		
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList list = (JList)e.getSource();
				String m = (String)list.getSelectedValue();
				if(m != null) {
					showDescription(m, desArea);
				}
			}

		});
	}
	
	private void showDescription(String identifier, JTextArea desArea) {
		String description = SoftwareMeasureIdentifier.getDescriptionOfMeasure(identifier);
		String output = "";
		String[] words = description.split(" ");
		FontMetrics fm = desArea.getFontMetrics(desArea.getFont());
		int stringWidth = 0;
		for(String word : words) {
			int currentWordWidth = fm.stringWidth(word + " ");
			stringWidth += currentWordWidth;
			if(stringWidth > desArea.getWidth()) {
				output += "\r\n";
				stringWidth = currentWordWidth;
			}
			output += word + " ";
		}
		desArea.setText(output);
	}
	
	
//	public static void main(String args[]) {
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		new MetricDescriptorDialog().setVisible(true);
//	}
}
