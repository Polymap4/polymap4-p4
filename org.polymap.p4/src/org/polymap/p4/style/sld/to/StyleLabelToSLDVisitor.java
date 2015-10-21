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

import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.styling.builder.AnchorPointBuilder;
import org.geotools.styling.builder.DisplacementBuilder;
import org.geotools.styling.builder.PointPlacementBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.geotools.styling.builder.TextSymbolizerBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.sld.to.helper.StyleColorToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleFontToSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelToSLDVisitor
        extends AbstractStyleToSLDVisitor {

    private static Double    ANCHOR_X_DEFAULT = 0.0d;

    private static Double    ANCHOR_Y_DEFAULT = 0.5d;

    private static Double    OFFSET_X_DEFAULT = 0.0d;

    private static Double    OFFSET_Y_DEFAULT = 0.0d;

    private static Double    ROTATION_DEFAULT = 0.0d;

    private final StyleLabel styleLabel;


    public StyleLabelToSLDVisitor( StyleLabel styleLabel ) {
        this.styleLabel = styleLabel;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.p4.style.daos.AbstractStyleToSLDVisitor#fillSLD(org.geotools.styling
     * .builder.StyledLayerDescriptorBuilder)
     */
    @Override
    public void fillSLD( SLDBuilder builder ) {
        if (styleLabel.labelText.get() != null) {
            RuleBuilder ruleBuilder = newRule( builder );
            TextSymbolizerBuilder textBuilder = builder.text( ruleBuilder );
            textBuilder.label( new AttributeExpressionImpl( new org.geotools.feature.NameImpl( styleLabel.labelText
                    .get() ) ) );
            if (styleLabel.labelFontColor.get() != null) {
                textBuilder.fill().color( new StyleColorToSLDHelper().getSLDColor( styleLabel.labelFontColor.get() ) );
            }
            if (styleLabel.labelFont.get() != null) {
                new StyleFontToSLDHelper().fillSLD( styleLabel.labelFont.get(), textBuilder.newFont() );
            }
            if ((styleLabel.labelAnchor.get() != null && !(styleLabel.labelAnchor.get().x.get().compareTo(
                    ANCHOR_X_DEFAULT ) == 0 && styleLabel.labelAnchor.get().y.get().compareTo( ANCHOR_Y_DEFAULT ) == 0))
                    || (styleLabel.labelOffset.get() != null && !(styleLabel.labelOffset.get().x.get().compareTo(
                            OFFSET_X_DEFAULT ) == 0 && styleLabel.labelOffset.get().y.get()
                            .compareTo( OFFSET_Y_DEFAULT ) == 0))
                    || (styleLabel.labelRotation.get() != null && styleLabel.labelRotation.get().compareTo(
                            ROTATION_DEFAULT ) != 0)) {
                PointPlacementBuilder placementBuilder = textBuilder.pointPlacement();
                if (styleLabel.labelAnchor.get() != null) {
                    AnchorPointBuilder anchorBuilder = placementBuilder.anchor();
                    anchorBuilder.x( styleLabel.labelAnchor.get().x.get() );
                    anchorBuilder.y( styleLabel.labelAnchor.get().y.get() );
                }
                if (styleLabel.labelOffset.get() != null) {
                    DisplacementBuilder offsetBuilder = placementBuilder.displacement();
                    offsetBuilder.x( styleLabel.labelOffset.get().x.get() );
                    offsetBuilder.y( styleLabel.labelOffset.get().y.get() );
                }
                if (styleLabel.labelRotation.get() != null) {
                    placementBuilder.rotation( styleLabel.labelRotation.get() );
                }
            }
        }
    }
}
