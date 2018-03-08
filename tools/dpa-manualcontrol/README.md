
Purpose of dpa-manualcontrol
---
dpa-manualcontrol is used as a quick way of getting information about the newspapers that has been ingested.
A webinterface is started and it is possible to browse around the single pages and see if they look correct.

Everything is viewed as a treestructure with pages in a newspaper and newspapers in a delivery.

Pages can be either "Confirmed" or "Rejected", and this status can be saved in a delivery.
The result of the "Confirmed" or "Rejected" is stored in "Fedora" which is the system where all metadata of the newspapers is stored.


How to run in dev environment
---
It is necessary to disable singlesignon when running in development environment
To disable the singlesignon part, disable it in the web.xml
Disable CAS filter from:
<!--Start of CAS filter -->

to:
<!--End of CAS filter -->


Run this server starting "JettyRunner" in the testpackage


How to run in stage environment
---
Go to the following url: http://mirzam:9021/dpa-manualcontrol
Login and get redirected to the page for manual control


How to use/test "Delivery validation"
---


| Action                                                      | Expected result                                                     | Information                               |
| ----------------------------------------------------------- |:-------------------------------------------------------------------:| ----------------------------------------------:|
| Go to the url application url                               | manualcontrolpage is shown                                          | jetty=http://localhost:8080/dpa-manualcontrol/ |
| Select "Delivery validation" in the navigation menu at the top |                                                                  |
| Select a month where some deliveries has been ingested      |                                                                     |                            |
| Click "Prepare month"                                       | all deliveries in the selected month is now cashed on the server    |
| Click "Start"                                               | all the cashed deliveries for the selected month is now shown in the UI |
| Select a delivery in the table                              | all newspapers in the delivery is now shown in the table "DeliveryTitleInfo" |
| Select a newspaper in the table "DeliveryTitleInfo"         | all pages in the newspaper is now shown in the table "Page"         |
| Click on a page                                             | the page will be shown in the buttom of the browserwindow           |
| Click "Confirmed" or "Rejected" on a few of the pages       | A checkbox is checked in the row selected in the table "Page"       |
| Click "Save check" at the button at the top of the page     | A dialog is shown with all confirmed or rejected pages              |
| Click "Ok" in the dialog                                    | The dialog is closed and the delivery is checked in "DeliveryTitleInfo" |


How to use/test "TitleValidation"
---

| Action                                                      | Expected result                                                     | Information                               |
| ----------------------------------------------------------- |:-------------------------------------------------------------------:| ----------------------------------------------:|
| Go to the url application url                               | manualcontrolpage is shown                                          | jetty=http://localhost:8080/dpa-manualcontrol/ |
| Select "TitleValidation" in the navigation menu at the top  |                                                                  |
| Select a month where some deliveries has been ingested      |                                                                     |                            |
| Click "Prepare month"                                       | all deliveries in the selected month is now cashed on the server    |
| Click "Start"                                               | all the cashed deliveries for the selected month is now shown in the UI |
| Select a newspaper in the table "List"                      | all deliveries containing the newspaper is now shown in "DeliveryTitleInfo" |
| Select a delivery in the table "DeliveryTitleInfo"          | all pages in the newspaper is now shown in the table "Page"         |
| Click on a page                                             | the page will be shown in the buttom of the browserwindow           |
| Click "Confirmed" or "Rejected" on a few of the pages       | A checkbox is checked in the row selected in the table "Page"       |
| Click "Save check" at the button at the top of the page     | A dialog is shown with all confirmed or rejected pages              |
| Click "Ok" in the dialog                                    | The dialog is closed and the delivery is checked in "DeliveryTitleInfo" |



run config
---
It is possible to add a parameter to indicate that the application searches for validated deliveries

?validated=true

