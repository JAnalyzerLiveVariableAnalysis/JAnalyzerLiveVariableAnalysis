package ui.resultBrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;

public class MetricMath {
	
	
	/**
	 * 均值
	 * @param datas
	 * @return
	 */
	public static Double mean(List<Double> datas) {
		double sum = 0;
		for(double data : datas) {
			sum += data;
		}
		double avg = sum / datas.size();
		return avg;
	}
	
	
	/**
	 * 方差计算
	 * @param datas 要被计算的原始数据
	 */
	public static Double variance(List<Double> datas) {
		double res = 0;
		double avg = mean(datas);
		for(double data : datas) {
			double delta = data - avg;
			res += (delta * delta);
		}
		return res/datas.size();
	}
	
	/**
	 * 中位数计算
	 * @param datas
	 * @return 中位数
	 */
	public static Double median(List<Double> datas) {
		List<Double> temp = new ArrayList<Double>();
		for(double d : datas) {
			temp.add(d);
		}
		Collections.sort(temp);
		int index = temp.size()/2;
		if(temp.size()%2 == 0) {
			return (temp.get(index) + temp.get(index - 1))/2;
		} else {
			return temp.get(index);
		}
	}
	
	/**
	 * 四分位数
	 * 排序后取n/4个位置的数
	 * @param datas
	 * @return
	 */
	public static Double littleQuartile(List<Double> datas) {
		List<Double> temp = new ArrayList<Double>();
		for(double d : datas) {
			temp.add(d);
		}
		Collections.sort(temp);
		int index = temp.size()/4;
		return temp.get(index);
	}
	
	/**
	 * 大四分位数
	 * 排序后取n*3/4个位置的数
	 * @param datas
	 * @return
	 */
	public static Double bigQuartile(List<Double> datas) {
		List<Double> temp = new ArrayList<Double>();
		for(double d : datas) {
			temp.add(d);
		}
		Collections.sort(temp);
		int index = temp.size()*3/4;
		return temp.get(index);
	}

	/**
	 * 峰度计算
	 * @param datas
	 * @return
	 */
	public static Double kurtosis(List<Double> datas) {
		Kurtosis k = new Kurtosis();
		double[] values = new double[datas.size()];
		for(int index = 0; index < datas.size(); index++) {
			values[index] = datas.get(index);
		}
		double res = k.evaluate(values);
		return res;
//		double n = datas.size();
//		double avg = mean(datas);// 平均值
//		double variance = variance(datas);
//		double sd = Math.sqrt(variance);// 标准差
//		double res = 0;
//		for(double d : datas) {
//			double delta = d - avg;
//			double tmp = delta / sd;
//			double tmpPow4 = Math.pow(tmp, 4);
//			System.out.println(tmpPow4 +"   : " + sd +  "  :" + avg);
//			res += tmpPow4;
//		}
//		System.out.println(n);
//		double d = (n*(n+1)*res)/((n-1)*(n-2)*(n-3));
//		return d - (3*(n-1)*(n-1))/((n-2)*(n-3));
	}
	
	/**
	 * 偏度计算
	 * @param datas
	 * @return
	 */
	public static Double skewness(List<Double> datas) {
		Skewness skewness = new Skewness();
		double[] dataArray = new double[datas.size()];
		for(int index = 0; index < datas.size(); index++) {
			dataArray[index] = datas.get(index);
		}
		double res = skewness.evaluate(dataArray);
		return res;
		
//		double n = datas.size();
//		double avg = mean(datas);
//		double variance = variance(datas);
//		double sd = Math.sqrt(variance);
//		double res = 0;
//		for(double d : datas) {
//			double delta = d-avg;
//			double tmp = delta / sd;
//			double tmpPow3 = Math.pow(tmp, 3);
//			System.out.println(tmpPow3);
//			res += tmpPow3;
//		}
//		return (n * res)/((n-1) * (n - 2));
	}
	
	/**
	 * 变异系数
	 * @param datas
	 * @return
	 */
	public static Double CV(List<Double> datas) {
		double sd = Math.sqrt(variance(datas));
		double avg = mean(datas);
		return sd/avg;
	}
	
	/**
	 * 基尼指数
	 * 低于0.2		数据绝对平均
	 * 0.2 ~ 0.3	数据比较平均
	 * 0.3 ~ 0.4	数据相对合理
	 * 0.4 ~ 0.5 	数据差距较大
	 * 0.5以上 		数据差距悬殊
	 * @param datas
	 * @return
	 */
	public static Double gini(List<Double> datas) {
		Collections.sort(datas);
		double sum = 0;
		for(Double d : datas) {
			sum += d;
		}
		
		double[] w = new double[datas.size()-1];
		double s = 0;
		for(int i = 0; i < datas.size()-1; i++) {
			s += datas.get(i);
			// 第一组到第i组的总数占所有总数的比例
			w[i] = s/sum;
		}
		
		double res = 0;
		for(double t : w) {
			res += t;
		}
		return 1-((2*res+1)/datas.size());
	}
	
	
	public static void main(String args[]) {
		List<Double> datas = new ArrayList<Double>();


		datas.add(2121d);
		datas.add(34d);
		datas.add(33d);
		datas.add(32d);
		datas.add(43d);
		datas.add(32d);
		datas.add(432d);
		datas.add(421d);
		datas.add(421d);
//		datas.add(2.5712619446660896);
		
//		datas.add(3323d);
//		datas.add(3132d);
//		datas.add(1332d);
//		datas.add(435d);
//		datas.add(65d);
//		datas.add(76d);
//		datas.add(676d);
//		datas.add(13.0);
//		datas.add(23.0);
//		datas.add(12.0);
//		datas.add(44.0);
//		datas.add(55.0);
//		datas.add(2.1);
//		datas.add(1.1);
//		datas.add(0.0);
//		System.out.println(median(datas));
//		System.out.println(datas);
		System.out.println("cv:" + CV(datas));
		System.out.println("s:" + skewness(datas));
		System.out.println("k:" + kurtosis(datas));
//		
//		System.out.println("g:" + gini(datas));
		
//		System.out.println(littleQuartile(datas));
//		System.out.println(bigQuartile(datas));
	}
	
	
}
