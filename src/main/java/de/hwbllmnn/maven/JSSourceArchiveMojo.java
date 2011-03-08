/*
 * Licensed under GPL:
 * http://www.gnu.org/licenses/gpl.html
 */
package de.hwbllmnn.maven;

import static de.hwbllmnn.maven.JSArchiveMojo.unzip;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
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

	/**
	 * @component
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * 
	 * @component
	 */
	private ArtifactFactory artifactFactory;

	/**
	 * 
	 * @parameter expression="${localRepository}"
	 */
	private ArtifactRepository localRepository;

	private static void zip(File f, ZipOutputStream out, URI parent) throws IOException {
		if (f.getName().equalsIgnoreCase(".svn") || f.getName().equalsIgnoreCase("CVS"))
			return;

		if (parent == null)
			parent = f.toURI();

		String name = parent.relativize(f.getAbsoluteFile().toURI()).toString();
		if (f.isDirectory()) {
			if (!name.isEmpty()) {
				ZipEntry e = new ZipEntry(name);
				out.putNextEntry(e);
			}
			File[] fs = f.listFiles();
			if (fs != null) {
				for (File f2 : fs)
					zip(f2, out, parent);
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

		List<File> list = new LinkedList<File>();

		for (Object o : project.getDependencyArtifacts()) {
			Artifact a = (Artifact) o;
			if (a.getType().equals("jslib")) {
				Artifact source = artifactFactory.createArtifactWithClassifier(a.getGroupId(), a.getArtifactId(), a
						.getVersion(), "jslib", "source");
				try {
					artifactResolver.resolve(source, project.getRemoteArtifactRepositories(), localRepository);
				} catch (ArtifactResolutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ArtifactNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				list.add(source.getFile());
			}
		}

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
			for (File f : list) {
				File tmp = new File(target, f.getName());
				FileInputStream in = new FileInputStream(f);
				try {
					unzip(in, tmp);
					zip(tmp, out, tmp.getAbsoluteFile().toURI());
				} finally {
					closeQuietly(in);
				}
			}
		} catch (IOException e) {
			getLog().error(e);
		} finally {
			closeQuietly(out);
		}
	}

}
