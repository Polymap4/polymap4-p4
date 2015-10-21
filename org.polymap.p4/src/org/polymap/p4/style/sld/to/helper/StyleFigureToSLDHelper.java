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

import org.geotools.styling.builder.FillBuilder;
import org.geotools.styling.builder.MarkBuilder;
import org.geotools.styling.builder.StrokeBuilder;
import org.polymap.p4.style.entities.StyleFigure;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFigureToSLDHelper {

    public void fillSLD( StyleFigure styleFigure, MarkBuilder markBuilder ) {
        markBuilder.name( styleFigure.markerWellKnownName.get() );
        if (styleFigure.markerFill.get() != null || styleFigure.markerTransparency.get() != null) {
            FillBuilder fillBuilder = markBuilder.fill();
            if (styleFigure.markerFill.get() != null) {
                fillBuilder.color( new StyleColorToSLDHelper().getSLDColor( styleFigure.markerFill.get() ) );
            }
            if (styleFigure.markerTransparency.get() != null) {
                fillBuilder.opacity( styleFigure.markerTransparency.get() );
            }
        }
        StrokeBuilder strokeBuilder = markBuilder.stroke();
        if (styleFigure.markerStrokeSize.get() != null && styleFigure.markerStrokeSize.get() > 0) {
            strokeBuilder.width( styleFigure.markerStrokeSize.get() );
            if (styleFigure.markerStrokeColor.get() != null) {
                strokeBuilder.color( new StyleColorToSLDHelper().getSLDColor( styleFigure.markerStrokeColor.get() ) );
            }
            if (styleFigure.markerStrokeTransparency.get() != null) {
                strokeBuilder.opacity( styleFigure.markerStrokeTransparency.get() );
            }
        }
    }
}
