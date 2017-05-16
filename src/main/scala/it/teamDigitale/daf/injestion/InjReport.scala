package it.teamDigitale.daf.injestion

case class InjReport (
    uri: String = "-1",
    url: String = "-1",
    stdSchemaUri: Option[String] = None,
    isStd: Boolean = false,
    isInStd: Boolean = false,
    isOrd: Boolean = false,
    isRaw: Boolean = false,
    statusOrd: Option[Boolean] = None,
    statusStd: Option[Boolean] = None,
    statusRaw: Option[Boolean] = None
)