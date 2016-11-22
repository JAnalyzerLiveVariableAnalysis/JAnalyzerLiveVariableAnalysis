package softwareChange;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import analyzer.valuedNode.ValuedClassNode;
import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;

/**
 * An indicator to match the changed class.
 * @author Zhou Xiaocong
 * @since 2014/1/26/
 * @version 1.0
 */
public class ClassChangeIndicator extends NodeChangeIndicator {

	public ClassChangeIndicator(String indicatorFile) {
		super(indicatorFile);
	}

	public static ClassChangeIndicator getClassChangeIndicatorInstance(String indicatorFile, String baseId, String contrastId) {
		File file = new File(indicatorFile);
		if (!file.exists()) return null;
		ClassChangeIndicator indicator = new ClassChangeIndicator(indicatorFile);
		try {
			indicator.loadBase(baseId);
			indicator.loadContrast(contrastId);
		} catch (IOException exc) {
			exc.printStackTrace();
			return null;
		}
		return indicator;
	}
	
	/**
	 * Generate a change indicator. If two class satisfy one of the following condition will be regarded as the same class: 
	 * <p>(1) all of them are package member class, and has the same simple name;
	 * <p>(2) all of them are non package member class, and has the same simple name and their enclosing classes have the same simple name
	 * <p>(3) the similarity of two class are greater than or equal to a threshold (so far it is 0.95)
	 *   
	 */
	public static ClassChangeIndicator generateClassChangeIndicator(String baseId, NameTableManager baseManager, String contrastId, NameTableManager contManager, String indicatorFile) {
		ClassChangeIndicator indicator = new ClassChangeIndicator(indicatorFile);
		indicator.baseId = baseId;
		indicator.contrastId = contrastId;
		
		indicator.base = new ArrayList<String>();
		indicator.contrast = new ArrayList<String>();
		
		List<DetailedTypeDefinition> baseTypeList = baseManager.getSystemScope().getAllDetailedTypeDefinitions();
		List<DetailedTypeDefinition> contTypeList = contManager.getSystemScope().getAllDetailedTypeDefinitions();
		List<DetailedTypeDefinition> alignedContTypeList = new ArrayList<DetailedTypeDefinition>();
		double threshold = 0.95;
		
		for (int baseIndex = 0; baseIndex < baseTypeList.size(); baseIndex++) {
			DetailedTypeDefinition baseType = baseTypeList.get(baseIndex);
			boolean aligned = false;
			DetailedTypeDefinition contType = null;
			for (int contIndex = 0; contIndex < contTypeList.size(); contIndex++) {
				contType = contTypeList.get(contIndex);
				if (canBeAligned(baseType, contType)) {
					aligned = true;
					break;
				} else if (NameTableComparator.calculateDetailedTypeDefinitionSimilarity(baseType, contType, true) >= threshold) {
					aligned = true;
					break;
				}
			}
			if (aligned == true) {
				String baseLabel = baseType.getSimpleName() + "@" + baseType.getLocation().getUniqueId();
				indicator.base.add(baseLabel);
				String contLabel = contType.getSimpleName() + "@" + contType.getLocation().getUniqueId();
				indicator.contrast.add(contLabel);
				alignedContTypeList.add(contType);
			} else {
				String baseLabel = baseType.getSimpleName() + "@" + baseType.getLocation().getUniqueId();
				indicator.base.add(baseLabel);
				indicator.contrast.add("");
			}
		}
		
		for (int contIndex = 0; contIndex < contTypeList.size(); contIndex++) {
			DetailedTypeDefinition contType = contTypeList.get(contIndex);
			boolean aligned = false;
			for (int index = 0; index < alignedContTypeList.size(); index++) {
				DetailedTypeDefinition alignedType = alignedContTypeList.get(index);
				if (contType == alignedType) {
					aligned = true;
					break;
				}
			}
			if (aligned == false) {
				String contLabel = contType.getSimpleName() + "@" + contType.getLocation().getUniqueId();
				indicator.base.add("");
				indicator.contrast.add(contLabel);
				System.out.println("Add base = [], cont = [" + contLabel + "]");
			}
		}
		
		return indicator;
	}
	
