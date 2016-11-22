package gui.softwareMeasurement.mainFrame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gui.softwareMeasurement.codeBrowser.JSrcViewer;
import gui.softwareMeasurement.codeBrowser.LineNumberHeaderView;

public class SrcTabbedPane extends JTabbedPane {

	public SrcTabbedPane() {
		super(JTabbedPane.TOP);
		setMinimumSize(new Dimension(100, 100));

		this.addMouseMotionListener(new MouseAdapter() {
			final TabbedPaneIcon closeIcon = new TabbedPaneIcon(
					"res/img/close.png");
			final TabbedPaneIcon closeSelectedIcon = new TabbedPaneIcon(
					"res/img/close_selected.png");

			public void mouseMoved(MouseEvent e) {
				handleCloseIcon(e);
			}
			
			public void mouseDragged(MouseEvent e) {
				handleCloseIcon(e);
			}
			
			private void handleCloseIcon(MouseEvent e) {
				int tabNumber = getUI().tabForCoordinate(SrcTabbedPane.this,
						e.getX(), e.getY());
				if (tabNumber > -1
						&& ((TabbedPaneIcon) getIconAt(tabNumber)) != null) {
					Rectangle rect = ((TabbedPaneIcon) getIconAt(tabNumber))
							.getBounds();
					if (rect.contains(e.getX(), e.getY())) {
						setIconAt(tabNumber, closeSelectedIcon);
					} else {
						setIconAt(tabNumber, closeIcon);
					}
				}
			}
		});
		
		this.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				int tabNumber = getUI().tabForCoordinate(SrcTabbedPane.this,
						e.getX(), e.getY());
				if (tabNumber > -1
						&& ((TabbedPaneIcon) getIconAt(tabNumber)) != null) {
					Rectangle rect = ((TabbedPaneIcon) getIconAt(tabNumber))
							.getBounds();
					if (rect.contains(e.getX(), e.getY())) {
						remove(tabNumber);
					}
				}
			}
		});

		// 选择了另外的tab之后，在查找时要查找的文本也就不一样了
		this.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JScrollPane scrollPane = (JScrollPane) getSelectedComponent();
				// 获取状态栏
				StatusBar bar = getStatusBar();
				if (scrollPane != null) {
					JPanel panel = (JPanel) scrollPane.getViewport().getView();
					Component c =  panel.getComponent(0);
					if(c instanceof JSrcViewer) {
						JSrcViewer v = (JSrcViewer)c;
						// 修改装状态栏的行列号
						bar.setRowAndCol(v.getCaretRow(), v.getCaretCol());
						if (JSrcViewer.searcher != null) {// tab改变之后，searcher要搜索的对象也不一样了
							JSrcViewer.searcher.setSearchViewer(v);
							v.requestFocusInWindow();
						}
					} else {
						JTextArea v = (JTextArea)c;
						if (JSrcViewer.searcher != null) {// tab改变之后，searcher要搜索的对象也不一样了
							JSrcViewer.searcher.setSearchViewer(v);
							v.requestFocusInWindow();
						}
					}
				} else {// 没有打开的文件，行列号不显示
					bar.hideRowAndCol();
				}
			}
		});
		
	}

	public void addTab(String title, Component component) {
		final TabbedPaneIcon closeIcon = new TabbedPaneIcon("res/img/close.png");
		Container container = getParent();
		while( container != null && (!(container instanceof JsMetricFrame))) {
			container = container.getParent();
		}
		if(container != null) {
			JsMetricFrame frame = (JsMetricFrame)container;
			frame.mntmSearch.setEnabled(true);
			frame.mntmClose.setEnabled(true);
			frame.mntmCloseAll.setEnabled(true);
		}
		super.addTab(title, closeIcon, component);
	}

	/*
	 * 添加水平和垂直滚动条需要把JTextPane放到JPanel里，再把JPanel放到JScrollPane里。
	 * JTextPane如果没有水平滚动条的话JTextPane会自动换行，否则就不会。
	 */
	public JSrcViewer showSrc(String filePath) {
		JScrollPane scrollPane = findViewer(filePath);
		if (scrollPane == null) {
			String fileName = filePath
					.substring(filePath.lastIndexOf("\\") + 1);
			JSrcViewer v = new JSrcViewer(filePath);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(v);
			scrollPane = new JScrollPane(panel);
			JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
			if(verticalBar != null) {// 设置滚动条的滚动速度
				verticalBar.setUnitIncrement(35);
			}
			scrollPane.setRowHeaderView(new LineNumberHeaderView(v
					.getLineCount()));
			addTab(fileName, scrollPane);
			setSelectedComponent(scrollPane);
			setToolTipTextAt(getSelectedIndex(), filePath);
			v.requestFocusInWindow();
			return v;
		} else {
			setSelectedComponent(scrollPane);
			JPanel panel = (JPanel) scrollPane.getViewport().getView();
			return (JSrcViewer) panel.getComponent(0);
		}
	}

	/**
	 * 查找tabbbedPane中显示了该文件的tab,如果有则直接选中那个tab,否则新建一个tab
	 * @param filePath
	 * @return
	 */
	private JScrollPane findViewer(String filePath) {
		int tabNum = getTabCount();
		for (int i = 0; i < tabNum; i++) {
			JScrollPane scro = (JScrollPane) getComponentAt(i);
			JPanel panel = (JPanel) scro.getViewport().getView();
			// 找到容器中的JSrcViewer
			Component c = panel.getComponent(0);
			if(c != null && c instanceof JSrcViewer) {
				JSrcViewer viewer = (JSrcViewer)c;
				if (filePath.equals(viewer.getFilePath())) {
					return scro;
				}
			}
		}
		return null;
	}

	public void showSelectedMethod(int row, String methodName, String filePath) {
		JSrcViewer v = showSrc(filePath);
		v.setSelectedMethod(row, methodName);
	}

	public int getCurrentCursorRow() {
		JScrollPane scrollPane = (JScrollPane)getSelectedComponent();
		if(scrollPane != null) {
			JPanel panel = (JPanel)scrollPane.getViewport().getView();
			JSrcViewer v = (JSrcViewer)panel.getComponent(0);
			return v.getCaretRow();
		}
		return -1;
	}
	
	public int getCurrentCursorCol() {
		JScrollPane scrollPane = (JScrollPane)getSelectedComponent();
		if(scrollPane != null) {
			JPanel panel = (JPanel)scrollPane.getViewport().getView();
			JSrcViewer v = (JSrcViewer)panel.getComponent(0);
			return v.getCaretCol();
		}
		return -1;
	}
	
	
	private StatusBar getStatusBar() {
		Container container = getParent();
		while(container != null && !(container instanceof JsMetricFrame)) {
			container = container.getParent();
		}
		if(container != null) {
			JsMetricFrame mainFrame = (JsMetricFrame)container;
			return mainFrame.getStatusBar();
		}
		return null;
	}
}
