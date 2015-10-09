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
package org.polymap.p4.style.color;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.EventObject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.p4.P4Plugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ColorPanel
        extends DefaultPanel {

    enum COLOR_WIDGET_TYPE {
        PREPARE, BOX, HEX, SPINNER, DISPLAY, PALETTE
    };

    /**
     * 
     */
    private static final int            COLORBOX_HEIGHT = 200;

    /**
     * 
     */
    private static final int            COLORBOX_WIDTH  = 200;

    public static final PanelIdentifier ID              = PanelIdentifier.parse( "color" );


    private class PaletteListener
            extends MouseAdapter {

        private final Composite panelBody;

        private final RGB       rgb;


        public PaletteListener( Composite panelBody, RGB rgb ) {
            this.panelBody = panelBody;
            this.rgb = rgb;
        }


        @Override
        public void mouseDown( MouseEvent event ) {
            updateColorWidgets( panelBody, rgb, COLOR_WIDGET_TYPE.PALETTE );
        }
    }

    private boolean spinnerListenerActive = true;


    private class SpinnerListener
            implements ModifyListener {

        private final Composite panelBody;

        private final Spinner   spinner;

        private final int       colorIndex;


        public SpinnerListener( Composite panelBody, Spinner spinner, int colorIndex ) {
            this.panelBody = panelBody;
            this.spinner = spinner;
            this.colorIndex = colorIndex;
        }


        public void modifyText( ModifyEvent event ) {
            if (spinnerListenerActive) {
                updateColorFomSpinner( panelBody, colorIndex, spinner.getSelection() );
            }
        }
    }

    private static final int    PALETTE_BOX_SIZE        = 12;

    private static final int    PALETTE_BOXES_IN_ROW    = 14;

    private static final int    COLOR_DISPLAY_BOX_SIZE  = 76;

    private static final int    MAX_RGB_COMPONENT_VALUE = 255;

    // Color components
    private static final int    RED                     = 0;

    private static final int    GREEN                   = 1;

    private static final int    BLUE                    = 2;

    // Palette colors
    private static final RGB[]  PALETTE_COLORS          = new RGB[] { new RGB( 0, 0, 0 ), new RGB( 70, 70, 70 ),
            new RGB( 120, 120, 120 ), new RGB( 153, 0, 48 ), new RGB( 237, 28, 36 ), new RGB( 255, 126, 0 ),
            new RGB( 255, 194, 14 ), new RGB( 255, 242, 0 ), new RGB( 168, 230, 29 ), new RGB( 34, 177, 76 ),
            new RGB( 0, 183, 239 ), new RGB( 77, 109, 243 ), new RGB( 47, 54, 153 ), new RGB( 111, 49, 152 ),
            new RGB( 255, 255, 255 ), new RGB( 220, 220, 220 ), new RGB( 180, 180, 180 ), new RGB( 156, 90, 60 ),
            new RGB( 255, 163, 177 ), new RGB( 229, 170, 122 ), new RGB( 245, 228, 156 ), new RGB( 255, 249, 189 ),
            new RGB( 211, 249, 188 ), new RGB( 157, 187, 97 ), new RGB( 153, 217, 234 ), new RGB( 112, 154, 209 ),
            new RGB( 84, 109, 142 ), new RGB( 181, 165, 213 ) };

    private RGB                 rgb;

    private Label               colorDisplay;

    private Label               oldColorDisplay;

    private Canvas              colorBox;

    private CLabel              colorBoxMarker;

    private Spinner             spRed;

    private Spinner             spBlue;

    private Spinner             spGreen;

    private Text                colorHex;

    private MdToolkit           toolkit;

    private Button              applyButton;

    @Scope(P4Plugin.Scope)
    private Context<IColorInfo> colorInfo;


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.batik.IPanel#createContents(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Color Selection" );
        toolkit = (MdToolkit)getSite().toolkit();
        panelBody.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        prepareOpen( panelBody );
    }


    /**
     * Returns the currently selected color in the receiver.
     *
     * @return the RGB value for the selected color, may be null
     * @see PaletteData#getRGBs
     */
    public RGB getRGB() {
        return rgb;
    }


    /**
     * Sets the receiver's selected color to be the argument.
     *
     * @param rgb the new RGB value for the selected color, may be null to let the
     *        platform select a default when open() is called
     * @see PaletteData#getRGBs
     */
    public void setRGB( RGB rgb ) {
        this.rgb = rgb;
    }


    protected void prepareOpen( Composite panelBody ) {
        createControls( panelBody );
        if (rgb == null) {
            updateColorWidgets( panelBody, new RGB( 255, 255, 255 ), COLOR_WIDGET_TYPE.PREPARE );
        }
    }


    private void createControls( Composite panelBody ) {
        Composite top = toolkit.createComposite( panelBody, SWT.NONE );
        top.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        FormDataFactory.on( top ).top( 5 );

        Composite left = toolkit.createComposite( top, SWT.NONE );
        left.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );

        colorBox = new Canvas( left, SWT.NONE );
        colorBox.setBounds( new Rectangle( 0, 0, COLORBOX_WIDTH, COLORBOX_HEIGHT ) );
        FormDataFactory.on( colorBox ).width( COLORBOX_WIDTH ).height( COLORBOX_HEIGHT );
        colorBoxMarker = new CLabel( left, SWT.NONE );
        colorBoxMarker.setText( "O" );
        colorBoxMarker.moveAbove( null );

        colorBox.addListener( SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent( Event event ) {
                handleColorBoxMarkerPositionChanged( colorBox, colorBox.getBounds().width, event.x, event.y );
            }

        } );
        colorBoxMarker.addDragDetectListener( new DragDetectListener() {

            @Override
            public void dragDetected( DragDetectEvent event ) {
                handleColorBoxMarkerPositionChanged( colorBox, colorBox.getBounds().width, event.x, event.y );
            }
        } );
        setColorBoxColor( panelBody, getRGB() );

        Composite hexField = createHexField( left );
        FormDataFactory.on( hexField ).top( colorBox, dp( 30 ).pix() ).left( 0 ).right( 100 );

        Composite middle = toolkit.createComposite( top, SWT.NONE );
        middle.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        FormDataFactory.on( middle ).left( left, dp( 30 ).pix() ).right( 100 );

        Composite middleTop = toolkit.createComposite( middle, SWT.NONE );
        middleTop.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        colorDisplay = createColorAreaLabel( middleTop );
        oldColorDisplay = createColorAreaLabel( middleTop );
        FormDataFactory.on( colorDisplay ).left( 0 ).right( 50 );
        FormDataFactory.on( oldColorDisplay ).left( colorDisplay ).right( 100 );
        FormDataFactory.on( middleTop ).left( 0 ).right( 100 );

        Composite rgbArea = createRGBArea( middle );
        FormDataFactory.on( rgbArea ).top( middleTop ).left( 0 ).right( 100 );

        Composite right = createPalette( middle );
        FormDataFactory.on( right ).top( rgbArea ).left( 0 ).right( 100 );

        applyButton = createApplyButton( panelBody );
        FormDataFactory.on( applyButton ).top( top, dp( 30 ).pix() ).right( 100 );

    }


    private void handleColorBoxMarkerPositionChanged( Composite panelBody, int width, int x, int y ) {
        colorBoxMarker.setBounds( panelBody.getBounds().x + x - 10, panelBody.getBounds().y + y - 10, 20, 20 );
        if (x < colorBoxColors.length  && y < colorBoxColors[x].length) {
            Integer rgbValue = colorBoxColors[x][y];
            java.awt.Color awtColor = new java.awt.Color( rgbValue, true );
            Color swtColor = new Color( panelBody.getDisplay(), awtColor.getRed(), awtColor.getGreen(),
                    awtColor.getBlue() );
            colorBoxMarker.setBackground( swtColor );
            updateColorWidgets( panelBody, swtColor.getRGB(), COLOR_WIDGET_TYPE.BOX );
        }
    }

    private Integer[][] colorBoxColors = new Integer[COLORBOX_WIDTH][COLORBOX_HEIGHT];


    private void setColorBoxColor( Composite panelBody, RGB rgb ) {
        if (rgb == null) {
            rgb = new RGB( 255, 255, 255 );
        }
        Color color = new Color( panelBody.getDisplay(), rgb );
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB( color.getRed(), color.getGreen(), color.getBlue(), hsb );

        int rgbValue;
        float newS, newB;
        int actualRgbValue = java.awt.Color.HSBtoRGB( hsb[0], hsb[1], hsb[2] );
        BufferedImage bi = new BufferedImage( COLORBOX_WIDTH, COLORBOX_HEIGHT, BufferedImage.TYPE_INT_RGB );
        for (int w = 0; w < COLORBOX_WIDTH; w += 1) {
            for (int h = COLORBOX_HEIGHT - 1; h >= 0; h -= 1) {
                newS = Double.valueOf( (double)w / COLORBOX_WIDTH ).floatValue();
                newB = Double.valueOf( (double)h / COLORBOX_HEIGHT ).floatValue();
                rgbValue = java.awt.Color.HSBtoRGB( hsb[0], newS, newB );
                colorBoxColors[w][h] = rgbValue;
                if (rgbValue == actualRgbValue) {
                    if(colorBox.getBounds().contains( w, h )) {
                        colorBoxMarker.setBackground( color );
                        colorBoxMarker.setBounds( w - 10, h - 10, 20, 20 );
                    }
                }
                bi.setRGB( w, h, rgbValue );
            }
        }
        GC gc = new GC( colorBox );
        final PaletteData palette = new PaletteData( 0x0000FF, 0x00FF00, 0xFF0000 );
        DataBuffer dataBuffer = bi.getData().getDataBuffer();
        ImageData imageData = null;
        if (dataBuffer.getDataType() == DataBuffer.TYPE_BYTE) {
            byte[] bytes = ((DataBufferByte)dataBuffer).getData();
            imageData = new ImageData( bi.getWidth(), bi.getHeight(), 24, palette, 4, bytes );
        }
        else if (dataBuffer.getDataType() == DataBuffer.TYPE_INT) {
            int[] data = ((DataBufferInt)dataBuffer).getData();
            imageData = new ImageData( bi.getWidth(), bi.getHeight(), 24, palette );
            imageData.setPixels( 0, 0, data.length, data, 0 );
        }
        final Image swtImage = new Image( Display.getDefault(), imageData );
        gc.drawImage( swtImage, 0, 0 );
        gc.dispose();
    }


    private Composite createHexField( Composite panelBody ) {
        Composite comp = toolkit.createComposite( panelBody, SWT.NONE );
        comp.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        Label hexLabel = toolkit.createLabel( comp, "#", SWT.NONE );
        colorHex = toolkit.createText( comp, "", SWT.NONE );
        colorHex.addModifyListener( new ModifyListener() {

            public void modifyText( ModifyEvent e ) {
                String value = colorHex.getText();
                try {
                    java.awt.Color color = java.awt.Color.decode( value );
                    RGB rgb = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
                    updateColorWidgets( panelBody, rgb, COLOR_WIDGET_TYPE.HEX );
                }
                catch (NumberFormatException nfe) {
                    // ignore
                }
            }
        } );
        FormDataFactory.on( colorHex ).fill().left( hexLabel, dp( 5 ).pix() ).right( 100, -5 );
        return comp;
    }


    private Button createApplyButton( Composite comp ) {
        Button applyButton = toolkit.createButton( comp, "Apply selection", SWT.PUSH );
        applyButton.setEnabled( getRGB() != null );
        applyButton.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                colorInfo.get().setColor( getRGB() );
                PanelPath path = getSite().getPath();
                getContext().closePanel( path );
                EventManager.instance().publish( new EventObject( colorInfo.get() ) );
            }
        } );
        return applyButton;
    }


    private Composite createPalette( Composite parent ) {
        Composite paletteComp = toolkit.createComposite( parent, SWT.NONE );
        paletteComp.setLayout( new GridLayout( PALETTE_BOXES_IN_ROW, true ) );
        Label title = toolkit.createLabel( paletteComp, "Basic colors", SWT.NONE );
        GridData titleData = new GridData( SWT.LEFT, SWT.CENTER, true, false );
        titleData.horizontalSpan = PALETTE_BOXES_IN_ROW;
        title.setLayoutData( titleData );
        for (int i = 0; i < PALETTE_COLORS.length; i++) {
            createPaletteColorBox( paletteComp, PALETTE_COLORS[i] );
        }
        return paletteComp;
    }


    private Label createColorAreaLabel( Composite panelBody ) {
        Label colorDisplay = toolkit.createLabel( panelBody, "", SWT.BORDER | SWT.FLAT );
        FormDataFactory.on( colorDisplay ).width( COLOR_DISPLAY_BOX_SIZE ).height( COLOR_DISPLAY_BOX_SIZE );
        return colorDisplay;
    }


    private Composite createRGBArea( Composite panelBody ) {
        Composite spinComp = toolkit.createComposite( panelBody, SWT.NONE );
        spinComp.setLayout( new GridLayout( 2, false ) );
        toolkit.createLabel( spinComp, "Red", SWT.NONE );
        spRed = new Spinner( spinComp, SWT.BORDER );
        spRed.setMaximum( MAX_RGB_COMPONENT_VALUE );
        spRed.addModifyListener( new SpinnerListener( panelBody, spRed, RED ) );
        toolkit.createLabel( spinComp, "Green", SWT.NONE );
        spGreen = new Spinner( spinComp, SWT.BORDER );
        spGreen.setMaximum( MAX_RGB_COMPONENT_VALUE );
        spGreen.addModifyListener( new SpinnerListener( panelBody, spGreen, GREEN ) );
        toolkit.createLabel( spinComp, "Blue", SWT.NONE );
        spBlue = new Spinner( spinComp, SWT.BORDER );
        spBlue.setMaximum( MAX_RGB_COMPONENT_VALUE );
        spBlue.addModifyListener( new SpinnerListener( panelBody, spBlue, BLUE ) );
        return spinComp;
    }


    private void updateColorBox( Composite panelBody, RGB rgb ) {
        setColorBoxColor( panelBody, rgb );
    }


    private void updateColorDisplay( Composite panelBody, RGB rgb ) {
        if (rgb != null) {
            colorDisplay.setBackground( new Color( panelBody.getDisplay(), rgb ) );
        }
        else {
            colorDisplay.setBackground( null );
        }
        oldColorDisplay.setBackground( colorDisplay.getBackground() );
        if (applyButton != null) {
            applyButton.setEnabled( rgb != null );
        }
    }


    private void updateSpinners( RGB newRGB ) {
        if (newRGB != null) {
            spinnerListenerActive = false;
            spRed.setSelection( newRGB.red );
            spGreen.setSelection( newRGB.green );
            spBlue.setSelection( newRGB.blue );
            spinnerListenerActive = true;
        }
    }


    private void updateHexField( RGB newRGB ) {
        if (newRGB != null) {
            java.awt.Color color = new java.awt.Color( newRGB.red, newRGB.green, newRGB.blue );
            // ignore alpha value
            colorHex.setText( Integer.toHexString( (color.getRGB() & 0xffffff) | 0x1000000 ).substring( 1 )
                    .toUpperCase() );
        }
    }


    private Label createPaletteColorBox( Composite parent, RGB color ) {
        Label result = toolkit.createLabel( parent, "", SWT.BORDER | SWT.FLAT );
        result.setBackground( new Color( parent.getDisplay(), color ) );
        GridData data = new GridData();
        data.widthHint = PALETTE_BOX_SIZE;
        data.heightHint = PALETTE_BOX_SIZE;
        result.setLayoutData( data );
        result.addMouseListener( new PaletteListener( parent, color ) );
        return result;
    }


    private void updateColorFomSpinner( Composite parent, int colorIndex, int value ) {
        if (rgb == null) {
            rgb = new RGB( 255, 255, 255 );
        }
        RGB newRGB = new RGB( rgb.red, rgb.green, rgb.blue );
        switch (colorIndex) {
            case RED:
                newRGB.red = value;
                break;
            case GREEN:
                newRGB.green = value;
                break;
            case BLUE:
                newRGB.blue = value;
                break;
        }
        updateColorWidgets( parent, newRGB, COLOR_WIDGET_TYPE.SPINNER );
    }


    private void updateColorWidgets( Composite parent, RGB newRGB, COLOR_WIDGET_TYPE type ) {
        // if (newRGB != null && (rgb == null || (newRGB.red != rgb.red ||
        // newRGB.green != rgb.green || newRGB.blue != rgb.blue))) {
        if (type != COLOR_WIDGET_TYPE.BOX)
            updateColorBox( parent, newRGB );
        if (type != COLOR_WIDGET_TYPE.DISPLAY)
            updateColorDisplay( parent, newRGB );
        if (type != COLOR_WIDGET_TYPE.HEX)
            updateHexField( newRGB );
        if (type != COLOR_WIDGET_TYPE.SPINNER)
            updateSpinners( newRGB );

        updateColor( newRGB );
        // }
    }


    private void updateColor( RGB newRGB ) {
        if (newRGB != null) {
            if (rgb == null) {
                rgb = new RGB( 0, 0, 0 );
            }
            rgb.blue = newRGB.blue;
            rgb.green = newRGB.green;
            rgb.red = newRGB.red;
        }
        else {
            rgb = null;
        }
    }
}