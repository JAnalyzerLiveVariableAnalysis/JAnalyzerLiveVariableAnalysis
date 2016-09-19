package ui.structureBrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ui.structureBrowser.tree.NodeKind;
import ui.structureBrowser.tree.ProjectTreeNode;
import ui.structureBrowser.tree.VersionNode;

public class CloseVersionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private ProjectTreeManager manager;
	private VersionChoosenPane choosePane;
	private ScrollComboBox<String> comboBox;
	
	public CloseVersionDialog(ProjectTreeManager manager) {
		this.manager = manager;
		setTitle("选择要关闭的版本");
		setSize(450, 300);
		setResizable(false);
		setLocationRelativeTo(null);
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		init();
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		
		ButtonHandler handler = new ButtonHandler();
		okButton.addActionListener(handler);
		cancelButton.addActionListener(handler);
		
	}
	
	private void init() {
		JLabel label = new JLabel("选择项目:");
		int fontSize = label.getFont().getSize();
		label.setBounds(50,30, fontSize*8,fontSize + 5);
		contentPanel.add(label);
		List<ProjectTreeNode> proNodes = manager.getAllProjectNodes();
		comboBox = new ScrollComboBox<String>();
		comboBox.setBounds(label.getX() + label.getWidth()
 				, 30, 250, comboBox.getFont().getSize() + 5);
		contentPanel.add(comboBox);
		for(ProjectTreeNode node : proNodes) {
			comboBox.addItem(node.getSimpleName());
		}
		ProjectTreeNode node = manager.getProjectNodeByProjectName((String)comboBox.getSelectedItem());
		List<File> versions = parseVersion(node);
		choosePane = new VersionChoosenPane(versions);
		choosePane.setBounds((450-350)/2, 80, 350, 150);
		contentPanel.add(choosePane);
		
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					ProjectTreeNode node = manager.getProjectNodeByProjectName((String)comboBox.getSelectedItem());
					List<File> versions = parseVersion(node);
					if(choosePane != null) {
						contentPanel.remove(choosePane);
					}
					choosePane = new VersionChoosenPane(versions);
					choosePane.setBounds((450-350)/2, 80, 350, 150);
					contentPanel.add(choosePane);
				}
			}
		});
	}
	
	
	
	/**
	 * 根据项目节点获取节点下的版本信息
	 * @param node
	 * @return
	 */
	private List<File> parseVersion(ProjectTreeNode node) {
		if(node.getNodeKind() == NodeKind.PROJECT_NODE) {
			List<File> res = new ArrayList<File>();
			Enumeration<ProjectTreeNode> children = node.children();
			while(children.hasMoreElements()) {
				ProjectTreeNode child = children.nextElement();
				if(child.getNodeKind() == NodeKind.VERSION_NODE) {
					VersionNode n = (VersionNode)child;
					File file = new File(n.getVersionDir());
					res.add(file);
				}
			}
			return res;
		}
		return null;
	}
	
	
	class ButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if("OK".equals(command)) {
				List<File> versions = choosePane.getSelectedVersion();
				if(versions != null) {
					final String proName = (String)comboBox.getSelectedItem();
					ProjectTreeNode node = manager.getProjectNodeByProjectName(proName);
					for(File file : versions) {
						Enumeration<ProjectTreeNode> versionNodes = node.children();
						while(versionNodes.hasMoreElements()) {
							VersionNode versionNode = (VersionNode)versionNodes.nextElement();
							if(versionNode.getVersionDir().equals(file.getAbsolutePath())){
								versionNode.removeFromParent();
								break;
							}
						}
					}
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							manager.getTree().updateUI();
							manager.saveProject(proName);
						}
					});
				}
			}
			dispose();
		}
		
	}
}
