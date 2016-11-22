package gui.softwareMeasurement.metricBrowser;

public class MetricItem implements Item {

	private String identifier;
	private String description;
	private String type;

	public MetricItem(String identifier, String type, String description) {
		this.identifier = identifier;
		this.type = type;
		this.description = description;
	}
	
	@Override
	public String getItemString(int index) {
		switch(index) {
		case 0:
			return identifier;
		case 1:
			return type;
		case 2:
			return description;
		}
		return "";
	}

}
