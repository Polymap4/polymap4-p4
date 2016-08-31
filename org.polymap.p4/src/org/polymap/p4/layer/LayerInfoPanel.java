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
package org.polymap.p4.layer;

import static org.polymap.core.runtime.UIThreadExecutor.asyncFast;
import static org.polymap.core.runtime.event.TypeEventFilter.ifType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ops.DeleteLayerOperation;
import org.polymap.core.project.ops.UpdateLayerOperation;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.dashboard.IDashlet;
import org.polymap.rhei.batik.dashboard.ISubmitableDashlet;
import org.polymap.rhei.batik.dashboard.SubmitStatusChangeEvent;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.MetadataInfoDashlet;
import org.polymap.p4.catalog.ResourceInfoDashlet;
import org.polymap.p4.project.ProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerInfoPanel
        extends P4Panel {

    private static Log log = LogFactory.getLog( LayerInfoPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layer" );
    
    public static final String          DASHBOARD_ID = "org.polymap.p4.project.layer";
    
    @Scope( P4Plugin.Scope )
    private Context<ILayer>             layer;
    
    private Dashboard                   dashboard;

    private Button                      fab;

    
    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }


    @Override
    public void createContents( Composite parent ) {
        site().setSize( SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH2, SIDE_PANEL_WIDTH2 );
        site().title.set( "Layer" ); // + layer.get().label.get() );
        ContributionManager.instance().contributeTo( this, this );

        // dashboard
        dashboard = new Dashboard( getSite(), DASHBOARD_ID );
        dashboard.addDashlet( new LayerInfoDashlet( layer.get() )
                .addConstraint( new PriorityConstraint( 100 ) ) );
        try {
            NullProgressMonitor monitor = new NullProgressMonitor();
            P4Plugin.allResolver().metadata( layer.get(), monitor ).ifPresent( serviceInfo -> {
                dashboard.addDashlet( new MetadataInfoDashlet( serviceInfo )
                        .addConstraint( new PriorityConstraint( 9 ) ) );            
            });
            P4Plugin.allResolver().resInfo( layer.get(), monitor ).ifPresent( resInfo -> {
                dashboard.addDashlet( new ResourceInfoDashlet( resInfo )
                        .addConstraint( new PriorityConstraint( 10 ) ) );            
            });
        }
        catch (Exception e) {
            log.warn( "", e );
        }
        dashboard.createContents( parent );

        // fab
        fab = tk().createFab();
        fab.setToolTipText( "Submit changes" );
        fab.setVisible( false );
        fab.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    for (IDashlet dashlet : dashboard.dashlets()) {
                        if (dashlet instanceof ISubmitableDashlet) {
                            ((ISubmitableDashlet)dashlet).submit( new NullProgressMonitor() );
                        }
                    }
                    UpdateLayerOperation op = new UpdateLayerOperation()
                            .uow.put( layer.get().belongsTo() )
                            .layer.put( layer.get() );
                    
                    // XXX not async until op progress indicator is inplace
                    OperationSupport.instance().execute2( op, false, false, ev2 -> asyncFast( () -> {
                        if (ev2.getResult().isOK()) {
                            tk().createSnackbar( Appearance.FadeIn, "Saved" );
                        }
                    }));
                }
                catch (Exception e) {
                    StatusDispatcher.handleError( "Unable to submit all changes.", e );
                }
            }
        });
        
        EventManager.instance().subscribe( this, ifType( SubmitStatusChangeEvent.class, ev -> {
            return ev.getDashboard() == dashboard;
        }));
    }

    
    @EventHandler( display=true )
    protected void submitStatusChanged( SubmitStatusChangeEvent ev ) {
        fab.setVisible( true );
        if (fab != null && !fab.isDisposed()) {
            fab.setEnabled( ev.getsSubmitable() );
        }
    }
    
    
    /**
     * 
     */
    class DeleteLayerDashlet
            extends DefaultDashlet {

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( "Danger zone" );
            site.constraints.get().add( new PriorityConstraint( 0 ) );
            site.constraints.get().add( new MinWidthConstraint( 350, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            Button deleteBtn = tk().createButton( parent, "Delete this layer", SWT.PUSH );
            deleteBtn.setToolTipText( "Delete this layer." );
            deleteBtn.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent e ) {
//                    MdSnackbar snackbar = tk.createSnackbar();
//                    snackbar.showIssue( MessageType.WARNING, "We are going to delete the project." );
                    
                    DeleteLayerOperation op = new DeleteLayerOperation();
                    op.uow.set( ProjectRepository.unitOfWork() );
                    op.layer.set( layer.get() );

                    OperationSupport.instance().execute2( op, true, false, ev2 -> asyncFast( () -> {
                        if (ev2.getResult().isOK()) {
                            PanelPath parentPath = site().path().removeLast( 1 );
                            BatikApplication.instance().getContext().closePanel( parentPath );

//                            // close panel and parent, assuming that projct map is root
//                            getContext().openPanel( PanelPath.ROOT, new PanelIdentifier( "start" ) );
                        }
                        else {
                            StatusDispatcher.handleError( "Unable to delete project.", ev2.getResult().getException() );
                        }
                    }));
                }
            });
        }        
    }
    
}
