package softwareChange;

import java.io.PrintWriter;
import java.util.List;

import nameTable.NameTableManager;
import nameTable.nameDefinition.DetailedTypeDefinition;
import nameTable.nameDefinition.FieldDefinition;
import nameTable.nameDefinition.MethodDefinition;
import nameTable.nameDefinition.NameDefinition;
import nameTable.nameDefinition.NameDefinitionKind;
import nameTable.nameDefinition.PackageDefinition;
import nameTable.nameDefinition.TypeDefinition;
import nameTable.nameDefinition.VariableDefinition;
import nameTable.nameReference.TypeReference;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/10
 * @version 1.0
 */
public class NameTableComparator {
	private static final String splitter = "\t";

	/**
	 * Compare types and names of two field definitions
	 */
	public static boolean compareFieldDefinition(FieldDefinition one, FieldDefinition two, PrintWriter report) {
		StringBuilder reportString = new StringBuilder("Field");
		
		String locationString = splitter + one.getLocation().getUniqueId() + splitter + two.getLocation().getUniqueId();
		
		String oneName = one.getSimpleName();
		String twoName = two.getSimpleName();
		reportString.append(splitter + oneName + splitter + twoName);
		if (!oneName.equals(twoName)) {
			reportString.append(splitter + "have different field name");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
			return false;
		}
		
		String oneType = one.getType().toDeclarationString();
		String twoType = two.getType().toDeclarationString();
		if (!oneType.equals(twoType)) {
			reportString.append(splitter + "have different field type [" + oneType + "] v.s. [" + twoType + "]");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
			return false;
		} else {
			reportString.append(splitter + "are the same");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
			return true;
		}
	}
	
	
	/**
	 * Compare signatures of two method definitions, the signature of a method includes its return type, list of parameters and throw types   
	 */
	public static boolean compareMethodDefinitionSignature(MethodDefinition one, MethodDefinition two, PrintWriter report) {
		StringBuilder reportString = new StringBuilder("Method");
		
		String locationString = splitter + one.getLocation().getUniqueId() + splitter + two.getLocation().getUniqueId();
		
		String oneName = one.getSimpleName();
		String twoName = two.getSimpleName();
		reportString.append(splitter + oneName + splitter + twoName);
		if (!oneName.equals(twoName)) {
			reportString.append(splitter + "have different method name");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
			return false;
		}
		
		String oneReturnType = "void";
		if (one.getReturnType() != null) oneReturnType = one.getReturnType().toDeclarationString();
		String twoReturnType = "void";
		if (two.getReturnType() != null) twoReturnType = two.getReturnType().toDeclarationString();
		if (!oneReturnType.equals(twoReturnType)) {
			reportString.append(splitter + "have different return type [" + oneReturnType + "] v.s. [" + twoReturnType + "]");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
			return false;
		}
		
		List<VariableDefinition> oneParaList = one.getParameterList();
		List<VariableDefinition> twoParaList = two.getParameterList();
		if (oneParaList != null || twoParaList != null) {
			if (oneParaList == null) {
				reportString.append(splitter + "the first has no parameter, but the second has " + twoParaList.size() + " parameters");
			} else if (twoParaList == null) {
				reportString.append(splitter + "the first has " + oneParaList.size() + " parameters, but the second has no parameter");
			}
			if (oneParaList == null || twoParaList == null) {
				reportString.append(locationString);
				if (report != null) report.println(reportString.toString());
				return false;
			}
			
			if (oneParaList.size() != twoParaList.size()) {
				reportString.append(splitter + "the first has " + oneParaList.size() + " parameters, but the second has " + twoParaList.size() + " parameters");
				reportString.append(locationString);
				if (report != null) report.println(reportString.toString());
				return false;
			}
			
			for (int index = 0; index < oneParaList.size(); index++) {
				VariableDefinition onePara = oneParaList.get(index);
				VariableDefinition twoPara = twoParaList.get(index);
				String oneParaName = onePara.getSimpleName();
				String twoParaName = twoPara.getSimpleName();
				
				if (!oneParaName.equals(twoParaName)) {
					reportString.append(splitter + "parameter number " + index + " have different name [" + oneParaName + "] v.s. [" + twoParaName + "]");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					return false;
				}
				
				String oneParaType = onePara.getType().toDeclarationString();
				String twoParaType = twoPara.getType().toDeclarationString();
				if (!oneParaType.equals(twoParaType)) {
					reportString.append(splitter + "parameter number " + index + " have different type [" + oneParaType + "] v.s. [" + twoParaType + "]");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					return false;
				}
			}
		}
		
		List<TypeReference> oneThrowList = one.getThrowTypeList();
		List<TypeReference> twoThrowList = two.getThrowTypeList();
		if (oneThrowList != null || twoThrowList != null) {
			if (oneThrowList == null) {
				reportString.append(splitter + "the first has no throw type, but the second has " + twoThrowList.size() + " throw types");
			} else if (twoParaList == null) {
				reportString.append(splitter + "the first has " + oneThrowList.size() + " throw types, but the second has no throw type");
			}
			if (oneThrowList == null || twoThrowList == null) {
				reportString.append(locationString);
				if (report != null) report.println(reportString.toString());
				return false;
			}
			
			if (oneThrowList.size() != twoThrowList.size()) {
				reportString.append(splitter + "the first has " + oneThrowList.size() + " throw types, but the second has " + twoThrowList.size() + " throw types");
				reportString.append(locationString);
				if (report != null) report.println(reportString.toString());
				return false;
			}
			
			for (int index = 0; index < oneThrowList.size(); index++) {
				String oneThrowType = oneThrowList.get(index).toDeclarationString();
				String twoThrowType = twoThrowList.get(index).toDeclarationString();
				
				if (!oneThrowType.equals(twoThrowType)) {
					reportString.append(splitter + "Throw type number " + index + " have different name [" + oneThrowType + "] v.s. [" + twoThrowType + "]");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					return false;
				}
			}
		}
		
		reportString.append(splitter + "are the same");
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		return true;
	}
	
