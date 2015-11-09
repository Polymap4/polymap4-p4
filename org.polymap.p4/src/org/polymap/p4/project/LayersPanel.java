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
package org.polymap.p4.project;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ui.ProjectNodeContentProvider;
import org.polymap.core.project.ui.ProjectNodeLabelProvider;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.CheckboxActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayersPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LayersPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layers" );

    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<IMap>             map;
    
    /** Set before opening {@link LayerInfoPanel}. */
    @Scope(P4Plugin.Scope)
    protected Context<ILayer>           layer;
    
    private MdListViewer                viewer;
    
    /** The {@link MapViewer} of the parent panel ({@link ProjectMapPanel}). */
    private MapViewer<ILayer>           mapViewer;

    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectMapPanel )
                .map( parent -> {
                    getSite().setIcon( P4Plugin.images().svgImage( "layers.svg", NORMAL24 ) );
                    getSite().setTitle( "" );
                    getSite().setPreferredWidth( 200 );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void init() {
        mapViewer = ((ProjectMapPanel)parentPanel().get()).mapViewer;
    }


    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().create() );
        
        viewer = ((MdToolkit)getSite().toolkit()).createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        viewer.setContentProvider( new ProjectNodeContentProvider() );
        viewer.firstLineLabelProvider.set( new ProjectNodeLabelProvider() );
        
        viewer.iconProvider.set( new CellLabelProvider() {
            private Map<Object,Image> legendGraphics = new HashMap();
            
            @Override
            public void update( ViewerCell cell ) {
                ILayer layer = (ILayer)cell.getElement();
                cell.setImage( legendGraphics.containsKey( layer.id() )
                        ? legendGraphics.get( layer.id() )
                        : P4Plugin.images().svgImage( "layers.svg", NORMAL24 ) );
                
//                new UIJob( "Legend graphic" ) {
//                    @Override
//                    protected void runWithException( IProgressMonitor monitor ) throws Exception {
//                        Thread.sleep( 3000 );
//                        UIThreadExecutor.async( () -> {
//                            legendGraphics.put( layer.id(), P4Plugin.instance().imageForName( "resources/icons/map.png" ) );
//                            viewer.update( layer, null );
//                        }, UIThreadExecutor.runtimeException() );
//                    }
//                }.scheduleWithUIUpdate();
            }
        });
        
        viewer.firstSecondaryActionProvider.set( new CheckboxActionProvider() {
            @Override
            protected boolean initSelection( MdListViewer _viewer, Object elm ) {
                return mapViewer.isVisible( (ILayer)elm );
            }
            @Override
            protected void onSelectionChange( MdListViewer _viewer, Object elm ) {
                mapViewer.setVisible( (ILayer)elm, isSelected( elm ) );
            }
        });
        viewer.thirdSecondaryActionProvider.set( new LayerMoveUpActionProvider(mapViewer));
        
        viewer.addOpenListener( new IOpenListener() {
            @Override
            public void open( OpenEvent ev ) {
                SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                    layer.set( (ILayer)elm );
                    getContext().openPanel( getSite().getPath(), LayerInfoPanel.ID );                        
                });
            }
        } );

        viewer.setInput( map.get() );

        // avoid empty rows and lines
        viewer.getTree().setLayoutData( FormDataFactory.filled().noBottom().create() );
    }

}
