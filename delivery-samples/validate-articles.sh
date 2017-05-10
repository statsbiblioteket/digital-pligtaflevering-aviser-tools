#!/bin/sh

# validate all pages XML files.  An optional argument is an alternate delivery folder.+
find ${1:-.}/ -wholename "*/*/pages/*.xml" -print0 | parallel -0 xmllint --schema ../tools/dpa-tools/src/main/resources/xmlValidation/Article.xsd --noout \
    || echo "\n*** FAILED ***\n"

