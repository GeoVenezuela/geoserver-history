<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"><xsl:output method="xml" encoding="utf-8"/><xsl:param name="modelId"/><xsl:param name="widgetId"/><xsl:template match="/"><DIV><form><h3>Load WMS GetCapabilities:</h3><textarea cols="40" rows="1" onkeypress="config.objects.{$widgetId}.onKeyPress(event)">../lib/widget/wms/GmapCapabilities.xml</textarea></form></DIV></xsl:template><xsl:template match="text()|@*"/></xsl:stylesheet>
