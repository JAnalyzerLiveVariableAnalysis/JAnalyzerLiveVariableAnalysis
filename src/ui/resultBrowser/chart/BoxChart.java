package ui.resultBrowser.chart;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class BoxChart extends Chart {

	List<MeasureDistribution> datas;
	
	public BoxChart(String s, List<MeasureDistribution> datas) {
		super(s);
		this.datas = datas;
		JPanel jpanel = createDemoPanel();
		setContentPane(jpanel);
		
		pack();
		RefineryUtilities.centerFrameOnScreen(this);
	}

	private BoxAndWhiskerCategoryDataset createDataset() {
		DefaultBoxAndWhiskerCategoryDataset defaultboxandwhiskercategorydataset = new DefaultBoxAndWhiskerCategoryDataset();
		for (MeasureDistribution data : datas) {
			ArrayList list = new ArrayList();
			for(Double value : data.values) {
				list.add(value);
			}
			defaultboxandwhiskercategorydataset.add(list, 0, data.metricStr);
		}

		return defaultboxandwhiskercategorydataset;
	}


	private JFreeChart createChart(
			BoxAndWhiskerCategoryDataset boxandwhiskercategorydataset) {
		JFreeChart jfreechart = ChartFactory.createBoxAndWhiskerChart(
				"盒图", "度量", "值",boxandwhiskercategorydataset, false);
		CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
		categoryplot.setNoDataMessage("没有选中数据");
		categoryplot.setDomainGridlinesVisible(true);
		BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer)categoryplot.getRenderer();
		renderer.setMaximumBarWidth(0.075);
		
		CategoryAxis categoryAxis = categoryplot.getDomainAxis();
		categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		
		NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		return jfreechart;
	}

	public JPanel createDemoPanel() {
		chart = createChart(createDataset());
		ChartPanel chartpanel = new ChartPanel(chart);
		return chartpanel;
	}
	
	
}
