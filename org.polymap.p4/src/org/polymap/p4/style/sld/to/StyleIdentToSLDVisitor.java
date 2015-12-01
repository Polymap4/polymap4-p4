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
package org.polymap.p4.style.sld.to;

import org.geotools.styling.builder.NamedLayerBuilder;
import org.geotools.styling.builder.StyleBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleIdent;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleIdentToSLDVisitor
        extends AbstractStyleToSLDVisitor {

    private final StyleIdent styleIdent;


    public StyleIdentToSLDVisitor( StyleIdent styleIdent ) {
        this.styleIdent = styleIdent;
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        if (styleIdent.name.get() != null | styleIdent.title.get() != null) {
            NamedLayerBuilder namedLayer = builder.namedLayer();
            if (styleIdent.name.get() != null) {
                namedLayer.name( styleIdent.name.get() );
            }
            if (styleIdent.title.get() != null || styleIdent.description.get() != null) {
                StyleBuilder styleBuilder = builder.style( namedLayer );
                if (styleIdent.title.get() != null) {
                    styleBuilder.title( styleIdent.title.get() );
                }
                if (styleIdent.description.get() != null) {
                    styleBuilder.styleAbstract( styleIdent.description.get() );
                }
                // have to call this here as otherwise the title/description wouldn't
                // be set
                builder.featureTypeStyle( styleBuilder );
            }
        }
    }
}
