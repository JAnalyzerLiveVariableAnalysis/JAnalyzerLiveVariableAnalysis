package softwareMeasurement.metric.inheritance;

import java.util.List;

import nameTable.nameDefinition.DetailedTypeDefinition;
import softwareMeasurement.measure.SoftwareMeasure;

/**
 * A class to calculate the metric NOP, the number of parents including extended class and implemented interfaces
 * 
 * @author Wu Zhangshen
 * @since 2015Äê9ÔÂ2ÈÕ
 * @version 1.0
 * @update 2015/10/13, Zhou Xiaocong
 */
public class NOPMetric extends SoftwareInheritanceMetric {

	@Override
	public boolean calculate(SoftwareMeasure measure) {
		if (type == null || structManager == null) return false;

		List<DetailedTypeDefinition> parentList = structManager.getAllParentTypeList(type);
		double value = 0;
		if (parentList != null) value = parentList.size();
		
		measure.setValue(value);
		return true;
	}
}
