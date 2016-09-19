package softwareChange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * To be used to test if two nodes in different versions correspond to same definition according to an external indicator file, which gives 
 * the node label contrast table, i.e. different labels in different versions of a node. 
 * @author Zhou Xiaocong
 * @since 2014/1/21
 * @version 1.0
 */
public class NodeChangeIndicator {
	// The external indicator file stored the node label contrast table
	protected String indicatorFile = null;		
	// Since there may be many versions, we just buffer a node label list of a base version and a contrast version
	protected List<String> base = null;			
	protected List<String> contrast = null;
	
	protected String baseId = null;
	protected String contrastId = null;
	
	public NodeChangeIndicator(String indicatorFile) {
		this.indicatorFile = indicatorFile;
	}
	
	/**
	 * Load node label list for base version. 
	 * @param baseId : give the id of the base version, which should contains specialized char sequences saved in the 
	 * head line of the indicator file, such as "3.0.1" etc.
	 */
	public void loadBase(String baseId) throws IOException {
		if (indicatorFile == null) return;
		if (baseId.equals(this.baseId) && base != null) return;
		
		base = load(baseId);
		this.baseId = baseId;
	}
	
	/**
	 * Load node label list for contrast version. 
	 * @param contrastId : give the id of the base version, which should contains specialized char sequences saved in the 
	 * head line of the indicator file, such as "3.0.1" etc.
	 */
	public void loadContrast(String contrastId) throws IOException {
		if (indicatorFile == null) return;
		if (contrastId.equals(this.contrastId) && contrast != null) return;
		
		contrast = load(contrastId);
		this.contrastId = contrastId;
	}
	
	
	
	
	/**
	 * Generate a report of matched ratio of base and contrast  
	 */
	public void generateMatchedRatioReport(PrintWriter report) throws IOException {
		int baseTotal = 0;
		int baseMatched = 0;
		int contTotal = 0;
		int contMatched = 0;
		
		for (int index = 0; index < base.size() && index < contrast.size(); index++) {
			String baseLabel = base.get(index);
			String contLabel = contrast.get(index);
			
			boolean baseNonEmpty = baseLabel.contains("@");
			boolean contNonEmpty = contLabel.contains("@");
			if (baseNonEmpty) {
				baseTotal = baseTotal + 1;
				if (contNonEmpty) baseMatched = baseMatched + 1;
			}
			if (contNonEmpty) {
				contTotal = contTotal + 1;
				if (baseNonEmpty) contMatched = contMatched + 1;
			}
		}
		
		String reportString = "\tTotal\tMatched\tRatio";
		report.println(reportString);
		double baseRatio = (baseTotal == 0) ? 0 : (double)baseMatched / baseTotal;
		double contRatio = (contTotal == 0) ? 0 : (double)contMatched / contTotal;
		
		reportString = baseId + "\t" + baseTotal + "\t" + baseMatched + "\t" + baseRatio;
		report.println(reportString);
		
		reportString = contrastId + "\t" + contTotal + "\t" + contMatched + "\t" + contRatio;
		report.println(reportString);
		
		report.flush();
	}
	
	
	/**
	 * Write the change indicator to the indicatorFile. Generally, this method is used to write the automatically generated indicator.
	 * If the list of base and contrast are load from a file, do not write them to the file.
	 */
	public void write() throws IOException {
		if (baseId == null || contrastId == null) return;
		
		if (base.size() != contrast.size()) throw new AssertionError("The size of base list and contrast list is different!");

		PrintWriter report = new PrintWriter(new FileWriter(new File(indicatorFile)));
		String reportString = baseId + "\t" + contrastId;
		report.println(reportString);
		
		for (int index = 0; index < base.size() && index < contrast.size(); index++) {
			reportString = base.get(index) + "\t" + contrast.get(index);
			report.println(reportString);
		}
		report.close();
	}
	
	
	/**
	 * Use information in the external indicator file to test if two nodes given by their label can be binded to same definition
	 */
	public boolean canBindToSameDefinition(String baseNodeLabel, String contrastNodeLabel) {
		int baseIndex = -1;
		
		for (int index = 0; index < base.size(); index++) {
			String label = base.get(index);
			if (label.equals(baseNodeLabel)) {
				baseIndex = index;
				break; 
			}
		}
		if (baseIndex == -1) return false;
		if (baseIndex >= contrast.size()) return false;
		if (contrastNodeLabel.equals(contrast.get(baseIndex))) return true;
		else return false;
	}

	/**
	 * Get corresponding contrast label for the give base node label
	 */
	public String getContrastNodeLabel(String baseNodeLabel) {
		int baseIndex = -1;
		
		for (int index = 0; index < base.size(); index++) {
			String label = base.get(index);
			if (label.equals(baseNodeLabel)) {
				baseIndex = index;
				break; 
			}
		}
		if (baseIndex == -1) return "";
		return contrast.get(baseIndex).trim();
	}

	/**
	 * Get corresponding base label for the give contrast node label
	 */
	public String getBaseNodeLabel(String contrastNodeLabel) {
		int contrastIndex = -1;
		
		for (int index = 0; index < contrast.size(); index++) {
			String label = contrast.get(index);
			if (label.equals(contrastNodeLabel)) {
				contrastIndex = index;
				break; 
			}
		}
		if (contrastIndex == -1) return "";
		return base.get(contrastIndex).trim();
	}
	
	private List<String> load(String versionId) throws IOException {
		final String splitter = "\t";
		
		List<String> result = new ArrayList<String>();
		
		Scanner scanner = new Scanner(new File(indicatorFile));
		
		int versionIndex = -1;
		if (scanner.hasNextLine()) {
			// The first line of the file give the versions of the system. Version ids are splitted by '\t'
			String line = scanner.nextLine();
			String[] versionIds = line.split(splitter);
			
			for (int index = 0; index < versionIds.length; index++) {
				if (versionIds[index].contains(versionId) || versionId.contains(versionIds[index])) {
					versionIndex = index;
					break;
				}
			}
			
			if (versionIndex < 0) {
				scanner.close();
				throw new AssertionError("Can not find [" + versionId + "] in line [" + line + "]");
			}
		} else {
			scanner.close();
			throw new AssertionError("Can not read head line of the indicator file [" + indicatorFile + "]");
		}
		
		while (scanner.hasNextLine()) {
			// Each line give the different labels of the same node in different versions, which are splitted by '\t'
			// and versionIndex gives the label of the node in the version given by versionId 
			String line = scanner.nextLine();
			String[] labels = line.split(splitter);
			
			if (versionIndex < labels.length) result.add(labels[versionIndex]);
			else result.add("");
		}
		scanner.close();
		return result;
	}
}
