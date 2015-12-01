/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style.sld.from.helper;

import org.geotools.styling.ExternalGraphic;
import org.opengis.metadata.citation.OnLineResource;
import org.polymap.p4.style.entities.StyleImage;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleImageFromSLDHelper {

    public void fillSLD( StyleImage styleImage, ExternalGraphic exgr ) {
        if (exgr.getOnlineResource() != null) {
            OnLineResource onlineResource = exgr.getOnlineResource();
            if (onlineResource.getLinkage() != null) {
                styleImage.url.set( onlineResource.getLinkage().toString() );
            }
        }
    }
}
