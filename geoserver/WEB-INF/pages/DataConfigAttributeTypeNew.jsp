<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=1 width=100%>
<tr><td>
<table border=0 width=100%>
	<html:form action="DataConfigAttributeTypesNew">

	<tr><td valign="top" align="right">	
		<bean:message key="label.attributeTypeName"/>:
	</td>
	<td align="left">
		<html:select property="selectedDescription">
			<html:options property="dataStoreDescriptions"/>
		</html:select>
	</td></tr>

	<tr><td>&nbsp;</td><td align="left">
		<html:submit property="buttonAction" value="new">
			<bean:message key="label.new"/>
		</html:submit>
	</td></tr>
	
	</html:form>
	
</table>
</td></tr>
</table>