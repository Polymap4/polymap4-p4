<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
   xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
   xmlns="http://www.opengis.net/sld" 
   xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:xlink="http://www.w3.org/1999/xlink" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <NamedLayer>
    <Name>MyLayer</Name>
    <UserStyle>
      <Title>MyStyle</Title>
      <FeatureTypeStyle>
        <Rule>
          <Name>ZoomAndAttributeCombined</Name>
          <Title>Zoom And Attribute Combined</Title>
          <ogc:Filter>
            <ogc:PropertyIsEqualTo>
              <ogc:PropertyName>Type</ogc:PropertyName>
              <ogc:Literal>1.0000</ogc:Literal>
            </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <MinScaleDenominator>23000.0</MinScaleDenominator>
          <MaxScaleDenominator>1000000.0</MaxScaleDenominator>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill">#1A9641</CssParameter>
              <CssParameter name="fill-opacity">0.4</CssParameter>
            </Fill>
          </PolygonSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>

