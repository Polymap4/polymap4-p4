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

import org.geotools.styling.AnchorPoint;
import org.geotools.styling.Displacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.TextSymbolizer;
import org.polymap.p4.style.entities.StyleLabelPointPlacement;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelPointPlacementFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    private final StyleLabelPointPlacement styleLabelPointPlacement;


    public StyleLabelPointPlacementFromSLDHelper( StyleLabelPointPlacement styleLabelPointPlacement ) {
        this.styleLabelPointPlacement = styleLabelPointPlacement;
    }


    public void fromSLD( TextSymbolizer ts ) {
        handleGeoServerVendorExtensions( ts, styleLabelPointPlacement );
    }


    public void fromSLD( PointPlacement pp ) {
        visit( pp );
    }


    @Override
    public void visit( PointPlacement pp ) {
        if (pp.getRotation() != null) {
            styleLabelPointPlacement.rotation
                    .set( (double)pp.getRotation().accept( getNumberExpressionVisitor(), null ) );
        }
        super.visit( pp );
    }


    private void handleGeoServerVendorExtensions( TextSymbolizer ts, StyleLabelPointPlacement styleLabelPointPlacement ) {
        handleDoubleVendorOption( ts, styleLabelPointPlacement.maxDisplacement );
    }


    @Override
    public void visit( AnchorPoint ap ) {
        if (ap.getAnchorPointX() != null && ap.getAnchorPointY() != null) {
            styleLabelPointPlacement.anchor.createValue( anchor -> {
                anchor.x.set( (double)ap.getAnchorPointX().accept( getNumberExpressionVisitor(), null ) );
                anchor.y.set( (double)ap.getAnchorPointY().accept( getNumberExpressionVisitor(), null ) );
                return anchor;
            } );
        }
    }


    @Override
    public void visit( Displacement dis ) {
        if (dis.getDisplacementX() != null && dis.getDisplacementY() != null) {
            styleLabelPointPlacement.offset.createValue( offset -> {
                offset.x.set( (double)dis.getDisplacementX().accept( getNumberExpressionVisitor(), null ) );
                offset.y.set( (double)dis.getDisplacementY().accept( getNumberExpressionVisitor(), null ) );
                return offset;
            } );
        }
    }


    public static boolean containsVendorOption( TextSymbolizer ts ) {
        return hasAnyVendorOption( ts, "maxDisplacement" );
    }
}
