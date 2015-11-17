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
package org.polymap.p4.style.sld.from;

import java.util.Arrays;

import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.TextSymbolizer;
import org.polymap.p4.style.entities.StylePolygon;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePolygonFromSLDVisitor
        extends AbstractStyleFromSLDVisitor {

    private final StylePolygon stylePolygon;


    public StylePolygonFromSLDVisitor( StylePolygon stylePolygon ) {
        this.stylePolygon = stylePolygon;
    }


    @Override
    public void visit( Rule rule ) {
        Arrays.asList( rule.getSymbolizers() )
                .stream()
                .filter( symb -> symb instanceof TextSymbolizer )
                .forEach(
                        t -> new StyleLabelFromSLDVisitor( stylePolygon.polygonLabel.createValue( null ) )
                                .visit( (TextSymbolizer)t ) );
        Arrays.asList( rule.getSymbolizers() ).stream().filter( symb -> symb instanceof PolygonSymbolizer )
                .forEach( symb -> symb.accept( this ) );
    }


    @Override
    public void visit( PolygonSymbolizer poly ) {
        if (poly.getFill() != null) {
            poly.getFill().accept( this );
        }
        if (poly.getStroke() != null) {
            poly.getStroke().accept( this );
        }
    }


    @Override
    public void visit( ExternalGraphic exgr ) {
    }


    @Override
    public void visit( Stroke stroke ) {
    }
}
