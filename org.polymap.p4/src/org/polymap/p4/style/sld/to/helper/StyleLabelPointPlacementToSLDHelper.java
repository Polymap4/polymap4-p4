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

import org.geotools.styling.builder.AnchorPointBuilder;
import org.geotools.styling.builder.DisplacementBuilder;
import org.geotools.styling.builder.PointPlacementBuilder;
import org.geotools.styling.builder.TextSymbolizerBuilder;
import org.polymap.model2.Property;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleCoord;
import org.polymap.p4.style.entities.StyleLabelPointPlacement;

import com.google.common.collect.Lists;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelPointPlacementToSLDHelper {

    private static Double                  ANCHOR_X_DEFAULT = 0.0d;

    private static Double                  ANCHOR_Y_DEFAULT = 0.5d;

    private static Double                  OFFSET_X_DEFAULT = 0.0d;

    private static Double                  OFFSET_Y_DEFAULT = 0.0d;

    private static Double                  ROTATION_DEFAULT = 0.0d;

    private final StyleLabelPointPlacement styleLabelPointPlacement;


    public StyleLabelPointPlacementToSLDHelper( StyleLabelPointPlacement styleLabelPointPlacement ) {
        this.styleLabelPointPlacement = styleLabelPointPlacement;
    }


    public void fillSLD( SLDBuilder builder, TextSymbolizerBuilder textBuilder ) {
        if (((styleLabelPointPlacement.anchor.get() != null || styleLabelPointPlacement.offset.get() != null) && !isDefaultPointPlacementAndOffset( styleLabelPointPlacement ))
                || (styleLabelPointPlacement.rotation.get() != null && !isDefaultLabelRotation( styleLabelPointPlacement ))
                || styleLabelPointPlacement.maxDisplacement.get() != null) {
            internalFillSLD( builder, textBuilder );
        }
    }


    private void internalFillSLD( SLDBuilder builder, TextSymbolizerBuilder textBuilder ) {
        PointPlacementBuilder placementBuilder = textBuilder.pointPlacement();
        if (((styleLabelPointPlacement.anchor.get() != null || styleLabelPointPlacement.offset.get() != null) && !isDefaultPointPlacementAndOffset( styleLabelPointPlacement ))) {
            if (styleLabelPointPlacement.anchor.get() != null) {
                AnchorPointBuilder anchorBuilder = placementBuilder.anchor();
                anchorBuilder.x( styleLabelPointPlacement.anchor.get().x.get() );
                anchorBuilder.y( styleLabelPointPlacement.anchor.get().y.get() );
            }
            if (styleLabelPointPlacement.offset.get() != null) {
                DisplacementBuilder offsetBuilder = placementBuilder.displacement();
                offsetBuilder.x( styleLabelPointPlacement.offset.get().x.get() );
                offsetBuilder.y( styleLabelPointPlacement.offset.get().y.get() );
            }
        }
        if ((styleLabelPointPlacement.rotation.get() != null && !isDefaultLabelRotation( styleLabelPointPlacement ))) {
            if (styleLabelPointPlacement.rotation.get() != null) {
                placementBuilder.rotation( styleLabelPointPlacement.rotation.get() );
            }
        }
        handleGeoServerVendorExtensions( textBuilder, styleLabelPointPlacement );
    }


    private boolean isDefaultPointPlacementAndOffset( StyleLabelPointPlacement styleLabelPointPlacement ) {
        return isDefaultPointPlacement( styleLabelPointPlacement.anchor.get() )
                && isDefaultPointOffset( styleLabelPointPlacement.offset.get() );
    }


    private boolean isDefaultPointPlacement( StyleCoord labelAnchor ) {
        return labelAnchor == null
                || labelAnchor.x.get() == null
                || (labelAnchor.x.get().compareTo( ANCHOR_X_DEFAULT ) == 0 && labelAnchor.y.get().compareTo(
                        ANCHOR_Y_DEFAULT ) == 0);
    }


    private boolean isDefaultPointOffset( StyleCoord labelOffset ) {
        return labelOffset == null
                || labelOffset.x.get() == null
                || (labelOffset.x.get().compareTo( OFFSET_X_DEFAULT ) == 0 && labelOffset.y.get().compareTo(
                        OFFSET_Y_DEFAULT ) == 0);
    }


    private boolean isDefaultLabelRotation( StyleLabelPointPlacement styleLabelPointPlacement ) {
        return styleLabelPointPlacement.rotation.get().compareTo( ROTATION_DEFAULT ) == 0;
    }


    private void handleGeoServerVendorExtensions( TextSymbolizerBuilder textBuilder,
            StyleLabelPointPlacement styleLabelPointPlacement ) {
        Lists.<Property<?>>newArrayList( styleLabelPointPlacement.maxDisplacement ).stream()
                .forEach( property -> handleGeoServerVendorExtension( textBuilder, property ) );
    }


    private void handleGeoServerVendorExtension( TextSymbolizerBuilder textBuilder, Property<?> property ) {
        if (property.get() != null) {
            String label = property.info().getName();
            textBuilder.option( label, property.get() );
        }
    }
}