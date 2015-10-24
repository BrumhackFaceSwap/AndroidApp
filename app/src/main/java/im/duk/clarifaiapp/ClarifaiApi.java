package im.duk.clarifaiapp;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * Created by andreas on 24/10/2015.
 */
public class ClarifaiApi {

    public static String post(String token, String file) {
        HttpClient client = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();

        HttpPost post = new HttpPost("https://api.clarifai.com/v1/tag/");
        post.setHeader("Authorization", "Bearer S20i4aL5hqE9pcmFxEf1IsF7UU4Edo");

        //post.s
        return null;
    }

}
