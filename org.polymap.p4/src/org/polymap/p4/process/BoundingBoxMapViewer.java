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

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rap.openlayers.base.OlEvent;
import org.polymap.rap.openlayers.view.View;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class BoundingBoxMapViewer
        extends BaseMapViewer {

    private static final Log log = LogFactory.getLog( BoundingBoxMapViewer.class );
    
    public BoundingBoxMapViewer( Composite parent, ReferencedEnvelope bounds ) {
        super( parent );

        if (bounds != null) {
            parent.getDisplay().timerExec( 750, () -> {
                mapExtent.set( bounds );
            });
        }
        // XXX
        getMap().addEventListener( View.Event.CENTER, this, new View.ExtentEventPayload() );
    }

    
    @EventHandler( display=true )
    public void handleEvent( OlEvent ev ) {
        log.info( "event: " + ev.properties() );
        JSONArray extent = ev.properties().optJSONArray( "extent" );
        if (extent != null) {
            ReferencedEnvelope bounds = new ReferencedEnvelope( 
                    extent.getDouble( 0 ), extent.getDouble( 2 ), 
                    extent.getDouble( 1 ), extent.getDouble( 3 ),
                    maxExtent.get().getCoordinateReferenceSystem() );
            onBoundingBox( bounds );
        }
    }

    
    protected abstract void onBoundingBox( ReferencedEnvelope bounds );
    
}
