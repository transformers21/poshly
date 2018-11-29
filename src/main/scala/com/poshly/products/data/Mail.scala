package com.poshly.products.data

case class MailEnvelope(data: MailBody)

case class MailBody(subject: String, content: MailContent)

case class MailContent(text: String)
