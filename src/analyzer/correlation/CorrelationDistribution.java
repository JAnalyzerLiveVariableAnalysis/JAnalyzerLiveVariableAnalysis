package analyzer.correlation;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * Provide some static methods to calculate Spearman coefficient of rank correlation
 * @author Zhou Xiaocong
 * @since 2014/1/24/
 * @version 1.0
 */
public class CorrelationDistribution {

	private final static String tableFile = "E:\\ZxcWork\\ProgramAnalysis\\data\\SpearmanCoefficient.txt";
	private static double[][] table = null;
	private final static int nBegin = 7;
	private final static int nEnd = 30;
	private final static int probNumber = 6;

	/**
	 * Calculate Spearman Coefficient of ranks (1, 2, ..., ranks.length) with ranks  (ranks[0], ranks[1], ... ranks[ranks.length-1])
	 * There is no any tie in the given ranks (i.e. the integer array ranks), i.e. the ranks are different from each other
	 */
	public static double getSpearmanCoefficient(int[] ranks) {
		int n = ranks.length;
		double denominator = (double)(n * (n * n - 1));
		
		double squareSum = 0;
		for (int i = 0; i < n; i++) {
			squareSum += (ranks[i] - (i+1)) * (ranks[i] - (i+1));
		}
		double result = 1.0 - (6.0 * squareSum)/denominator;
		
		return result;
	}
	
	/**
	 * Calculate Spearman Coefficient of ranks (one[0], one[1], ..., one[n-1]) with ranks  (two[0], two[1], ..., two[n-1]), where 
	 * n = min(one.length, two.length)
	 * There is no any tie in the given ranks, i.e. the ranks are different from each other
	 */
	public static double getSpearmanCoefficient(int[] one, int[] two) {
		int n = Math.min(one.length, two.length);
		double squareSum = 0;
		for (int i = 0; i < n; i++) {
			squareSum += (one[i] - two[i]) * (one[i] - two[i]);
		}
		double result = 1 - (6 * squareSum)/(n * n * n - n);
		return result;
	}
	
	/**
	 * Calculate Spearman Coefficient of base ranks and contrast ranks in a rank pair manager 
	 * There is no any tie in the given ranks, i.e. the ranks are different from each other
	 */
	public static double getSpearmanCoefficient(RankPairManager manager) {
		List<RankPair> rankPairList = manager.getRankPairList();
		
		int n = rankPairList.size();
		double squareSum = 0;
		for (int i = 0; i < n; i++) {
			int baseRank = rankPairList.get(i).baseRank;
			int contrastRank = rankPairList.get(i).contrastRank;
			
			squareSum += (baseRank - contrastRank) * (baseRank - contrastRank);
		}
		double result = 1 - (6 * squareSum)/(n * n * n - n);
		
		return result;
	}
	
	/**
	 * Calculate Spearman Coefficient of base ranks and contrast ranks in a rank pair manager 
	 * We call the methods in apache commons-math3-3.2.jar to do calculation
	 */
	public static double getSpearmanCoefficientUsingMath3(RankPairManager manager) {
		List<RankPair> rankPairList = manager.getRankPairList();
		
		double result = 0;
		
		double[] baseRank = new double[rankPairList.size()];
		double[] contrastRank = new double[rankPairList.size()];
		
		for (int index = 0; index < rankPairList.size(); index++) {
			baseRank[index] = rankPairList.get(index).baseRank;
			contrastRank[index] = rankPairList.get(index).contrastRank;
		}
		
		SpearmansCorrelation calculator = new SpearmansCorrelation();
		result = calculator.correlation(baseRank, contrastRank);
		return result;
	}

	/**
	 * Calculate Spearman Coefficient of base values and contrast values in a value pair manager 
	 * We call the methods in apache commons-math3-3.2.jar to do calculation
	 */
	public static double getSpearmanCoefficientUsingMath3(ValuePairManager manager) {
		List<ValuePair> rankPairList = manager.getValuePairList();
		
		double result = 0;
		
		double[] baseValue = new double[rankPairList.size()];
		double[] contrastValue = new double[rankPairList.size()];
		
		for (int index = 0; index < rankPairList.size(); index++) {
			baseValue[index] = rankPairList.get(index).baseValue;
			contrastValue[index] = rankPairList.get(index).contrastValue;
		}
		
		SpearmansCorrelation calculator = new SpearmansCorrelation();
		result = calculator.correlation(baseValue, contrastValue);
		return result;
	}

