/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.TextSymbolizer;
import org.opengis.style.ExternalGraphic;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.LineCapType;
import org.polymap.p4.style.entities.StyleLine;
import org.polymap.p4.style.sld.from.helper.StyleColorFromSLDHelper;

import com.google.common.base.Joiner;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLineFromSLDVisitor
        extends AbstractStyleFromSLDVisitor {

    private final StyleLine styleLine;

    private boolean         firstLine  = true;

    private boolean         firstRule  = true;

    private boolean         borderMode = false;


    public StyleLineFromSLDVisitor( StyleLine styleLine ) {
        this.styleLine = styleLine;
    }


    @Override
    public void visit( FeatureTypeStyle fts ) {
        for (Rule rule : fts.rules()) {
            rule.accept( this );
            firstRule = false;
        }
        firstRule = true;
        borderMode = true;
    }


    @Override
    public void visit( Rule rule ) {
        Arrays.asList( rule.getSymbolizers() )
                .stream()
                .filter( symb -> symb instanceof TextSymbolizer )
                .forEach(
                        t -> new StyleLabelFromSLDVisitor( getStyleLineToUse().lineLabel.createValue( null ),
                                FeatureType.LINE_STRING )
                                .visit( (TextSymbolizer)t ) );
        List<LineSymbolizer> lines = getLinesSortedAscendentByLineWidth( rule );
        for (LineSymbolizer line : lines) {
            line.accept( this );
            firstLine = false;
        }
        firstLine = true;
    }


    private List<LineSymbolizer> getLinesSortedAscendentByLineWidth( Rule rule ) {
        return Arrays
                .asList( rule.getSymbolizers() )
                .stream()
                .filter( symb -> symb instanceof LineSymbolizer )
                .map( symb -> (LineSymbolizer)symb )
                .sorted(
                        ( LineSymbolizer line1, LineSymbolizer line2 ) -> (Double.valueOf( (double)line1.getStroke()
                                .getWidth().accept( getNumberExpressionVisitor(), null ) )).compareTo( (double)line2
                                .getStroke().getWidth().accept( getNumberExpressionVisitor(), null ) ) )
                .collect( Collectors.<LineSymbolizer>toList() );
    }


    @Override
    public void visit( org.geotools.styling.LineSymbolizer line ) {
        if (line.getStroke() != null) {
            line.getStroke().accept( this );
        }
    }


    @Override
    public void visit( Stroke stroke ) {
        StyleLine styleLineToUse = getStyleLineToUse();
        if (stroke.getColor() != null) {
            new StyleColorFromSLDHelper().fromSLD( styleLineToUse.lineColor, stroke.getColor() );
        }
        if (stroke.getWidth() != null) {
            styleLineToUse.lineWidth.set( ((Double)stroke.getWidth().accept( getNumberExpressionVisitor(), null ))
                    .intValue() );
        }
        if (stroke.getGraphicStroke() != null) {
            if (stroke.getGraphicStroke().graphicalSymbols().stream().filter( symbol -> symbol instanceof Mark )
                    .map( symbol -> (Mark)symbol ).anyMatch( mark -> mark.getWellKnownName() != null )
                    || stroke.getGraphicStroke().graphicalSymbols().stream()
                            .anyMatch( symbol -> symbol instanceof ExternalGraphic )) {
                styleLineToUse.lineSymbol.createValue( symbol -> {
                    new StylePointFromSLDVisitor( symbol ).visit( stroke.getGraphicStroke() );
                    return symbol;
                } );
            }
        }
        if (stroke.getLineCap() != null) {
            styleLineToUse.lineCap.set( LineCapType.getTypeForLabel( (String)stroke.getLineCap().accept(
                    getStringExpressionVisitor(), null ) ) );
        }
        if (stroke.getDashArray() != null && stroke.getDashArray().length > 0) {
            List<String> parts = new ArrayList<String>();
            for (float value : stroke.getDashArray()) {
                parts.add( String.valueOf( value ) );
            }
            styleLineToUse.lineDashPattern.set( (String)Joiner.on( " " ).join( parts ) );
            if (stroke.getDashOffset() != null) {
                styleLineToUse.lineDashOffset.set( ((Double)stroke.getDashOffset().accept(
                        getNumberExpressionVisitor(), null )) );
            }
        }
    }


    private StyleLine getStyleLineToUse() {
        StyleLine styleLineToUse = null;
        if (borderMode) {
            styleLineToUse = styleLine.border.get();
            if (styleLineToUse == null) {
                styleLineToUse = styleLine.border.createValue( null );
            }
        }
        else if (!firstRule) {
            styleLineToUse = styleLine.additionalLineStyles.createElement( null );
        }
        else if (!firstLine) {
            styleLineToUse = styleLine.alternatingLineStyles.createElement( null );
        }
        else {
            styleLineToUse = styleLine;
        }
        return styleLineToUse;
    }
}
