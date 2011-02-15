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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * @goal jslib
 * @phase package
 * 
 * @author stranger
 */
public class JSArchiveMojo extends AbstractMojo {

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
			Writer rout = null;
			try {
				is = new FileInputStream(f);
				if (f.getName().endsWith(".js")) {
					JavaScriptCompressor yui = new JavaScriptCompressor(new InputStreamReader(is, "UTF-8"), null);
					rout = new OutputStreamWriter(out, "UTF-8");
					yui.compress(rout, -1, true, false, false, false);
					rout.flush();
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
		Artifact artifact = project.getArtifact();

		List<File> list = new LinkedList<File>();

		for (Object o : project.getDependencyArtifacts()) {
			Artifact a = (Artifact) o;
			if (a.getType().equals("jslib")) {
				list.add(a.getFile());
			}
		}

		File target = new File(project.getBasedir(), "target");
		if (!target.exists() && !target.mkdirs()) {
			throw new MojoFailureException("Could not create target directory!");
		}
		File file = new File(project.getBasedir(), "target/" + project.getArtifactId() + "-" + project.getVersion()
				+ ".jslib");

		artifact.setFile(file);
		try {
			out = new ZipOutputStream(new FileOutputStream(file));
			zip(dir, out, dir.toURI());
			for (File f : list) {
				zip(f, out, f.toURI());
			}
		} catch (IOException e) {
			getLog().error(e);
		} finally {
			closeQuietly(out);
		}
	}

}
