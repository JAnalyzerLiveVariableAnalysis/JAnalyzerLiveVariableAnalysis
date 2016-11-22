package gui.softwareMeasurement.structureBrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressDialog extends JFrame {
	
	JLabel verLabel = new JLabel("提示");
	JProgressBar verProgressBar = new JProgressBar();
	
	JLabel packLabel = new JLabel("包");
	JProgressBar packProgressBar = new JProgressBar();
	JPanel contentPane = new JPanel();
	JPanel buttonPane;
	
	public ProgressDialog(final ButtonActionListener listener) {
		setMinimumSize(new Dimension(450,300));
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setTitle("创建中，请稍候...");
		contentPane.setLayout(null);
		this.add(contentPane, BorderLayout.CENTER);
		contentPane.add(verLabel);
		contentPane.add(verProgressBar);
		
		contentPane.add(packLabel);
		contentPane.add(packProgressBar);
		
		verProgressBar.setStringPainted(true);
		packProgressBar.setStringPainted(true);
		
		JButton cancelButton = new JButton("取消");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listener.cancelButtonIsDown();
				setVisible(false);
			}
		});
		
		buttonPane = new JPanel();
		buttonPane.add(cancelButton);
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		contentPane.add(buttonPane);
		
		addWindowFocusListener(new WindowFocusListener() {
			
			@Override
			public void windowLostFocus(WindowEvent e) {
				requestFocus();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
				
			}
		});
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				listener.cancelButtonIsDown();
			}
		});
//		progressBar.setMinimum(0);
//		progressBar.setMaximum(100);
//		this.add(buttonPane, BorderLayout.SOUTH);
	}
	
//	public static void main(String args[]) {
//		ProgressDialog dialg = new ProgressDialog(new ButtonActionListener() {
//			
//			@Override
//			public void cancelButtonIsDown() {
//				System.out.println("cancel");
//			}
//		});
//		dialg.setVisible(true);
//	}
	
	public void paint(Graphics g) {
		super.paint(g);
		int contentWidth = contentPane.getWidth();
		int contentHeight = contentPane.getHeight();
		int labelX = 30;
		int labelY = 30;
		verLabel.setBounds(labelX, labelY, contentWidth - labelX * 2, getFont().getSize() + 5);
		verProgressBar.setBounds(labelX, labelY + verLabel.getHeight() + 5,
				contentWidth - 2 * labelX, getFont().getSize() + 10);
		
		packLabel.setBounds(labelX, verProgressBar.getY() + verProgressBar.getHeight() + 50, 
				verLabel.getWidth(), verLabel.getHeight());
		packProgressBar.setBounds(labelX, packLabel.getY() + packLabel.getHeight() + 5,
				verProgressBar.getWidth(), verProgressBar.getHeight());

		buttonPane.setBounds(0, contentHeight - 50, contentWidth, 50);
		
		
	}
	
	
	/**
	 * 设置对话框中提示的内容
	 * @param note
	 */
	public void setVerNote(String note) {
		verLabel.setText(note);
	}
	
	public void setPackNote(String note) {
		packLabel.setText(note);
	}
	
	
	/**
	 * 设置进度条的值
	 * @param progress
	 */
	public void setVerProgress(int progress) {
		verProgressBar.setValue(progress);
	}
	
	public void setPackProgress(int progress) {
		packProgressBar.setValue(progress);
	}
	
	/**
	 * 设置进度条的最小值
	 * @param mininum
	 */
	public void setVerMinimum(int mininum) {
		verProgressBar.setMinimum(mininum);
	}
	
	public void setPackMinimum(int mininum) {
		packProgressBar.setMinimum(mininum);
	}
	
	/**
	 * 设置进度条的最大值
	 * @param maximun
	 */
	public void setVerMaximum(int maximun) {
		verProgressBar.setMaximum(maximun);
	}
	
	public void setPackMaximum(int maximun) {
		packProgressBar.setMaximum(maximun);
	}

	public void setIndeterminate(boolean b) {
		packProgressBar.setIndeterminate(b);
	}
	
	public int getVerMaximum() {
		return verProgressBar.getMaximum();
	}
	
	public int getPackMaximum() {
		return packProgressBar.getMaximum();
	}
	
	public void setVerProgressString(String text) {
		verProgressBar.setString(text);
	}
	
	public void setPackProgressString(String text) {
		packProgressBar.setString(text);
	}
}

interface ButtonActionListener {
	void cancelButtonIsDown();
}