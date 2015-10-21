/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.data.imports.shapefile;

import java.util.List;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterSite;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShpImporter
        implements Importer {

    private static Log log = LogFactory.getLog( ShpImporter.class );
    
    private ImporterSite            site;

    @ContextIn
    protected List<File>            files;

    @ContextIn
    protected File                  shp;

    
    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings("hiding") ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;
        site.icon.set( P4Plugin.images().svgImage( "shp.svg", SvgImageRegistryHelper.NORMAL24 ) );
        site.summary.set( "Shapefile: " + shp.getName() );
        site.description.set( "A Shapefile is a common file format which contains features of the same type." );
        site.terminal.set( true );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        tk.createLabel( parent, "Files: " + files );
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}