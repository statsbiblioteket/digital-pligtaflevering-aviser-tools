
Reading/writing the datamodel
---
The application reads the initial statistics of the content of deliveries from the roundtrip-object in the stream "DELIVERYSTATISTICS".

When the application is storing the results of newspapervalidation, it is stored in the title-object.
It is stored as XML in a stream called "VALIDATIONINFO"

When validation of a delivery is done the application can write a event called "ManualValidationDone" to the roundtrip-object.
