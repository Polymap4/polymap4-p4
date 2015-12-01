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

import org.geotools.styling.LinePlacement;
import org.geotools.styling.TextSymbolizer;
import org.polymap.p4.style.entities.StyleLabelLinePlacement;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelLinePlacementFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    private final StyleLabelLinePlacement styleLabelLinePlacement;


    public StyleLabelLinePlacementFromSLDHelper( StyleLabelLinePlacement styleLabelLinePlacement ) {
        this.styleLabelLinePlacement = styleLabelLinePlacement;
    }


    public void fromSLD( TextSymbolizer ts ) {
        handleGeoServerVendorExtensions( ts, styleLabelLinePlacement );
    }


    public void fromSLD( LinePlacement lp ) {
        visit( lp );
    }


    private void handleGeoServerVendorExtensions( TextSymbolizer ts, StyleLabelLinePlacement styleLabelLinePlacement ) {
        handleDoubleVendorOption( ts, styleLabelLinePlacement.maxDisplacement );
        handleBooleanVendorOption( ts, styleLabelLinePlacement.followLine );
        handleDoubleVendorOption( ts, styleLabelLinePlacement.maxAngleDelta );
        handleDoubleVendorOption( ts, styleLabelLinePlacement.repeat );
    }


    @Override
    public void visit( LinePlacement lp ) {
        if (lp.getPerpendicularOffset() != null) {
            styleLabelLinePlacement.perpendicularOffset.set( (double)lp.getPerpendicularOffset().accept(
                    getNumberExpressionVisitor(), null ) );
        }
        super.visit( lp );
    }


    public static boolean containsVendorOption( TextSymbolizer ts ) {
        return hasAnyVendorOption( ts, "maxDisplacement", "followLine", "maxAngleDelta", "repeat" );
    }
}
