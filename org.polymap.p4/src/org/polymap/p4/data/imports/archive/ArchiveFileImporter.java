/* 
 * polymap.org
 * Copyright (C) 2015, the @autors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.data.imports.archive;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.List;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.ImportTempDir;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.imports.utils.CharSetSelection;
import org.polymap.p4.imports.utils.CharsetPromptBuilder;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ArchiveFileImporter
        implements Importer {

    private static Log log = LogFactory.getLog( ArchiveFileImporter.class );

    protected ImporterSite          site;
    
    @ContextIn
    protected File                  file;
    
    @ContextOut
    protected List<File>            result;
    
    private CharSetSelection        charSetSelection = new CharSetSelection();
    
    protected Exception             exception;
    

    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings("hiding") ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "file-multiple.svg", NORMAL24 ) );
        site.summary.set( "Archive: " + file.getName() );
        site.description.set( "A archive file contains other files. Click to import files from within the archive." );
        site.terminal.set( false );
    }

    
    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // charset prompt
        site.newPrompt( "charset" )
                .summary.put( "Filename encoding" )
                .description.put( "The encoding of the filenames. If unsure use UTF8." )
                .value.put( "UTF8" )
                .severity.put( Severity.VERIFY )
                .extendedUI.put( new CharsetPromptBuilder( charSetSelection ));
    }
    
    
    @Override
    public void verify( IProgressMonitor monitor ) {
        try {
            // testing long running operation :)
            Thread.sleep( 0 );
            
            result = new ArchiveReader()
                    .targetDir.put( ImportTempDir.create() )
                    .charset.put( charSetSelection.getSelected() )
                    .run( file, monitor );
            
            exception = null;;
            site.ok.set( true );
        }
        catch (Exception e) {
            exception = e;
            site.ok.set( false );
        }
    }    

    
    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        if (result == null) {
            tk.createFlowText( parent,
                    "\nUnable to read the data.\n\n" +
                    "**Reason**: " + exception.getLocalizedMessage() );            
        }
        else {
            org.eclipse.swt.widgets.List list = tk.createList( parent, SWT.V_SCROLL, SWT.H_SCROLL );
            result.stream().sorted().forEach( f -> list.add( f.getName() ) );
        }
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // everything is done by verify()
    }
}
