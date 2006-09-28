<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"><xsl:output method="xml" encoding="utf-8"/><xsl:param name="version">$Name$</xsl:param><xsl:param name="version1" select="substring-after($version,'mapbuilder-lib-')"/><xsl:param name="version2" select="normalize-space(substring-before($version1,'$'))"/><xsl:template match="/"><div><xsl:choose><xsl:when test="$version2"><xsl:value-of select="$version2"/></xsl:when><xsl:otherwise><xsl:text>CVS Head</xsl:text></xsl:otherwise></xsl:choose></div></xsl:template><xsl:template match="text()|@*"/></xsl:stylesheet>
