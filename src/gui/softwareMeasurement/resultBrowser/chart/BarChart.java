package gui.softwareMeasurement.resultBrowser.chart;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;



public class BarChart extends Chart {
	
	List<ClassMeasureData> datas;
	JScrollPane pane;
	private boolean isSorted = false;
	
	public BarChart(final String s,final List<ClassMeasureData> datas) {
		super(s);
		
		copyData(datas);// 初始化类属性 datas
		createAndShowChart();
		RefineryUtilities.centerFrameOnScreen(this);
		
		final JMenuItem mntmSort = new JMenuItem("排序");
		mntmSort.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("排序")) {
					mntmSort.setText("恢复");
					mntmSort.setActionCommand("恢复");
					isSorted = true;
				} else {
					copyData(datas);
					mntmSort.setText("排序");
					mntmSort.setActionCommand("排序");
					isSorted = false;
				}
				remove(pane);
				validate();
				createAndShowChart();
			}
		});
		menu.add(mntmSort);
	}
	
	private void copyData(List<ClassMeasureData> datas) {
		this.datas = new ArrayList<ClassMeasureData>();
		for(ClassMeasureData d : datas) {
			this.datas.add(d);
		}
	}
	
	private void createAndShowChart() {
		pane = new JScrollPane(createChartPanel());
		getContentPane().add(pane);
		pack();
	}

	private DefaultCategoryDataset createDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		if(!isSorted) {
			for(ClassMeasureData data : datas) {
				for(MeasurementValue measure : data.getMeasures()) {
					dataset.addValue(measure.value,  measure.metric, data.getClassName());
				}
			}
		}
		if(datas.get(0).measures.size() > 1 && isSorted) {
			List<MeasureDistribution> distributions = parseDistributionByClassMeasures(datas);
			for(MeasureDistribution dis : distributions) {
				Collections.sort(dis.values, new Comparator<Double>() {
					@Override
					public int compare(Double d1, Double d2) {
						if(d1 > d2) {
							return 1;	// >
						}
						if(d1 < d2) {
							return -1; 	// <
						}
						return 0; // =
					}
				});
				int count = 1;
				for(double v : dis.values) {
					dataset.addValue(v,  dis.metricStr, (count++)+"");
				}
			}
			
		}
		if(datas.get(0).measures.size() == 1 && isSorted) {
			Collections.sort(datas, new Comparator<ClassMeasureData>() {
				@Override
				public int compare(ClassMeasureData d1, ClassMeasureData d2) {
					double v1 = d1.measures.get(0).value;
					double v2 = d2.measures.get(0).value;
					if(v1 > v2) {
						return 1;	// >
					}
					if(v1 < v2) {
						return -1; 	// <
					}
					return 0; // =
					}
			});
			for(ClassMeasureData data : datas) {
				String columnKey = data.getClassName();
				for(MeasurementValue measure : data.getMeasures()) {
					dataset.addValue(measure.value,  measure.metric, columnKey);
				}
			}
		}
		return dataset;
	}

	// 创建图的面板
	private ChartPanel createChartPanel() {
		boolean showShape = true;
		if(datas.size() > 30 || (isSorted && datas.get(0).measures.size() > 1)) {
			showShape = false;
		}
		
		DefaultCategoryDataset dataset = createDataset();
		// 创建柱状图
		chart = ChartFactory.createBarChart("柱状图", "类名", "度量值", dataset,
				PlotOrientation.VERTICAL,true, true, false);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setNoDataMessage("没有选中数据");
		
		CategoryAxis category = plot.getDomainAxis();
		category.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		if(!showShape) category.setVisible(false);
		
		BarRenderer renderer = (BarRenderer)plot.getRenderer();
		renderer.setMaximumBarWidth(0.075);// 设置柱子的最大宽度
		
		ChartPanel chartpanel = new ChartPanel(chart);
		return chartpanel;
	}
	
	
	private List<MeasureDistribution> parseDistributionByClassMeasures(List<ClassMeasureData> datas) {
		ArrayList<MeasureDistribution> res = new ArrayList<MeasureDistribution>();
		// 所有传进来的数据都计算的是同样数量和类型的度量
		List<MeasurementValue> measures = datas.get(0).measures;
		for(int i = 0; i < measures.size(); i++) {
			MeasurementValue measure = measures.get(i);
			ArrayList<Double> values = new ArrayList<Double>();
			MeasureDistribution msd = new MeasureDistribution(measure.metric, values);
			res.add(msd);
		}
		
		for(ClassMeasureData data : datas) {
			for(MeasurementValue measure : data.measures) {
				List<Double> valueList = getValueList(res, measure.metric);
				valueList.add(measure.value);
			}
		}
		return res;
	}
	
	private List<Double> getValueList(List<MeasureDistribution> list, String metricId) {
		for(MeasureDistribution m : list) {
			if(m.metricStr.equals(metricId)) {
				return m.values;
			}
		}
		return null;
	}
}
