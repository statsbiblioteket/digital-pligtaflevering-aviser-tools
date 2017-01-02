<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
    <title>Validation using Schematron rules</title>
    <ns prefix="cat" 
        uri="http://www.iro.umontreal.ca/lapalme/wine-catalog"/>
    <xsl:key name="colors" match="/cellar-book/cat:wine-catalog/cat:wine"
        use="cat:properties/cat:color"/>

   <pattern id="section-check">
      <rule context="section">
         <assert test="/article/administrativedata/articleid">The element Person must have a Title attribute.</assert>
         <assert test="title">This section has no title</assert>
         <assert test="para">This section has no paragraphs</assert>
         <let name="testing" value="/article/administrativedata/articleid"/>
      </rule>
   </pattern>

      <pattern id="other-bob">
         <rule context="section">
            <let name="testit" value="/article/administrativedata/articleid"/>
         </rule>
      </pattern>

       <pattern id="other-check">
           <rule context="cellar">
               <let name="nbBottles" value="sum(wine/quantity)"/>
               <report test="$nbBottles &lt; 10">
                   Only <value-of select="$nbBottles"/> bottles left in the cellar.
               </report>
               <!-- nb of bottles of each color in the cellar -->
               <let name="winesFromCellar" value="/cellar-book/cellar/wine"/>
               <let name="nbReds"
                value="sum($winesFromCellar[@code=key('colors','red')/@code]/quantity)"/>
               <let name="nbWhites"
                value="sum($winesFromCellar[@code=key('colors','white')/@code]/quantity)"/>
               <let name="nbRosés"
                value="sum($winesFromCellar[@code=key('colors','rosé')/@code]/quantity)"/>
               <let name="nbColors" value="$nbReds+$nbWhites+$nbRosés"/>
           </rule>
       </pattern>



    
</schema>