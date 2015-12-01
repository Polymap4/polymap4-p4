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
package org.polymap.p4.style.sld.from;

import org.geotools.styling.Fill;
import org.geotools.styling.Halo;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.TextSymbolizer;
import org.opengis.style.Font;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.entities.StyleLabelLinePlacement;
import org.polymap.p4.style.entities.StyleLabelPointPlacement;
import org.polymap.p4.style.sld.from.helper.StyleColorFromSLDHelper;
import org.polymap.p4.style.sld.from.helper.StyleFontFromSLDHelper;
import org.polymap.p4.style.sld.from.helper.StyleLabelLinePlacementFromSLDHelper;
import org.polymap.p4.style.sld.from.helper.StyleLabelPointPlacementFromSLDHelper;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelFromSLDVisitor
        extends AbstractStyleFromSLDVisitor {

    private final StyleLabel  styleLabel;

    private final FeatureType host;


    public StyleLabelFromSLDVisitor( StyleLabel styleLabel, FeatureType host ) {
        this.styleLabel = styleLabel;
        this.host = host;
    }


    @Override
    public void visit( TextSymbolizer ts ) {
        if (ts.getLabel() != null) {
            styleLabel.labelText.set( (String)ts.getLabel().accept( getLabelExpressionVisitor(), null ) );
        }
        if (ts.getHalo() != null) {
            ts.getHalo().accept( this );
        }
        handleGeoServerVendorExtensions( ts, styleLabel );
        super.visit( ts );
    }


    @Override
    public void visit( Halo halo ) {
        if (halo.getRadius() != null) {
            styleLabel.haloRadius.set( (double)halo.getRadius().accept( getNumberExpressionVisitor(), null ) );
        }
        if (halo.getFill() != null) {
            new StyleColorFromSLDHelper().fromSLD( styleLabel.haloFill, halo.getFill().getColor() );
        }
    }


    private void handleGeoServerVendorExtensions( TextSymbolizer ts, StyleLabel styleLabel ) {
        if (host != FeatureType.LINE_STRING && StyleLabelPointPlacementFromSLDHelper.containsVendorOption( ts )) {
            new StyleLabelPointPlacementFromSLDHelper( getOrCreatePointPlacement() ).fromSLD( ts );
        }
        if (host != FeatureType.POINT && host != FeatureType.POLYGON && StyleLabelLinePlacementFromSLDHelper.containsVendorOption( ts )) {
            new StyleLabelLinePlacementFromSLDHelper( getOrCreateLinePlacement() ).fromSLD( ts );
        }
        handleDoubleVendorOption( ts, styleLabel.autoWrap );
    }


    @Override
    public void visit( Fill fill ) {
        if (fill.getColor() != null) {
            new StyleColorFromSLDHelper().fromSLD( styleLabel.labelFontColor, fill.getColor() );
        }
        super.visit( fill );
    }


    public void visit( Font font ) {
        if (font.getFamily().size() > 0) {
            styleLabel.labelFont.createValue( newValue -> new StyleFontFromSLDHelper().fromSLD( newValue, font ) );
        }
        super.visit( font );
    }


    @Override
    public void visit( PointPlacement pp ) {
        if (host != FeatureType.LINE_STRING && (pp.getRotation() != null || pp.getAnchorPoint() != null)) {
            new StyleLabelPointPlacementFromSLDHelper( getOrCreatePointPlacement() ).fromSLD( pp );
        }
        super.visit( pp );
    }


    @Override
    public void visit( LinePlacement lp ) {
        if (host != FeatureType.POINT && host != FeatureType.POLYGON && lp.getPerpendicularOffset() != null) {
            new StyleLabelLinePlacementFromSLDHelper( getOrCreateLinePlacement() ).fromSLD( lp );
        }
        super.visit( lp );
    }


    private StyleLabelPointPlacement getOrCreatePointPlacement() {
        StyleLabelPointPlacement pp = styleLabel.pointPlacement.get();
        return pp == null ? styleLabel.pointPlacement.createValue( null ) : pp;
    }


    private StyleLabelLinePlacement getOrCreateLinePlacement() {
        StyleLabelLinePlacement lp = styleLabel.linePlacement.get();
        return lp == null ? styleLabel.linePlacement.createValue( null ) : lp;
    }
}
