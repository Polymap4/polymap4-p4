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

import java.util.function.Supplier;

import org.geotools.styling.builder.FontBuilder;
import org.polymap.p4.style.entities.StyleFont;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFontToSLDHelper {

    private static String  FONT_FAMILY_DEFAULT = "Serif";

    private static Integer FONT_SIZE_DEFAULT   = 10;

    private static String  FONT_STYLE_DEFAULT  = "normal";

    private static String  FONT_WEIGHT_DEFAULT = "normal";


    public void fillSLD( StyleFont styleFont, Supplier<FontBuilder> fontBuilderSupplier ) {
        if (styleFont.family.get() != null
                && (!FONT_FAMILY_DEFAULT.equals( styleFont.family.get() )
                        || styleFont.size.get().intValue() != FONT_SIZE_DEFAULT
                        || !FONT_STYLE_DEFAULT.equals( styleFont.italic.get() ? "italic" : "normal" ) || !FONT_WEIGHT_DEFAULT
                            .equals( styleFont.bold.get() ? "bold" : "normal" ))) {
            FontBuilder fontBuilder = fontBuilderSupplier.get();
            fontBuilder.familyName( styleFont.family.get() );
            if (styleFont.size.get() != null) {
                fontBuilder.size( styleFont.size.get() );
            }
            if (styleFont.italic.get() != null) {
                fontBuilder.styleName( styleFont.italic.get() ? "italic" : "normal" );
            }
            if (styleFont.bold.get() != null) {
                fontBuilder.weightName( styleFont.bold.get() ? "bold" : "normal" );
            }
        }
    }
}