	/**
	 * <p>Compare two detailed type definitions (i.e. class definition), return the similarity of the first (i.e. one) compared to the second (i.e. two).
	 * The calculation of the similarity is based on the name, the super types, the fields and the methods in these two definition, that is, 
	 * <p>[(the number of the same super-types, fields and methods) + (1, if the two class name has the same name, otherwise 0)] / (the total number of the fields and methods in the first definition)
	 * <p>We use the above methods compareFieldDefinition() and compareMethodDefinition() to test if the fields and methods in the two definitions 
	 * respectively are the same. 
	 */
	public static double compareDetailedTypeDefinition(DetailedTypeDefinition one, DetailedTypeDefinition two, PrintWriter report) {
		double ratio = 1.0;
		int oneTotalItems = 1;
		int sameItems = 0;

		StringBuilder reportString = new StringBuilder("Class");
		String locationString = splitter + one.getLocation().getUniqueId() + splitter + two.getLocation().getUniqueId();
		
		String oneName = one.getSimpleName();
		String twoName = two.getSimpleName();
		reportString.append(splitter + oneName + splitter + twoName);
		int changeIndex = reportString.length();
		
		if (oneName.equals(twoName)) {
			reportString.append(splitter + "have the same simple name [" + oneName + "]");
			sameItems = sameItems + 1;
		} else {
			reportString.append(splitter + "have different simple name [" + oneName + "] v.s. [" + twoName + "]");
		}
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		
		reportString.delete(changeIndex, reportString.length());
		String oneQualifiedName = one.getFullQualifiedName();
		String twoQualifiedName = two.getFullQualifiedName();
		if (oneQualifiedName.equals(twoQualifiedName)) {
			reportString.append(splitter + "have the same qualified name [" + oneQualifiedName + "]");
			sameItems = sameItems + 1;
		} else {
			reportString.append(splitter + "have different qualified name [" + oneQualifiedName + "] v.s. [" + twoQualifiedName + "]");
		}
		oneTotalItems = oneTotalItems + 1;
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		
		List<TypeReference> oneSuperList = one.getSuperList();
		List<TypeReference> twoSuperList = two.getSuperList();
		boolean hasDifferentSuperType = false;
		if (oneSuperList != null) {
			for (int oneIndex = 0; oneIndex < oneSuperList.size(); oneIndex++) {
				String oneSuperType = oneSuperList.get(oneIndex).toDeclarationString();
				boolean oneSuperFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoSuperList != null) {
					for (int twoIndex = 0; twoIndex < twoSuperList.size(); twoIndex++) {
						String twoSuperType = twoSuperList.get(twoIndex).toDeclarationString();
						if (oneSuperType.equals(twoSuperType)) {
							oneSuperFound = true;
							break;
						}
					}
					if (oneSuperFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The super type [" + oneSuperType + "] in the first class is not in the second class");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentSuperType = true;
					} else sameItems = sameItems + 1;
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The super type [" + oneSuperType + "] in the first class is not in the second class");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentSuperType = true;
				}
			}
		}

