package ui.resultBrowser.chart;

import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class HistChart extends Chart {

	MeasureDistribution data;
	
	public HistChart(String s, MeasureDistribution data) {
		super(s);
		this.data = data;
		JPanel jpanel = createDemoPanel();
		
		setContentPane(jpanel);
		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		
	}

	private IntervalXYDataset createDataset() {
		HistogramDataset histogramdataset = new HistogramDataset();
		double ad[] = new double[data.values.size()];
		int index = 0;
		for(Double value : data.values) {
			ad[index] = value;
			index ++;
		}
		if(ad.length/5 > 1) {
			histogramdataset.addSeries(data.metricStr, ad, ad.length/5);
		} else {
			histogramdataset.addSeries(data.metricStr, ad, 1);
		}
		return histogramdataset;
	}

	private static JFreeChart createChart(IntervalXYDataset intervalxydataset) {
		JFreeChart jfreechart = ChartFactory.createHistogram(
				"直方图", "度量值", "频数", intervalxydataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot xyplot = (XYPlot) jfreechart.getPlot();
		xyplot.setNoDataMessage("没有选中数据");
		xyplot.setForegroundAlpha(0.85F);
		NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
		xybarrenderer.setDrawBarOutline(true);
		return jfreechart;
	}

	public JPanel createDemoPanel() {
		chart = createChart(createDataset());
		ChartPanel chartpanel = new ChartPanel(chart);
		return chartpanel;
	}


}