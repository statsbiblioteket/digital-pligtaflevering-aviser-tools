#!/usr/bin/env bash

# Generate md5sum compatible output for a complete delivery in DOMS.

# fail if any command fails - this is to avoid checking the return code of any curl call
set -e

VERBOSE=

if [ "$1" == "-v" ]
then
  VERBOSE=$1
  shift
fi

if [ -z $1 ]
then
    echo "usage:  [P=\"-u user:pass\"] $0 [-v] uuid:...dpa_roundtrip..."
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

O=${O:-http://${FEDORA_HOST:-localhost}:7880/fedora/objects}
# For non-local servers use fedoraReadOnlyAdmin - find password in /home/doms/services/fedora/server/config/fedora-users.xml
# xmlstarlet sel -t -m '//user[@name="fedoraReadOnlyAdmin"]' -v '@password' -n /tmp/fedora-users.xml
P=${P:--u ${FEDORA_USER:-fedoraAdmin}:${FEDORA_PASSWORD:-fedoraAdminPass}}

# ----------------------------------------------------------------------

# http://localhost:7880/fedora/objects/uuid:a8072382-20db-4d48-aa3a-5ad1392a242f/datastreams/DC/content
ROUNDTRIP_DC=$($CURL -s $P "$O/$ROUNDTRIP_UUID/datastreams/DC/content")
ROUNDTRIP_CURL_EXIT_CODE=$?
 if [  $ROUNDTRIP_CURL_EXIT_CODE -gt 0 ]
 then
    echo "curl error: $ROUNDTRIP_CURL_EXIT_CODE (No DOMS there?)"
    exit 1
 fi

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

PAPER_UUID_SEEN=no

for PAPER_UUID in $(echo $ROUNDTRIP_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
    PAPER_UUID_SEEN=yes
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

        case $PAPER_PART_IDENTIFIER in
        *pages)
            # print out md5sum entry for all PDF files 

            if [ -n "$VERBOSE" ]
            then
                echo "$PAPER_PART_IDENTIFIER $PAPER_PART_UUID"
            fi

            PAGES_RELSEXT=$($CURL -s $P "$O/$PAPER_PART_UUID/datastreams/RELS-EXT/content")
            for PAGE_UUID in $(echo $PAGES_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
                # Each page node has XML datastream and a subnode to put metadata on the PDF file.
                PAGE_XML_FILENAME=$($CURL -s $P "$O/$PAGE_UUID/datastreams/DC/content" | $XMLSTARLET sel -t -m '//dc:identifier[starts-with(text(), "path:")]' -v '.' | sed 's![^/]*/!!' )
                $CURL -s $P "$O/$PAGE_UUID/datastreams/XML/content" | md5sum | awk "{print \$1 \"  ${PAGE_XML_FILENAME}.xml\"}"

                PAGE_RELSEXT=$($CURL -s $P "$O/$PAGE_UUID/datastreams/RELS-EXT/content")

                for PAGE_CONTENT_UUID in $(echo $PAGE_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
		            # We now look at the node for the individual PDF file.
		            # Extract the path name from DC and strip the "path:dl_XXXX_rtY/" prefix.
		            PAGE_CONTENT_FILENAME=$($CURL -s $P "$O/$PAGE_CONTENT_UUID/datastreams/DC/content" | $XMLSTARLET sel -t -m '//dc:identifier[starts-with(text(), "path:")]' -v '.' | sed 's![^/]*/!!' )
                    # Get redirect target as returned by Fedora - https://unix.stackexchange.com/a/157219/4869
                    REAL_URL=$($CURL -w "%{url_effective}\n" -I -L -S -s $P "$O/$PAGE_CONTENT_UUID/datastreams/CONTENTS/content" -o /dev/null)
        		    # and md5sum the content.
                    curl -s $REAL_URL | md5sum | awk "{print \$1 \"  $PAGE_CONTENT_FILENAME\"}"
                done
            done
            ;;

        *articles)
	    # print out md5sum entry for XML file.  We assume an ".xml" suffix.
            if [ -n "$VERBOSE" ]
            then
                echo "$PAPER_PART_IDENTIFIER $PAPER_PART_UUID"
            fi

            ARTICLES_RELSEXT=$($CURL -s $P "$O/$PAPER_PART_UUID/datastreams/RELS-EXT/content")
            for ARTICLE_UUID in $(echo $ARTICLES_RELSEXT | $XMLSTARLET sel $N -t -m '//fedora:hasPart[starts-with(@rdf:resource, "info:fedora/")] ' -v 'substring-after(@rdf:resource, "info:fedora/")' -n); do
		        XML_CONTENT_FILENAME=$($CURL -s $P "$O/$ARTICLE_UUID/datastreams/DC/content" | $XMLSTARLET sel -t -m '//dc:identifier[starts-with(text(), "path:")]' -v '.' | sed 's![^/]*/!!' )
		        # Article XML is in the "XML" datastream of the node.
                $CURL -s $P "$O/$ARTICLE_UUID/datastreams/XML/content" | md5sum | awk "{print \$1 \"  ${XML_CONTENT_FILENAME}.xml\"}"
            done
            ;;
        esac
    done
done

if [  "$PAPER_UUID_SEEN" = "no" ]
then
    echo "No newspapers stored for delivery round trip"
fi



