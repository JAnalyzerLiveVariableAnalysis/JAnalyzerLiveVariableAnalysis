package gui.softwareMeasurement.resultBrowser;

import gui.softwareMeasurement.metricBrowser.Item;

public class RecorderItem implements Item {

	private String[] values;
	
	public RecorderItem(String[] values) {
		this.values = values;
	}
	
	@Override
	public String getItemString(int index) {
		return values[index];
	}

}
