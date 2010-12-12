/*
 * Licensed under GPL:
 * http://www.gnu.org/licenses/gpl.html
 */
package de.hwbllmnn.maven;

import static org.codehaus.plexus.util.FileUtils.copyFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * This goal is used to assemble the artifacts of the submodules into the target/dist directory.
 * 
 * @goal dist
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 28721 $, $Date: 2010-12-10 15:23:52 +0100 (Fri, 10 Dec 2010) $
 */
public class DistMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * If set to true, default artifacts will not be copied.
	 * 
	 * @parameter default-value="false"
	 * @required
	 */
	private boolean includeOnlyAttachedArtifacts;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		File basedir = project.getBasedir();
		File target = new File(basedir, "target/dist");

		if (!target.isDirectory() && !target.mkdirs()) {
			log.warn("Could not create target directory: " + target);
		}

		List<Artifact> artifacts = new LinkedList<Artifact>();
		for (Object o : project.getCollectedProjects()) {
			MavenProject module = (MavenProject) o;
			List<?> arts = module.getAttachedArtifacts();
			for (Object obj : arts) {
				artifacts.add((Artifact) obj);
			}
			if (!includeOnlyAttachedArtifacts) {
				artifacts.add(module.getArtifact());
			}
		}

		log.info("Collected artifacts: " + artifacts);

		for (Artifact a : artifacts) {
			File file = a.getFile();
			try {
				copyFile(file, new File(target, file.getName()));
				log.info("Copied artifact " + file.getName());
			} catch (IOException e) {
				log.warn("Could not copy artifact: " + file);
			}
		}
	}

}
