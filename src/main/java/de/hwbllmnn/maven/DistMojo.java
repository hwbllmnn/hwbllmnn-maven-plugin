/*
 * Licensed under GPL:
 * http://www.gnu.org/licenses/gpl.html
 */
package de.hwbllmnn.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * @goal copy
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
     * @parameter
     */
    private Copy[] files;

    public void execute()
                            throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        if ( files == null ) {
            log.debug( "No files configured." );
            return;
        }

        File basedir = project.getBasedir();
        for ( Copy copy : files ) {
            log.info( "Copy " + copy.from + " to " + copy.to );
            File from = new File( basedir, copy.from );
            File to = new File( basedir, copy.to );
            if ( !to.getParentFile().mkdirs() ) {
                log.warn( "Could not create parent directories for " + to + "." );
                continue;
            }
//            try {
//                copyFile( from, to );
//            } catch ( IOException e ) {
//                log.warn( "Could not copy " + copy.from + " to " + copy.to + ": " + e.getLocalizedMessage() );
//                log.debug( e );
//            }
        }
    }

    /**
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     *
     * @version $Revision: 28721 $, $Date: 2010-12-10 15:23:52 +0100 (Fri, 10 Dec 2010) $
     */
    public static class Copy {
        /**
         * @parameter
         */
        String from;

        /**
         * @parameter
         */
        String to;

        @Override
        public String toString() {
            return from + " -> " + to;
        }
    }

}
