package analyzer.correlation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.math3.stat.StatUtils;

import util.Debug;

/**
 * @author Zhou Xiaocong
 * @since 2014/1/23
 * @version 1.0
 */
public class TestCorrelationAnalyzer {
	public static final double COMPARING_MINMAL_VALUE = 10E-10;


	public static void main(String[] args) {
		String rootPath = "E:\\";
		
		String systemPath = rootPath + "ZxcTools\\jEdit\\"; 
/*		String[] versionPaths = {"jEdit(3.0)", "jEdit(3.1)", "jEdit(3.2)", "jEdit(4.0)", "jEdit(4.0.2)", "jEdit(4.0.3)", 
				"jEdit(4.1)", "jEdit(4.2)", "jEdit(4.3)", "jEdit(4.3.3)", "jEdit(4.4.1)",  "jEdit(4.4.2)", "jEdit(4.5.0)", 
				"jEdit(4.5.1)", "jEdit(4.5.2)", "jEdit(5.0.0)", "jEdit(5.1.0)",
		};
*/		
		String[] versionPaths = {"jEdit(3.0)", "jEdit(3.2)", "jEdit(4.0)",  
				"jEdit(4.1)", "jEdit(4.2)", "jEdit(4.3)", "jEdit(4.4.1)", "jEdit(5.1.0)",
		};

		PrintWriter output = null;
		String result = rootPath + "ZxcWork\\ProgramAnalysis\\data\\result.txt";
		
		PrintWriter report = null;
		try {
			String info = rootPath + "ZxcWork\\ProgramAnalysis\\data\\debug.txt";
			output = new PrintWriter(new FileOutputStream(new File(info)));
			Debug.setWriter(output);
			
			report = new PrintWriter(new FileWriter(new File(result))); 
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	
//		double prob = CorrelationDistribution.spearmanRightTailProbability(20, 0.8);
//		System.out.println("Prob(20, X > 0.3) <= " + prob);
		
		testCalculateSpearmanCoefficent(rootPath, systemPath, versionPaths, report);
		
		report.close();
	}
	
	public static void testCalculateSpearmanCoefficent(String rootPath, String systemPath, String[] versionPaths, PrintWriter report) {
		int minValue = 50;
//		int distance = 1;
		int length = 20;
		int versionNumber = versionPaths.length;
		int dataNumber = (versionNumber - 1) * versionNumber;
//		int dataNumber = ((versionNumber - distance + 1) * (versionNumber - distance))/2;
		double[] allCoef = new double[dataNumber];
		double[] preCoef = new double[dataNumber];
		double[] postCoef = new double[dataNumber];
		
		report.println("Versions\tSize\tAll Ranks\tProb <=\tFirst " + length + " Ranks\tProb <=\tLast " + length + " Ranks\tProb <=");
		
		int dataIndex = 0;
		for (int firstIndex = 0; firstIndex < versionPaths.length; firstIndex++) {
//			if (firstIndex != 0) continue;
			
			for (int secondIndex = 0; secondIndex < versionPaths.length; secondIndex++) {
				if (secondIndex == firstIndex) continue;
				
//				if (secondIndex != 1) continue;
				
				try {
					String valueFile = rootPath + "ZxcWork\\ProgramAnalysis\\data\\jEdit\\" + versionPaths[firstIndex] + "vs" + versionPaths[secondIndex] + "Length.txt";

					System.out.println("Calculate " + versionPaths[firstIndex] + " with " + versionPaths[secondIndex]);
					String id = versionPaths[firstIndex] + " vs " + versionPaths[secondIndex];
					ValuePairManager manager = new ValuePairManager(id);
					manager.read(valueFile, true);
					manager = manager.getSubManager(minValue);
					
					int size = manager.size();
					
					double allCoefficient = manager.getSpearmanCoefficient();
					double allProb = CorrelationDistribution.spearmanRightTailProbability(length, allCoefficient);
					
					ValuePairManager preManager = manager.getPreSubManager(length);
					double preCoefficient = preManager.getSpearmanCoefficient();
					double preProb = CorrelationDistribution.spearmanRightTailProbability(length, preCoefficient);
					
					ValuePairManager postManager = manager.getPostSubManager(length);
					double postCoefficient = postManager.getSpearmanCoefficient();
					double postProb = CorrelationDistribution.spearmanRightTailProbability(length, postCoefficient);
					
					if (dataIndex < dataNumber) {
						allCoef[dataIndex] = allCoefficient;
						preCoef[dataIndex] = preCoefficient;
						postCoef[dataIndex] = postCoefficient;
						dataIndex = dataIndex + 1;
					}
					report.println(id + "\t" + size + "\t" + allCoefficient + "\t" + allProb + "\t" + preCoefficient + "\t" + preProb + "\t" + postCoefficient + "\t" + postProb);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
		
		String reportString = "Total " + dataNumber + " minimal: \t" + StatUtils.min(allCoef) + "\t\t" + StatUtils.min(preCoef) + "\t\t" + StatUtils.min(postCoef);
		report.println(reportString);
		
		reportString = "Total " + dataNumber + " maximal: \t" + StatUtils.max(allCoef) + "\t\t" + StatUtils.max(preCoef) + "\t\t" + StatUtils.max(postCoef);
		report.println(reportString);
		
		reportString = "Total " + dataNumber + " means: \t" + StatUtils.mean(allCoef) + "\t\t" + StatUtils.mean(preCoef) + "\t\t" + StatUtils.mean(postCoef);
		report.println(reportString);
		
		reportString = "Total " + dataNumber + " variance: \t" + StatUtils.variance(allCoef) + "\t\t" + StatUtils.variance(preCoef) + "\t\t" + StatUtils.variance(postCoef);
		report.println(reportString);
		
	}
}
