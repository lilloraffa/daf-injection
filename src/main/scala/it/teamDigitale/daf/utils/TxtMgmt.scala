package it.teamDigitale.daf.utils

object TxtMgmt {
  def normString(text: String): String = {
    text.toLowerCase().replace(" ", "_")
  }
}