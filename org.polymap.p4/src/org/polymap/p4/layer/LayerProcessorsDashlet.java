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

import static org.polymap.core.runtime.event.TypeEventFilter.isType;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.ProcessorConfig;
import org.polymap.core.project.ProjectNode.ProjectNodeCommittedEvent;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.BatikPlugin;
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

import org.polymap.model2.runtime.UnitOfWork;
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

    /** Inbound: */
    @Scope(P4Plugin.Scope)
    protected Context<ILayer>           layer;

    /** Outbound: */
    @Scope(P4Plugin.Scope)
    protected Context<ProcessorConfig>  selected;

    private MdToolkit                   tk;
    
    private MdListViewer                list;

    private Button                      addBtn;
    
    private Button                      removeBtn;
    
    private Button                      clearBtn;
    

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
        createButtons( parent );
        updateEnables();
        
        FormDataFactory.on( list.getTree() ).fill().noBottom().height( 200 );
        FormDataFactory.on( addBtn ).top( list.getTree() ).left( 5 ).right( 35, -3 ).bottom( 100 );
        FormDataFactory.on( removeBtn ).top( list.getTree() ).left( 35, 3 ).right( 65, -3 ).bottom( 100 );
        FormDataFactory.on( clearBtn ).top( list.getTree() ).left( 65, 3 ).right( 95 ).bottom( 100 );
        
        EventManager.instance().subscribe( this, isType( ProjectNodeCommittedEvent.class, ev ->
                true /*XXX ev.getEntityId().equals( layer.get().id() )*/ ) );
    }
    
    
    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
    }


    protected void updateEnables() {
        removeBtn.setEnabled( UIUtils.selection( list.getSelection() ).size() > 0 );
    }
    
    
    @EventHandler( display=true, delay=100 )
    protected void onLayerCommit( List<ProjectNodeCommittedEvent> evs ) {
        if (!list.getControl().isDisposed()) {
            list.setInput( layer.get().processorConfigs );
            list.refresh();
        }
    }
    
    
    protected void createList( Composite parent ) {
        list = tk.createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        list.firstLineLabelProvider.set( FunctionalLabelProvider.of( cell -> { 
            cell.setText( ((ProcessorConfig)cell.getElement()).ext.get().get().getName() );
        }));
        list.secondLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
            cell.setText( ((ProcessorConfig)cell.getElement()).ext.get().get().getDescription().orElse( "" ) );
        }));
        list.iconProvider.set( FunctionalLabelProvider.of( cell -> { 
            cell.setImage( P4Plugin.images().svgImage( "download-network.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }));
        list.firstSecondaryActionProvider.set( new ActionProvider() {
            @Override public void update( ViewerCell cell ) {
                cell.setImage( P4Plugin.images().svgImage( "chevron-right.svg", SvgImageRegistryHelper.NORMAL24 ) );
            }
            @Override public void perform( MdListViewer viewer, Object elm ) {
                openDetailPanel( (ProcessorConfig)elm );
            }
        });
        list.addOpenListener( ev -> {
            ProcessorConfig config = (ProcessorConfig)UIUtils.selection( list.getSelection() ).first().get();
            openDetailPanel( config );
        });
        list.addSelectionChangedListener( ev -> {
            updateEnables();            
        });
        list.setContentProvider( new ListTreeContentProvider() );
//        list.setComparator( new ViewerComparator() {
//            @Override
//            public int compare( Viewer viewer, Object elm1, Object elm2 ) {
//                return ((ModuleInfo)elm1).label().compareTo( ((ModuleInfo)elm2).label() );
//            }
//        });
        list.setInput( layer.get().processorConfigs );
    }


    protected void openDetailPanel( ProcessorConfig config ) {
        UnitOfWork nested = layer.get().belongsTo().newUnitOfWork();
        ILayer nestedLayer = nested.entity( layer.get() );
        selected.set( nestedLayer.processorConfigs.stream().filter( c -> c.id.get().equals( config.id.get() ) ).findAny().get() );
        BatikApplication.instance().getContext().openPanel( site().panelSite().getPath(), LayerProcessorPanel.ID );
    }
    
    
    protected void createButtons( Composite parent ) {
        addBtn = tk.createButton( parent, "Add...", SWT.PUSH );
        addBtn.setImage( P4Plugin.images().svgImage( "plus-circle.svg", SvgImageRegistryHelper.WHITE24 ) );
        addBtn.setToolTipText( "Add a new processor to the pipeline of this layer" );
        addBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
            openDialog();
        }));

        removeBtn = tk.createButton( parent, "Remove", SWT.PUSH );
        removeBtn.setImage( P4Plugin.images().svgImage( "delete.svg", SvgImageRegistryHelper.WHITE24 ) );
        removeBtn.setToolTipText( "Remove the currently selected processor" );
        removeBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
            ProcessorConfig config = (ProcessorConfig)UIUtils.selection( list.getSelection() ).first().get();
            removeProcessor( config );
        }));

        clearBtn = tk.createButton( parent, "Clear", SWT.PUSH );
        clearBtn.setImage( BatikPlugin.images().svgImage( "close-circle.svg", SvgImageRegistryHelper.WHITE24 ) );
        clearBtn.setToolTipText( "Clear <b>all</b> processors of this layer" );
        clearBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
            clearProcessors();
        }));
    }
    
    
    protected void openDialog() {
        tk.createSimpleDialog( "Pipeline processors" )
            .setContents( parent -> {
                list = tk.createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
                list.firstLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
                    ProcessorExtension ext = (ProcessorExtension)cell.getElement();
                    cell.setText( ext.getName() );            
                }));
                list.secondLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
                    ProcessorExtension ext = (ProcessorExtension)cell.getElement();
                    cell.setText( ext.getDescription().orElse( "" ) );            
                }));
                list.setContentProvider( new ListTreeContentProvider() );
                list.setInput( ProcessorExtension.all() );
                
                parent.setLayout( FormLayoutFactory.defaults().create() );
                FormDataFactory.on( list.getControl() ).fill().width( 330 ).height( 250 );
            })
            .addCancelAction()
            .addOkAction( "ADD", () -> {
                UIUtils.selection( list.getSelection() ).first().ifPresent( sel -> 
                        addProcessor( (ProcessorExtension)sel ) );
                return true;
            })
            .open();
    }


    protected void addProcessor( ProcessorExtension ext ) {
        UnitOfWork nested = layer.get().belongsTo().newUnitOfWork();
        ILayer nestedLayer = nested.entity( layer.get() );
        selected.set( nestedLayer.processorConfigs.createElement( ProcessorConfig.init( ext ) ) );
        BatikApplication.instance().getContext().openPanel( site().panelSite().getPath(), LayerProcessorPanel.ID );
    }


    protected void removeProcessor( ProcessorConfig config ) {
        layer.get().processorConfigs.remove( config );
        layer.get().belongsTo().commit();
    }
    

    protected void clearProcessors() {
        layer.get().processorConfigs.clear();
//        layer.get().label.set( layer.get().label.get() );
        layer.get().belongsTo().commit();
    }
    
}
