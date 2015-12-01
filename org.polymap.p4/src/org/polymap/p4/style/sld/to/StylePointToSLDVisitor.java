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

import org.geotools.styling.builder.GraphicBuilder;
import org.geotools.styling.builder.PointSymbolizerBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.sld.to.helper.StyleFigureToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleImageToSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePointToSLDVisitor
        extends AbstractStyleToSLDVisitor {

    private final StylePoint stylePoint;


    public StylePointToSLDVisitor( StylePoint stylePoint ) {
        this.stylePoint = stylePoint;
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        if (stylePoint.markerLabel.get() != null) {
            new StyleLabelToSLDVisitor( stylePoint.markerLabel.get() ).fillSLD( builder );
        }
        if (isPointDefined( stylePoint )) {
            RuleBuilder ruleBuilder = singletonRule( builder );
            internalFillSLD( builder, ruleBuilder );
        }
    }


    private boolean isPointDefined( StylePoint stylePoint ) {
        return stylePoint.markerFigure.get() != null || stylePoint.markerImage.get() != null
                || stylePoint.markerSize.get() != null || stylePoint.markerRotation.get() != null;
    }


    public void fillSLD( SLDBuilder builder, RuleBuilder ruleBuilder ) {
        if (isPointDefined( stylePoint )) {
            internalFillSLD( builder, ruleBuilder );
        }
    }


    private void internalFillSLD( SLDBuilder builder, RuleBuilder ruleBuilder ) {
        PointSymbolizerBuilder pointBuilder = builder.point( ruleBuilder );
        GraphicBuilder pointGraphicBuilder = builder.pointGraphicBuilder( pointBuilder );
        fillSLD( pointGraphicBuilder );
    }


    public void fillSLD( GraphicBuilder graphicBuilder ) {
        if (stylePoint.markerSize.get() != null) {
            graphicBuilder.size( stylePoint.markerSize.get() );
        }
        if (stylePoint.markerRotation.get() != null) {
            graphicBuilder.rotation( stylePoint.markerRotation.get() );
        }
        if (stylePoint.markerFigure.get() != null) {
            new StyleFigureToSLDHelper().fillSLD( stylePoint.markerFigure.get(), graphicBuilder.mark() );
        }
        else if (stylePoint.markerImage.get() != null) {
            new StyleImageToSLDHelper().fillSLD( stylePoint.markerImage.get(), graphicBuilder );
        }
    }
}
