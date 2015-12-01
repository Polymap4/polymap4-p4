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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.AbstractSLDModel;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleLine;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.entities.StylePolygon;
import org.polymap.p4.style.font.IFontInfo;
import org.polymap.p4.style.icon.IImageInfo;
import org.polymap.p4.style.label.IStyleLabelInfo;
import org.polymap.p4.style.point.IStylePointInfo;
import org.polymap.p4.style.ui.label.StyleLabelUI;
import org.polymap.p4.style.ui.point.StylePointUI;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylerUIFactory {

    private final StyleIdentUI                                          identUI;

    private final StyleLabelUI                                          labelUI;

    private final StylePointUI                                          pointUI;

    private final StyleLineUI                                           lineUI;

    private final StylePolygonUI                                        polygonUI;

    private final Map<FeatureType,Supplier<? extends AbstractSLDModel>> models;


    public StylerUIFactory( IAppContext context, IPanelSite panelSite, UnitOfWork newSimpleStylerUnitOfWork,
            Context<IImageInfo> imageInfoInContext, Context<IColorInfo> colorInfoInContext,
            Context<IFontInfo> fontInfoInContext, Context<IStyleLabelInfo> styleLabelInfo, Context<IStylePointInfo> stylePointInfo ) {
        identUI = new StyleIdentUI( context, panelSite );
        labelUI = new StyleLabelUI( context, panelSite, fontInfoInContext, colorInfoInContext, styleLabelInfo );
        pointUI = new StylePointUI( context, panelSite, stylePointInfo );
        lineUI = new StyleLineUI( context, panelSite, imageInfoInContext, colorInfoInContext, stylePointInfo );
        polygonUI = new StylePolygonUI( context, panelSite, imageInfoInContext, colorInfoInContext );

        models = new HashMap<FeatureType,Supplier<? extends AbstractSLDModel>>();
    }


    public StyleIdentUI getIdentUI() {
        return identUI;
    }


    public StyleLabelUI getLabelUI() {
        return labelUI;
    }


    public StylePointUI getPointUI() {
        return pointUI;
    }


    public StyleLineUI getLineUI() {
        return lineUI;
    }


    public StylePolygonUI getPolygonUI() {
        return polygonUI;
    }


    public Map<FeatureType,Supplier<? extends AbstractSLDModel>> getModels() {
        return models;
    }


    @SuppressWarnings("unchecked")
    public void updateGeometryUI( UnitOfWork newSimpleStylerUnitOfWork, FeatureType ft ) {
        if (ft == FeatureType.POINT) {
            getPointUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
            getPointUI().setModelFunction( (Supplier<StylePoint>)models.get( ft ) );
        }
        else if (ft == FeatureType.LINE_STRING) {
            getLineUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
            getLineUI().setModelFunction( (Supplier<StyleLine>)models.get( ft ) );
        }
        else if (ft == FeatureType.POLYGON) {
            getPolygonUI().setUnitOfWork( newSimpleStylerUnitOfWork.newUnitOfWork() );
            getPolygonUI().setModelFunction( (Supplier<StylePolygon>)models.get( ft ) );
        }
    }
}
