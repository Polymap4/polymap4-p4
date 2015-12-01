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

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.StylePolygon;
import org.polymap.p4.style.icon.IImageInfo;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePolygonUI
        extends AbstractStylerFragmentUI {

    private final IAppContext         context;

    private final IPanelSite          panelSite;

    private final Context<IImageInfo> imageInfoInContext;

    private final Context<IColorInfo> colorInfoInContext;

    private Supplier<StylePolygon>    stylePolygonSupplier   = null;

    private UnitOfWork                stylePolygonUnitOfWork = null;

    private StylePolygon              stylePolygon           = null;


    public StylePolygonUI( IAppContext context, IPanelSite panelSite, Context<IImageInfo> imageInfoInContext,
            Context<IColorInfo> colorInfoInContext ) {
        this.context = context;
        this.panelSite = panelSite;
        this.imageInfoInContext = imageInfoInContext;
        this.colorInfoInContext = colorInfoInContext;
    }


    public void setModelFunction( Supplier<StylePolygon> stylePolygonSupplier ) {
        this.stylePolygonSupplier = stylePolygonSupplier;
        this.stylePolygon = null;
    }


    public void setUnitOfWork( UnitOfWork stylePolygonUnitOfWork ) {
        this.stylePolygonUnitOfWork = stylePolygonUnitOfWork;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        if(stylePolygon == null) {
            stylePolygon = stylePolygonSupplier.get();
        }
        // TODO
        return site.getPageBody();
    }


    @Override
    public void submitUI() {
        if(stylePolygonUnitOfWork != null && stylePolygonUnitOfWork.isOpen()) {
            stylePolygonUnitOfWork.commit();
        }
    }


    @Override
    public void resetUI() {
        if(stylePolygonUnitOfWork != null && stylePolygonUnitOfWork.isOpen()) {
            stylePolygonUnitOfWork.close();
        }
    }
}
