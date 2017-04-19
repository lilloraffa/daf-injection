# Injection Module

!!!WARNING!!! this is still a very premature prototype!

The module is meant to manage the data injection into the Data & Analytic Fraework (DAF). At this version, batch injection of CSV has been implemented.

The conceptual framework allows for the existence of two different type of datasets:
1. Standard Datasets: are those that follow a detailed metadata schema and rules defined in a proper 'standard schema', and are used to describe standard phenomena that are valid nationwide.
2. Normal Datasets: all the rests, described by its own dataschema.

Every datasets in input goes along with its own data schema (called `ConvSchema` in the code). If the input dataset belongs to a Standard Dataset, then the ConvSchema (which, again, is one per datasets) contains a reference to the "Standard Schema" (`StdSchema` in the code). Many datasets may belong to one `StdSchema`, only one `ConvSchema` is defined for each dataset.

The module works as follow:
- it reads all files contained in a configurable folder. Here, data should be organized into subfolders corresponding to each data owner (the entity that sends the data into the platform).
- Once the dataset has been read, the module looks for the corresponding dataschema (saved in MongoDB). If the dataschema is found, the module try to see if there is an associated "standard schema" to which the input data is associated to. If the standard schema is found, then the module does coherence checks to make sure the input schema is made in accordance to the predefined "standard schema"
- Then the actual dataset is checked wrt the resulting schema. If all the checks passed, then the dataset gets saved into DAF.
- Saving methodology depends on the fact that the dataset belongs to the standard one or not.

## Example Data
To see it at work, you can use the example data provided into the directory `data`. Here we provide examples to make an injection of public transport data (following GTFS standard) from different owners (cities in our case; in this example we are using Torino and Palermo). The data belongs to a Standard Schema GTFS_*, so that all dataset coming from different owner will be saved into an unique database and in a uniform way.

In the folder [data](https://github.com/lilloraffa/daf-injection/tree/master/data) you will find:
- `mongo_daf.zip`: contains the mongodb database for both ConvSchema and StdSchema
- `daf_injftp.zip`: contains the datasets to be injested -> extract this into a folder and make it the `sftpBasePath` of your `application.conf`
- `schema`: ConvSchema and StdSchema in json files (the same content is in the MongoDb dump above)
