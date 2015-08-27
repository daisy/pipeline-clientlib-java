package org.daisy.pipeline.client.persistence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ContextTempDirImpl implements Context { // TODO: rename to JobContextTempDirImpl
	
	File contextDir;
	
	Map<File,String> filePaths = new HashMap<File,String>();
	
	public ContextTempDirImpl() throws IOException {
		contextDir = Files.createTempDirectory("dp2client").toFile();
	}

	@Override
	public void removeFile(File file) {
		filePaths.remove(file);
	}

	@Override
	public void addFile(File file, String contextPath) {
		filePaths.put(file, contextPath);
	}

	@Override
	public String getPath(File file) {
		return filePaths.get(file);
	}
	
	@Override
	public File getFile(String contextPath) {
		assert(contextPath != null);
		for (File file : filePaths.keySet()) {
			if (filePaths.get(file).equals(contextPath)) {
				return file;
			}
		}
		return null;
	}

	@Override
	public boolean exists(String contextPath) {
		assert(contextPath != null);
		String dirPath = contextPath + (contextPath.endsWith("/") ? "" : "/");
		for (File file : filePaths.keySet()) {
			if (filePaths.get(file).startsWith(dirPath) || filePaths.get(file).equals(contextPath)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isFile(String contextPath) {
		File file = getFile(contextPath);
		return file != null && file.isFile();
	}

	@Override
	public boolean isDirectory(String contextPath) {
		assert(contextPath != null);
		String dirPath = contextPath + (contextPath.endsWith("/") ? "" : "/");
		for (File file : filePaths.keySet()) {
			if (filePaths.get(file).startsWith(dirPath)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public File getZip() {
		File zip;
		try {
			zip = Files.createTempFile("dp2client", ".zip").toFile();
			org.daisy.pipeline.client.utils.Files.zip(contextDir, zip);
			return zip;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}