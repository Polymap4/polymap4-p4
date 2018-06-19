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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rap.openlayers.base.OlEvent;
import org.polymap.rap.openlayers.base.OlMap;
import org.polymap.rap.openlayers.base.OlMap.Event;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public abstract class ClickMapViewer
        extends BaseMapViewer {

    private static final Log log = LogFactory.getLog( ClickMapViewer.class );


    public ClickMapViewer( Composite parent ) {
        super( parent );

        getMap().addEventListener( Event.CLICK, this, new OlMap.ClickEventPayload() );
    }

    
    @EventHandler( display=true )
    protected void handleEvent( OlEvent ev ) {
        OlMap.ClickEventPayload.findIn( ev ).ifPresent( payload -> {
            onClick( new Coordinate( payload.coordinate().x, payload.coordinate().y ) );
        });
    }
    

    protected abstract void onClick( Coordinate coordinate );
    
}
