package gui.softwareMeasurement.mainFrame;

import java.awt.Container;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class Log {

	private static JTextArea logArea;
//	private static JTextPane textPane;
//	public static boolean isMetricLogging = false;
	private static JScrollPane pane;
	private static JTabbedPane tabbedPane;
	private Log() {}
	
	static void setTextArea(JTextArea area) {
		logArea = area;
		logArea.setEditable(false);
		Container c = logArea.getParent();
		pane = null;
		while(c != null && !(c instanceof JTabbedPane)) {
			if(c instanceof JScrollPane) {
				pane = (JScrollPane)c;
			}
			c = c.getParent();
		}
		if(c != null) {
			tabbedPane = (JTabbedPane)c;
		}
	}
	
	public static void requestFocus() {
		logArea.requestFocus();
	}
	
	public static void consoleLog(String info) {
//		if(!isMetricLogging && logArea != null) {
		tabbedPane.setSelectedIndex(1);
		logArea.append(info+"\r\n");
		if(logArea.getLineCount() > 300) {
			try {
				logArea.getDocument().remove(0, logArea.getLineEndOffset(0));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
//			logArea.setCaretPosition(logArea.getText().length());
			if(pane != null) {
				JScrollBar vBar = pane.getVerticalScrollBar();
				if(vBar != null) {
					vBar.setValue(vBar.getMaximum() - vBar.getModel().getExtent());
				}
			}
		}
//		}
	}

//	public static void setinfoArea(JTextPane infoTextPane) {
//		textPane = infoTextPane;
//		textPane.setEditable(false);
//	}
	
//	public static void infoLog(String info) {
//		textPane.setText(info);
//        Document docs = textPane.getDocument();//获得文本对象
//        try {
//            docs.insertString(docs.getLength(), info + "\r\n", null);//对文本进行追加
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//	}
	
}
