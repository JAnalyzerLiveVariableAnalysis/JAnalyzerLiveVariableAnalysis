package gui.softwareMeasurement.resultBrowser.chart;

import java.util.ArrayList;
import java.util.List;

public class ClassMeasureData {

	public String className;
	public List<MeasurementValue> measures;
	
	public ClassMeasureData(String name) {
		className = name;
		measures = new ArrayList<MeasurementValue>();
	}
	
	public String getClassName() {
		return className;
	}
	
	public void addMeasure(MeasurementValue m) {
		measures.add(m);
	}
	
	public List<MeasurementValue> getMeasures() {
		return measures;
	}
	
}

