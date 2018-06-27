These are delivery samples for easy usage during
development.

If you need more, ask Jens Henrik for where to find more on isilon.



Two small batches that is as close as possible to the delivery format that we expect to recieve from Infomedia.
one is located in dl_20160811_rt1, and the other one is located in dl_20160913_rt1

dl_20160811_rt1/
---
The batch dl_20160811_rt1 is mainly based on the files in  `$HOME/verapdf/corpus/veraPDF-corpus-staging/PDF_A-1b/6.1 File structure/6.1.2 File header` 
which are copyrighted by the VeraPDF consortium and licenced under CC-BY-4.0.  See http://verapdf.org/home/#licensing for details.

dl_20160913_rt1/
---
The batch dl_20160913_rt1 is mainly based on metadataexamples from Infomedia


Generating checksum-file
---
checksums.txt generated with

    find verapdf -type f | sort | xargs md5sum > md5sums.txt

MD5SUMS.txt is the old format of the checksum-file this is keept for test and documentation purpose

TODO:  xml files are not yet filled out.

