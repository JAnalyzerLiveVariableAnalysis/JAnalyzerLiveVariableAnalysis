package ui.metricBrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;

/**
 * 展示所有支持的度量，并选择需要计算的部分
 * @author Wu zhangsheng
 */
public class MetricDescriptionDlg extends JDialog{
	
	String selectedMetrics;

	public MetricDescriptionDlg(final JTextField field) {
		setModal(true);
		setTitle("支持的度量");
		setLayout(new BorderLayout());
		setSize(500, 400);
		setLocationRelativeTo(null);
		String[] columnNames = {"","metric","metric type","description"};
		ArrayList<Item> items = new ArrayList<Item>();
		
		List<SoftwareMeasure> avaMetric = SoftwareMeasureIdentifier.getAvailableMeasures();

		int count = 0;
		String type = "size";
		for(SoftwareMeasure metric : avaMetric){
			String description =  SoftwareMeasureIdentifier.getDescription(metric);
			if(count == 10) {
				type = "cohesion";
			}
			if(count == 34) {
				type = "coupling";
			}
			if(count == 52) {
				type = "inheritance";
			}
			
			MetricItem item = new MetricItem(metric.getIdentifier(), type, description);
			items.add(item);
			count++;
		}
		final ItemChoosenPane choosenPane = new ItemChoosenPane(items, columnNames, false);
		add(choosenPane, BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.setActionCommand("OK");
		JButton cancelButton = new JButton("取消");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);
		add(buttonPane, BorderLayout.SOUTH);
		
		ActionListener acListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if(command.equals("OK")) {
					List<Item> selectedItems = choosenPane.getSelectedItems();
					String selectedMetrics = "";
					for(Item i : selectedItems) {
						selectedMetrics += i.getItemString(0) + ";";
					}
					if(field != null) {
						field.setText(selectedMetrics);
					} else {
						MetricDescriptionDlg.this.selectedMetrics = selectedMetrics;
					}
				}
				dispose();
			}
		};
			
		okButton.addActionListener(acListener);
		cancelButton.addActionListener(acListener);
	}
	
	public String getSelectedMetrics() {
		if(selectedMetrics.trim().equals("")) return null;
		return selectedMetrics;
	}
	
}
