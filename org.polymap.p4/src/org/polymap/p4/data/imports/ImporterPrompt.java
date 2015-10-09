/* 
 * Copyright (C) 2015, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.data.imports;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ImporterPrompt
        extends Configurable {
    
    public enum Severity {
        INFO, VERIFY, MANDATORY;
    }

    /** 
     * The severity of this prompt. Defaults to {@link Severity#INFO}. 
     */
    @Mandatory
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterPrompt,Severity> severity;

    /** 
     * Short summary of the idea of this prompt. 
     */
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterPrompt,String>   summary;

    /**
     * What to decide here? What is the effect and consequences? Are there defaults
     * or 'best practices'?
     */
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterPrompt,String>   description;

    /** 
     * The {@link #extendedUI} of this prompt should set it to 'true' when
     * this prompt is verified and/or has enough info from user to allow the
     * importer to run.
     */
    @Mandatory
    @DefaultBoolean( false )
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterPrompt,Boolean>  ok;
    
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterPrompt,PromptUIBuilder> extendedUI;
    

    protected ImporterPrompt() {
        severity.set( Severity.INFO );
    }

    
    abstract ImporterContext context();

    
    /**
     * 
     */
    @FunctionalInterface
    public static interface PromptUIBuilder {
        
        public Composite createContents( ImporterPrompt prompt, Composite parent );
        
    }

}