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
import org.geotools.styling.builder.FillBuilder;
import org.geotools.styling.builder.HaloBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.geotools.styling.builder.TextSymbolizerBuilder;
import org.polymap.model2.Property;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.sld.to.helper.StyleColorToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleFontToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleLabelLinePlacementToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleLabelPointPlacementToSLDHelper;

import com.google.common.collect.Lists;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelToSLDVisitor
        extends AbstractStyleToSLDVisitor {

    private final StyleLabel styleLabel;


    public StyleLabelToSLDVisitor( StyleLabel styleLabel ) {
        this.styleLabel = styleLabel;
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        if (styleLabel.labelText.get() != null) {
            RuleBuilder ruleBuilder = singletonRule( builder );
            internalFillSLD( builder, ruleBuilder );
        }
    }


    public void fillSLD( SLDBuilder builder, RuleBuilder ruleBuilder ) {
        if (styleLabel.labelText.get() != null) {
            internalFillSLD( builder, ruleBuilder );
        }
    }


    private void internalFillSLD( SLDBuilder builder, RuleBuilder ruleBuilder ) {
        TextSymbolizerBuilder textBuilder = builder.text( ruleBuilder );
        textBuilder
                .label( new AttributeExpressionImpl( new org.geotools.feature.NameImpl( styleLabel.labelText.get() ) ) );
        handleGeoServerVendorExtensions( textBuilder, styleLabel );
        if (styleLabel.labelFontColor.get() != null) {
            textBuilder.fill().color( new StyleColorToSLDHelper().getSLDColor( styleLabel.labelFontColor.get() ) );
        }
        if (styleLabel.labelFont.get() != null) {
            new StyleFontToSLDHelper().fillSLD( styleLabel.labelFont.get(), ( ) -> textBuilder.newFont() );
        }
        if (styleLabel.linePlacement.get() != null) {
            new StyleLabelLinePlacementToSLDHelper( styleLabel.linePlacement.get() ).fillSLD( builder, textBuilder );
        }
        else if (styleLabel.pointPlacement.get() != null) {
            new StyleLabelPointPlacementToSLDHelper( styleLabel.pointPlacement.get() ).fillSLD( builder, textBuilder );
        }
        if (styleLabel.haloRadius.get() != null && styleLabel.haloRadius.get() > 0) {
            HaloBuilder haloBuilder = textBuilder.halo();
            haloBuilder.radius( styleLabel.haloRadius.get() );
            if (styleLabel.haloFill.get() != null) {
                FillBuilder haloFillBuilder = haloBuilder.fill();
                haloFillBuilder.color( new StyleColorToSLDHelper().getSLDColor( styleLabel.haloFill.get() ) );
            }
        }
    }


    private void handleGeoServerVendorExtensions( TextSymbolizerBuilder textBuilder, StyleLabel styleLabel ) {
        Lists.<Property<?>>newArrayList( styleLabel.autoWrap ).stream()
                .forEach( property -> handleGeoServerVendorExtension( textBuilder, property ) );
    }


    private void handleGeoServerVendorExtension( TextSymbolizerBuilder textBuilder, Property<?> property ) {
        if (property.get() != null) {
            String label = property.info().getName();
            textBuilder.option( label, property.get() );
        }
    }
}