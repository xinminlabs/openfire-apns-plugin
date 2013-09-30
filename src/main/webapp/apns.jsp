<%@ page import="java.io.File,
                 java.util.List,
                 org.jivesoftware.openfire.XMPPServer,
                 org.jivesoftware.util.*,
                 com.wecapslabs.openfire.plugin.apns.OpenfireApns,
                 org.apache.commons.fileupload.FileItem,
                 org.apache.commons.fileupload.disk.DiskFileItemFactory,
                 org.apache.commons.fileupload.servlet.ServletFileUpload,
                 org.apache.commons.fileupload.FileUploadException"
    errorPage="error.jsp"
%>

<%  // Get parameters
    boolean save = request.getParameter("save") != null;
    boolean success = request.getParameter("success") != null;
    boolean error = request.getParameter("error") != null;
    String password = ParamUtils.getParameter(request, "password");
    String badge = ParamUtils.getParameter(request, "badge");
    String sound = ParamUtils.getParameter(request, "sound");
    String production = ParamUtils.getParameter(request, "production");

    OpenfireApns plugin = (OpenfireApns) XMPPServer.getInstance().getPluginManager().getPlugin("openfire-apns");

    // Handle a save
    if (save) {
        plugin.setPassword(password);
        plugin.setBadge(badge);
        plugin.setSound(sound);
        plugin.setProduction(production);

        try {
            List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

            for (FileItem item : multiparts) {
                if (!item.isFormField()) {
                    String filename = item.getName();
                    item.write(new File(OpenfireApns.keystorePath()));
                }
            }
            response.sendRedirect("apns.jsp?success=true");
            return;
        } catch (Exception e) {
            response.sendRedirect("apns.jsp?error=true");
            return;
        }

    }

    password = plugin.getPassword();
    badge = Integer.toString(plugin.getBadge());
    sound = plugin.getSound();
    production = plugin.getProduction() ? "true" : "false";
%>

<html>
    <head>
        <title>APNS Settings Properties</title>
        <meta name="pageID" content="apns-settings"/>
    </head>
    <body>

<%  if (success) { %>
    <div class="jive-success">
    <table cellpadding="0" cellspacing="0" border="0">
    <tbody>
        <tr><td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0"></td>
        <td class="jive-icon-label">
            APNS certificate updated successfully.
        </td></tr>
    </tbody>
    </table>
    </div><br>
<% } %>

<form action="apns.jsp?save" method="post" enctype="multipart/form-data">

<div class="jive-contentBoxHeader">APNS certificate</div>
<div class="jive-contentBox">
    <label for="file">p12 certificate:</label>
    <input type="file" name="file" />
    <br>

    <label for="password">certificate password:</label>
    <input type="password" name="password" value="<%= password %>" />
    <br>

    <label for="badge">payload badge</label>
    <input type="badge" name="badge" value="<%= badge %>" />
    <br>

    <label for="sound">payload sound</label>
    <input type="badge" name="sound" value="<%= sound %>" />
    <br>

    <label for="production">sandbox or production</label>
    <input type="radio" name="sandbox" value="false" <%= production == "true" ? "" : "checked" %>>Sandbox
    <input type="radio" name="production" value="true" <%= production == "true" ? "checked" : "" %>>Production
</div>
<input type="submit" value="Save">
</form>


</body>
</html>
