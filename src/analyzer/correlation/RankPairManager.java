package analyzer.correlation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * A manager to store and manage a list of rank pairs. Such list can be used to analyze the correlation between two rank data. 
 * @author Zhou Xiaocong
 * @since 2014/1/23/
 * @version 1.0
 */
public class RankPairManager {
	String id = null;
	List<RankPair> rankPairList = null;


	public RankPairManager(String id) {
		this.id = id;
		rankPairList = new ArrayList<RankPair>();
	}
	
	public void addRankPair(String id, int baseRank, int contrastRank) {
		RankPair rankPair = new RankPair(id, baseRank, contrastRank);
		rankPairList.add(rankPair); 
	}
	
	public List<RankPair> getRankPairList() {
		return rankPairList;
	}
	
	/**
	 * Read a list of rank pair from a file. Note that every rank is greater than zero!
	 * In general, the line is sorted by its first rank (i.e. base rank) in ascending order!  
	 * @param file: Each line of the file gives a id and its two ranks as "id\tbaseRank\tcontrastRank" 
	 * @param hasTitle: the first line is the title or the id? 
	 */
	public void read(String file, boolean hasTitle) throws IOException {
		final String splitter = "\t";		// the table char is used as splitter in the file
		final int infoNumber = 3;
		
		rankPairList.clear();
		
		Scanner scanner = new Scanner(new File(file));
		
		int lineCounter = 0;
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (lineCounter == 0 && hasTitle == true) {
				// The first line is the title line of the file, ignore this line
				lineCounter = lineCounter + 1;
				continue;
			} else {
				// This is line give a id string and two ranks. We assume the file use '\t' to split the id and the label
				String[] infoStrings = line.split(splitter);
				if (infoStrings.length < infoNumber) {
					scanner.close();
					throw new AssertionError("In file [" + file + ", illegal line [" + line + "]"); 
				}
				
				String idString = infoStrings[0];
				int baseRank = Integer.parseInt(infoStrings[1]);
				int contrastRank = Integer.parseInt(infoStrings[2]);
				lineCounter = lineCounter + 1;
				
				if (baseRank > 0 && contrastRank > 0) rankPairList.add(new RankPair(idString, baseRank, contrastRank));
			}
		}
		scanner.close();
	}
	
	
	public void sortByBaseRank(boolean ascendingOrder) {
		Collections.sort(rankPairList, new RankPairComparator(true, ascendingOrder));
	}

	public void sortByContrastRank(boolean ascendingOrder) {
		Collections.sort(rankPairList, new RankPairComparator(false, ascendingOrder));
	}
	
	/**
	 * Return a copy of sub-list from index 0 to length-1 of the rank pair listed in the current manager
	 * We copy the rank pair in the list, so change the rank of the copy will be not change the rank 
	 * pair in the original (i.e. the current) manager 
	 */
	public RankPairManager getPreSubManager(int length) {
		RankPairManager result = new RankPairManager(id);
		for (int index = 0; index < length && index < rankPairList.size(); index++) {
			RankPair rankPair = rankPairList.get(index);
			result.addRankPair(rankPair.id, rankPair.baseRank, rankPair.contrastRank);
		}
		
		return result;
	}

	/**
	 * Return a copy of sub-list from index size() - length to size() - 1 of the rank pair listed in the current manager
	 * We copy the rank pair in the list, so change the rank of the copy will be not change the rank 
	 * pair in the original (i.e. the current) manager 
	 */
	public RankPairManager getPostSubManager(int length) {
		RankPairManager result = new RankPairManager(id);
		int startIndex = rankPairList.size() - length;
		if (startIndex < 0) startIndex = 0;
		
		for (int index = startIndex; index < rankPairList.size(); index++) {
			RankPair rankPair = rankPairList.get(index);
			result.addRankPair(rankPair.id, rankPair.baseRank, rankPair.contrastRank);
		}
		
		return result;
	}
	
	/**
	 * Test if the list of rank pair is a standard rank pair list. A standard list satisfy:
	 * (1) the base rank is between 1 and size(), and in ascending order
	 * (2) every contrast rank is between 1 and size()
	 * However, two id strings may have the same base ranks or contrast ranks 
	 */
	public boolean isStandard() {

		int formRank = 1;
		int maxRank = rankPairList.size();
		for (int index = 0; index < rankPairList.size(); index++) {
			RankPair rankPair = rankPairList.get(index);
			if (rankPair.baseRank < formRank || rankPair.baseRank > maxRank) return false;
			if (rankPair.contrastRank < 0 || rankPair.contrastRank > maxRank) return false;
			
			formRank = rankPair.baseRank;
		}
		return true;
	}
	
	/**
	 * Standardize the rank pair list in the manager to satisfy the conditions before the method isStandard(), i.e. 
	 * after standardizing, call its isStandard() will always return true. 
	 */
	public void standardize() {
		if (isStandard() == true) return;
		
		// Sort the rank by base rank in ascending order
		sortByBaseRank(true);
		// Set the base rank to from 1 to size() (i.e equal to its index in the list!
		for (int index = 0; index < rankPairList.size(); index++) {
			RankPair rankPair = rankPairList.get(index);
			rankPair.baseRank = index + 1;
		}
		
		// Set the contrast rank to between 1 and size(), and remains their original rank order! 
		int nextRank = 1;
		int size = rankPairList.size();
		while (nextRank <= size) {
			int contIndex = 0;
			while (rankPairList.get(contIndex).contrastRank < nextRank && contIndex < size) contIndex = contIndex + 1;
			if (contIndex > size) break;
			
			int minIndex = contIndex;
			for (int index = contIndex + 1; index < size; index++) {
				int contRank = rankPairList.get(index).contrastRank ;
				if ( contRank >= nextRank && contRank < rankPairList.get(minIndex).contrastRank) minIndex = index;
			}
			
			RankPair rankPair = rankPairList.get(minIndex);
			rankPair.contrastRank = nextRank;
			nextRank = nextRank + 1;
		}
	}
	
	public double getSpearmanCoefficient() {
		return CorrelationDistribution.getSpearmanCoefficient(this);
	}
}


class RankPair {
	protected String id = null;
	protected int baseRank = 0;
	protected int contrastRank = 0;
	
	public RankPair(String id, int baseRank, int contrastRank) {
		this.id = id;
		this.baseRank = baseRank;
		this.contrastRank = contrastRank;
	}
}

class RankPairComparator implements Comparator<RankPair> {
	private boolean isBase = true;
	private boolean isAscending = true;
	
	public RankPairComparator(boolean isBase, boolean isAscending) {
		this.isBase = isBase;
		this.isAscending = isAscending;
	}

	@Override
	public int compare(RankPair one, RankPair two) {
		if (isBase == true) {
			if (isAscending == true) return (one.baseRank - two.baseRank);
			else return (two.baseRank - one.baseRank);
		} else {
			if (isAscending == true) return (one.contrastRank - two.contrastRank);
			else return (two.contrastRank - one.contrastRank);
		}
	}
}

