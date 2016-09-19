package softwareMeasurement.metric.size;

import nameTable.NameTableManager;
import nameTable.nameScope.NameScope;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.metric.SoftwareStructMetric;
import softwareStructure.SoftwareStructManager;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ7ÈÕ
 * @version 1.0
 */
public abstract class SoftwareSizeMetric implements SoftwareStructMetric {
	protected SoftwareStructManager structManager = null;
	protected NameTableManager tableManager = null;
	protected SourceCodeParser parser = null;
	
	protected NameScope objectScope = null;
	
	@Override
	public void setSoftwareStructManager(SoftwareStructManager structManager) {
		this.structManager = structManager;
		tableManager = structManager.getNameTableManager();
		parser = tableManager.getSouceCodeParser();
	}

	@Override
	public void setMeasuringObject(NameScope objectScope) {
		this.objectScope = objectScope;
	}

	public NameScope getMeasuringObject() {
		return objectScope;
	}
	
	@Override
	public abstract boolean calculate(SoftwareMeasure measure);
}
