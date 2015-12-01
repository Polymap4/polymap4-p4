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
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.TextSymbolizer;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.sld.from.helper.StyleFigureFromSLDHelper;
import org.polymap.p4.style.sld.from.helper.StyleImageFromSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePointFromSLDVisitor
        extends AbstractStyleFromSLDVisitor {

    private final StylePoint stylePoint;


    public StylePointFromSLDVisitor( StylePoint stylePoint ) {
        this.stylePoint = stylePoint;
    }


    @Override
    public void visit( Rule rule ) {
        Arrays.asList( rule.getSymbolizers() )
                .stream()
                .filter( symb -> symb instanceof TextSymbolizer )
                .forEach(
                        t -> new StyleLabelFromSLDVisitor( stylePoint.markerLabel.createValue( null ),
                                FeatureType.POINT ).visit( (TextSymbolizer)t ) );
        Arrays.asList( rule.getSymbolizers() ).stream().filter( symb -> symb instanceof PointSymbolizer )
                .forEach( symb -> symb.accept( this ) );
    }


    @Override
    public void visit( PointSymbolizer ps ) {
        if (ps.getGraphic() != null) {
            ps.getGraphic().accept( this );
        }
    }


    @Override
    public void visit( Graphic gr ) {
        if (gr.getSize() != null) {
            stylePoint.markerSize.set( ((Double)gr.getSize().accept( getNumberExpressionVisitor(), null )) );
        }
        if (gr.getRotation() != null) {
            stylePoint.markerRotation.set( ((Double)gr.getRotation().accept( getNumberExpressionVisitor(), null )) );
        }
        super.visit( gr );
    }


    @Override
    public void visit( Mark mark ) {
        if (mark.getWellKnownName() != null) {
            stylePoint.markerFigure.createValue( figure -> {
                new StyleFigureFromSLDHelper( figure ).fillSLD( mark );
                return figure;
            } );
        }
    }


    @Override
    public void visit( ExternalGraphic exgr ) {
        if (exgr.getURI() != null) {
            stylePoint.markerImage.createValue( image -> {
                new StyleImageFromSLDHelper().fillSLD( image, exgr );
                return image;
            } );
        }
    }
}
