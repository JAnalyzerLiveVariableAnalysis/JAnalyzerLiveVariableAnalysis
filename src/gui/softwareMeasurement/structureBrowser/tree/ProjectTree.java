package gui.softwareMeasurement.structureBrowser.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import gui.softwareMeasurement.mainFrame.ExpandableTitle;
import gui.softwareMeasurement.mainFrame.SrcTabbedPane;
import gui.softwareMeasurement.mainFrame.StatusBar;
import gui.softwareMeasurement.structureBrowser.ProjectTreeManager;
import softwareMeasurement.measure.SoftwareMeasureIdentifier;

public class ProjectTree extends JTree implements TitleObserver{

	ProjectTreeManager manager;
	StatusBar statusBar;

	/**展示少量规模度量信息**/
	private JTable mTable;
	ExpandableTitle title = new ExpandableTitle();
	JScrollPane metricInfo;
	
	private ProjectTreeNode lastNode = null;
	public static boolean canFresh = true;
	public static boolean isBuilding = false;
	JPanel pane = null;
	

	private HashSet<TreePath> flagSet = new HashSet<TreePath>();

	public ProjectTree(ProjectTreeManager manager) {
		super();
		this.manager = manager;
		init();
	}

	public ProjectTree(TreeNode root, ProjectTreeManager manager) {
		super(root);
		this.manager = manager;
		init();
	}

	public ProjectTree(TreeModel model, ProjectTreeManager manager) {
		super(model);
		this.manager = manager;
		init();
	}

