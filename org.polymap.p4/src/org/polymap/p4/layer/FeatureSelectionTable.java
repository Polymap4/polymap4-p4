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

import static org.opengis.filter.sort.SortOrder.ASCENDING;
import static org.opengis.filter.sort.SortOrder.DESCENDING;
import static org.polymap.core.data.DataPlugin.ff;
import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.core.ui.FormDataFactory.on;
import static org.polymap.core.ui.SelectionAdapter.on;
import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.DISABLED12;

import java.io.IOException;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureEvent.Type;
import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.sort.SortOrder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.StructuredSelection;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.ActionText;
import org.polymap.rhei.batik.toolkit.ClearTextAction;
import org.polymap.rhei.batik.toolkit.TextActionItem;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.table.DefaultFeatureTableColumn;
import org.polymap.rhei.table.FeatureTableViewer;
import org.polymap.rhei.table.IFeatureTableElement;

import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionTable {

    private static Log log = LogFactory.getLog( FeatureSelectionTable.class );
    
    public static final String          TOOLBAR_TAG = "FeatureSelectionTable";

    private FeatureSelection            featureSelection;
    
    private FeatureStore                fs;
    
    private IPanel                      panel;
    
    private FeatureTableViewer          viewer;
    
    private LazyFeatureContentProvider  contentProvider;

    private ActionText                  searchText;

    private MdToolbar2                  toolbar;

    
    public FeatureSelectionTable( Composite parent, FeatureSelection featureSelection, IPanel panel ) {
        BatikApplication.instance().getContext().propagate( this );
        this.featureSelection = featureSelection;
        this.panel = panel;

        parent.setLayout( FormLayoutFactory.defaults().create() );

        try {
            this.fs = featureSelection.waitForFs().get();  // already loaded by LayerFeatureTableContribution
//            this.features = fs.getFeatures( featureSelection.filter() );
        }
        catch (Exception e) {
            log.warn( "", e );
            tk().createLabel( parent, "<p>Unable to fetch features.</p>Reason: " + e.getLocalizedMessage() );
            return;
        }
        
        // topbar
        Composite topbar = on( tk().createComposite( parent ) ).fill().noBottom().height( 36 ).control();
        topbar.setLayout( FormLayoutFactory.defaults().spacing( 3 ).margins( 3 ).create() );
    
        // seach
        createTextSearch( topbar );
        on( searchText.getControl() ).fill().right( 38 );
        
        // toolbar
        toolbar = tk().createToolbar( topbar,  SWT.FLAT );
        on( toolbar.getControl() ).fill().noLeft().right( 100 );
        ContributionManager.instance().contributeTo( toolbar, panel, TOOLBAR_TAG );

        // table viewer
        createTableViewer( parent );
        on( viewer.getTable() ).fill().top( topbar );
        
        // listen to commit events
        EventManager.instance().subscribe( this, ifType( FeatureEvent.class, ev -> 
                ev.getType() == Type.COMMIT &&
                ev.getFeatureSource() == fs ) );
        
        // listen to click events
        EventManager.instance().subscribe( this, ifType( FeatureClickEvent.class, ev -> 
                ev.getSource() == this.featureSelection &&
                ev.clicked.isPresent() ) );
    }
    
    
    protected MdToolkit tk() {
        return (MdToolkit)panel.site().toolkit();
    }
    
    
    protected void createTableViewer( Composite parent ) {
        viewer = new FeatureTableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER );
        contentProvider = new LazyFeatureContentProvider();
        contentProvider.filter( featureSelection.filter() );
    
        // add columns
        DefaultFeatureTableColumn first = null;
        for (PropertyDescriptor prop : fs.getSchema().getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                DefaultFeatureTableColumn column = new DefaultFeatureTableColumn( prop );
                // disable default sorting behaviour
                column.setSortable( false );
                viewer.addColumn( column );
                first = first != null ? first : column;
                
                column.getViewerColumn().getColumn().addSelectionListener( new SelectionAdapter() {
                    private SortOrder currentOrder = SortOrder.ASCENDING;
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        // with selection RAP produces huge JS which fails in browser
                        viewer.setSelection( StructuredSelection.EMPTY );
                        currentOrder = currentOrder.equals( ASCENDING ) ? DESCENDING : ASCENDING;
                        contentProvider.sort( column, currentOrder );
                    }
                });
            }
        }
        
        // it is important to sort any column; otherwise preserving selection during refresh()
        // always selects a new element, which causes an event, which causes a refresh() ...
        contentProvider.sort( first, SortOrder.ASCENDING );
        
        //
        viewer.setContentProvider( contentProvider );
        viewer.setInput( fs );

        // selection -> FeaturePanel
        viewer.addSelectionChangedListener( ev -> {
            on( ev.getSelection() ).first( IFeatureTableElement.class ).ifPresent( elm -> {
                log.info( "selection: " + elm );
                featureSelection.setClicked( elm.unwrap( Feature.class ).get() );
            
                BatikApplication.instance().getContext().openPanel( panel.site().path(), FeaturePanel.ID );
            });
        });        
    }
    

    @EventHandler( display=true )
    protected void onFeatureClick( FeatureClickEvent ev ) throws IOException {
        if (!viewer.getTable().isDisposed()) {
            IFeatureTableElement[] selected = viewer.getSelectedElements();
            String clickedFid = ev.clicked.get().getIdentifier().getID();
            if (selected.length != 1 || !selected[0].fid().equals( clickedFid )) {
                // viewer.setSelection() does not work with LazyContentProvider
                int index = contentProvider.indexOfFid( clickedFid );
                viewer.getTable().select( index );
                viewer.getTable().showSelection();
            }
        }
        else {
            EventManager.instance().unsubscribe( this );
        }
    }


    @EventHandler( display=true )
    protected void onFeatureChange( FeatureEvent ev ) {
        if (!viewer.getTable().isDisposed()) {
            // XXX this tries to preserve selection; this is index based; it causes
            // a selection event; if sort has changed, another element ist selected!
            viewer.refresh();
        }
        else {
            EventManager.instance().unsubscribe( this );
        }
    }
    
    
    protected void createTextSearch( Composite topbar ) {
        searchText = tk().createActionText( topbar, null, SWT.BORDER );
        searchText.performOnEnter.put( true );
        
        new TextActionItem( searchText, TextActionItem.Type.DEFAULT )
                .action.put( ev -> doSearch() )
                .text.put( "Search..." )
                .tooltip.put( "Search in all text properties. " +
                        "Allowed wildcards are: * and ?<br/>" +
                        "* is appended by default if no other wildcard is given" )
                .icon.put( P4Plugin.images().svgImage( "magnify.svg", DISABLED12 ) );
        
        new ClearTextAction( searchText );
    }
    
    
    protected void doSearch() {
        Filter filter = featureSelection.filter();
        String s = searchText.getText().getText();
        if (!StringUtils.isBlank( s )) {
            if (!s.contains( "*" ) && !s.contains( "?" ) ) {
                s = s + "*";
            }
            for (PropertyDescriptor prop : fs.getSchema().getDescriptors()) {
                if (String.class.isAssignableFrom( prop.getType().getBinding() )) {
                    PropertyIsLike isLike = ff.like( ff.property( prop.getName() ), s, "*", "?", "\\" );
                    filter = filter == Filter.INCLUDE ? isLike : ff.or( filter, isLike ); 
                }
            }
        }
        viewer.setSelection( StructuredSelection.EMPTY );
        log.info( "FILTER: "  + filter );
//        FeatureCollection filtered = features.subCollection( filter );
//        log.info( "RESULT: "  + filtered.size() );
        contentProvider.filter( filter );
    }
    
}
