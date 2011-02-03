/*
 * Licensed under GPL:
 * http://www.gnu.org/licenses/gpl.html
 */
package de.hwbllmnn.maven;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.AttachedArtifact;

/**
 * @goal jslib-source
 * @phase package
 * 
 * @author stranger
 */
public class JSSourceArchiveMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	private static void zip(File f, ZipOutputStream out, URI parent) throws IOException {
		if (f.getName().equalsIgnoreCase(".svn")) {
			return;
		}

		if (parent == null) {
			parent = f.toURI();
		}

		String name = parent.relativize(f.getAbsoluteFile().toURI()).toString();
		if (f.isDirectory()) {
			if (!name.isEmpty()) {
				ZipEntry e = new ZipEntry(name);
				out.putNextEntry(e);
			}
			File[] fs = f.listFiles();
			if (fs != null) {
				for (File f2 : fs) {
					zip(f2, out, parent);
				}
			}
		} else {
			ZipEntry e = new ZipEntry(name);
			out.putNextEntry(e);
			InputStream is = null;
			try {
				is = new FileInputStream(f);
				if (f.getName().endsWith(".js")) {
					copy(is, out);
				}
				copy(is, out);
			} finally {
				closeQuietly(is);
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File dir = new File(project.getBasedir(), "src/main/javascript");
		ZipOutputStream out = null;
		AttachedArtifact artifact = new AttachedArtifact(project.getArtifact(), "jslib", "source",
				new DefaultArtifactHandler("jslib"));

		File target = new File(project.getBasedir(), "target");
		if (!target.exists() && !target.mkdirs()) {
			throw new MojoFailureException("Could not create target directory!");
		}
		File file = new File(project.getBasedir(), "target/" + project.getArtifactId() + "-" + project.getVersion()
				+ "-source.jslib");

		artifact.setFile(file);
		project.addAttachedArtifact(artifact);
		try {
			out = new ZipOutputStream(new FileOutputStream(file));
			zip(dir, out, dir.toURI());
		} catch (IOException e) {
			getLog().error(e);
		} finally {
			closeQuietly(out);
		}
	}

}
