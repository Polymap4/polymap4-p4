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
package org.polymap.p4.style.entities;

import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleFigure
        extends StyleGraphics {

    @Nullable
    public Property<String>     markerWellKnownName;

    @Nullable
    public Property<StyleColor> markerFill;

    @Nullable
    public Property<Double>     markerTransparency;

    @Nullable
    public Property<Double>     markerStrokeSize;

    @Nullable
    public Property<StyleColor> markerStrokeColor;

    @Nullable
    public Property<Double>     markerStrokeTransparency;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((markerFill == null) ? 0 : markerFill.hashCode());
        result = prime * result + ((markerStrokeColor == null) ? 0 : markerStrokeColor.hashCode());
        result = prime * result + ((markerStrokeSize == null) ? 0 : markerStrokeSize.hashCode());
        result = prime * result + ((markerStrokeTransparency == null) ? 0 : markerStrokeTransparency.hashCode());
        result = prime * result + ((markerTransparency == null) ? 0 : markerTransparency.hashCode());
        result = prime * result + ((markerWellKnownName == null) ? 0 : markerWellKnownName.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        StyleFigure other = (StyleFigure)obj;
        if (markerFill == null) {
            if (other.markerFill != null)
                return false;
        }
        else if (!markerFill.equals( other.markerFill ))
            return false;
        if (markerStrokeColor == null) {
            if (other.markerStrokeColor != null)
                return false;
        }
        else if (!markerStrokeColor.equals( other.markerStrokeColor ))
            return false;
        if (markerStrokeSize == null) {
            if (other.markerStrokeSize != null)
                return false;
        }
        else if (!markerStrokeSize.equals( other.markerStrokeSize ))
            return false;
        if (markerStrokeTransparency == null) {
            if (other.markerStrokeTransparency != null)
                return false;
        }
        else if (!markerStrokeTransparency.equals( other.markerStrokeTransparency ))
            return false;
        if (markerTransparency == null) {
            if (other.markerTransparency != null)
                return false;
        }
        else if (!markerTransparency.equals( other.markerTransparency ))
            return false;
        if (markerWellKnownName == null) {
            if (other.markerWellKnownName != null)
                return false;
        }
        else if (!markerWellKnownName.equals( other.markerWellKnownName ))
            return false;
        return true;
    }
}
