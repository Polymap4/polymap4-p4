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

import java.awt.Color;

import org.opengis.filter.expression.Expression;
import org.polymap.model2.Property;
import org.polymap.p4.style.entities.StyleColor;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleColorFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    public StyleColor fromSLD( Property<StyleColor> styleColorProp, Expression colorExpression ) {
        Color awtColor = (Color) colorExpression.accept( getColorExpressionVisitor(), null ); 
        styleColorProp.createValue(color -> initStyleColor(color, awtColor));
        return styleColorProp.get();
    }
    
    private StyleColor initStyleColor( StyleColor color, Color awtColor ) {
        color.red.set( awtColor.getRed() );
        color.green.set( awtColor.getGreen() );
        color.blue.set( awtColor.getBlue() );
        return color;
    }    
}
