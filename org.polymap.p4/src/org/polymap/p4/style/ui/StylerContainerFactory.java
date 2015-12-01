/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.SimpleStyler;
import org.polymap.p4.style.entities.AbstractSLDModel;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleComposite;
import org.polymap.p4.style.entities.StyleFeature;
import org.polymap.p4.style.entities.StyleIdent;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.entities.StyleLine;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.entities.StylePolygon;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.batik.BatikFormContainer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylerContainerFactory {

    private Map<String,Function<Composite,Composite>>      styleContainers       = new HashMap<String,Function<Composite,Composite>>();

    private Map<FeatureType,Function<Composite,Composite>> geometryContainers    = new HashMap<FeatureType,Function<Composite,Composite>>();

    private MdToolkit                                      mdToolkit;

    private final StylerPageFactory                        stylerPageFactory;

    private final CurrentFeatureTypeProvider               currentFeatureTypeProvider;

    private UnitOfWork                                     newGeometryUnitOfWork = null;

    private final StylerUIFactory                          stylerUIFactory;


    public StylerContainerFactory( SimpleStyler simpleStyler, MdToolkit mdToolkit, StylerPageFactory stylerPageFactory,
            CurrentFeatureTypeProvider currentFeatureTypeProvider, StylerUIFactory stylerUIFactory ) {
        this.mdToolkit = mdToolkit;
        this.stylerPageFactory = stylerPageFactory;
        this.currentFeatureTypeProvider = currentFeatureTypeProvider;
        this.stylerUIFactory = stylerUIFactory;
    }


    public void createStyleIdentUI( StyleIdent styleIdent ) {
        String label = StylerUIConstants.IDENT_STR;
        DefaultFormPage page = stylerPageFactory.createStyleIdentPage( styleIdent );
        currentFeatureTypeProvider.set( styleIdent.featureType.get() );
        registerBatikContainer( page, label, null );
    }


    public void createStyleLabelUI( SimpleStyler simpleStyler ) {
        StyleFeature styleFeature = simpleStyler.styleFeatures.iterator().next();
        StyleComposite styleComposite = styleFeature.styleComposite.get();
        Supplier<StyleLabel> styleLabelFunction = ( ) -> styleComposite.styleLabels.isEmpty() ? styleComposite.styleLabels
                .createElement( null )
                : styleComposite.styleLabels.iterator().next();
        createStyleLabelUI( styleLabelFunction );
    }


    public void createStyleLabelUI( SimpleStyler simpleStyler, FeatureType ft ) {
        Supplier<StyleLabel> styleLabelSupplier = getStyleLabelForFeatureType( simpleStyler, ft );
        createStyleLabelUI( styleLabelSupplier );
    }


    private void createStyleLabelUI( Supplier<StyleLabel> styleLabelSupplier ) {
        String label = StylerUIConstants.LABEL_STR;
        DefaultFormPage page = stylerPageFactory.createStyleLabelPage( styleLabelSupplier );
        registerBatikContainer( page, label, null );
        stylerUIFactory.getModels().put( FeatureType.TEXT, styleLabelSupplier );
    }


    public void createStylePointUI( SimpleStyler simpleStyler, UnitOfWork unitOfWork ) {
        StyleFeature styleFeature = simpleStyler.styleFeatures.iterator().next();
        StyleComposite styleComposite = styleFeature.styleComposite.get();
        Supplier<StylePoint> stylePointFunction = ( ) -> styleComposite.stylePoints.isEmpty() ? styleComposite.stylePoints
                .createElement( null )
                : styleComposite.stylePoints.iterator().next();
        String label = StylerUIConstants.STYLE_STR;
        DefaultFormPage page = stylerPageFactory.createStylePointPage( stylePointFunction );
        registerBatikContainer( page, label, currentFeatureTypeProvider.get() );
        stylerUIFactory.getModels().put( FeatureType.POINT, stylePointFunction );
    }


    public void createStyleLineUI( SimpleStyler simpleStyler, UnitOfWork unitOfWork ) {
        StyleFeature styleFeature = simpleStyler.styleFeatures.iterator().next();
        StyleComposite styleComposite = styleFeature.styleComposite.get();
        Supplier<StyleLine> styleLineFunction = ( ) ->
            styleComposite.styleLines.isEmpty() ? styleComposite.styleLines
                        .createElement( null ) : styleComposite.styleLines.iterator().next();
        String label = StylerUIConstants.STYLE_STR;
        DefaultFormPage page = stylerPageFactory.createStyleLinePage( styleLineFunction );
        registerBatikContainer( page, label, currentFeatureTypeProvider.get() );
        stylerUIFactory.getModels().put( FeatureType.LINE_STRING, styleLineFunction );
    }


    public void createStylePolygonUI( SimpleStyler simpleStyler, UnitOfWork unitOfWork ) {
        StyleFeature styleFeature = simpleStyler.styleFeatures.iterator().next();
        StyleComposite styleComposite = styleFeature.styleComposite.get();
        Supplier<StylePolygon> stylePolygonFunction = ( ) -> styleComposite.stylePolygons.isEmpty() ? styleComposite.stylePolygons
                .createElement( null )
                : styleComposite.stylePolygons.iterator().next();
        String label = StylerUIConstants.STYLE_STR;
        DefaultFormPage page = stylerPageFactory.createStylePolygonPage( stylePolygonFunction );
        registerBatikContainer( page, label, currentFeatureTypeProvider.get() );
        stylerUIFactory.getModels().put( FeatureType.POLYGON, stylePolygonFunction );
    }


    private void registerBatikContainer( DefaultFormPage page, String label, FeatureType featureType ) {
        BatikFormContainer pageContainer = new BatikFormContainer( page );
        Function<Composite,Composite> contentFunction = createContentFunction( mdToolkit, label, pageContainer );
        if (featureType != null) {
            geometryContainers.put( featureType, contentFunction );
        }
        styleContainers.put( label, contentFunction );
    }


    private Function<Composite,Composite> createContentFunction( MdToolkit tk, String label,
            BatikFormContainer pageContainer ) {
        return new Function<Composite,Composite>() {

            @Override
            public Composite apply( Composite parent ) {
                Composite composite = tk.createComposite( parent, SWT.NONE );
                pageContainer.createContents( composite );
                return composite;
            }
        };
    }


    public Function<Composite,Composite> getOrCreateGeometryContainer( SimpleStyler simpleStyler,
            UnitOfWork newSimpleStylerUnitOfWork,
            FeatureType featureType ) {
        Function<Composite,Composite> containerFunction = geometryContainers.get( featureType );
        if (containerFunction == null) {
            if (newGeometryUnitOfWork != null) {
                if (newGeometryUnitOfWork.isOpen()) {
                    // ask for save or at least show confirmation dialog
                    newGeometryUnitOfWork.close();
                }
            }
            newGeometryUnitOfWork = newSimpleStylerUnitOfWork.newUnitOfWork();
            switch (featureType) {
                case POINT:
                    createStylePointUI( simpleStyler, newGeometryUnitOfWork );
                    break;
                case LINE_STRING:
                    createStyleLineUI( simpleStyler, newGeometryUnitOfWork );
                    break;
                case POLYGON:
                    createStylePolygonUI( simpleStyler, newGeometryUnitOfWork );
                    break;
                default: {
                }
            }
            containerFunction = geometryContainers.get( featureType );
        }
        return containerFunction;
    }


    public Supplier<StyleLabel> getStyleLabelForFeatureType( SimpleStyler simpleStyler, FeatureType featureType ) {
        Supplier<? extends AbstractSLDModel> modelfunction = stylerUIFactory.getModels().get( featureType );
        Supplier<StyleLabel> styleLabelSupplier = null;
        switch (featureType) {
            case TEXT:
                styleLabelSupplier = ( ) -> (StyleLabel)modelfunction.get();
                createStylePointUI( simpleStyler, newGeometryUnitOfWork );
                break;
            case POINT:
                styleLabelSupplier = ( ) -> getOrCreateLabel( ((StylePoint)modelfunction.get()).markerLabel );
                createStylePointUI( simpleStyler, newGeometryUnitOfWork );
                break;
            case LINE_STRING:
                styleLabelSupplier = ( ) -> getOrCreateLabel( ((StyleLine)modelfunction.get()).lineLabel );
                createStyleLineUI( simpleStyler, newGeometryUnitOfWork );
                break;
            case POLYGON:
                styleLabelSupplier = ( ) -> getOrCreateLabel( ((StylePolygon)modelfunction.get()).polygonLabel );
                createStylePolygonUI( simpleStyler, newGeometryUnitOfWork );
                break;
            default: {
            }
        }
        return styleLabelSupplier;
    }


    private StyleLabel getOrCreateLabel( Property<StyleLabel> prop ) {
        return prop.get() == null ? prop.createValue( null ) : prop.get();
    }


    public Map<String,Function<Composite,Composite>> getStyleContainers() {
        return styleContainers;
    }
}
