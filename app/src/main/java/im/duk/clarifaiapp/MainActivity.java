package im.duk.clarifaiapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

            Bitmap drawingPhoto = photo.copy(bitmap_config, true);

            FaceDetector faceDetector = new FaceDetector(photo.getWidth(), photo.getHeight(), 3);
            FaceDetector.Face[] faces;
            faces = new FaceDetector.Face[3];
            int faceCount = faceDetector.findFaces(drawingPhoto, faces);
            Toast.makeText(this, "Face count:" + faceCount, Toast.LENGTH_SHORT).show();
            Canvas canvas = new Canvas(drawingPhoto);
            PointF point = new PointF();
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setAlpha(100);
            faces[0].getMidPoint(point);
            canvas.drawCircle(point.x, point.y, faces[0].eyesDistance(), paint);
            imageView.setImageBitmap(drawingPhoto);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ClarifaiClient clarifaiClient = new ClarifaiClient("nSB3QSeOYfxKkSAuGXHpodxsKoM6mNcbzEp8su0z", "otytNX9yjKyw4AA82r9uTVMjqefIopIWgSN1qm7g");
                    List<RecognitionResult> results = clarifaiClient.recognize(new RecognitionRequest(new File(imageFileLocation)));
                    final List<Tag> tags = results.get(0).getTags();
                    final Tag[] tagarr = tags.toArray(new Tag[tags.size()]);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(MainActivity.this, tagarr[0].getName(), Toast.LENGTH_SHORT).show();
                            String message = "";
                            for (Tag tag : tags) {
                                message += tag.getName() + ": " + tag.getProbability() + "\n";

                            }


                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Possible in the image:")
                                    .setMessage(message)
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
