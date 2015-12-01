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

import java.util.EventObject;
import java.util.List;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.p4.style.AbstractFormFieldInfo;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FontInfo
        extends AbstractFormFieldInfo
        implements IFontInfo {

    private RGB        rgb      = null;

    private FontData[] fontList = new FontData[0];


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.p4.style.IColorInfo#getColor()
     */
    @Override
    public RGB getColor() {
        return rgb;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.p4.style.IColorInfo#setColor(org.eclipse.swt.graphics.RGB)
     */
    @Override
    public void setColor( RGB rgb ) {
        this.rgb = rgb;
    }


    @EventHandler(delay = 100, display = true)
    protected void onStatusChange( List<EventObject> evs ) {
        evs.forEach( ev -> {
            if (ev.getSource() == this) {
                getFormField().setValue( new Object[] { getFontData(), getColor() } );
            }
        } );
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.p4.style.font.IFontInfo#setFontData(org.eclipse.swt.graphics.FontData
     * [])
     */
    @Override
    public void setFontData( FontData[] fontList ) {
        this.fontList = fontList;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.p4.style.font.IFontInfo#getFontData()
     */
    @Override
    public FontData[] getFontData() {
        return fontList;
    }
}
