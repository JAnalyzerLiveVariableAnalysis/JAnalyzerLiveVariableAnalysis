package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.NameScopeFilter;
import nameTable.NameScopeVisitor;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameScope.CompilationUnitScope;
import nameTable.nameScope.NameScope;
import nameTable.nameScope.NameScopeKind;
import nameTable.nameScope.SystemScope;
import softwareMeasurement.CompilationUnitMeasurement;
import softwareMeasurement.measure.MeasureObjectKind;
import softwareMeasurement.measure.SoftwareMeasure;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;
import softwareStructure.SoftwareStructManager;
import util.Debug;
import util.SourceCodeParser;

/**
 * @author Zhou Xiaocong
 * @since 2015Äê9ÔÂ17ÈÕ
 * @version 1.0
 */
public class CompilationUnitMetricCollector {

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
		for (int i = 0; i < versions.length; i++) collectQualitasCompilationUnitMeasure(systemName, versions[i]);
		output.close();
	}

	public static void collectQualitasCompilationUnitMeasure(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_UNIT, systemName, version, true);

		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasCompilationUnitMeasure(systemPath, writer);
		writer.close();
	}
	
	public static void collectQualitasCompilationUnitMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailableCompilationUnitMeasureList();
		
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
//		List<CompilationUnitScope> unitList = manager.getAllCompilationUnitScopes();
		
		NameScopeVisitor visitor = new NameScopeVisitor();
		visitor.setFilter(new CompilationUnitFilter());
		SystemScope rootScope = manager.getRootScope();
		
		rootScope.accept(visitor);
		List<NameScope> scopeList = visitor.getResult();
		
		writer.print("File");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + SoftwareMeasureIdentifier.getDescription(measure));
		writer.println("\tNotes");
		
		Debug.setStart("Begin scan class....");
		for (NameScope scope : scopeList) {
			CompilationUnitScope unit = (CompilationUnitScope)scope;
			
			System.out.println("Scan file: " + unit.getUnitFullName());
			
			Debug.setStart("Begin calculating measures....!");
			CompilationUnitMeasurement measurement = new CompilationUnitMeasurement(unit, structManager);
			measurement.getMeasureList(measureList);
			measurement.printToRow(writer, false);
			Debug.time("End calculating....");
		}
		Debug.time("End scan.....");
	}

	public static List<SoftwareMeasure> getAvailableCompilationUnitMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.ELOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.BLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NLOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOC),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOPT),
				
				new SoftwareMeasure(SoftwareMeasureIdentifier.FLD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.MTHD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.PARS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.LOCV),
				new SoftwareMeasure(SoftwareMeasureIdentifier.STMN),

				new SoftwareMeasure(SoftwareMeasureIdentifier.BYTE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.WORD),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CHAR),
		};
		List<SoftwareMeasure> measureList = new ArrayList<SoftwareMeasure>();
		for (int index = 0; index < measures.length; index++) measureList.add(measures[index]);
		return measureList;
	}
}

class CompilationUnitFilter extends NameScopeFilter {

	@Override
	public boolean accept(NameScope scope) {
		if (scope.getScopeKind() != NameScopeKind.NSK_COMPILATION_UNIT) return false;	
		return true;
	}
}
