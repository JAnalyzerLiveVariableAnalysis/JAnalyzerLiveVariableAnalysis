package gui.softwareMeasurement.resultBrowser.chart;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

public class LineChart extends Chart {

	List<ClassMeasureData> datas;
	JPanel jpanel;
	int seriesNum;
	boolean isSorted = false;
	
	public LineChart(final String s,final List<ClassMeasureData> datas) {
		super(s);
		this.datas = datas;
		
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
				remove(jpanel);
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
		jpanel = createDemoPanel();
		setContentPane(jpanel);
		pack();		
	}

	private CategoryDataset createDataset() {
		
		seriesNum = 0;

		DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
		if(datas.get(0).measures.size() > 1 && isSorted) {//要显示的度量数量超过1种
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
				seriesNum = 0;
				for(double v : dis.values) {
					seriesNum++;
					defaultcategorydataset.addValue(v,  dis.metricStr, seriesNum+"");
				}
			}	
		}
		if(datas.get(0).measures.size() == 1 && isSorted) {
			Collections.sort(LineChart.this.datas, new Comparator<ClassMeasureData>() {
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
					seriesNum ++;
					defaultcategorydataset.addValue(measure.value,  measure.metric, columnKey);
				}
			}
		}
		if(!isSorted) {
			for(ClassMeasureData data : datas) {
				String columnKey = data.getClassName();
				for(MeasurementValue measure : data.getMeasures()) {
					seriesNum ++;
					defaultcategorydataset.addValue(measure.value,  measure.metric, columnKey);
				}
			}
		}
		return defaultcategorydataset;
	}
	
	private JFreeChart createChart(CategoryDataset categorydataset) {
		boolean showShape = true;
		if(datas.size() > 30 || (isSorted && datas.get(0).measures.size() > 1)) {
			showShape = false;
		}
		JFreeChart jfreechart = ChartFactory.createLineChart(
				"线图", "类", "度量值", categorydataset,
				PlotOrientation.VERTICAL, true, true, false);
		CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
		categoryplot.setNoDataMessage("没有选中数据");
		CategoryAxis categoryAxis = categoryplot.getDomainAxis();
		categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		if(!showShape) categoryAxis.setVisible(false);
		
		NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
				.getRenderer();
		for(int i=0; i < seriesNum; i++) {
			lineandshaperenderer.setSeriesShapesVisible(i, showShape);
		}
		lineandshaperenderer
				.setSeriesShape(2, ShapeUtilities.createDiamond(4F));
		lineandshaperenderer.setDrawOutlines(true);
		lineandshaperenderer.setUseFillPaint(true);
		lineandshaperenderer.setBaseFillPaint(Color.white);
		return jfreechart;
	}

	public JPanel createDemoPanel() {
		chart = createChart(createDataset());
		return new ChartPanel(chart);
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
