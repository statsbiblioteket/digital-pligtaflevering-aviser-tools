This directory contains test batches conforming to the initial
documentation submitted by Infomedia.  They should be directly
ingestable.

If there is an inconsistency between files and documentation, please
fix files and code to conform to documentation.

Note that the file "transfer_acknowledged" is present even though it
is not described in the initial documentation.  This file indicates
that SB has acknowledged that this batch roundtrip was transferred (as
indicated by the "transfer_complete" file being updated as the last),
and is required for the ingester to take action.  This may change
later if we decide to emulate this process too.

20160811-RT1:
---

A test batch generated internally at SB with pages taken from the VeraPDF
test suite.  The idea is that every page will fail the 

/tra 2016-08-31
