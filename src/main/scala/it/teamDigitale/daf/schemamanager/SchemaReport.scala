package it.teamDigitale.daf.schemamanager

@Deprecated
case class SchemaReport (
      hasInputDataSchema: Boolean,
      hasStdSchema: Boolean,
      checkStdSchema: Boolean,  //Given StdSchema, it checks if the mapping is done correctly
      //checkStdConstr: Boolean,  //Given StdSchema, it checks that the StdConstr on data are satisfied.
      checkInSchema: Boolean  //Checks that the input data is coherent with the declared schema
      //checkInConstr: Boolean  //Checks that the constraints on data are satisfied.
  )