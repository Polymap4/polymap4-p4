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

import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.TextSymbolizer;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleComposite;
import org.polymap.p4.style.entities.StyleLine;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.entities.StylePolygon;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;
import org.polymap.p4.style.sld.from.StyleLabelFromSLDVisitor;
import org.polymap.p4.style.sld.from.StyleLineFromSLDVisitor;
import org.polymap.p4.style.sld.from.StylePointFromSLDVisitor;
import org.polymap.p4.style.sld.from.StylePolygonFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleCompositeFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    private StyleComposite styleComposite = null;

    private TextSymbolizer ts             = null;

    private boolean        linesHandled   = false;


    public StyleCompositeFromSLDHelper( StyleComposite styleComposite ) {
        this.styleComposite = styleComposite;
    }


    @Override
    public void visit( Style style ) {
        if (style.featureTypeStyles().stream().flatMap( fts -> fts.rules().stream() )
                .flatMap( rule -> rule.symbolizers().stream() )
                .anyMatch( symbolizer -> symbolizer instanceof LineSymbolizer )) {
            StyleLine styleLine = styleComposite.styleLines.createElement( null );
            new StyleLineFromSLDVisitor( styleLine ).visit( style );
            linesHandled = true;
        }
        else {
            super.visit( style );
        }
    }


    @Override
    public void visit( TextSymbolizer ts ) {
        this.ts = ts;
    }


    @Override
    public void visit( PointSymbolizer ps ) {
        StylePoint stylePoint = styleComposite.stylePoints.createElement( null );
        if (ts != null) {
            new StyleLabelFromSLDVisitor( stylePoint.markerLabel.createValue( null ), FeatureType.POINT ).visit( ts );
        }
        new StylePointFromSLDVisitor( stylePoint ).visit( ps );
    }


    @Override
    public void visit( LineSymbolizer ls ) {
        // TODO what happens with lines with borders and line associated lines in
        // zoom?
        if (!linesHandled) {
            StyleLine styleLine = styleComposite.styleLines.createElement( null );
            new StyleLineFromSLDVisitor( styleLine ).visit( ls );
        }
    }


    @Override
    public void visit( PolygonSymbolizer ls ) {
        StylePolygon stylePolygon = styleComposite.stylePolygons.createElement( null );
        if (ts != null) {
            new StyleLabelFromSLDVisitor( stylePolygon.polygonLabel.createValue( null ), FeatureType.POLYGON )
                    .visit( ts );
        }
        new StylePolygonFromSLDVisitor( stylePolygon ).visit( ls );
    }
}
