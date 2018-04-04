/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.style;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.dashboard.ISubmitableDashlet;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.FeatureLayer;
import org.polymap.p4.layer.RasterLayer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LayerStyleDashlet
        extends DefaultDashlet
        implements ISubmitableDashlet {

    private static final Log log = LogFactory.getLog( LayerStyleDashlet.class );
    
    /** Inbound: */
    @Scope( P4Plugin.Scope )
    private Context<ILayer>             layer;

    @Scope( P4Plugin.Scope )
    protected Context<MapViewer<ILayer>> mainMapViewer;

    private PanelSite                   panelSite;
    
    private StyleEditor                 editor;
    
    
    public LayerStyleDashlet( PanelSite panelSite ) {
        this.panelSite = panelSite;
    }


    @Override
    public void init( DashletSite site ) {
        super.init( site );
        site.title.set( "Style" );
        //site.constraints.get().add( new MinHeightConstraint( 600, 1 ) );
    }


    @Override
    public void dispose() {
        if (editor != null) {
            editor.dispose();
            editor = null;
        }
    }


    @Override
    public boolean submit( IProgressMonitor monitor ) throws Exception {
        assert site().isDirty() && site().isValid();
        editor.store();
        return true;
    }


    @Override
    public void createContents( Composite parent ) {
        MdToolkit tk = (MdToolkit)getSite().toolkit();                    

        // default message
        parent.setLayout( FormLayoutFactory.defaults().margins( 10 ).create() );
        FormDataFactory.on( tk.createLabel( parent, 
                "Connecting data source of this layer...<br/><br/>(WMS layers do not have an editable style)", SWT.WRAP ) )
                .fill().noBottom().height( 55 );
        parent.layout( true, true );

        // FeatureLayer?
        FeatureLayer.of( layer.get() ).thenAccept( fl -> {
            UIThreadExecutor.async( () -> {
                if (fl.isPresent()) {
                    UIUtils.disposeChildren( parent );
                    try {
                        PipelineFeatureSource fs = fl.get().featureSource();
                        FeatureStyleEditorInput editorInput = new FeatureStyleEditorInput(); 
                        editorInput.styleIdentifier.set( layer.get().styleIdentifier.get() ); 
                        editorInput.featureStore.set( fs );
                        editorInput.featureType.set( fs.getSchema() );
                        mainMapViewer.ifPresent( mapViewer -> {
                            editorInput.maxExtent.set( mapViewer.maxExtent.get() );
                            editorInput.mapExtent.set( mapViewer.mapExtent.get() );
                            editorInput.mapSize.set( mapViewer.getControl().getSize() );
                        });
                        
                        editor = new FeatureStyleEditor( editorInput ) {
                            @Override
                            protected void enableSubmit( boolean enabled ) {
                                site().enableSubmit( enabled, enabled );
                            }
                        };
                        editor.createContents( parent, tk );
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                        tk.createLabel( parent, "Unable to create styler." );            
                    }
                }
                parent.getParent().getParent().layout( true, true );
            });
        })
        .exceptionally( e -> {
            log.warn( "", e );
            tk.createLabel( parent, "Unable to data from layer." );
            return null;
        });
        
        // RasterLayer?
        RasterLayer.of( layer.get() ).thenAccept( rl -> {
            UIThreadExecutor.async( () -> {
                if (rl.isPresent()) {
                    UIUtils.disposeChildren( parent );
                    try {
                        RasterStyleEditorInput editorInput = new RasterStyleEditorInput();
                        editorInput.styleIdentifier.set( layer.get().styleIdentifier.get() ); 
                        editorInput.gridCoverageReader.set( rl.get().gridCoverageReader() );
                        editorInput.gridCoverage.set( rl.get().gridCoverage() );
                        
                        editor = new RasterStyleEditor( editorInput ) {
                            @Override
                            protected void enableSubmit( boolean enabled ) {
                                //site().enableSubmit( enabled, enabled );
                                site().enableSubmit( true, true );
                                parent.getParent().getParent().layout( true, true );
                            }
                        };
                        editor.createContents( parent, tk );
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                        tk.createLabel( parent, "Unable to create styler." );            
                    }
                }
                parent.getParent().getParent().layout( true, true );
            });
        })
        .exceptionally( e -> {
            log.warn( "", e );
            tk.createLabel( parent, "Unable to data from layer." );
            return null;
        });

    }
    
}
