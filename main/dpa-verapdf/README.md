VeraPDF is used to extract meta information about PDF files relevant
to PDF/A and similar for long term preservation, and has been developed
along with this project.

The output from VeraPDF is put in a VERAPDF data stream on each DOMS 
object corresponding to a PDF-file.

Our usage as a library has clearly not been in the test suite used
by the developers as we have had massive problems with cleaning
up resources after usage.  As of version 1.10 we expect that most
of our problems have been handled.  Look out for not being allowed to
open more files and left over files in the temporary directory.

We currently depend on the openpreservation repository which has
proven flaky.  When an appropriate release makes it to Maven Central we need to migrate.

/tra 2017-11-29
