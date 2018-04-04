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

import java.util.Optional;

import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.contribution.IToolbarContribution;
import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;

import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.FeatureLayer;
import org.polymap.p4.layer.FeatureSelectionTable;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * Contributes a button that opens {@link LayerStylePanel} to the toolbar of
 * {@link FeatureSelectionTable}.
 *
 * @author Falko Bräutigam
 * @author Steffen Stundzig
 */
public class LayerStyleContrib
        implements IToolbarContribution {

    private static final IMessages          i18n = Messages.forPrefix( "Styler" );

    private ActionItem                      item;
    
    private Optional<LayerStylePanel>       childPanel = Optional.empty();

    /** Outbound: */
    @Scope( P4Plugin.StyleScope )
    protected Context<FeatureStyleEditorInput> editorInput;

    @Scope( P4Plugin.Scope )
    protected Context<FeatureLayer>         featureLayer;

    /** Inbound: */
    @Scope( P4Plugin.Scope )
    protected Context<MapViewer<ILayer>>    mainMapViewer;


    
    @Override
    public void fillToolbar( IContributionSite site, MdToolbar2 toolbar ) {
        if (site.panel() instanceof ProjectMapPanel 
                && site.tagsContain( FeatureSelectionTable.TOOLBAR_TAG )) {
            assert item == null;
            
            featureLayer.ifPresent( fs -> {
                try {
                    editorInput.set( new FeatureStyleEditorInput() ); 
                    editorInput.get().styleIdentifier.set( fs.layer().styleIdentifier.get() ); 
                    editorInput.get().featureStore.set( fs.featureSource() );
                    editorInput.get().featureType.set( fs.featureSource().getSchema() );
                    mainMapViewer.ifPresent( mapViewer -> {
                        editorInput.get().maxExtent.set( mapViewer.maxExtent.get() );
                        editorInput.get().mapExtent.set( mapViewer.mapExtent.get() );
                        editorInput.get().mapSize.set( mapViewer.getControl().getSize() );
                    });

                    item = new ActionItem( toolbar );
                    item.text.set( "" );
                    item.icon.set( P4Plugin.images().svgImage("palette.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
                    item.tooltip.set( i18n.get("start") );
                    item.action.set( ev -> {
                        //                    assert !childPanel.isPresent();
                        childPanel = site.context().openPanel( site.panelSite().path(), LayerStylePanel.ID );
                    });
                }
                catch (Exception e) {
                    StatusDispatcher.handleError( "Error during load of the FeatureStore", e );
                }
            });
           
                
//                // FIXME does not work
//                site.context().addListener( LayerStyleContrib.this, ev2 -> 
//                        ev2.getPanel() == childPanel.orElse( null ) && ev2.getType().isOnOf( EventType.LIFECYCLE ) );
//            item.onUnselected.set( ev -> {
//                if (childPanel.isPresent() && !childPanel.get().isDisposed()) {
//                    site.context().closePanel( childPanel.get().site().path() );
//                    childPanel = Optional.empty();
//                    site.context().removeListener( LayerStyleContrib.this );
//                }
//            });
        }
    }

    
//    @EventHandler( display=true )
//    protected void childPanelClosed( PanelChangeEvent ev ) {
//        log.info( "Child panel lifecycle: " + ev.getPanel().site().panelStatus() );
//        if (item != null /*&& !item.isDisposed()*/
//                && ev.getPanel().site().panelStatus() == PanelStatus.CREATED) {
//            item.selected.set( false );
//        }
//    }
    
}