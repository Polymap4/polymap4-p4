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
package org.polymap.p4.style.sld.to.helper;

import org.geotools.styling.builder.LinePlacementBuilder;
import org.geotools.styling.builder.TextSymbolizerBuilder;
import org.polymap.model2.Property;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleLabelLinePlacement;

import com.google.common.collect.Lists;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelLinePlacementToSLDHelper {

    private static Double                 PERPENDICULAR_OFFSET_DEFAULT = 0.0d;

    private final StyleLabelLinePlacement styleLabelLinePlacement;


    public StyleLabelLinePlacementToSLDHelper( StyleLabelLinePlacement styleLabelLinePlacement ) {
        this.styleLabelLinePlacement = styleLabelLinePlacement;
    }


    public void fillSLD( SLDBuilder builder, TextSymbolizerBuilder textBuilder ) {
        if (!(styleLabelLinePlacement.perpendicularOffset.get() == null || styleLabelLinePlacement.perpendicularOffset
                .get().compareTo( PERPENDICULAR_OFFSET_DEFAULT ) == 0)
                || styleLabelLinePlacement.followLine.get() != null
                || styleLabelLinePlacement.maxAngleDelta.get() != null
                || styleLabelLinePlacement.maxDisplacement.get() != null
                || styleLabelLinePlacement.repeat.get() != null) {
            internalFillSLD( builder, textBuilder );
        }
    }


    private void internalFillSLD( SLDBuilder builder, TextSymbolizerBuilder textBuilder ) {
        if (!(styleLabelLinePlacement.perpendicularOffset.get() == null || styleLabelLinePlacement.perpendicularOffset
                .get().compareTo( PERPENDICULAR_OFFSET_DEFAULT ) == 0)) {
            LinePlacementBuilder placementBuilder = textBuilder.linePlacement();
            placementBuilder.offset( styleLabelLinePlacement.perpendicularOffset.get() );
        }
        handleGeoServerVendorExtensions( textBuilder, styleLabelLinePlacement );
    }


    private void handleGeoServerVendorExtensions( TextSymbolizerBuilder textBuilder,
            StyleLabelLinePlacement styleLabelLinePlacement ) {
        Lists.<Property<?>>newArrayList( styleLabelLinePlacement.followLine, styleLabelLinePlacement.maxAngleDelta,
                styleLabelLinePlacement.maxDisplacement, styleLabelLinePlacement.repeat ).stream()
                .forEach( property -> handleGeoServerVendorExtension( textBuilder, property ) );
    }


    private void handleGeoServerVendorExtension( TextSymbolizerBuilder textBuilder, Property<?> property ) {
        if (property.get() != null) {
            String label = property.info().getName();
            textBuilder.option( label, property.get() );
        }
    }
}
