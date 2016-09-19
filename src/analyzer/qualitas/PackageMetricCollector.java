package analyzer.qualitas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nameTable.NameDefinitionVisitor;
import nameTable.NameTableFilter;
import nameTable.NameTableManager;
import nameTable.creator.NameDefinitionCreator;
import nameTable.creator.NameTableCreator;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameScope.SystemScope;
import softwareMeasurement.PackageMeasurement;
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
public class PackageMetricCollector {

	public static void main(String[] args) {
		String systemName = "weka";
		String[] versions = QualitasPathsManager.getSystemVersions(systemName);

		PrintWriter output = null;
		try {
			String info = QualitasPathsManager.getDebugFile();
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasPackageMeasure(systemName, versions[0]);

//		for (int i = 0; i < versions.length; i++) collectQualitasPackageMeasure(systemName, versions[i]);
		output.close();
	}

	public static void collectQualitasPackageMeasure(String systemName, String version) {
		String systemPath = QualitasPathsManager.getSystemPath(systemName, version);
		String result = QualitasPathsManager.getMeasureResultFile(MeasureObjectKind.MOK_PACKAGE, systemName, version, true);

		PrintWriter writer = new PrintWriter(System.out);
		try {
			writer = new PrintWriter(new FileOutputStream(new File(result)));
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		collectQualitasPackageMeasure(systemPath, writer);
		writer.close();
	}

	
	public static void collectQualitasPackageMeasure(String path, PrintWriter writer) {
		List<SoftwareMeasure> measureList = getAvailablePackageMeasureList();
		
		SourceCodeParser parser = new SourceCodeParser(path);
		NameTableCreator creator = new NameDefinitionCreator(parser);

		Debug.setStart("Begin creating system, path = " + path);
		NameTableManager manager = creator.createNameTableManager();
		Debug.time("End creating.....");
		Debug.flush();
		
		SoftwareStructManager structManager = new SoftwareStructManager(manager);
		
		NameDefinitionVisitor visitor = new NameDefinitionVisitor();
		visitor.setFilter(new PackageFilter());
		SystemScope rootScope = manager.getRootScope();
		
		rootScope.accept(visitor);
		List<NameDefinition> definitionList = visitor.getResult();
		
		writer.print("Package");
		for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.getIdentifier());
		writer.println("\tLabel");
		
		Debug.setStart("Begin scan class....");
		for (NameDefinition definition : definitionList) {
			PackageDefinition packageDef = (PackageDefinition)definition;
			
			System.out.println("Scan package: " + packageDef.getFullQualifiedName());
			
			Debug.setStart("Begin calculating measures....!");
			PackageMeasurement measurement = new PackageMeasurement(packageDef, structManager);
			measurement.getMeasureList(measureList);

			writer.print(packageDef.getFullQualifiedName());
			for (SoftwareMeasure measure : measureList) writer.print("\t" + measure.valueString());
			writer.println(packageDef.getUniqueId());
			
			Debug.time("End calculating....");
		}
		Debug.time("End scan.....");
	}

	public static List<SoftwareMeasure> getAvailablePackageMeasureList() {
		SoftwareMeasure[] measures = {
				new SoftwareMeasure(SoftwareMeasureIdentifier.FILE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.CLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.INTF),
				new SoftwareMeasure(SoftwareMeasureIdentifier.ENUM),

				new SoftwareMeasure(SoftwareMeasureIdentifier.NonTopTYPE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopPubTYPE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopNonPubTYPE),
				new SoftwareMeasure(SoftwareMeasureIdentifier.NonTopCLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopPubCLS),
				new SoftwareMeasure(SoftwareMeasureIdentifier.TopNonPubCLS),

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

class PackageFilter extends NameTableFilter {

	@Override
	public boolean accept(NameDefinition definition) {
		if (definition.getDefinitionKind() != NameDefinitionKind.NDK_PACKAGE) return false;	
		return true;
//		if (definition.getSimpleName().contains("DetailedTypeFilter")) return true;
//		else return false;
	}
}