		if (twoSuperList != null) {
			for (int twoIndex = 0; twoIndex < twoSuperList.size(); twoIndex++) {
				String twoSuperType = twoSuperList.get(twoIndex).toDeclarationString();
				boolean twoSuperFound = false;
				// oneTotalItems = oneTotalItems + 1; If the super type of the second is not in the first, we do not calculate it in the total items!
				if (oneSuperList != null) {
					for (int oneIndex = 0; oneIndex < oneSuperList.size(); oneIndex++) {
						String oneSuperType = oneSuperList.get(oneIndex).toDeclarationString();
						if (twoSuperType.equals(oneSuperType)) {
							twoSuperFound = true;
							break;
						}
					}
					if (twoSuperFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The super type [" + twoSuperType + "] in the second class is not in the first class");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentSuperType = true;
					} // else sameItems = sameItems + 1; The same item has countered in the last loop statement!
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The super type [" + twoSuperType + "] in the second class is not in the first class");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentSuperType = true;
				}
			}
		}
		
		if (hasDifferentSuperType == false) {
			reportString.delete(changeIndex, reportString.length());
			reportString.append(splitter + "have the same super type list");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
		}
		
		List<FieldDefinition> oneFieldList = one.getFieldList();
		List<FieldDefinition> twoFieldList = two.getFieldList();
		boolean hasDifferentField = false;
		if (oneFieldList != null) {
			for (int oneIndex = 0; oneIndex < oneFieldList.size(); oneIndex++) {
				FieldDefinition oneField = oneFieldList.get(oneIndex);
				boolean oneFieldFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoFieldList != null) {
					for (int twoIndex = 0; twoIndex < twoFieldList.size(); twoIndex++) {
						FieldDefinition twoField = twoFieldList.get(twoIndex);
						if (compareFieldDefinition(oneField, twoField, null)) {
							oneFieldFound = true;
							break;
						}
					}
					if (oneFieldFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The field [" + oneField.getSimpleName() + "] in the first class is not in the second class");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentField = true;
					} else {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "have the same field [" + oneField.toDeclarationString() + "]");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						sameItems = sameItems + 1;
					}
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The field [" + oneField.getSimpleName() + "] in the first class is not in the second class");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentField = true;
				}
			}
		}

		if (twoFieldList != null) {
			for (int twoIndex = 0; twoIndex < twoFieldList.size(); twoIndex++) {
				FieldDefinition twoField = twoFieldList.get(twoIndex);
				boolean twoFieldFound = false;
				// oneTotalItems = oneTotalItems + 1; If the field of the second is not in the first, we do not calculate it in the total items!
				if (oneFieldList != null) {
					for (int oneIndex = 0; oneIndex < oneFieldList.size(); oneIndex++) {
						FieldDefinition oneField = oneFieldList.get(oneIndex);
						if (compareFieldDefinition(oneField, twoField, null)) {
							twoFieldFound = true;
							break;
						}
					}
					if (twoFieldFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The field [" + twoField.getSimpleName() + "] in the second class is not in the first class");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentField = true;
					} // else sameItems = sameItems + 1; The same item has countered in the last loop statement!
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The field [" + twoField.getSimpleName() + "] in the second class is not in the first class");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentField = true;
				}
			}
		}
		
		if (hasDifferentField == false) {
			reportString.delete(changeIndex, reportString.length());
			reportString.append(splitter + "have the same field list");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
		}
		
		List<MethodDefinition> oneMethodList = one.getMethodList();
		List<MethodDefinition> twoMethodList = two.getMethodList();
		boolean hasDifferentMethod = false;
		if (oneMethodList != null) {
			for (int oneIndex = 0; oneIndex < oneMethodList.size(); oneIndex++) {
				MethodDefinition oneMethod = oneMethodList.get(oneIndex);
				boolean oneMethodFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoMethodList != null) {
					for (int twoIndex = 0; twoIndex < twoMethodList.size(); twoIndex++) {
						MethodDefinition twoMethod = twoMethodList.get(twoIndex);
						if (compareMethodDefinitionSignature(oneMethod, twoMethod, null)) {
							oneMethodFound = true;
							break;
						}
					}
					if (oneMethodFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The method [" + oneMethod.getSimpleName() + "] in the first class is not in the second class");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentMethod = true;
					} else {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "have the same signature method [" + oneMethod.getSimpleName() + "]");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						sameItems = sameItems + 1;
					}
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The method [" + oneMethod.getSimpleName() + "] in the first class is not in the second class");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentMethod = true;
				}
			}
		}

		if (twoMethodList != null) {
			for (int twoIndex = 0; twoIndex < twoMethodList.size(); twoIndex++) {
				MethodDefinition twoMethod = twoMethodList.get(twoIndex);
				boolean twoMethodFound = false;
				// oneTotalItems = oneTotalItems + 1; If the Method of the second is not in the first, we do not calculate it in the total items!
				if (oneMethodList != null) {
					for (int oneIndex = 0; oneIndex < oneMethodList.size(); oneIndex++) {
						MethodDefinition oneMethod = oneMethodList.get(oneIndex);
						if (compareMethodDefinitionSignature(oneMethod, twoMethod, null)) {
							twoMethodFound = true;
							break;
						}
					}
					if (twoMethodFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The method [" + twoMethod.getSimpleName() + "] in the second class is not in the first class");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentMethod = true;
					} // else sameItems = sameItems + 1; The same item has countered in the last loop statement!
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The Method [" + twoMethod.getSimpleName() + "] in the second class is not in the first class");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentMethod = true;
				}
			}
		}
		
		if (hasDifferentMethod == false) {
			reportString.delete(changeIndex, reportString.length());
			reportString.append(splitter + "have the same method list");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
		}

		reportString.delete(changeIndex, reportString.length());
		ratio = (double)sameItems / oneTotalItems;
		if (sameItems == oneTotalItems) {
			reportString.append(splitter + "are the same");
		} else {
			reportString.append(splitter + "The similarity of two classes is " + ratio);
		}
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		
		return ratio;
	}
	
	/**
	 * Return the similarity of the first name definition compared to the second name definition. 
	 */
	public static double calculateNameDefinitionSimilarity(NameDefinition one, NameDefinition two, boolean ignoreName) {
		if (one.getDefinitionKind() == NameDefinitionKind.NDK_TYPE && two.getDefinitionKind() == NameDefinitionKind.NDK_TYPE) {
			TypeDefinition oneType = (TypeDefinition)one;
			TypeDefinition twoType = (TypeDefinition)two;
			
			if (oneType.isDetailedType() && twoType.isDetailedType()) {
				DetailedTypeDefinition oneDetailedType = (DetailedTypeDefinition)oneType;
				DetailedTypeDefinition twoDetailedType = (DetailedTypeDefinition)twoType;
				
				return calculateDetailedTypeDefinitionSimilarity(oneDetailedType, twoDetailedType, ignoreName);
			} else return 0;
		} else return 0;
	}

	/**
	 * <p>Return the similarity of the first (i.e. one) compared to the second (i.e. two).
	 * The calculation of the similarity is based on the name, the super types, the fields and the methods in these two definition, that is, 
	 * <p>[(the number of the same super-types, fields and methods) + (1, if the two class name has the same name, otherwise 0)] / (the total number of the fields and methods in the first definition)
	 * <p>We use the above methods compareFieldDefinition() and compareMethodDefinition() to test if the fields and methods in the two definitions 
	 * respectively are the same. 
	 * <p> Important note: calculateDetailedTypeDefinitionSimilarity(one, two) != calculateDetailedTypeDefinitionSimilarity(two, one)!!
	 */
	public static double calculateDetailedTypeDefinitionSimilarity(DetailedTypeDefinition one, DetailedTypeDefinition two, boolean ignoreName) {
		double ratio = 1.0;
		int oneTotalItems = 0;
		int sameItems = 0;
		// In Java regular expression pattern, "\\." match ".", since "." has a specialized meaning in regular expression patterns
		final String QUALIFIER_REGULAR_EXPERSSION = "\\.";		

		// We believe a package member type is never similar to a non-package member type
		if (one.isPackageMember() && !two.isPackageMember()) return 0;
		if (!one.isPackageMember() && two.isPackageMember()) return 0;
		
		// We believe a interface is never similar to a class type
		if (!one.isInterface() && two.isInterface()) return 0;
		if (one.isInterface() && !two.isInterface()) return 0;
		
		String oneName = one.getSimpleName();
		String twoName = two.getSimpleName();
		
		if (!ignoreName) {
			oneTotalItems = oneTotalItems + 1;
			if (oneName.equals(twoName)) sameItems = sameItems + 1;
		}

		String oneQualifiedName = one.getFullQualifiedName();
		String twoQualifiedName = two.getFullQualifiedName();
		if (!ignoreName) {
			if (oneQualifiedName.equals(twoQualifiedName)) sameItems = sameItems + 1;
			oneTotalItems = oneTotalItems + 1;
		}
		
		if (!one.isPackageMember() && !two.isPackageMember()) {
			// If two classes are not package member, only when they have same simple name and including in same name package member type, we 
			// continue to calculate the similarity!
			if (!oneName.equals(twoName)) return 0;
			
			String[] oneSplitNames = oneQualifiedName.split(QUALIFIER_REGULAR_EXPERSSION); 
			String[] twoSplitNames = twoQualifiedName.split(QUALIFIER_REGULAR_EXPERSSION);
			
			// oneIndex and twoIndex give the index of the type name include the non-package member type!
			int oneIndex = oneSplitNames.length-2;
			int twoIndex = twoSplitNames.length-2;
			if (oneIndex < 0 || twoIndex < 0) return 0;

			if (!oneSplitNames[oneIndex].equals(twoSplitNames[twoIndex])) return 0;
		}
		
		List<TypeReference> oneSuperList = one.getSuperList();
		List<TypeReference> twoSuperList = two.getSuperList();
		if (oneSuperList != null) {
			for (int oneIndex = 0; oneIndex < oneSuperList.size(); oneIndex++) {
				String oneSuperType = oneSuperList.get(oneIndex).toDeclarationString();
				boolean oneSuperFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoSuperList != null) {
					for (int twoIndex = 0; twoIndex < twoSuperList.size(); twoIndex++) {
						String twoSuperType = twoSuperList.get(twoIndex).toDeclarationString();
						if (oneSuperType.equals(twoSuperType)) {
							oneSuperFound = true;
							break;
						}
					}
					if (oneSuperFound == true) sameItems = sameItems + 1;
				}
			}
		}

		// If a type is in the super type list of the second class, but not in the super type list of the first class, it will not
		// impact the calculate of the similarity of the first class to the second class.
		// So calculateDetailedTypeDefinitionSimilarity(one, two) may not equal to calculateDetailedTypeDefinitionSimilarity(two, one)!! 
		
		List<FieldDefinition> oneFieldList = one.getFieldList();
		List<FieldDefinition> twoFieldList = two.getFieldList();
		if (oneFieldList != null) {
			for (int oneIndex = 0; oneIndex < oneFieldList.size(); oneIndex++) {
				FieldDefinition oneField = oneFieldList.get(oneIndex);
				boolean oneFieldFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoFieldList != null) {
					for (int twoIndex = 0; twoIndex < twoFieldList.size(); twoIndex++) {
						FieldDefinition twoField = twoFieldList.get(twoIndex);
						if (compareFieldDefinition(oneField, twoField, null)) {
							oneFieldFound = true;
							break;
						}
					}
					if (oneFieldFound == true) sameItems = sameItems + 1;
				}
			}
		}

		List<MethodDefinition> oneMethodList = one.getMethodList();
		List<MethodDefinition> twoMethodList = two.getMethodList();
		if (oneMethodList != null) {
			for (int oneIndex = 0; oneIndex < oneMethodList.size(); oneIndex++) {
				MethodDefinition oneMethod = oneMethodList.get(oneIndex);
				boolean oneMethodFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoMethodList != null) {
					for (int twoIndex = 0; twoIndex < twoMethodList.size(); twoIndex++) {
						MethodDefinition twoMethod = twoMethodList.get(twoIndex);
						if (compareMethodDefinitionSignature(oneMethod, twoMethod, null)) {
							oneMethodFound = true;
							break;
						}
					}
					if (oneMethodFound == true) sameItems = sameItems + 1;
				}
			}
		}

		if (oneTotalItems > 0) ratio = (double)sameItems / oneTotalItems;
		else {
			if (oneName.equals(twoName)) ratio = 1;
			else ratio = 0;
		}
		return ratio;
	}
	
	/**
	 * Compare two packages by comparing class name (i.e. without comparing the contents of the class) in the package. Calculate 
	 * the similarity of the two packages as (the number of the same class name in two packages)/(the max number of the classes in the first packages) 
	 */
	public static double comparePackageDefinitionByClassName(PackageDefinition one, PackageDefinition two, PrintWriter report) {
		double ratio = 1.0;
		int oneTotalItems = 1;
		int sameItems = 0;

		StringBuilder reportString = new StringBuilder("Package");
		String locationString = splitter + one.getFullQualifiedName() + splitter + two.getFullQualifiedName();
		
		String oneName = one.getSimpleName();
		String twoName = two.getSimpleName();
		reportString.append(splitter + oneName + splitter + twoName);
		int changeIndex = reportString.length();
		
		if (oneName.equals(twoName)) {
			reportString.append(splitter + "have the same simple name [" + oneName + "]");
			sameItems = sameItems + 1;
		} else {
			reportString.append(splitter + "have different simple name [" + oneName + "] v.s. [" + twoName + "]");
		}
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		
		reportString.delete(changeIndex, reportString.length());
		String oneQualifiedName = one.getFullQualifiedName();
		String twoQualifiedName = two.getFullQualifiedName();
		if (oneQualifiedName.equals(twoQualifiedName)) {
			reportString.append(splitter + "have the same qualified name [" + oneQualifiedName + "]");
			sameItems = sameItems + 1;
		} else {
			reportString.append(splitter + "have different qualified name [" + oneQualifiedName + "] v.s. [" + twoQualifiedName + "]");
		}
		oneTotalItems = oneTotalItems + 1;
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		
		List<DetailedTypeDefinition> oneTypeList = one.getAllDetailedTypeDefinitions();
		List<DetailedTypeDefinition> twoTypeList = two.getAllDetailedTypeDefinitions();
		boolean hasDifferentType = false;
		if (oneTypeList != null) {
			for (int oneIndex = 0; oneIndex < oneTypeList.size(); oneIndex++) {
				String oneTypeName = oneTypeList.get(oneIndex).getSimpleName();
				boolean oneTypeFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoTypeList != null) {
					for (int twoIndex = 0; twoIndex < twoTypeList.size(); twoIndex++) {
						String twoTypeName = twoTypeList.get(twoIndex).getSimpleName();
						if (oneTypeName.equals(twoTypeName)) {
							oneTypeFound = true;
							break;
						}
					}
					if (oneTypeFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The type [" + oneTypeName + "] in the first package is not in the second package");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentType = true;
					} else {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "have the same type [" + oneTypeName + "] in two packages");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						sameItems = sameItems + 1;
					}
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The type [" + oneTypeName + "] in the first package is not in the second package");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentType = true;
				}
			}
		}

		if (twoTypeList != null) {
			for (int twoIndex = 0; twoIndex < twoTypeList.size(); twoIndex++) {
				String twoTypeName = twoTypeList.get(twoIndex).getSimpleName();
				boolean twoTypeFound = false;
				// oneTotalItems = oneTotalItems + 1; If the super type of the second is not in the first, we do not calculate it in the total items!
				if (oneTypeList != null) {
					for (int oneIndex = 0; oneIndex < oneTypeList.size(); oneIndex++) {
						String oneTypeName = oneTypeList.get(oneIndex).getSimpleName();
						if (twoTypeName.equals(oneTypeName)) {
							twoTypeFound = true;
							break;
						}
					}
					if (twoTypeFound == false) {
						reportString.delete(changeIndex, reportString.length());
						reportString.append(splitter + "The type [" + twoTypeName + "] in the second package is not in the first package");
						reportString.append(locationString);
						if (report != null) report.println(reportString.toString());
						hasDifferentType = true;
					} // else sameItems = sameItems + 1; The same item has countered in the last loop statement!
				} else {
					reportString.delete(changeIndex, reportString.length());
					reportString.append(splitter + "The type [" + twoTypeName + "] in the second package is not in the first package");
					reportString.append(locationString);
					if (report != null) report.println(reportString.toString());
					hasDifferentType = true;
				}
			}
		}
		
		if (hasDifferentType == false) {
			reportString.delete(changeIndex, reportString.length());
			reportString.append(splitter + "have the same type list");
			reportString.append(locationString);
			if (report != null) report.println(reportString.toString());
		}
		
		reportString.delete(changeIndex, reportString.length());
		ratio = (double)sameItems / oneTotalItems;
		if (sameItems == oneTotalItems) {
			reportString.append(splitter + "are the same");
		} else {
			reportString.append(splitter + "The similarity of the first packages to the second package is " + ratio);
		}
		reportString.append(locationString);
		if (report != null) report.println(reportString.toString());
		return ratio;
	}

	/**
	 * Calculate the similarity of the two packages as (the number of the same class name in two packages)/
	 * (the number of the classes in the first packages) 
	 */
	public static double calculatePackageDefinitionSimilarityByClassName(PackageDefinition one, PackageDefinition two) {
		double ratio = 1.0;
		int oneTotalItems = 1;
		int sameItems = 0;

		String oneName = one.getSimpleName();
		String twoName = two.getSimpleName();
		if (oneName.equals(twoName)) sameItems = sameItems + 1;
		
		String oneQualifiedName = one.getFullQualifiedName();
		String twoQualifiedName = two.getFullQualifiedName();
		if (oneQualifiedName.equals(twoQualifiedName)) sameItems = sameItems + 1;
		oneTotalItems = oneTotalItems + 1;
		
		List<DetailedTypeDefinition> oneTypeList = one.getAllDetailedTypeDefinitions();
		List<DetailedTypeDefinition> twoTypeList = two.getAllDetailedTypeDefinitions();
		if (oneTypeList != null) {
			for (int oneIndex = 0; oneIndex < oneTypeList.size(); oneIndex++) {
				String oneTypeName = oneTypeList.get(oneIndex).getSimpleName();
				boolean oneTypeFound = false;
				oneTotalItems = oneTotalItems + 1;
				if (twoTypeList != null) {
					for (int twoIndex = 0; twoIndex < twoTypeList.size(); twoIndex++) {
						String twoTypeName = twoTypeList.get(twoIndex).getSimpleName();
						if (oneTypeName.equals(twoTypeName)) {
							oneTypeFound = true;
							break;
						}
					}
					if (oneTypeFound == true) sameItems = sameItems + 1;
				}
			}
		}

		ratio = (double)sameItems / oneTotalItems;
		return ratio;
	}
	
	/**
	 * Use the above method comparePackageDefinitionByClassName() to compare all package pairs with one simple name contained in another 
	 * qualified name in the two name table
	 */
	public static void compareNameTabelAtPackageLevel(NameTableManager one, NameTableManager two, PrintWriter report) {
		List<PackageDefinition> onePackageList = one.getAllPackageDefinitions();
		List<PackageDefinition> twoPackageList = two.getAllPackageDefinitions();
		
		for (PackageDefinition onePackage : onePackageList) {
			String oneSimpleName = onePackage.getSimpleName();
			String oneFullyQualifiedName = onePackage.getFullQualifiedName();
			
			for (PackageDefinition twoPackage : twoPackageList) {
				String twoSimpleName = twoPackage.getSimpleName();
				String twoFullyQualifiedName = twoPackage.getFullQualifiedName();
				
				if (oneFullyQualifiedName.contains(twoSimpleName) || twoFullyQualifiedName.contains(oneSimpleName)) {
					System.out.println("Compare package [" + oneFullyQualifiedName + "] with [" + twoFullyQualifiedName + "]....");
					comparePackageDefinitionByClassName(onePackage, twoPackage, report);
				}
			}
		}
	}
	

	/**
	 * Use the above method calculatePackageDefinitionSimilarityByClassName() to calculate all package pairs in the two name tables!
	 * Only those information with the similarity greater than or equal to threshold will be written to the report!
	 */
	public static void calculateSimilarityAtPackageLevel(NameTableManager one, NameTableManager two, PrintWriter report, double threshold) {
		List<PackageDefinition> onePackageList = one.getAllPackageDefinitions();
		List<PackageDefinition> twoPackageList = two.getAllPackageDefinitions();

		for (PackageDefinition onePackage : onePackageList) {
			String oneSimpleName = onePackage.getSimpleName();
			String oneFullyQualifiedName = onePackage.getFullQualifiedName();
			
			for (PackageDefinition twoPackage : twoPackageList) {
				String twoSimpleName = twoPackage.getSimpleName();
				String twoFullyQualifiedName = twoPackage.getFullQualifiedName();
				
				System.out.println("Compare package [" + oneFullyQualifiedName + "] with [" + twoFullyQualifiedName + "]....");
				double ratio = calculatePackageDefinitionSimilarityByClassName(onePackage, twoPackage);
				
				if (ratio > threshold) {
					StringBuilder reportString = new StringBuilder("Package");
					reportString.append(splitter + oneSimpleName + splitter + twoSimpleName);
					reportString.append(splitter + ratio);
					reportString.append(splitter + oneFullyQualifiedName + splitter + twoFullyQualifiedName);
					report.println(reportString.toString());
				}
			}
		}
	}

	/**
	 * Use the above method calculateDetailedTypeDefinitionSimilarity() to calculate all class pairs in the two name tables!
	 * Only those information with the similarity greater than or equal to threshold will be written to the report!
	 */
	public static void calculateSimilarityAtDetailedTypeLevel(NameTableManager one, NameTableManager two, PrintWriter report, double threshold) {
		List<DetailedTypeDefinition> oneTypeList = one.getSystemScope().getAllDetailedTypeDefinitions();
		List<DetailedTypeDefinition> twoTypeList = two.getSystemScope().getAllDetailedTypeDefinitions();

		for (DetailedTypeDefinition oneType : oneTypeList) {
			String oneSimpleName = oneType.getSimpleName();
			String oneFullyQualifiedName = oneType.getFullQualifiedName();

//			if (!oneSimpleName.equals("NameSpace")) continue;
			
			for (DetailedTypeDefinition twoType : twoTypeList) {
				String twoSimpleName = twoType.getSimpleName();
				String twoFullyQualifiedName = twoType.getFullQualifiedName();

//				if (!twoSimpleName.equals("NameSpace")) continue;
				
				double ratio = calculateDetailedTypeDefinitionSimilarity(oneType, twoType, true);
				System.out.println("Compare detailed type [" + oneFullyQualifiedName + "] with [" + twoFullyQualifiedName + "].... ratio = " + ratio);
				
				if (ratio >= threshold) {
					StringBuilder reportString = new StringBuilder("Class");
					reportString.append(splitter + oneSimpleName + splitter + twoSimpleName);
					reportString.append(splitter + ratio);
					reportString.append(splitter + oneFullyQualifiedName + splitter + twoFullyQualifiedName);
					report.println(reportString.toString());
				}
			}
		}
	}
	
}
