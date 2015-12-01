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

import org.polymap.model2.Composite;
import org.polymap.model2.Property;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleCoord extends Composite {

    public Property<Double> x;
    
    public Property<Double> y;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if(obj == null) {
            return false;
        }
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        StyleCoord other = (StyleCoord)obj;
        if (x == null) {
            if (other.x != null)
                return false;
        }
        else if (!x.equals( other.x ))
            return false;
        if (y == null) {
            if (other.y != null)
                return false;
        }
        else if (!y.equals( other.y ))
            return false;
        return true;
    }
}
