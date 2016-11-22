package gui.softwareMeasurement.resultBrowser.chart;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

public class Chart extends ApplicationFrame {

	protected JFreeChart chart;
	protected JMenuBar menuBar = new JMenuBar();
	protected JMenu menu;
	
	final static String FILE_STR = " 文件 ";
	final static String SAVE_STR = "保存为...";
	final static String JPG_FILE_STR = "JPG文件";
	final static String PNG_FILE_STR = "PNG文件";
	
	public Chart(String title) {
		super(title);
		initMenuBar();
	}
	
	public void windowClosing(final WindowEvent event) {
        if (event.getWindow() == this) {
            dispose();
        }
    }
	

	protected void initMenuBar() {
		setJMenuBar(menuBar);
		
		menu = new JMenu(FILE_STR);
		menuBar.add(menu);
		
		JMenu menu_1 = new JMenu(SAVE_STR);
		menu.add(menu_1);
		
		JMenuItem mntmJpg = new JMenuItem(JPG_FILE_STR);
		menu_1.add(mntmJpg);
		
		JMenuItem mntmPng = new JMenuItem(PNG_FILE_STR);
		menu_1.add(mntmPng);
		
		ActionListenerImpl ali = new ActionListenerImpl();
		mntmJpg.addActionListener(ali);
		mntmPng.addActionListener(ali);
	}
	
	class ActionListenerImpl implements ActionListener {
		File fRes;
		boolean isJPG = true;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			isJPG = true;
			FileNameExtensionFilter filter = null;
			if(e.getActionCommand().equals(JPG_FILE_STR)) {
				filter = new FileNameExtensionFilter(
						"*.jpg", "jpg");
			}
			if(e.getActionCommand().equals(PNG_FILE_STR)) {
				filter = new FileNameExtensionFilter(
						"*.png", "png");
				isJPG = false;
			}
			chooser.setFileFilter(filter);
			int res = chooser.showSaveDialog(Chart.this);
			if(res == JFileChooser.APPROVE_OPTION) {
				fRes = chooser.getSelectedFile();
				new Thread() {
					public void run() {
						if(isJPG && !fRes.getName().endsWith(".jpg")) {
							fRes = new File(fRes.getAbsolutePath() + ".jpg");
						}
						if(!isJPG && !fRes.getName().endsWith(".png")) {
							fRes = new File(fRes.getAbsolutePath() + ".png");
						}
						try {
							if(fRes.exists()) {
								int confirm = JOptionPane.showConfirmDialog(Chart.this, "文件"+fRes.getName()+"已存在，是否覆盖?", 
										"提示",JOptionPane.YES_NO_OPTION);
								if(confirm == JOptionPane.YES_OPTION) {
									fRes.delete();
									if(isJPG) {
										ChartUtilities.saveChartAsJPEG(fRes, Chart.this.chart, Chart.this.getWidth(), Chart.this.getHeight());
									}else {
										ChartUtilities.saveChartAsPNG(fRes, Chart.this.chart, Chart.this.getWidth(), Chart.this.getHeight());
									}
								}
							} else {
								if(isJPG) {
									ChartUtilities.saveChartAsJPEG(fRes, Chart.this.chart, Chart.this.getWidth(), Chart.this.getHeight());
								}else {
									ChartUtilities.saveChartAsPNG(fRes, Chart.this.chart, Chart.this.getWidth(), Chart.this.getHeight());
								}
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}.start();
			}
		}
		
	}
	
	
}
