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
package org.polymap.p4.style.font;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FontDAO {

    public static final String FONT_FAMILY = "fontFamily";

    public static final String FONT_SIZE   = "fontSize";

    public static final String FONT_BOLD   = "fontBold";

    public static final String FONT_ITALIC = "fontItalic";

    public static final String FONT_COLOR  = "fontColor";

    private FontData           fontData    = null;

    private String             fontFamily  = null;

    private Integer            fontSize    = null;

    private Boolean            fontBold    = Boolean.FALSE;

    private Boolean            fontItalic  = Boolean.FALSE;

    private RGB                fontColor   = null;


    public FontData getFontData() {
        if(fontData != null) {
            fontData.setName( getFontFamily() );
            fontData.setHeight( getFontSize() );
            int style = SWT.NORMAL;
            if (fontBold) {
                style |= SWT.BOLD;
            }
            if (fontItalic) {
                style |= SWT.ITALIC;
            }
            fontData.setStyle( style );
        }
        return fontData;
    }


    public String getFontFamily() {
        return fontFamily;
    }


    public void setFontFamily( String fontFamily ) {
        this.fontFamily = fontFamily;
    }


    public Integer getFontSize() {
        return fontSize;
    }


    public void setFontSize( Integer fontSize ) {
        this.fontSize = fontSize;
    }


    public Boolean isFontBold() {
        return fontBold;
    }


    public void setFontBold( Boolean fontBold ) {
        this.fontBold = fontBold;
    }


    public Boolean isFontItalic() {
        return fontItalic;
    }


    public void setFontItalic( Boolean fontItalic ) {
        this.fontItalic = fontItalic;
    }


    public void setFontData( FontData fontData ) {
        this.fontData = fontData;
    }


    public RGB getFontColor() {
        return fontColor;
    }


    public void setFontColor( RGB fontColor ) {
        this.fontColor = fontColor;
    }
}
