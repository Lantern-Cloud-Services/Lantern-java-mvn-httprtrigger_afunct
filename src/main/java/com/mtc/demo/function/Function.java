package com.mtc.demo.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.sql.*;
//import java.util.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);
        
        String favorite = "";
        try
        {
            favorite = getData(context, name);
            context.getLogger().info(favorite);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello " + name + " from Azure function for Java! Your favorite icecream is: " + favorite).build();
        }
    }

    private String getData(ExecutionContext context, String username) throws Exception
    {
        String url = System.getenv("SQLCONNSTR_icecreamdbconnstr");
        //String url = "jdbc:sqlserver://jcooklcs-sqlserver1.database.windows.net:1433;database=jcookLCS-sqldb1;user=sqladmin@jcooklcs-sqlserver1;password=Pa$$w0rd!;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
        
        Connection connection = DriverManager.getConnection(url);
        
        context.getLogger().info("Select data");
        PreparedStatement selectStatement = connection.prepareStatement("select favorite from userfavorites where username = '" + username + "'");

        ResultSet resultSet = selectStatement.executeQuery();
        if (!resultSet.next()) {
            context.getLogger().info("There is no data in the database!");
            return "unknown";
        }
        return resultSet.getString("favorite");

    }


}