	/**
	 * Check the base list and contrast list as detailed type definition, find the possible error and possible more matched labels. 
	 * Generate a report for the result. 
	 */
	public void generateCheckReport(NameTableManager baseManager, NameTableManager contManager, PrintWriter report) {
		final double alignedThreshold = 0.5;
		final double toFindThreshold = 0.75;
		List<DetailedTypeDefinition> baseTypeList = new ArrayList<DetailedTypeDefinition>();
		List<DetailedTypeDefinition> contTypeList = new ArrayList<DetailedTypeDefinition>();
		
		for (int index = 0; index < base.size() && index < contrast.size(); index++) {
			String baseLabel = base.get(index);
			String contLabel = contrast.get(index);
			ValuedClassNode baseNode = null;
			DetailedTypeDefinition baseType = null;
			ValuedClassNode contNode = null;
			DetailedTypeDefinition contType = null;
			
			System.out.println("First check base [" + baseLabel + "] with contrast [" + contLabel + "]....");
			
			if (!baseLabel.equals("")) {
				baseNode = new ValuedClassNode(baseLabel, baseLabel);
				baseNode.bindDefinition(baseManager);
				baseType = (DetailedTypeDefinition)baseNode.getDefinition();
			}
			if (!contLabel.equals("")) {
				contNode = new ValuedClassNode(contLabel, contLabel);
				contNode.bindDefinition(contManager);
				contType = (DetailedTypeDefinition)contNode.getDefinition();
			}
			
			if (baseType != null && contType != null) {
				double similarity = NameTableComparator.calculateDetailedTypeDefinitionSimilarity(baseType, contType, true);
				if (!canBeAligned(baseType, contType)) {
					String reportString = "Index [" + index + "]: base [" + baseLabel + "] and contrast [" + contLabel + "] can not be aligned, and their similarity is " + similarity;
					report.println(reportString);
				} else {
					if (similarity < alignedThreshold) {
						String reportString = "Index [" + index + "]: base [" + baseLabel + "] and contrast [" + contLabel + "] have less than " + alignedThreshold + " similarity [ " + similarity + "]!";
						report.println(reportString);
					}
				}
			}
			
			baseTypeList.add(baseType);
			contTypeList.add(contType);
		}
		
		for (int index = 0; index < base.size() && index < contrast.size(); index++) {
			String baseLabel = base.get(index);
			String contLabel = contrast.get(index);
			DetailedTypeDefinition baseType = baseTypeList.get(index);
			DetailedTypeDefinition contType = contTypeList.get(index);

			System.out.println("Second check base [" + baseLabel + "] with contrast [" + contLabel + "]....");
			
			if (baseType != null && contType == null) {
				String reportString = "Index [" + index + "]: base [" + baseLabel + "] may match the following contrast class: ";
				boolean found = false;
				
				for (int contIndex = 0; contIndex < contTypeList.size(); contIndex++) {
					String tempBaseLabel = base.get(contIndex);
					if (!tempBaseLabel.equals("")) continue;
					
					DetailedTypeDefinition tempType = contTypeList.get(contIndex);
					if (tempType != null) {
						double similarity = NameTableComparator.calculateDetailedTypeDefinitionSimilarity(baseType, tempType, true); 
						if (similarity >= toFindThreshold) {
							if (found == false) {
								report.println(reportString);
								found = true;
							}
							reportString = "\t" + tempType.getSimpleName() + "@" + tempType.getLocation().getUniqueId() + ", similarity = " + similarity;
							report.println(reportString);
						}
					}
				}
			}

			if (baseType == null && contType != null) {
				String reportString = "Index [" + index + "]: contrast [" + contLabel + "] may match the following base class: ";
				boolean found = false;
				
				for (int baseIndex = 0; baseIndex < baseTypeList.size(); baseIndex++) {
					String tempContLabel = base.get(baseIndex);
					if (!tempContLabel.equals("")) continue;

					DetailedTypeDefinition tempType = baseTypeList.get(baseIndex);
					if (tempType != null) {
						double similarity = NameTableComparator.calculateDetailedTypeDefinitionSimilarity(tempType, contType, true); 
						if (similarity >= toFindThreshold) {
							if (found == false) {
								report.println(reportString);
								found = true;
							}
							reportString = "\t" + tempType.getSimpleName() + "@" + tempType.getLocation().getUniqueId() + ", similarity = " + similarity;
							report.println(reportString);
						}
					}
				}
			}
		}
		
		report.flush();
	}
	

	private static boolean canBeAligned(DetailedTypeDefinition baseType, DetailedTypeDefinition contType) {
		final String QUALIFIER_REGULAR_EXPERSSION = "\\.";		

		// We believe a package member type is never align to a non-package member type
		if (baseType.isPackageMember() && !contType.isPackageMember()) return false;
		if (!baseType.isPackageMember() && contType.isPackageMember()) return false;
		
		// We believe a interface is never align to a class type
		if (!baseType.isInterface() && contType.isInterface()) return false;
		if (baseType.isInterface() && !contType.isInterface()) return false;
		
		String baseTypeName = baseType.getSimpleName();
		String contTypeName = contType.getSimpleName();
		
		// The simple name must be the same!
		if (!baseTypeName.equals(contTypeName)) return false;
		
		
		if (!baseType.isPackageMember() && !contType.isPackageMember()) {
			// If contType classes are not package member, only when they have same simple name and including in same name package member type
			
			String baseTypeQualifiedName = baseType.getFullQualifiedName();
			String contTypeQualifiedName = contType.getFullQualifiedName();

			String[] baseTypeSplitNames = baseTypeQualifiedName.split(QUALIFIER_REGULAR_EXPERSSION); 
			String[] contTypeSplitNames = contTypeQualifiedName.split(QUALIFIER_REGULAR_EXPERSSION);
			
			// baseTypeIndex and contTypeIndex give the index of the type name include the non-package member type!
			int baseTypeIndex = baseTypeSplitNames.length-2;
			int contTypeIndex = contTypeSplitNames.length-2;
			if (baseTypeIndex < 0 || contTypeIndex < 0) return false;

			if (!baseTypeSplitNames[baseTypeIndex].equals(contTypeSplitNames[contTypeIndex])) return false;
		}
		
		return true;
	}
}
