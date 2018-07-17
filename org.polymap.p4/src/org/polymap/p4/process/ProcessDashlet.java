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
package org.polymap.p4.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;

import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.data.process.Modules;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.ActionText;
import org.polymap.rhei.batik.toolkit.ClearTextAction;
import org.polymap.rhei.batik.toolkit.TextActionItem;
import org.polymap.rhei.batik.toolkit.TextActionItem.Type;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.FunctionalLabelProvider;
import org.polymap.rhei.batik.toolkit.md.ListTreeContentProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.RasterLayer;

/**
 * Shows a list of available processing modules ({@link JGTModel}). Lets the user
 * choose one to process in {@link ProcessModulePanel}.
 *
 * @author Falko Bräutigam
 */
public class ProcessDashlet
        extends DefaultDashlet {

    private static final Log log = LogFactory.getLog( ProcessDashlet.class );
    
    /** Inbound: */
    @Scope( P4Plugin.Scope )
    private Context<ILayer>         layer;
    
    /** Outbound: for {@link ProcessModulePanel} */
    @Scope( P4Plugin.Scope )
    private Context<BackgroundJob>  bgjob;
    
    private PanelSite               panelSite;

    private MdToolkit               tk;

    private MdListViewer            list;

    private ActionText              searchText;
    
    
    public ProcessDashlet( PanelSite panelSite ) {
        this.panelSite = panelSite;
    }

    @Override
    public void init( DashletSite site ) {
        super.init( site );
        site.title.set( "Processing" );
        this.tk = (MdToolkit)getSite().toolkit();                    
        //site.constraints.get().add( new MinHeightConstraint( 600, 1 ) );
    }


    @Override
    public void createContents( Composite parent ) {
        // default message
        parent.setLayout( FormLayoutFactory.defaults().margins( 8, 0 ).spacing( 0 ).create() );
        FormDataFactory.on( tk.createLabel( parent, 
                "Connecting data source of this layer...<br/>(Feature/Vector processing is not supported yet)", SWT.WRAP ) )
                .fill().noBottom().height( 45 );
        parent.layout( true, true );

        // RasterLayer?
        RasterLayer.of( layer.get() ).thenAccept( rl -> {
            UIThreadExecutor.async( () -> {
                if (rl.isPresent()) {
                    UIUtils.disposeChildren( parent );
                    createSearchText( parent );
                    createModuleList( parent );
                    
                    FormDataFactory.on( searchText.getControl() ).fill().noBottom();
                    FormDataFactory.on( list.getControl() ).fill().top( searchText.getControl() ).height( 400 );
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
    
    
    protected void createSearchText( Composite parent ) {
        searchText = tk.createActionText( parent, "" )
                .performOnEnter.put( false )
                .textHint.put( "Search..." );
        new TextActionItem( searchText, Type.DEFAULT )
                .action.put( ev -> filterList( searchText.getTextText() ) )
                .tooltip.put( "Search in name and description of the modules" )
                .icon.put( BatikPlugin.images().svgImage( "magnify.svg", SvgImageRegistryHelper.DISABLED12 ) );
        new ClearTextAction( searchText );
        //searchText.getText().forceFocus();
    }

    
    protected void filterList( String pattern ) {
        ViewerFilter filter = new ViewerFilter() {
            @Override
            public boolean select( Viewer viewer, Object parent, Object elm ) {
                ModuleInfo info = (ModuleInfo)elm;
                return info.name().startsWith( pattern )
                        || info.label().startsWith( pattern )
                        || info.description().map( v -> v.contains( pattern ) ).orElse( false );
            }
        };
        list.setFilters( new ViewerFilter[] {filter} );
        //list.refresh();
    }

    
    protected void createModuleList( Composite parent ) {
        list = tk.createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        // first line
        list.firstLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
            ModuleInfo info = (ModuleInfo)cell.getElement();
            cell.setText( info.label() );            
        }));
        // second line
        list.secondLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
            ModuleInfo info = (ModuleInfo)cell.getElement();
            cell.setText( info.description().orElse( "..." ) );            
        }));
        // icon
        list.iconProvider.set( FunctionalLabelProvider.of( cell -> 
            cell.setImage( P4Plugin.images().svgImage( "play-circle-outline.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) ) ) );
        // arrow right
        list.firstSecondaryActionProvider.set( new ActionProvider() {
            @Override
            public void update( ViewerCell cell ) {
                cell.setImage( P4Plugin.images().svgImage( "chevron-right.svg", SvgImageRegistryHelper.NORMAL24 ) );
            }
            @Override
            public void perform( MdListViewer viewer, Object elm ) {
            }
        });
        list.addOpenListener( ev -> {
            ModuleInfo moduleInfo = (ModuleInfo)((IStructuredSelection)list.getSelection()).getFirstElement();
            bgjob.set( new BackgroundJob( moduleInfo, layer.get() ) );
            BatikApplication.instance().getContext().openPanel( panelSite.path(), ProcessModulePanel.ID );
        });
        list.setContentProvider( new ListTreeContentProvider() );
        list.setComparator( new ViewerComparator() {
            @Override
            public int compare( Viewer viewer, Object elm1, Object elm2 ) {
                return ((ModuleInfo)elm1).label().compareTo( ((ModuleInfo)elm2).label() );
            }
        });
        list.setInput( Modules.instance().rasterExecutables() );
    }
    
}
