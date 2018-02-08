/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package org.polymap.p4.layer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.FunctionalLabelProvider;
import org.polymap.rhei.batik.toolkit.md.ListTreeContentProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;

/**
 * Configuration of additional {@link PipelineProcessor}s of the render
 * {@link Pipeline} of this {@link ILayer}.
 *
 * @author Falko Bräutigam
 */
public class LayerProcessorsDashlet
        extends DefaultDashlet {

    private static final Log log = LogFactory.getLog( LayerProcessorsDashlet.class );

    protected static final IMessages    i18n = Messages.forPrefix( "LayerPipelineDashlet" );

    /** Inbound: Set before opening {@link LayerInfoPanel}. */
    @Scope(P4Plugin.Scope)
    protected Context<ILayer>           layer;

    /** Outbound: */
    @Scope(P4Plugin.Scope)
    protected Context<ProcessorExtension> selected;

    private MdToolkit                   tk;
    
    private MdListViewer                list;

    private Button                      addBtn;
    

    @Override
    public void init( DashletSite site ) {
        super.init( site );
        site.title.set( "Render pipeline" );
        site.constraints.get().add( new MinWidthConstraint( 350, 1 ) );
        tk = (MdToolkit)site().toolkit();
    }

    
    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 3 ).margins( 0, 0, 3, 0 ).create() );
        createList( parent );
        createAddButton( parent );
        
        FormDataFactory.on( list.getTree() ).fill().noBottom().height( 200 );
        FormDataFactory.on( addBtn ).top( list.getTree() ).left( 31 ).right( 69 ).bottom( 100 );
    }
    
    
    protected void createList( Composite parent ) {
        list = tk.createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        // first line
        list.firstLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
            ProcessorExtension ext = (ProcessorExtension)cell.getElement();
            cell.setText( ext.getName() );            
        }));
        // second line
        list.secondLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
            ProcessorExtension ext = (ProcessorExtension)cell.getElement();
            cell.setText( ext.getDescription().orElse( "" ) );            
        }));
        // icon
        list.iconProvider.set( FunctionalLabelProvider.of( cell -> 
            cell.setImage( P4Plugin.images().svgImage( "play-circle-outline.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) ) ) );
        // arrow right
        list.firstSecondaryActionProvider.set( new ActionProvider() {
            @Override public void update( ViewerCell cell ) {
                cell.setImage( P4Plugin.images().svgImage( "chevron-right.svg", SvgImageRegistryHelper.NORMAL24 ) );
            }
            @Override public void perform( MdListViewer viewer, Object elm ) {
            }
        });
        list.addOpenListener( ev -> {
            selected.set( (ProcessorExtension)((IStructuredSelection)list.getSelection()).getFirstElement() );
            BatikApplication.instance().getContext().openPanel( site().panelSite().getPath(), LayerProcessorPanel.ID );
        });
        list.setContentProvider( new ListTreeContentProvider() );
//        list.setComparator( new ViewerComparator() {
//            @Override
//            public int compare( Viewer viewer, Object elm1, Object elm2 ) {
//                return ((ModuleInfo)elm1).label().compareTo( ((ModuleInfo)elm2).label() );
//            }
//        });
        list.setInput( ProcessorExtension.all() );
    }

    
    protected void createAddButton( Composite parent ) {
        addBtn = tk.createButton( parent, "Add", SWT.PUSH );
        addBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
            throw new RuntimeException();
        }));
    }
    
}
