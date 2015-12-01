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

import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.geotools.filter.expression.AbstractExpressionVisitor;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylerExpressionVisitor
        extends AbstractExpressionVisitor {

    @Override
    public Object visit( Literal expr, Object extraData ) {
        if (expr.getValue() != null) {
            if (expr.getValue().toString().startsWith( "#" )) {
                String hexValue = expr.getValue().toString();
                Color color = Color.decode( hexValue );
                return new RGB( color.getRed(), color.getGreen(), color.getBlue() );
            }
            else {
                try {
                   return new Double( Double.parseDouble( expr.getValue().toString() ) );
                }
                catch (NumberFormatException nfe) {
                    //
                }
            }
        }
        return expr.getValue();
    }
    
    @Override
    public Object visit( PropertyName expr, Object extraData ) {
        return expr.getPropertyName();
    }
}
