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

import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.Stroke;
import org.polymap.p4.style.entities.StyleFigure;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFigureFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    private final StyleFigure styleFigure;


    public StyleFigureFromSLDHelper( StyleFigure styleFigure ) {
        this.styleFigure = styleFigure;
    }


    public StyleFigure fillSLD( Mark mark ) {
        if (mark.getWellKnownName() != null) {
            styleFigure.markerWellKnownName.set( (String)mark.getWellKnownName().accept( getStringExpressionVisitor(),
                    null ) );
        }
        super.visit( mark );
        return styleFigure;
    }
    
    @Override
    public void visit( Fill fill ) {
        if (fill.getColor() != null) {
            new StyleColorFromSLDHelper().fromSLD( styleFigure.markerFill, fill.getColor() );
        }
        if (fill.getOpacity() != null) {
            styleFigure.markerTransparency.set( (double) fill.getOpacity()
                    .accept( getNumberExpressionVisitor(), null ) );
        }
        super.visit( fill );
    }


    @Override
    public void visit( Stroke stroke ) {
        if (stroke.getColor() != null) {
            new StyleColorFromSLDHelper().fromSLD( styleFigure.markerStrokeColor, stroke.getColor() );
        }
        if (stroke.getWidth() != null) {
            styleFigure.markerStrokeSize.set( ((Double)stroke.getWidth().accept( getNumberExpressionVisitor(), null )) );
        }
    }
}
