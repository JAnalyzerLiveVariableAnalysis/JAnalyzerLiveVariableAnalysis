package gui.softwareMeasurement.resultBrowser.chart;

import java.util.List;

public class MeasureDistribution {
	String metricStr;
	List<Double> values;

	public MeasureDistribution(String metricStr, List<Double> values) {
		this.metricStr = metricStr;
		this.values = values;
	}
	
}