	private void init() {
		setToggleClickCount(100);
		setRootVisible(false);
		setShowsRootHandles(true);
		title.setExpandedObserver(this);
		
		mTable = new JTable(){
			public String getToolTipText(MouseEvent event){
				Point point=event.getPoint();
				int column = columnAtPoint(point);
				int row = rowAtPoint(point);
				Object value = getValueAt(row, column);
				if(value != null){
					String metricId = (String)value ;
					String description = SoftwareMeasureIdentifier.getDescriptionOfMeasure(metricId);
					return description;
				}
				return null;
			}
		};
		mTable.setShowGrid(false);
		
		addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object o = getLastSelectedPathComponent();
				if(o == null) {// 删除一个项目节点之后会出现被选中的节点为空的情况，
						// 也可以同时以此来作为没有项目的情况的处理
					pane.setVisible(false);
				}
				if(o instanceof ProjectTreeNode) {
					final ProjectTreeNode n = (ProjectTreeNode)o;
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							getMetricData(n);
							if(pane == null) {
								initInfoPanel();
								pane.setVisible(true);
								metricInfo.setViewportView(pane);
							} else {
								pane.setVisible(true);
								pane.repaint();
							}
						}
					});
				}
			}
		});
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F5 && canFresh) {
					manager.updateTree();
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			int x;
			int y;

			public void mousePressed(MouseEvent e) {
				x = e.getX();
				y = e.getY();
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					setSelectionPath(path);
				}
			}

			public void mouseClicked(MouseEvent e) {
				SrcTabbedPane stabbedPane = (SrcTabbedPane) manager
						.getTextTabPane();
				TreePath path = getPathForLocation(x, y);
				if (path != null) {
					Object o = path.getLastPathComponent();
					if (o instanceof ProjectTreeNode) {
						ProjectTreeNode node = (ProjectTreeNode) o;
						lastNode = node;
						if (e.getClickCount() == 2) {
							if (node.NODE_KIND == NodeKind.CLASS_NODE) {
								final ClassNode classNode = (ClassNode) node;
								stabbedPane.showSrc(classNode.filePath);
								System.out.println("打开的文件的路径：" + classNode.filePath);
							} else {
								if (isExpanded(path)) {
									collapsePath(path);
								} else {
									expandPath(path);
								}
								if (node.NODE_KIND == NodeKind.METHOD_NODE
										|| node.NODE_KIND == NodeKind.FIELD_NODE) {
									String locationStr = node.getLocation();
									String row_col = locationStr.substring(0, locationStr.indexOf('@'));
									String rowStr = row_col.substring(0, row_col.indexOf(':'));
									int row = Integer.valueOf(rowStr);
									ClassNode classNode = (ClassNode) node.getParent();
									stabbedPane.showSelectedMethod(row,	node.getSimpleName(), classNode.filePath);
								}
							}
						}
					}
				}
			}

			public void mouseReleased(MouseEvent e) {
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					setSelectionPath(path);
				}
				ProjectTreePopupMenu popup = new ProjectTreePopupMenu(manager);
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				} else {
					popup.setVisible(false);
				}
			}
			
		});
	}
	/**
	 * 根据当前结构浏览器中被选中的节点来更新度量列表信息，并且更新ExpandableTitle中的
	 * Label,使之对应正确的节点。
	 * @param node
	 */
	private void getMetricData(final ProjectTreeNode node) {
		if(node != lastNode && node != null) {
			if(title != null) {
				title.setText(node.getSimpleName() + "的度量信息(您可以在菜单中计算更详尽的度量信息):");
			}
			
			String[] ids = node.getSizeMetricIdentifier();
			String[] values = node.getSizeMetricValue();
			int rowCount = ids.length;
			
			DefaultTableModel model = new DefaultTableModel(ids.length, 2){
				public boolean isCellEditable(int row, int col) {
					return false;
				}
			};
			for(int i = 0; i < rowCount; i++) {
				model.setValueAt(ids[i], i, 0);
				model.setValueAt(values[i], i, 1);
			}

			mTable.setModel(model);
//			TableColumn firstColumn = mTable.getColumnModel().getColumn(0);
//			firstColumn.setPreferredWidth(mTable.getWidth()/3);
//			firstColumn.setMinWidth(mTable.getWidth()/5);
//			firstColumn.setMaxWidth(mTable.getWidth()/2);
		}
	}
	
	/**
	 * 将ExpandableTitle和展示度量的Table合并为一个JPanel
	 * @return JPanel，当中包含了一个可展开的行，展开之后显示一个列表，
	 * 这个列表中包含了少量的规模度量信息
	 */
	private void initInfoPanel() {
		pane = new JPanel();
		pane.setLayout(null);
		int pHeight = 0, pWidth = 0;
		pWidth = metricInfo.getWidth();

		FontMetrics fm = getFontMetrics(getFont());
		
		int titleHeight = fm.getHeight();
		pHeight = titleHeight *(mTable.getRowCount()+1) + titleHeight + 8;
		pane.setPreferredSize(new Dimension(pWidth, pHeight));
		pane.setBackground(Color.white);
		// ExpandableTitle的宽度如果不够，会导致当中的JLabel被挤压到可见的面板之外
		// 因此如果见不到text，则很有可能是宽度设置过小(宽度设置中不加30或者更小些，
		// 可能出现看不到label中的文字)
		title.setBounds(0, 0, pWidth, titleHeight + 6);
		// 而Table的高度不够时，也会导致部分信息无法看到(如：高度设置中不+1，在本机测试显示出来之后10行数据，而实际有12行)
		mTable.setBounds(20, title.getHeight(), metricInfo.getWidth(), titleHeight * (mTable.getRowCount()+1));
		
		if(!title.isExpanded()) {
			mTable.setVisible(false);
		} else {
			mTable.setVisible(true);
		}
		pane.add(title);
		pane.add(mTable);
	}

	public void deleteNode(ProjectTreeNode node) {
		node.removeFromParent();// 删除项目
		String projectId = node.getSimpleName();
		File file = new File(".info/" + projectId);
		if (file.exists()) {
			file.delete();
		}
	}
	

	/**
	 * 根据传入的节点找到节点所在的版本节点
	 * 
	 * @param node
	 * @return
	 */
	private ProjectTreeNode findVersionNode(ProjectTreeNode node) {
		// 找到了根结点之上都没找到，说明没有传入版本信息
		if (node == null)
			return null;
		// 找到了项目节点都没找到版本节点，也说明没有传入版本信息
		if (node.NODE_KIND == NodeKind.PROJECT_NODE)
			return null;

		if (node.NODE_KIND == NodeKind.VERSION_NODE) {
			return node;
		} else {
			return findVersionNode((ProjectTreeNode) node.getParent());
		}
	}

	/**
	 * 根据传入的节点找到其所在的项目节点
	 * 
	 * @param node
	 * @return
	 */
	private ProjectTreeNode findProjectNode(ProjectTreeNode node) {
		if (node.NODE_KIND == NodeKind.PROJECT_NODE) {
			return node;
		} else {
			return findProjectNode((ProjectTreeNode) node.getParent());
		}
	}

	/**
	 * 加载xml文件中的树的信息
	 */
	public void load() {
		SAXReader reader = new SAXReader();
		Document document;
		File infoDir = new File(".info");
		File[] infoFiles = infoDir.listFiles();
		ArrayList<Element> projectEles = new ArrayList<Element>();
		FileInputStream fis = null;
		if (infoFiles != null) {
			for (File file : infoFiles) {
				try {
					// 不要用read(file)来获取document,否则异常抛出时会锁住文件导致文件无法删除
					fis = new FileInputStream(file);
					document = reader.read(fis);
					Element root = document.getRootElement();
					Element projectEle = root.element("project");// 项目节点相关标签
					projectEles.add(projectEle);
				} catch (DocumentException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "项目" + file.getName()
							+ "的信息损坏，将会丢失项目信息！");
					try {
						// 关闭流，删除文件
						fis.close();
						file.delete();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		if (canFresh) {
			LoadTreeWorker worker = new LoadTreeWorker(this, projectEles);
			worker.execute();// 载入项目
		}
	}

	/**
	 * 将ProjectTree的节点信息以xml文件保存起来
	 */
	public synchronized void save(String projectId) {
		try {
			File proFile = new File(".info/" + projectId);
			if (!proFile.isFile()) {
				proFile.createNewFile();
			}
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("utf8");

			FileOutputStream fos = new FileOutputStream(proFile);
			XMLWriter xmlWriter = new XMLWriter(fos, format);

			ProjectTreeNode proNode = manager
					.getProjectNodeByProjectName(projectId);
			Document document = DocumentHelper.createDocument();
			Element proRoot = document.addElement("root");
			createProjectEle(proNode, proRoot);

			xmlWriter.write(document);
			xmlWriter.flush();
			xmlWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 树的节点信息解析为xml标签
	 * 
	 * @param projectNodes
	 * @param root
	 */
	private void createProjectEle(ProjectTreeNode projectNode, Element root) {
		boolean hasVersion = false;
		Element project = root.addElement("project");
		project.addAttribute("label", projectNode.label);
		project.addAttribute("metrics", projectNode.sizeMetrics);
		if (projectNode.children() != null
				&& projectNode.children().hasMoreElements()) {
			// 如果项目节点下面有版本节点，设置标志
			ProjectTreeNode n = (ProjectTreeNode) projectNode.children()
					.nextElement();
			if (n.NODE_KIND == NodeKind.VERSION_NODE) {
				hasVersion = true;
			}
		}
		if (hasVersion)// 有版本节点，加载
			createVersionEle(projectNode.children(), project);
		else
			// 没有版本节点，则直接加载项目下的内容
			createPackageEle(projectNode.children(), project);
	}

	/**
	 * 将树的版本节点解析为xml标签
	 * 
	 * @param versionNodes
	 * @param projectEle
	 */
	private void createVersionEle(Enumeration<ProjectTreeNode> versionNodes,
			Element projectEle) {
		while (versionNodes.hasMoreElements()) {
			Element version = projectEle.addElement("version");
			VersionNode node = (VersionNode) versionNodes.nextElement();
			version.addAttribute("label", node.label);
			version.addAttribute("metrics", node.sizeMetrics);
			createPackageEle(node.children(), version);
		}
	}

	/**
	 * 将树的包节点解析为xml标签
	 * 
	 * @param packageNodes
	 * @param versionEle
	 */
	private void createPackageEle(Enumeration<ProjectTreeNode> packageNodes,
			Element versionEle) {
		while (packageNodes.hasMoreElements()) {
			Element packageEle = versionEle.addElement("package");
			PackageNode node = (PackageNode) packageNodes.nextElement();
			packageEle.addAttribute("label", node.label);
			packageEle.addAttribute("metrics", node.sizeMetrics);
			createClassEle(node.children(), packageEle);
		}
	}

	/**
	 * 将树的类节点解析为xml标签
	 * 
	 * @param classNodes
	 * @param packageEle
	 */
	private void createClassEle(Enumeration<ProjectTreeNode> classNodes,
			Element packageEle) {
		while (classNodes.hasMoreElements()) {
			Element classEle = packageEle.addElement("class");
			ClassNode node = (ClassNode) classNodes.nextElement();
			classEle.addAttribute("label", node.label);
			classEle.addAttribute("path", node.filePath);
			classEle.addAttribute("metrics", node.sizeMetrics);
			createFieldAndMethodEle(node.children(), classEle);
		}
	}

	/**
	 * 将树的属性节点以及方法节点解析为xml标签
	 * 
	 * @param fieldAndMethoNodes
	 * @param classEle
	 */
	private void createFieldAndMethodEle(
			Enumeration<ProjectTreeNode> fieldAndMethoNodes, Element classEle) {
		while (fieldAndMethoNodes.hasMoreElements()) {
			ProjectTreeNode node = fieldAndMethoNodes.nextElement();
			if (node.NODE_KIND == NodeKind.FIELD_NODE) {
				Element fieldEle = classEle.addElement("field");
				FieldNode fieldNode = (FieldNode) node;
				fieldEle.addAttribute("label", fieldNode.label);
				fieldEle.addAttribute("type", fieldNode.type);
				fieldEle.addAttribute("metrics", fieldNode.sizeMetrics);
			}
			if (node.NODE_KIND == NodeKind.METHOD_NODE) {
				Element methodEle = classEle.addElement("method");
				MethodNode methodNode = (MethodNode) node;
				methodEle.addAttribute("label", methodNode.label);
				if (methodNode.params != null) {
					for (String paramStr : methodNode.params) {
						Element param = methodEle.addElement("param");
						param.addAttribute("type", paramStr);
					}
				}
				methodEle.addAttribute("metrics", node.sizeMetrics);
			}
		}
	}

	public void setStatusBar(StatusBar bar) {
		this.statusBar = bar;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}
	
	public JTable getMetricTable() {
		return mTable;
	}

	/**
	 * ExpandableTitle展开和收起会触发这个方法
	 */
	@Override
	public void update(boolean isExpanded) {
		if(isExpanded) {
			mTable.setVisible(true);
		} else {
			mTable.setVisible(false);
		}
	}
	
	public void findAndSelectNodeByNodeName(String name) {
		Object root = getModel().getRoot();
		TreePath treePath = new TreePath(root);
		treePath = findInPath(treePath, name);
		if (treePath != null) {
			if(!flagSet.contains(treePath)) {
				flagSet.add(treePath);
				setSelectionPath(treePath);
				scrollPathToVisible(treePath);
			}
		} else {
			if(flagSet.isEmpty()) {
				JOptionPane.showMessageDialog(null, "未搜索到相关数据！");
			} else {
				flagSet.clear();
			}
		}
	}
	
	private TreePath findInPath(TreePath treePath, String text) {
		Object object = treePath.getLastPathComponent();
		if (object == null) {
			return null;
		}

		String str = text.toLowerCase();
		String value = object.toString().toLowerCase();
		if(object instanceof ProjectTreeNode) {
			ProjectTreeNode node = (ProjectTreeNode)object;
			int index = -1;
			if(node.getNodeKind() == NodeKind.FIELD_NODE) {
				// 字段的展示字符串中是以   变量名:类型  的格式展示
				// 搜索时不搜索其类型
				index = value.indexOf(':');
			}
			if(node.getNodeKind() == NodeKind.METHOD_NODE) {
				// 方法的展示字符串中是以   方法名(参数类型列表)  的格式展示
				// 搜索时不考虑其参数
				index = value.indexOf('(');
			}
			if(index != -1) {
				value = value.substring(0, index);
			}
		}
		if (value.contains(str) && !flagSet.contains(treePath)) {
			return treePath;
		} else {
			TreeModel model = getModel();
			int n = model.getChildCount(object);
			for (int i = 0; i < n; i++) {
				Object child = model.getChild(object, i);
				TreePath path = treePath.pathByAddingChild(child);
				path = findInPath(path, str);
				if (path != null && !flagSet.contains(path)) {
					return path;
				}
			}
			return null;
		}
	}

	public void setMetricArea(JScrollPane infoScroll) {
		metricInfo = infoScroll;
		metricInfo.addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				int pHeight = 0, pWidth = 0;
				pWidth = metricInfo.getWidth();
				FontMetrics fm = getFontMetrics(getFont());
				int titleHeight = fm.getHeight();
				pHeight = titleHeight *(mTable.getRowCount()+1) + titleHeight + 8;
				if(pane != null) {
					pane.setPreferredSize(new Dimension(pWidth, pHeight));
				}
				// ExpandableTitle的宽度如果不够，会导致当中的JLabel被挤压到可见的面板之外
				// 因此如果见不到text，则很有可能是宽度设置过小(宽度设置中不加30或者更小些，
				// 可能出现看不到label中的文字)
				if(title != null) {
					title.setBounds(0, 0, pWidth, titleHeight + 6);
					if(mTable != null) {
						// 而Table的高度不够时，也会导致部分信息无法看到(如：高度设置中不+1，在本机测试显示出来之后10行数据，而实际有12行)
						mTable.setBounds(20, title.getHeight(), metricInfo.getWidth(), titleHeight * (mTable.getRowCount()+1));
					}
				}
			}
			
		});
	}
	
	public JScrollPane getMetricArea() {
		return metricInfo;
	}

}
