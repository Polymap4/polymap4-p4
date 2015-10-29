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
package org.polymap.p4.imports.utils.prompt;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.polymap.p4.imports.utils.ISelectionAware;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CharSetSelection
        implements ISelectionAware<Charset,Pair<String,String>> {
    
    private Charset selected = null;
    private final List<Pair<String,String>> selectable;
    
    public CharSetSelection() {
        selectable = Charset.availableCharsets().entrySet().stream()
                .map( entry -> Pair.of( entry.getKey(), entry.getValue().displayName() ) )
                .collect( Collectors.toList() );
    }

    public List<Pair<String,String>> getSelectable() {
        return selectable;
    }


    public Pair<String,String> getDefault() {
        return Pair.of( "UTF-8", "UTF-8" );
    }


    @Override
    public Charset getSelected() {
        return selected;
    }


    @Override
    public void setSelected( Charset selected ) {
        this.selected = selected;
    }
}
