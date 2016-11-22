package gui.softwareMeasurement.metricBrowser;

/**
 * getItemName通常置于ItemChoosenPane列表中的第二列
 * getItemDescription通常置于ItemChoosenPane列表中的第三列
 * @author Wu zhangsheng
 */
public interface Item {

	// 得到item在第index列处要展示出来的字符串
	String getItemString(int index);

}
