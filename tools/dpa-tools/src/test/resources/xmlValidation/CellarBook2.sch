<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    queryBinding="xslt2">
    <title>Validation using Schematron rules</title>
    <ns prefix="cat" 
        uri="http://www.iro.umontreal.ca/lapalme/wine-catalog"/>
    <xsl:key name="colors" match="/cellar-book/cat:wine-catalog/cat:wine"
        use="cat:properties/cat:color"/>
    
    <pattern>
        <rule context="wine">
            <report test="rating/@stars>1 and not(comment)"> 
                There should be a comment for a wine with more than one star.
            </report>
        </rule>
    </pattern>
    
    <pattern>
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
            <!-- check for a well balanced cellar!!! -->
            <assert test="$nbReds>$nbColors div 3">
                Not enough reds (<value-of select="$nbReds"/> over
                <value-of select="$nbColors"/>) left in the cellar.
            </assert>
        </rule>
    </pattern>
    
    <pattern abstract="true" id="spacesAtStartEnd">
        <rule context="comment|cat:comment|cat:food-pairing|cat:tasting-note">
            <report test="starts-with($elem,' ') or 
                          substring($elem,string-length($elem))=' '">
                A <value-of select="name($elem)"/> element within a <name/> 
                should not start or end with a space.
            </report>
        </rule>
    </pattern>
    <pattern is-a="spacesAtStartEnd">
        <param name="elem" value="cat:bold"/>
    </pattern>
    <pattern is-a="spacesAtStartEnd">
        <param name="elem" value="cat:emph"/>
    </pattern>
    
</schema>