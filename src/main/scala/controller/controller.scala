package controller

import scalatags.Text.all as scalatags

extension (doc: scalatags.doctype)
  def toResponse: cask.Response[String] = cask.Response(
    doc.render,
    headers = Seq("Content-Type" -> "text/html")
  )

extension (frag: scalatags.Frag)
  def toResponse: cask.Response[String] = cask.Response(
    frag.render,
    headers = Seq("Content-Type" -> "text/html")
  )