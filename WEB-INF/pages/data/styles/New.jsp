<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<html:form action="/config/data/styleNew">

<table class="info">
  <tr>
    <td class="label">
      <bean:message key="label.styleID"/>:
	</td>
    <td class="datum">
	  <html:text property="styleID" size="60"/>
    </td>
  </tr>
    <tr>
    <td class="label">&nb;</td>
    <td class="datum">
	  <html:submit property="action" value="new">
		<bean:message key="label.new"/>
	  </html:submit>			
	</td>
  </tr>
</table>	

</html:form>