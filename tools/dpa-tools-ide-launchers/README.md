This project contains "launchers" for the various autonomous
components and other programs in DPA (characterized with a
shell-script being generated elsewhere), as there is no portable
standard way to have "launch class X with arguments Y in directory Z
etc.".

A typical launcher for an autonomous component is responsible for

* Change to the correct working directory if needed.
* Identify and locate the needed resources, like specific folders
  containing needed files, and specific properties.
* Ensure that the environment is as expected.  (memory/cores/disk
  space etc)
* Invoke the main method with the appropriate arguments.  For
  autonomous components this is a property file followed by zero or
  more "key=value" strings.
  
Sample code as of 2017-02-14:

    public static void main(String[] args) {

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryMainLauncher.class, "delivery-samples");

        CreateDeliveryMain.main(new String[]{
                "create-delivery.properties",
                "autonomous.agent=register-batch-trigger",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString()});
    }


Originally these were implemented as jUnit tests, which brings a lot
of nice features in modern IDEs, but has a tendency to confuse Maven
and programmers, hence it was refactored into plain main-classes.

Note that there can be multiple launchers with different
configurations for the same autonomous component.

A given launcher can - if documented in the Javadoc for the class - 
assume the existance of services either in vagrant (on localhost)
or on an internal server, but must fail loudly fast
if a required service does not exist.

/tra 2017-02-14
