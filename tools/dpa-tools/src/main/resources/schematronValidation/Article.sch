<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
    <title>Validation using Schematron rules</title>
    <ns prefix="cat" 
        uri="http://www.iro.umontreal.ca/lapalme/wine-catalog"/>
    <xsl:key name="colors" match="/cellar-book/cat:wine-catalog/cat:wine"
        use="cat:properties/cat:color"/>


    <element name="nbReds">
        <simpleType>
            <restriction base="xs:string">
                <enumeration value="RI"/>
                <enumeration value="CT"/>
                <enumeration value="NC"/>
            </restriction>
        </simpleType>
    </element>


    <pattern id="section-check">
      <rule context="section">
         <assert test="/article/administrativedata/articleid">The element Person must have a Title attribute.</assert>
         <assert test="title">This section has no title</assert>
         <assert test="para">This section has no paragraphs</assert>
         <let name="testing" value="/article/administrativedata/articleid"/>
      </rule>
   </pattern>

   <pattern id="article-fields">
      <rule context="article">
         <assert test="string-length(/article/administrativedata/articleid) = 36">articleId needs to be 36 characters</assert>
         <assert test="ends-with(/article/administrativedata/filename, '.xml')">file needs to be xml</assert>
          <report test="string-length(/article/administrativedata/articleid) = 800">
              Every book that is on loan must have a return date
          </report>
      </rule>


        <rule context="article">
            <let name="source" value="/article/metadata/source"/>
            <report test="($source != 'aarhusstiftstidende') and
            ($source != 'arbejderen') and
            ($source != 'berlingsketidende') and
            ($source != 'boersen') and
            ($source != 'bornholmstidende') and
            ($source != 'bt') and
            ($source != 'dagbladetkoege') and
            ($source != 'dagbladetringsted') and
            ($source != 'dagbladetroskilde') and
            ($source != 'dagbladetstruer') and
            ($source != 'dernordschleswiger') and
            ($source != 'ekstrabladet') and
            ($source != 'flensborgavis') and
            ($source != 'fredericiadagblad1890') and
            ($source != 'frederiksborgamtsavis') and
            ($source != 'fyensstiftstidende') and
            ($source != 'fynsamtsavissvendborg') and
            ($source != 'helsingoerdagblad') and
            ($source != 'herningfolkeblad') and
            ($source != 'holstebrodagblad') and
            ($source != 'horsensfolkeblad1866') and
            ($source != 'information') and
            ($source != 'jydskevestkystenaabenraa') and
            ($source != 'jydskevestkystenbillund') and
            ($source != 'jydskevestkystenesbjerg') and
            ($source != 'jydskevestkystenhaderslev') and
            ($source != 'jydskevestkystenkolding1995') and
            ($source != 'jydskevestkystensoenderborg') and
            ($source != 'jydskevestkystentoender') and
            ($source != 'jydskevestkystenvarde') and
            ($source != 'jydskevestkystenvejen') and
            ($source != 'kristeligtdagblad') and
            ($source != 'lemvigfolkeblad') and
            ($source != 'licitationen') and
            ($source != 'lollandfalstersfolketidende') and
            ($source != 'metroxpressoest') and
            ($source != 'metroxpressvest') and
            ($source != 'midtjyllandsavis1857') and
            ($source != 'morgenavisenjyllandsposten') and
            ($source != 'morsoefolkeblad') and
            ($source != 'nordjyskestiftstidendeaalborg') and
            ($source != 'nordjyskestiftstidendehimmerland') and
            ($source != 'nordjyskestiftstidendevendsyssel') and
            ($source != 'nordvestnytholbaek') and
            ($source != 'nordvestnytkalundborg') and
            ($source != 'politiken') and
            ($source != 'politikenweekly') and
            ($source != 'randersamtsavis') and
            ($source != 'ringkjoebingamtsdagblad') and
            ($source != 'sjaellandskenaestved') and
            ($source != 'sjaellandskeslagelse') and
            ($source != 'skivefolkeblad') and
            ($source != 'thisteddagblad') and
            ($source != 'vejleamtsfolkeblad') and
            ($source != 'viborgstiftsfolkeblad') and
            ($source != 'weekendavisen') and
            ($source != 'testavis') and
            ($source != 'verapdf')">
                Every book that is on loan must have a return date
            </report>
        </rule>


   </pattern>

    
</schema>