package ui.structureBrowser;

import java.io.File;
import java.util.List;

public class ProjectInformation {

	private String rootPath;
	private String projectName;

	public ProjectInformation(String rootPath, String projectName) {
		this.rootPath = rootPath;
		this.projectName = projectName;
	}

	public String getLinkedDirPath() {
		return rootPath;
	}

	public String getProjectName() {
		return projectName;
	}

}