	/**
	 * Calculate Spearman Coefficient of two double arrays 
	 * We call the methods in apache commons-math3-3.2.jar to do calculation
	 */
	public static double getSpearmanCoefficientUsingMath3(double[] one, double[] two) {
		double result = 0;
		
		SpearmansCorrelation calculator = new SpearmansCorrelation();
		result = calculator.correlation(one, two);
		return result;
	}
	
	/**
	 * Calculate the right tail probability of a spearman coefficient, i.e. P(X >= coef). The sample number (i.e. n) must be greater than 2.
	 * <p>If (n >= 2 && n < 6), we give the accuracy value of the probability by generate all permutations;
	 * <p>If (n >= 7 && n <= 30), we use a table to give the estimated value of the probability, the accuracy value is less than or equal to the return value.
	 * <p>If (n > 30), we use Normal Distribution to give the estimated value of the probability.
	 */
	public static double spearmanRightTailProbability(int n, double coef) {
		if (n < 2) throw new AssertionError("The sample number n (= " + n + ") is too small!");
		if (n < 6) return spearmanRTPLessThanSix(n, coef);
		if (n >= nBegin && n <= nEnd) return spearmanRTPLessThan30(n, coef);
		
		return spearmanRTPGreaterThan30(n, coef);
	}
	
	private static boolean nextPermutation(int[] current) {
		int n = current.length;
		int j = n - 2;
		while (current[j] > current[j+1]) j = j - 1;
		int k = n - 1;
		while (current[j] > current[k]) k = k - 1;
		
		int temp = current[j];
		current[j] = current[k];
		current[k] = temp;
		
		int r = n - 1;
		int s = j + 1;
		while (r > s) {
			temp = current[r];
			current[r] = current[s];
			current[s] = temp;
			r = r - 1;
			s = s + 1;
		}
		
		for (j = 0; j < n; j++) {
			if (current[j] != (n-j)) return true;
		}
		return false;
	}
	
	private static double spearmanRTPLessThanSix(int n, double coef) {
		if (n > 6) throw new AssertionError("The sample number n (= " + n + ") is too small or too large!"); 
		
		double epsilon = 10e-10;
		
		int total = 0;
		int greater = 0;
		
		int[] permutation = new int[n];
		for (int i = 0; i < n; i++) permutation[i] = i + 1;
		total = total + 1;
		double value = getSpearmanCoefficient(permutation);
		if (Math.abs(value - coef) < epsilon || value > coef) greater = greater + 1;
		
		while (nextPermutation(permutation)) {
			total = total + 1;
			value = getSpearmanCoefficient(permutation);
			if (Math.abs(value - coef) < epsilon || value > coef) greater = greater + 1;
		}

		total = total + 1;
		value = getSpearmanCoefficient(permutation);
		if (Math.abs(value - coef) < epsilon || value > coef) greater = greater + 1;

		double result = (double)greater / total;
		return result;
	}
	
	private static double spearmanRTPLessThan30(int n, double coef) {
		if (table == null) loadSpearmanTable();
		
		int rowIndex = n - nBegin + 1;
		if (coef < table[rowIndex][0]) return table[0][0];
		
		for (int column = 0; column < probNumber-1; column++) {
			if (coef >= table[rowIndex][column] && coef < table[rowIndex][column+1]) return table[0][column];
		}
		return table[0][probNumber-1];
	}
	
	private static void loadSpearmanTable() {
		table = new double[nEnd-nBegin+2][probNumber];
		
		try {
			Scanner scanner = new Scanner(new File(tableFile));
			int lineCounter = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] lineStrings = line.split("\t");
				
				for (int index = 1; index < lineStrings.length; index++) {
					table[lineCounter][index-1] = Double.parseDouble(lineStrings[index]);
				}
				
				lineCounter = lineCounter + 1;
			}
			scanner.close();
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new AssertionError("Can not load sperman coefficient probability table!");
		}
		
	}
	
	private static double spearmanRTPGreaterThan30(int n, double coef) {
		double zStatistics = coef * Math.sqrt(n-1);
		
		NormalDistribution distribution = new NormalDistribution();
		double result = 1 - distribution.cumulativeProbability(zStatistics); 
		return result;
	}
	
}
