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

   <pattern id="article-fields">
      <rule context="article">
         <assert test="string-length(/article/administrativedata/articleid) = 36">articleId needs to be 36 characters</assert>
         <assert test="ends-with(/article/administrativedata/filename, '.xml')">file needs to be xml</assert>
      </rule>
   </pattern>

    
</schema>