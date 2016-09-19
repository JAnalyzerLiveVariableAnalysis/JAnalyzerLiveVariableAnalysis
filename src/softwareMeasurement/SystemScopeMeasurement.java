package softwareMeasurement;

import java.io.PrintWriter;

import nameTable.nameScope.SystemScope;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareStructure.SoftwareStructManager;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê7ÔÂ11ÈÕ
 * @version 1.0
 */
public class SystemScopeMeasurement extends NameScopeMeasurement {

	public SystemScopeMeasurement(SystemScope scope, SoftwareStructManager manager) {
		super(scope, manager);
	}
	
	public SystemScope getSystemScope() {
		return (SystemScope)scope;
	}
	
	/**
	 * Print the measures to one or two rows of table. If the boolean parameter printId is false, only print the value,
	 * otherwise, print the measure identifiers in a row, and the value in another row. Anyway, the class name will be
	 * in the first column of the value row. The table symbol is used to as the splitter of the columns. 
	 */
/*	public void printToRow(PrintWriter writer, boolean printId, String systemId, String systemDescription) {
		if (printId == true) {
			writer.print("Measure Id\t");
			for (SoftwareMeasure measure : measureList) {
				writer.print(measure.getIdentifier() + "\t");
			}
			writer.println();
		}
		
		writer.print(systemId + "\t");
		for (SoftwareMeasure measure : measureList) {
			if (measure.isUsable()) writer.print(measure.getValue() + "\t");
			else writer.print("N.A.\t");
		}
		writer.println(systemDescription);
	}*/
}
