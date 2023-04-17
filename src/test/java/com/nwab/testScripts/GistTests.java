package com.nwab.testScripts;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.nwab.testData.TokenStore;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


public class GistTests 
{

    //Final variables
    private static final String TOKEN = TokenStore.getToken();
    private static final String API_VERSION = "2022-11-28";
    private static final boolean IS_PUBLIC = false;
    private static final String BASE_URI = "https://api.github.com/gists";
    //Gist variables
    private static String DESCRIPTION = "Testing Gist Creation API";
    private static String gistID;
    //For file 1
    private static String file1Name = "myfile.txt";
    private static String file1Content = "The is a test gist";
    //For skipping cleanup when gists have already been deleted
    private boolean skipCleanup = false;

    @After
    public void cleanUpGists()
    {
        if(!skipCleanup){
            //Delete test gist
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", API_VERSION)
                    .contentType(ContentType.JSON)
                    .delete(BASE_URI + "/" + gistID);

            Assert.assertEquals(204, response.getStatusCode());
        }
    }
    
    @Test
    public void testCreateNewGist()
    {
        //Given I sent a request to create a Gist
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"description\":\"" + DESCRIPTION + "\",\"public\":" + IS_PUBLIC + ",\"files\":{\"" + file1Name + "\":{\"content\":\"" + file1Content + "\"}}}")
                .post(BASE_URI);

        //variable stored for cleanup after tests
        gistID = response.jsonPath().getString("id");

        //Then the API returns a 201 code and the Gist is created
        Assert.assertEquals(201, response.getStatusCode());
        Assert.assertTrue(response.getBody().asString().contains(DESCRIPTION));

    }

    @Test
    public void listAllGistsForUser()
    {
        //Given I have created a Gist
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"description\":\"" + DESCRIPTION + "\",\"public\":" + IS_PUBLIC + ",\"files\":{\"" + file1Name + "\":{\"content\":\"" + file1Content + "\"}}}")
                .post(BASE_URI);

        gistID = response.jsonPath().getString("id");

        //When I query all Gists for a user
                 response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .get(BASE_URI);
        
        //Then the ID for the created Gist is present in the response
        Assert.assertTrue(response.getBody().asString().contains(gistID));
        
    }

    @Test
    public void updateExistingGist()
    {
        //Set variable strings
        file1Content = "This file will be updated";
        String file1ContentUpdated = "Text succesfully updated.";
        String file2Name = "myFile2.txt";
        String file2Content = "This file will be deleted";

        //Given I have created a Gist
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"description\":\"" + DESCRIPTION + "\",\"public\":" + IS_PUBLIC + ",\"files\":{\"" + file1Name + "\":{\"content\":\"" + file1Content + "\"},\"" + file2Name + "\":{\"content\":\"" + file2Content + "\"}}}")
                .post(BASE_URI);
        
        gistID = response.jsonPath().getString("id");
        Assert.assertEquals(201, response.getStatusCode());

        //When I update file1 and delete file2
                response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"files\":{\""+ file1Name +"\":{\"content\":\""+ file1ContentUpdated +"\"},\""+ file2Name +"\":null}}")
                .patch(BASE_URI + "/" + gistID);
        //Then these changes are successful
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertTrue(response.getBody().asString().contains(file1ContentUpdated));
        Assert.assertFalse(response.getBody().asString().contains(file2Name));
    }

    @Test
    public void deleteGist()
    {
        //Given I sent a request to create a Gist
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .body("{\"description\":\"" + DESCRIPTION + "\",\"public\":" + IS_PUBLIC + ",\"files\":{\"" + file1Name + "\":{\"content\":\"" + file1Content + "\"}}}")
                .post(BASE_URI);

        gistID = response.jsonPath().getString("id");

        //When I send a request to delete this Gist
                response = RestAssured.given()
                .header("Authorization", "Bearer " + TOKEN)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", API_VERSION)
                .contentType(ContentType.JSON)
                .delete(BASE_URI + "/" + gistID);

        //Then the Gist is deleted successfully
        Assert.assertEquals(204, response.getStatusCode());

        //Skip cleanUp method as Gist already deleted
        skipCleanup = true;
    }

    
}
