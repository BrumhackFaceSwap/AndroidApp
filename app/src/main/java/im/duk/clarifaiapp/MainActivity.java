package im.duk.clarifaiapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private ImageView imageView;
    private String imageFileLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void buttonOnClick(View view) {
        Intent cameraIntent = new Intent();
        cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File image = null;
        try {
            image = createImageFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
        startActivityForResult(cameraIntent, ACTIVITY_START_CAMERA_APP);
        Toast.makeText(this, "Camera button pressed!", Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            Toast.makeText(this, "Picture taken successfully", Toast.LENGTH_SHORT).show();
            //Bundle extras = data.getExtras();
//            Bitmap photo = (Bitmap) extras.get("data");

//            imageView.setImageBitmap(photo);
            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap photo = BitmapFactory.decodeFile(imageFileLocation, bitmap_options);
            Bitmap.Config bitmap_config = photo.getConfig();
            if(bitmap_config == null) {
                bitmap_config = Bitmap.Config.RGB_565;
            }

            final Bitmap drawingPhoto = photo.copy(bitmap_config, true);

            FaceDetector faceDetector = new FaceDetector(photo.getWidth(), photo.getHeight(), 3);
            FaceDetector.Face[] faces;
            faces = new FaceDetector.Face[3];
            int faceCount = faceDetector.findFaces(drawingPhoto, faces);
            Toast.makeText(this, "Face count:" + faceCount, Toast.LENGTH_SHORT).show();
            final Canvas canvas = new Canvas(drawingPhoto);
//            PointF point = new PointF();
//            Paint paint = new Paint();
//            paint.setColor(Color.RED);
//            paint.setAlpha(100);
//            faces[0].getMidPoint(point);
            //canvas.drawCircle(point.x, point.y, faces[0].eyesDistance(), paint);
            imageView.setImageBitmap(drawingPhoto);
            final FaceDetector.Face face = faces[0];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] tags = new String[] {
                            "BearHead",
                            "CatHead",
                            "ChickenHead",
                            "CowHead",
                            "DeerHead",
                    };
                    Map<String, String> imageMap = new HashMap<String, String>();
                    imageMap.put("BearHead", "bear");
                    imageMap.put("CatHead", "cat");
                    imageMap.put("ChickenHead", "chicken");
                    imageMap.put("CowHead", "cow");
                    imageMap.put("DeerHead", "deer");


                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    drawingPhoto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                    String base64EncodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    final ArrayList<String> responseResults = new ArrayList<String>();

                    String accessToken = "y6fHD7OLjZJUj8n4SDpL4VvaOmFTyl";

//                    String tag = tags[0];
                    for (String tag : tags) {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("https://api-alpha.clarifai.com/v1/curator/concepts/default/" + tag + "/predict");
                        httpPost.setHeader("Authorization", "Bearer " + accessToken);
                        httpPost.setHeader("Content-type", "application/json");

                    try {
                        StringEntity se = new StringEntity("{\"urls\":[\"http://a3.files.biography.com/image/upload/c_fill,cs_srgb,dpr_1.0,g_face,h_300,q_80,w_300/MTE5NDg0MDU1MTUyNzIzNDcx.jpg\"]}");
                        httpPost.setEntity(se);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    /*
                        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
                        nameValuePair.add(new BasicNameValuePair("encoded_data", base64EncodedImage));
                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    */

                        try {
                            HttpResponse response = httpClient.execute(httpPost);
                            responseResults.add(EntityUtils.toString(response.getEntity()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    final Map<String, Double> scoreMap = new HashMap<String, Double>();
                    for(int i = 0; i < tags.length; ++i) {
                        try {
                            JSONObject jso = new JSONObject(responseResults.get(i));
                            String animal = tags[i];
                            JSONArray jsa = (JSONArray) jso.get("urls");
                            Double score = (Double)((JSONObject) jsa.get(0)).get("score");
                            scoreMap.put(animal, score);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Double highestScore = null;
                    String highestAnimal = null;
                    for(String key : scoreMap.keySet()) {
                        Double currentScore = scoreMap.get(key);
                        if (highestScore != null) {
                            if(currentScore.doubleValue() > highestScore.doubleValue()) {
                                highestScore = currentScore;
                                highestAnimal = key;
                            }
                        } else {
                            highestScore = currentScore;
                            highestAnimal = key;
                        }
                    }
                    final String copyOfHighestAnimal = highestAnimal;
                    final double copyOfHighestScore = highestScore.doubleValue();
                    final Map<String, String> copyOfImageMap = imageMap;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Resources res = getResources();
                            int picId = res.getIdentifier(copyOfImageMap.get(copyOfHighestAnimal), "drawable", getApplicationContext().getPackageName());

                            Bitmap bitmapDrawable = BitmapFactory.decodeResource(res, picId);
                            Paint paint = new Paint();
                            PointF point = new PointF();
                            face.getMidPoint(point);
                            canvas.drawBitmap(bitmapDrawable, point.x - (bitmapDrawable.getWidth() / 2), point.y - (bitmapDrawable.getHeight() / 2), paint);

                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Possible in the image:")
                                    .setMessage(copyOfHighestAnimal + ": " + copyOfHighestScore)
//                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            // continue with delete
//                                        }
//                                    })
//                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            // do nothing
//                                        }
//                                    })

                                    .show();
                        }
                    });
                }
            }).start();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timestamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        this.imageFileLocation = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
