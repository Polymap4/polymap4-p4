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
package org.polymap.p4.style.ui;

import java.util.function.Supplier;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.entities.StyleIdent;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.entities.StyleLine;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.entities.StylePolygon;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylerPageFactory {

    private final StylerUIFactory            stylerUIFactory;

    private final CurrentFeatureTypeProvider currentFeatureTypeProvider;

    private final UnitOfWork                 newSimpleStylerUnitOfWork;


    public StylerPageFactory( StylerUIFactory stylerUIFactory, CurrentFeatureTypeProvider currentFeatureTypeProvider,
            UnitOfWork newSimpleStylerUnitOfWork ) {
        this.stylerUIFactory = stylerUIFactory;
        this.currentFeatureTypeProvider = currentFeatureTypeProvider;
        this.newSimpleStylerUnitOfWork = newSimpleStylerUnitOfWork;
    }


    public DefaultFormPage createStyleIdentPage( StyleIdent styleIdent ) {
        return new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                stylerUIFactory.getIdentUI().setModel( styleIdent );
                stylerUIFactory.getIdentUI().createContents( formSite );
            }
        };
    }


    public DefaultFormPage createStyleLabelPage( Supplier<StyleLabel> labelFunction ) {
        DefaultFormPage page;
        page = new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                stylerUIFactory.getLabelUI().setFeatureType( currentFeatureTypeProvider.get() );
                stylerUIFactory.getLabelUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
                stylerUIFactory.getLabelUI().setModelFunction( labelFunction );
                stylerUIFactory.getLabelUI().createContents( formSite );
            }
        };
        return page;
    }


    public DefaultFormPage createStylePointPage( Supplier<StylePoint> pointFunction ) {
        DefaultFormPage page;
        page = new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                stylerUIFactory.getPointUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
                stylerUIFactory.getPointUI().setModelFunction( pointFunction );
                stylerUIFactory.getPointUI().createContents( formSite );
            }
        };
        return page;
    }


    public DefaultFormPage createStyleLinePage( Supplier<StyleLine> styleLineFunction ) {
        DefaultFormPage page;
        page = new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                stylerUIFactory.getLineUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
                stylerUIFactory.getLineUI().setModelFunction( styleLineFunction );
                stylerUIFactory.getLineUI().createContents( formSite );
            }
        };
        return page;
    }


    public DefaultFormPage createStylePolygonPage( Supplier<StylePolygon> stylePolygonFunction ) {
        DefaultFormPage page;
        page = new DefaultFormPage() {

            @Override
            public void createFormContents( IFormPageSite formSite ) {
                stylerUIFactory.getPolygonUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
                stylerUIFactory.getPolygonUI().setModelFunction( stylePolygonFunction );
                stylerUIFactory.getPolygonUI().createContents( formSite );
            }
        };
        return page;
    }
}
