package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.filter.NameTableFilter;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameScope.SystemScope;
import nameTable.visitor.NameDefinitionVisitor;
import softwareMeasurement.MethodMeasurement;
import softwareMeasurement.measure.MeasureObjectKind;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.SoftwareStructManager;
import sourceCodeAST.SourceCodeFileSet;
import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class MethodMetricCollector {

	public static void main(String[] args) {
		String systemName = "ant";
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);

		PrintWriter output = null;
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		for (int i = 0; i < versions.length; i++) collectQualitasMethodMeasure(systemName, versions[i]);
		output.close();
	}

	public static void collectQualitasMethodMeasure(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_METHOD, systemName, version, true);

		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasMethodMeasure(systemPath, writer);
		writer.close();
	}
	
	
	public static void collectQualitasMethodMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableMethodMeasureList();
		
		SourceCodeFileSet parser = new SourceCodeFileSet(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new MethodFilter());
		SystemScope rootScope = manager.getSystemScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		writer.print("Method");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + SoftwareMeasureIdentifier.getDescription(measure));
		writer.println("\tClass\tPackage\tLabel");
		
		Debug.setStart("Begin scan method....");
		for (NameDefinition definition : definitionList) {
			MethodDefinition method = (MethodDefinition)definition;
			
			System.out.println("Scan method: " + method.getFullQualifiedName());
			
			Debug.setStart("Begin calculating measures....!");
			MethodMeasurement measurement = new MethodMeasurement(method, structManager);
			measurement.getMeasureList(measureList);

			writer.print(method.getSimpleName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println("\t" + method.getEnclosingType().getSimpleName() + "\t" + method.getEnclosingType().getEnclosingPackage().getSimpleName() + "\t" + method.getUniqueId());
			Debug.time("End calculating....");
		}
		Debug.time("End scan.....");
	}

	public static List<SoftwareMeasure> getAvailableMethodMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),

		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
}

class MethodFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (!definition.isMethodDefinition()) return false;	
		return true;
	}
}

