/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.FunctionalLabelProvider;
import org.polymap.rhei.batik.toolkit.md.ListTreeContentProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.p4.process.BackgroundJob.State;
import org.polymap.p4.process.ProcessProgressMonitor.ProgressEvent;

/**
 * The list of currently running {@link BackgroundJob}s.
 *
 * @author Falko Bräutigam
 */
public class ProcessProgressPanel
        extends P4Panel {

    private static final Log log = LogFactory.getLog( ProcessProgressPanel.class );
    
    public static final PanelIdentifier ID = PanelIdentifier.parse( "processProgress" );

    /** Outbound: for {@link ProcessModulePanel} */
    @Scope( P4Plugin.Scope )
    private Context<BackgroundJob>  bgjob;
    
    private MdListViewer            list;

    /** The input of the {@link #list}. */
    private Set<BackgroundJob>      listInput;
    

    @Override
    public boolean beforeInit() {
        if (parentPanel().orElse( null ) instanceof ProjectMapPanel) {
            site().icon.set( P4Plugin.images().svgImage( "play-circle-outline.svg", P4Plugin.HEADER_ICON_CONFIG ) );
            site().tooltip.set( "Background processing jobs" );
            site().title.set( "" );
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        super.dispose();
        EventManager.instance().unsubscribe( this );
    }


    @Override
    public void createContents( Composite parent ) {
        site().title.set( "Background Jobs" );
        
        list = tk().createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        list.firstLineLabelProvider.set( FunctionalLabelProvider.of( cell -> {
            BackgroundJob elm = (BackgroundJob)cell.getElement();
            cell.setText( elm.moduleInfo().label() );            
        }));
        list.secondLineLabelProvider.set( new DescriptionLabelProvider() );
        list.iconProvider.set( FunctionalLabelProvider.of( cell -> 
            cell.setImage( P4Plugin.images().svgImage( "play-circle-outline.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) ) ) );
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
            BackgroundJob selected = (BackgroundJob)((IStructuredSelection)list.getSelection()).getFirstElement();
            bgjob.set( selected );
            BatikApplication.instance().getContext().openPanel( site().path(), ProcessModulePanel.ID );
        });
        list.setContentProvider( new ListTreeContentProvider() );
//        list.setComparator( new ViewerComparator() {
//            @Override
//            public int compare( Viewer viewer, Object elm1, Object elm2 ) {
//                return ((ModuleInfo)elm1).title().compareTo( ((ModuleInfo)elm2).title() );
//            }
//        });
        list.setInput( listInput = new HashSet( BackgroundJob.running() ) );
      
        // listen to ProgressEvent
        EventManager.instance().subscribe( this );
        
        parent.setLayout( FormLayoutFactory.defaults().create() );
        FormDataFactory.on( list.getControl() ).fill().height( 400 );
    }

    
    @EventHandler( scope=org.polymap.core.runtime.event.Event.Scope.JVM, display=true, delay=500 )
    protected void onProgress( List<ProgressEvent> evs ) {
        for (ProgressEvent ev : evs) {
            if (listInput.contains( ev.getSource() )) {
                // simple update if this job is in the list already
                list.update( ev.getSource(), null );
            }
        }
    }
    
    
    @EventHandler( scope=org.polymap.core.runtime.event.Event.Scope.JVM, display=true )
    protected void onStateChange( BackgroundJob.StateEvent ev ) {
        // refresh entire list if this job was not seen before
        list.setInput( listInput = new HashSet( BackgroundJob.running() ) );
    }
    
    
    /**
     * 
     */
    class DescriptionLabelProvider
            extends CellLabelProvider {

        @Override
        public void update( ViewerCell cell ) {
            BackgroundJob elm = (BackgroundJob)cell.getElement();
            String s = elm.completed().map( v -> v.toString() ).orElse( "?" ) + "% complete";
            if (elm.isCanceled() && elm.state() == State.RUNNING) {
                s += "(Cancel requested)";
            }
            if (elm.isCanceled() && elm.state() == State.RUNNING) {
                s += "(Canceled)";
            }
            cell.setText( s );
        }
    }
    
}
