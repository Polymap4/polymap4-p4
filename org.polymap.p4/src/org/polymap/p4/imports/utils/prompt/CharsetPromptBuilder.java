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

import org.apache.commons.lang3.tuple.Pair;
import org.polymap.p4.data.imports.ImporterPrompt;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CharsetPromptBuilder
        extends AbstractPromptBuilder<Charset,Pair<String,String>> {

    public CharsetPromptBuilder( CharSetSelection charSetProvider ) {
        super( charSetProvider );
    }


    @Override
    protected boolean getInitialSelection( Pair<String,String> cs ) {
        boolean selected = false;
        if (getProvider().getSelected() == null) {
            selected = cs.getKey() == getProvider().getDefault().getKey();
        }
        else {
            selected = cs.getKey() == getProvider().getSelected().name();
        }
        return selected;
    }


    @Override
    protected Pair<String,String> transformFromDisplayValue( String listEntry ) {
        return getProvider().getSelectable().stream().filter( selectable -> selectable.getValue().equals( listEntry ) )
                .findFirst().get();
    }


    @Override
    protected String transformToDisplayValue( Pair<String,String> value ) {
        return value.getValue();
    }


    @Override
    protected Pair<String,String> transformToValueObject( Charset selected ) {
        return Pair.of( selected.name(), selected.displayName() );
    }


    @Override
    public void submit( ImporterPrompt prompt ) {
        Charset charset = Charset.forName( getValue().getKey() );
        getProvider().setSelected( charset );
        prompt.ok.set( true );
        prompt.value.put( charset.displayName() );
    }
}
