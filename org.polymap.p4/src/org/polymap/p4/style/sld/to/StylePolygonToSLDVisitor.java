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

import org.geotools.styling.builder.FillBuilder;
import org.geotools.styling.builder.GraphicBuilder;
import org.geotools.styling.builder.PolygonSymbolizerBuilder;
import org.geotools.styling.builder.RuleBuilder;
import org.geotools.styling.builder.StrokeBuilder;
import org.polymap.p4.style.SLDBuilder;
import org.polymap.p4.style.entities.StylePolygon;
import org.polymap.p4.style.sld.to.helper.StyleColorToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleFigureToSLDHelper;
import org.polymap.p4.style.sld.to.helper.StyleImageToSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePolygonToSLDVisitor
        extends AbstractStyleToSLDVisitor {

    private final StylePolygon stylePolygon;


    public StylePolygonToSLDVisitor( StylePolygon stylePolygon ) {
        this.stylePolygon = stylePolygon;
    }


    @Override
    public void fillSLD( SLDBuilder builder ) {
        RuleBuilder ruleBuilder = singletonRule( builder );
        fillSLD( builder, ruleBuilder );
    }


    public void fillSLD( SLDBuilder builder, RuleBuilder ruleBuilder ) {
        if (stylePolygon.polygonLabel.get() != null) {
            new StyleLabelToSLDVisitor( stylePolygon.polygonLabel.get() ).fillSLD( builder, ruleBuilder );
        }
        if (stylePolygon.fill.get() != null || stylePolygon.strokeWidth.get() != null
                || stylePolygon.image.get() != null) {
            PolygonSymbolizerBuilder polygonBuilder = ruleBuilder.polygon();
            if (stylePolygon.fill.get() != null) {
                FillBuilder fillBuilder = builder.polygonFill( polygonBuilder );
                fillBuilder.color( new StyleColorToSLDHelper().getSLDColor( stylePolygon.fill.get() ) );
                if(stylePolygon.fillOpacity.get() != null) {
                    fillBuilder.opacity( stylePolygon.fillOpacity.get() );
                }
            }
            if (stylePolygon.strokeWidth.get() != null) {
                StrokeBuilder strokeBuilder = polygonBuilder.stroke();
                strokeBuilder.width( stylePolygon.strokeWidth.get() );
                strokeBuilder.color( new StyleColorToSLDHelper().getSLDColor( stylePolygon.strokeColor.get() ) );
            }
            if (stylePolygon.figure.get() != null || stylePolygon.image.get() != null
                    || stylePolygon.symbolSize.get() != null) {
                GraphicBuilder polygonGraphicBuilder = builder.polygonGraphicBuilder( polygonBuilder );
                fillSLD( polygonGraphicBuilder );
                if (stylePolygon.symbolSize.get() != null) {
                    polygonGraphicBuilder.size( stylePolygon.symbolSize.get() );
                }
            }
        }
    }


    public void fillSLD( GraphicBuilder graphicBuilder ) {
        if (stylePolygon.figure.get() != null) {
            new StyleFigureToSLDHelper().fillSLD( stylePolygon.figure.get(), graphicBuilder.mark() );
        }
        else if (stylePolygon.image.get() != null) {
            new StyleImageToSLDHelper().fillSLD( stylePolygon.image.get(), graphicBuilder );
        }
    }
}
