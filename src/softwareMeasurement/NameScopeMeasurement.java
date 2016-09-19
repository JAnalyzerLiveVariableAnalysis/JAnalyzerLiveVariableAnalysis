package softwareMeasurement;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.metric.SoftwareStructMetric;
import softwareMeasurement.metric.SoftwareStructMetricFactory;
import softwareStructure.SoftwareStructManager;
import util.Debug;
import nameTable.nameScope.NameScope;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ2ÈÕ
 * @version 1.0
 */
public abstract class NameScopeMeasurement {
	// The class to be measured 
	protected NameScope scope = null;		
	// The list of measure results for buffering the results
//	protected List<SoftwareMeasure> measureList = null;
	// The software structure manager used to calculate the measures
	protected SoftwareStructManager structManager = null;
	
	public NameScopeMeasurement(NameScope scope, SoftwareStructManager manager) {
		this.scope = scope;
		this.structManager = manager;
	}

	public NameScope getScope() {
		return scope;
	}
	
	public SoftwareStructManager getSoftwareStructManager() {
		return structManager;
	}
	
	/**
	 * Calculate the given measure, set the value, and return the measure
	 */
	public SoftwareMeasure getMeasure(SoftwareMeasure measure) {
		// First, search the measure in the buffered list
//		if (measureList == null) measureList = new ArrayList<SoftwareMeasure>();
//		for (SoftwareMeasure calculatedMeasure : measureList) {
//			if (calculatedMeasure.getIdentifier().equals(measure.getIdentifier())) {
//				measure.setValue(calculatedMeasure.getValue());
//				measure.setUsable();
//				return measure;
//			}
//		}
		
		// Calculate the measure for the current class.
		// At first, we get an appropriate metric for calculating the measure
		SoftwareStructMetric metric = SoftwareStructMetricFactory.getMetricInstance(measure);
		if (metric == null) return measure;
		
		metric.setMeasuringObject(scope);
		metric.setSoftwareStructManager(structManager);
//		Debug.time("\tBegin to calculate measure " + measure.getIdentifier());
		if (metric.calculate(measure) == true) {
			// Create a new measure to store the result, and buffer it to the list
			SoftwareMeasure bufferMeasure = new SoftwareMeasure(measure);
//			measureList.add(bufferMeasure);
		}
//		Debug.time("\tEnd to calculate measure " + measure.getIdentifier());
		return measure;
	}
	
	/**
	 * Calculate the given measure, set the value, and return the measure. This method uses String to give measure  
	 */
	public SoftwareMeasure getMeasureByIdentifier(String measureIdentifier) {
		SoftwareMeasure measure = new SoftwareMeasure(measureIdentifier);
		return getMeasure(measure);
	}
	
	/**
	 * Calculate the given measures, set the values, and return the measures
	 */
	public List<SoftwareMeasure> getMeasureList(List<SoftwareMeasure> measures) {
		for (SoftwareMeasure measure : measures) getMeasure(measure);
		return measures;
	}

	/**
	 * Calculate the given measures, set the values, and return the measures
	 */
	public List<SoftwareMeasure> getMeasureListByIdentifiers(List<String> measureIdentifiers) {
		List<SoftwareMeasure> resultList = new ArrayList<SoftwareMeasure>();
		for (String measureIdentifier : measureIdentifiers) resultList.add(getMeasureByIdentifier(measureIdentifier));
		return resultList;
	}
	
	/**
	 * Print the measures to one or two rows of table. If the boolean parameter printId is false, only print the value,
	 * otherwise, print the measure identifiers in a row, and the value in another row. Anyway, the class name will be
	 * in the first column of the value row. The table symbol is used to as the splitter of the columns. 
	 */
	public void printToRow(PrintWriter writer, boolean printId) {
//		if (printId == true) {
//			writer.print("Measure Id\t");
//			for (SoftwareMeasure measure : measureList) {
//				writer.print(measure.getIdentifier() + "\t");
//			}
//			writer.println();
//		}
//		writer.print(scope.getScopeName() + "\t");
//		for (SoftwareMeasure measure : measureList) {
//			if (measure.isUsable()) writer.print(measure.getValue() + "\t");
//			else writer.print("N.A.\t");
//		}
//		writer.println();
	}

	/**
	 * Print the distribution to one or two columns of table. If the boolean parameter printScope is false, only print the value,
	 * otherwise, print the name scope in a column, and the value in another column. Anyway, the measure identifier will be
	 * in the first row of the value column. The table symbol is used to as the splitter of the columns. 
	 */
	public void printToColumn(PrintWriter writer, boolean printId) {
//		if (printId == true) {
//			writer.println("Measure Id\t" + scope.getScopeName());
//		} else writer.println(scope.getScopeName());
//		for (SoftwareMeasure measure : measureList) {
//			if (printId == true) writer.print(measure.getIdentifier() + "\t");
//			if (measure.isUsable()) writer.println(measure.getValue());
//			else writer.println("N.A.");
//		}
	}
}
