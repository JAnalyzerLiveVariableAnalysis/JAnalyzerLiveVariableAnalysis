package gui.softwareMeasurement.resultBrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ResultFileTreePopupMenuAcListener implements ActionListener {

	Node node;
	ResultFileTree tree;
	
	public ResultFileTreePopupMenuAcListener(ResultFileTree tree, Node node) {
		this.node = node;
		this.tree = tree;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals("打开文件")) {
			if(node.file.isFile()) {
				tree.showResult(node.file);
			}
		}
		if(command.equals("删除")) {
			deleteFile(node.file);
			node.removeFromParent();
			tree.updateUI();
		}
	}
	
	private void deleteFile(File file) {
		if(file.isDirectory()) {
			File[] lists = file.listFiles();
			for(File f : lists) {
				deleteFile(f);
			}
		}
		file.delete();
	}

}
