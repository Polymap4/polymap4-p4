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

import org.opengis.style.Font;
import org.polymap.p4.style.entities.StyleFont;
import org.polymap.p4.style.sld.from.AbstractStyleFromSLDVisitor;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFontFromSLDHelper
        extends AbstractStyleFromSLDVisitor {

    public StyleFont fromSLD( StyleFont styleFont, Font font ) {
        if (font.getFamily().size() > 0) {
            styleFont.family.set( (String)font.getFamily().get( 0 ).accept( getStringExpressionVisitor(), null ) );
        }
        if (font.getSize() != null) {
            styleFont.size.set( ((Double)font.getSize().accept( getNumberExpressionVisitor(), null )) );
        }
        if (font.getWeight() != null) {
            styleFont.bold.set( (boolean)font.getWeight().accept( getFontWeightExpressionVisitor(), null ) );
        }
        if (font.getStyle() != null) {
            styleFont.italic.set( (boolean)font.getStyle().accept( getFontStyleExpressionVisitor(), null ) );
        }
        return styleFont;
    }
}
