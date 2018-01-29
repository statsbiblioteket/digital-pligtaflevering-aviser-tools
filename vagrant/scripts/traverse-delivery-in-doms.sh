#!/usr/bin/env bash

# Traverse DPA Roundtrip in DOMS.

if [ -z $1 ]
then
    echo "usage:  $0 uuid:...dpa_roundtrip..."
    exit 1
fi

# ----------------------------------------------------------------------
# xmlstarlet and curl must be on PATH

CURL=curl
XMLSTARLET=xmlstarlet

# xmlstarlet namespace declarations
N="-N rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns# -N fedora=info:fedora/fedora-system:def/relations-external# -N dc=http://purl.org/dc/elements/1.1/"

ROUNDTRIP_UUID=$1

# curl -s -u fedoraAdmin:fedoraAdminPass 'http://localhost:7880/fedora/objects/uuid:627ef2a0-88d4-4423-8b15-6f37dc522e29/datastreams/RELS-EXT/content'
# curl -s $P "$O/$UUID/datastreams/RELS-EXT/content"

O="http://localhost:7880/fedora/objects"
P="-u fedoraAdmin:fedoraAdminPass"

# ----------------------------------------------------------------------

# http://localhost:7880/fedora/objects/uuid:a8072382-20db-4d48-aa3a-5ad1392a242f/datastreams/DC/content
ROUNDTRIP_DC=$($CURL -s $P "$O/$ROUNDTRIP_UUID/datastreams/DC/content")

# <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
#  <dc:title>DPA Roundtrip</dc:title>
#  <dc:identifier>uuid:a8072382-20db-4d48-aa3a-5ad1392a242f</dc:identifier>
#  <dc:identifier>path:dl_20180114_rt1</dc:identifier>
# </oai_dc:dc>

if [ "DPA Roundtrip" != "$(echo $ROUNDTRIP_DC | $XMLSTARLET sel $N -t -m '//dc:title' -v '.')" ]
then
    echo "Not DPA Roundtrip."
    exit 1
fi

# echo "Original path: $(echo $ROUNDTRIP_DC | $XMLSTARLET sel -t -m '//dc:identifier[starts-with(text(), "path:")]' -v '.')"

# http://localhost:7880/fedora/objects/uuid:a8072382-20db-4d48-aa3a-5ad1392a242f/datastreams/RELS-EXT/content
ROUNDTRIP_RELSEXT=$($CURL -s $P "$O/$ROUNDTRIP_UUID/datastreams/RELS-EXT/content")

# <?xml version="1.0"?>
# <rdf:RDF xmlns:doms="http://doms.statsbiblioteket.dk/relations/default/0/1/#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
#   <rdf:Description rdf:about="info:fedora/uuid:a8072382-20db-4d48-aa3a-5ad1392a242f">
#     <hasModel xmlns="info:fedora/fedora-system:def/model#" rdf:resource="info:fedora/doms:ContentModel_DPARoundTrip"/>
#     <hasModel xmlns="info:fedora/fedora-system:def/model#" rdf:resource="info:fedora/doms:ContentModel_Item"/>
#     <hasModel xmlns="info:fedora/fedora-system:def/model#" rdf:resource="info:fedora/doms:ContentModel_DOMS"/>
#     <doms:isPartOfCollection rdf:resource="info:fedora/doms:DPA_Collection"/>
#     <hasPart xmlns="info:fedora/fedora-system:def/relations-external#" rdf:resource="info:fedora/uuid:4170dbda-f215-495f-937d-52977b87fc01"/>
#     <hasPart xmlns="info:fedora/fedora-system:def/relations-external#" rdf:resource="info:fedora/uuid:23a76588-97d1-4b31-a6f2-2166f0790694"/>
#     ...
#  </rdf:Description>
# </rdf:RDF>

for PAPER_UUID in $(echo $ROUNDTRIP_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
    # uuid:4170dbda-f215-495f-937d-52977b87fc01
    PAPER_RELSEXT=$($CURL -s $P "$O/$PAPER_UUID/datastreams/RELS-EXT/content")

    # <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    #   <rdf:Description rdf:about="info:fedora/uuid:4170dbda-f215-495f-937d-52977b87fc01">
    #     <isPartOfCollection xmlns="http://doms.statsbiblioteket.dk/relations/default/0/1/#" rdf:resource="info:fedora/doms_sboi_dpaCollection"/>
    #     <hasPart xmlns="info:fedora/fedora-system:def/relations-external#" rdf:resource="info:fedora/uuid:7bd4fb56-f268-426d-bab1-cdd8aefbde7f"/>
    #     <hasPart xmlns="info:fedora/fedora-system:def/relations-external#" rdf:resource="info:fedora/uuid:6131049f-21fd-467a-9c34-21b231e29940"/>
    #   </rdf:Description>
    # </rdf:RDF>

    for PAPER_PART_UUID in $(echo $PAPER_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
        # articles and pages
        PAPER_PART_DC=$($CURL -s $P "$O/$PAPER_PART_UUID/datastreams/DC/content")

        # path:dl_20180114_rt1/aarhusstiftstidende/pages
        PAPER_PART_IDENTIFIER=$(echo $PAPER_PART_DC | $XMLSTARLET sel $N -t -m '//dc:identifier[starts-with(text(), "path:")]' -v 'text()')

        if [[ "$PAPER_PART_IDENTIFIER" = *pages ]]
        then
            # print out URL's for all PDF files and download them to see if pdfinfo likes them.

            echo "$PAPER_PART_IDENTIFIER $PAPER_PART_UUID"

            PAGES_RELSEXT=$($CURL -s $P "$O/$PAPER_PART_UUID/datastreams/RELS-EXT/content")
            for PAGE_UUID in $(echo $PAGES_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
                # Each page node has XML datastream and a subnode to put metadata on the PDF file.
                PAGE_RELSEXT=$($CURL -s $P "$O/$PAGE_UUID/datastreams/RELS-EXT/content")

                for PAGE_CONTENT_UUID in $(echo $PAGE_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
                    # Get redirect target - https://unix.stackexchange.com/a/157219/4869
                    REAL_URL=$($CURL -w "%{url_effective}\n" -I -L -S -s $P "$O/$PAGE_CONTENT_UUID/datastreams/CONTENTS/content" -o /dev/null)
                    # Optionally rewrite URL if not valid and the bitrepository is mounted locally (not relevant for vagrant images)
                    curl -s $REAL_URL | pdfinfo - >/dev/null
                    RESULT=$?
                    # echo $RESULT
                    if [ $RESULT -eq 0 ]
                    then
                        echo $REAL_URL ok
                    else
                        echo $REAL_URL bad pdf
                    fi
                done
            done
        fi


        #for PDF_ARTICLE_UUID in $(echo $PAPER_PART_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
        #    echo $PAPER_UUID $PAPER_PART_UUID $PDF_ARTICLE_UUID
        #done
    done
done




