package org.daisy.pipeline.client.filestorage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.utils.XML;
import org.w3c.dom.Document;

public class JobStorage implements JobStorageInterface {

	private Job job;
	private File directory;
	private Map<String,File> contextFiles = new HashMap<String,File>();
	private boolean lazyLoaded = false;

	/**
	 * Load or create a job with the given id in the given job storage.
	 * 
	 * @param jobId job id
	 * @param jobStorage job storage
	 */
	public JobStorage(Job job, File jobStorage) {
		assert(job.getId() != null);
		directory = new File(jobStorage, job.getId());
		directory.mkdirs();
	}

	private void lazyLoad() {
		if (lazyLoaded) {
			return;
		}

		if (directory != null && directory.exists()) {
			File jobFile = new File(directory, "job.xml");
			String jobString = null;
			Document jobDocument = null;
			if (jobFile.exists()) {
				try {
					byte[] encoded = Files.readAllBytes(Paths.get(jobFile.getPath()));
					jobString = new String(encoded, Charset.defaultCharset());
					jobDocument = XML.getXml(jobString);

				} catch (IOException e) {
					Pipeline2Logger.logger().error("Unable to load job.xml: "+jobFile.getAbsolutePath(), e);
				}

				if (jobDocument != null) {
					try {
						job = new Job(jobDocument);

					} catch (Pipeline2Exception e) {
						Pipeline2Logger.logger().error("Failed to load job: "+jobFile.getAbsolutePath(), e);
					}
				}
			}
		}

		lazyLoaded = true;
	}

	@Override
	public void save() {
		save(true);
	}
	
	/**
	 * Save the job to the job storage.
	 * 
	 * By default, will move the files instead of copying them.
	 * 
	 * @param moveFiles if set to false, will make copies of the context files instead of moving them.
	 */
	public void save(boolean moveFiles) {
		lazyLoad();

		File jobFile = new File(directory, "job.xml");
		Document jobDocument = job.toXml();

		try {
			String jobRequestString = XML.toString(jobDocument);
			Files.write(jobFile.toPath(), jobRequestString.getBytes());

		} catch (IOException e) {
			Pipeline2Logger.logger().error("Unable to store XML for job", e);
		}

		if (directory != null && directory.exists()) {
			if (!contextFiles.isEmpty()) {
				File contextDir = new File(directory, "context");
				for (String contextPath : contextFiles.keySet()) {
					File file = contextFiles.get(contextPath);

					if (!file.exists()) {
						Pipeline2Logger.logger().error("File or directory does not exist and can not be added to context: '"+file.getAbsolutePath()+"'");
						continue;
					}

					File contextFile = new File(contextDir, contextPath);
					try {
						assert contextFile.getCanonicalPath().startsWith(contextDir.getCanonicalPath() + File.separator); // contextFile is inside contextDir

						if (!file.getCanonicalPath().equals(contextFile.getCanonicalPath())) {
							if (moveFiles) {
								Files.move(file.toPath(), contextFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
								
							} else {
								Files.copy(file.toPath(), contextFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.COPY_ATTRIBUTES);
							}
						}

					} catch (IOException e) {
						Pipeline2Logger.logger().error("Unable to copy from '"+file.getAbsolutePath()+"' to '"+contextFile.getAbsolutePath(), e);
					}
				}
			}
		}

	}

	/**
	 * List all job IDs that are stored in the given job storage folder.
	 * 
	 * @param jobStorage the directory where jobs are stored
	 * @return a list of all the job IDs in the job storage
	 */
	public static List<String> listJobs(File jobStorage) {
		List<String> jobs = new ArrayList<String>();
		if (jobStorage.isDirectory()) {
			for (File directory : jobStorage.listFiles()) {
				if (directory.isDirectory()) {
					String jobId = directory.getName();
					jobs.add(jobId);
				}
			}
		}
		Collections.sort(jobs);
		return jobs;
	}

	/** Deletes the job including all its files from the job storage. */
	public void delete() {
		if (directory != null && directory.exists()) {
			try {
				deleteRecursively(directory);

			} catch (IOException e) {
				Pipeline2Logger.logger().error("Unable to delete job: "+directory.getAbsolutePath(), e);
			}
		}
	}

	private void deleteRecursively(File directory) throws IOException {
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				deleteRecursively(file);
			}
		}
		Files.delete(directory.toPath());
	}

	@Override
	public void removeContextFile(String contextPath) {
		contextFiles.remove(contextPath);
	}

	@Override
	public void addContextFile(File file, String contextPath) {
		if (contextPath == null) {
			contextPath = file.getName();
		}
		
		if (file.isFile()) {
			contextFiles.put(contextPath, file);
			
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addContextFile(f, new File(contextPath, f.getName()).toString());
			}
			
		} 
	}

	@Override
	public String getContextFilePath(File file) {
		for (String contextPath : contextFiles.keySet()) {
			if (contextFiles.get(contextPath).equals(file)) {
				return contextPath;
			}
		}
		return null;
	}

	@Override
	public File getContextFile(String contextPath) {
		return contextFiles.get(contextPath);
	}

	@Override
	public boolean existsInContext(String contextPath) {
		return contextFiles.containsKey(contextPath);
	}

	@Override
	public boolean isFileInContext(String contextPath) {
		File file = getContextFile(contextPath);
		return file != null && file.isFile();
	}

	@Override
	public boolean isDirectoryInContext(String contextPath) {
		if (contextPath == null) {
			return false;
		}
		String dirPath = contextPath + (contextPath.endsWith("/") ? "" : "/");
		for (String path : contextFiles.keySet()) {
			if (path.startsWith(dirPath)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public File getContextDir() {
		return new File(directory, "context");
	}

	@Override
	public File getContextZip() {
		File zip;
		try {
			zip = Files.createTempFile("dp2client", ".zip").toFile();
			org.daisy.pipeline.client.utils.Files.zip(getContextDir(), zip);
			return zip;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
