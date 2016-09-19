package analyzer.correlation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/24
 * @version 1.0
 */
public class ValuePairManager {

	String id = null;
	List<ValuePair> valuePairList = null;

	public ValuePairManager(String id) {
		this.id = id;
		valuePairList = new ArrayList<ValuePair>();
	}

	public void addValuePair(String id, double oneValue, double twoValue) {
		ValuePair valuePair = new ValuePair(id, oneValue, twoValue);
		valuePairList.add(valuePair); 
	}
	
	public List<ValuePair> getValuePairList() {
		return valuePairList;
	}
	
	/**
	 * Read a list of value pair from a file. Note that every value is greater than zero!
	 * In general, the line is sorted by its first value (i.e. base value) in ascending order!  
	 * @param file: Each line of the file gives a id and its two values as "id\tbaseValue\tcontrastValue" 
	 * @param hasTitle: the first line is the title or the id? 
	 */
	public void read(String file, boolean hasTitle) throws IOException {
		final String splitter = "\t";		// the table char is used as splitter in the file
		final int infoNumber = 3;
		
		valuePairList.clear();
		
		Scanner scanner = new Scanner(new File(file));
		
		int lineCounter = 0;
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (lineCounter == 0 && hasTitle == true) {
				// The first line is the title line of the file, ignore this line
				lineCounter = lineCounter + 1;
				continue;
			} else {
				// This is line give a id string and two values. We assume the file use '\t' to split the id and the label
				String[] infoStrings = line.split(splitter);
				if (infoStrings.length < infoNumber) {
					scanner.close();
					throw new AssertionError("In file [" + file + ", illegal line [" + line + "]"); 
				}
				
				String idString = infoStrings[0];
				double baseValue = Double.parseDouble(infoStrings[1]);
				double contrastValue = Double.parseDouble(infoStrings[2]);
				lineCounter = lineCounter + 1;
				
				if (baseValue > 0 && contrastValue > 0) valuePairList.add(new ValuePair(idString, baseValue, contrastValue));
			}
		}
		scanner.close();
	}
	
	public int size() {
		return valuePairList.size();
	}
	
	public void sortByBaseValue(boolean ascendingOrder) {
		Collections.sort(valuePairList, new ValuePairComparator(true, ascendingOrder));
	}

	public void sortByContrastValue(boolean ascendingOrder) {
		Collections.sort(valuePairList, new ValuePairComparator(false, ascendingOrder));
	}

	/**
	 * Return a copy of sub-list from index 0 to length-1 of the value pair listed in the current manager
	 * We copy the value pair in the list, so change the value of the copy will be not change the value 
	 * pair in the original (i.e. the current) manager 
	 */
	public ValuePairManager getPreSubManager(int length) {
		ValuePairManager result = new ValuePairManager(id);
		for (int index = 0; index < length && index < valuePairList.size(); index++) {
			ValuePair valuePair = valuePairList.get(index);
			result.addValuePair(valuePair.id, valuePair.baseValue, valuePair.contrastValue);
		}
		
		return result;
	}

	/**
	 * Return a copy of sub-list from index size() - length to size() - 1 of the value pair listed in the current manager
	 * We copy the value pair in the list, so change the value of the copy will be not change the value 
	 * pair in the original (i.e. the current) manager 
	 */
	public ValuePairManager getPostSubManager(int length) {
		ValuePairManager result = new ValuePairManager(id);
		int startIndex = valuePairList.size() - length;
		if (startIndex < 0) startIndex = 0;
		
		for (int index = startIndex; index < valuePairList.size(); index++) {
			ValuePair valuePair = valuePairList.get(index);
			result.addValuePair(valuePair.id, valuePair.baseValue, valuePair.contrastValue);
		}
		
		return result;
	}
	
	
	/**
	 * Return a copy of sub-list of the value pair which base value and contrast value greater than or equal to the given threshold 
	 * listed in the current manager. 
	 * <p>We copy the value pair in the list, so change the value of the copy will be not change the value 
	 * pair in the original (i.e. the current) manager 
	 */
	public ValuePairManager getSubManager(double threshold) {
		ValuePairManager result = new ValuePairManager(id);
		
		for (int index = 0; index < valuePairList.size(); index++) {
			ValuePair valuePair = valuePairList.get(index);
			if (valuePair.baseValue >= threshold && valuePair.contrastValue >= threshold) {
				result.addValuePair(valuePair.id, valuePair.baseValue, valuePair.contrastValue);
			}
		}
		
		return result;
	}
	
	public double getSpearmanCoefficient() {
		return CorrelationDistribution.getSpearmanCoefficientUsingMath3(this);
	}
	
	public void display(PrintStream out) {
		out.println(id);
		for (int i = 0; i < valuePairList.size(); i++) {
			ValuePair pair = valuePairList.get(i);
			out.println("<" + pair.id + ", " + pair.baseValue + ", " + pair.contrastValue + ">");
		}
	}
}


class ValuePair {
	protected String id = null;
	protected double baseValue = 0;
	protected double contrastValue = 0;
	
	public ValuePair(String id, double baseValue, double contrastValue) {
		this.id = id;
		this.baseValue = baseValue;
		this.contrastValue = contrastValue;
	}
}

class ValuePairComparator implements Comparator<ValuePair> {
	private boolean isBase = true;
	private boolean isAscending = true;
	
	public ValuePairComparator(boolean isBase, boolean isAscending) {
		this.isBase = isBase;
		this.isAscending = isAscending;
	}

	@Override
	public int compare(ValuePair one, ValuePair two) {
		if (isBase == true) {
			if (isAscending == true) return (int)(one.baseValue - two.baseValue);
			else return (int)(two.baseValue - one.baseValue);
		} else {
			if (isAscending == true) return (int)(one.contrastValue - two.contrastValue);
			else return (int)(two.contrastValue - one.contrastValue);
		}
	}
}

