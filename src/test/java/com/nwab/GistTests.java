package com.nwab;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


public class GistTests 
{


    private static final String token = "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";
    private static final String API_VERSION = "2022-11-28";
    private static String description;
    private static final boolean isPublic = false;
    private static String fileName;
    private static String fileContent;
    private static final String baseUri = "https://api.github.com/gists";
    private String gistID;

    @After
    public void cleanUpGists()
    {
        //Delete test gist
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .delete(baseUri + "/" + gistID);

        Assert.assertEquals(204, response.getStatusCode());
    }
    
    @Test
    public void testCreateNewGist()
    {
        //Set variable strings
        description = "Testing Gist Creation API";
        fileName = "myfile.txt";
        fileContent = "The is a test Gist";

        //Given I sent a request to create a Gist
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"description\":\"" + description + "\",\"public\":" + isPublic + ",\"files\":{\"" + fileName + "\":{\"content\":\"" + fileContent + "\"}}}")
                .post(baseUri);

        gistID = response.jsonPath().getString("id");

        //Then the API returns a 201 code and the Gist is created
        Assert.assertEquals(201, response.getStatusCode());
        Assert.assertTrue(response.getBody().asString().contains(description));

    }

    @Test
    public void listAllGistsForUser()
    {
        //Set variable strings
        description = "Testing Gist Creation API";
        fileName = "myfile.txt";
        fileContent = "The is a test Gist";

        //Given I have created a Gist
        Response creationResponse = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"description\":\"" + description + "\",\"public\":" + isPublic + ",\"files\":{\"" + fileName + "\":{\"content\":\"" + fileContent + "\"}}}")
                .post(baseUri);

        gistID = creationResponse.jsonPath().getString("id");

        //When I query all Gists for a user
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .get(baseUri);
        
        //Then the ID for the created Gist is present in the response
        Assert.assertTrue(response.getBody().asString().contains(gistID));
        
    }

    @Test
    public void updateExistingGist()
    {

    }

    @Test
    public void deleteGist()
    {


    }

    
}
