/*
 * Licensed under GPL:
 * http://www.gnu.org/licenses/gpl.html
 */
package de.hwbllmnn.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to set up eclipse workspaces to my liking.
 * 
 * @goal setup-workspace
 * @aggregator
 * @requiresDirectInvocation
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class EclipseWorkspaceMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String workspace = System.getProperty("eclipse.workspace");
        if (workspace == null) {
            throw new MojoFailureException("You need to specify the eclipse workspace to modify "
                    + "using -Declipse.workspace=<workspace-directory>");
        }

        File dir = new File(workspace, ".metadata/.plugins/");

        try {
            classicTheme(dir);
            editorSettings(dir);
        } catch (Exception e) {
            throw new MojoFailureException(e.getLocalizedMessage(), e);
        }
    }

    private void classicTheme(File dir) throws FileNotFoundException {
        File file = new File(dir, "org.eclipse.core.runtime/.settings");
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        file = new File(file, "org.eclipse.e4.ui.css.swt.theme.prefs");
        PrintStream out = new PrintStream(new FileOutputStream(file));
        out.println("eclipse.preferences.version=1");
        out.println("themeid=org.eclipse.e4.ui.css.theme.e4_classic");
        out.close();
    }

    private void editorSettings(File dir) throws IOException {
        File file = new File(dir, "org.eclipse.core.runtime/.settings");
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        file = new File(file, "org.eclipse.ui.editors.prefs");
        PrintStream out = new PrintStream(new FileOutputStream(file));
        out.println("AbstractTextEditor.Navigation.SmartHomeEnd=false");
        out.println("eclipse.preferences.version=1");
        out.println("lineNumberRuler=true");
        out.println("spacesForTabs=true");
        out.close();
        file = new File(dir, "org.eclipse.core.runtime/.settings");
        file = new File(file, "org.eclipse.jdt.ui.prefs");
        InputStream in = EclipseWorkspaceMojo.class.getResourceAsStream("/eclipse/org.eclipse.jdt.ui.prefs");
        OutputStream os = new FileOutputStream(file);
        IOUtils.copy(in, os);
        in.close();
        out.close();
    }

}
