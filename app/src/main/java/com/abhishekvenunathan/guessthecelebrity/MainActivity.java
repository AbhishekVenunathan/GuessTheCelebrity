package com.abhishekvenunathan.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button button1, button2, button3, button4;
    ImageView imageView;
    TextView score;

    ArrayList<String> celebURL = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    String[] answers = new String[4];
    int locationOfAnswer = 0;
    int ChosenCeleb = 0;
    int correct=0, total=0;

    public class downloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url=new URL(urls[0]);
                urlConnection=(HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public class imageTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;


            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public void celebChosen (View view){
        if (view.getTag().toString().equals(Integer.toString(locationOfAnswer))){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            correct++;
            total++;
            score.setText("Score:"+correct+"/"+total);
            newQuestion();
        }else{
            Toast.makeText(this, "Wrong! It was "+celebNames.get(ChosenCeleb)+".", Toast.LENGTH_SHORT).show();
            total++;
            score.setText("Score:"+correct+"/"+total);
            newQuestion();
        }

    }

    public void newQuestion(){
        try {
            Random rand = new Random();
            ChosenCeleb = rand.nextInt(celebURL.size());

            imageTask downloadImage = new imageTask();
            Bitmap celebImage = downloadImage.execute(celebURL.get(ChosenCeleb)).get();

            imageView.setImageBitmap(celebImage);

            locationOfAnswer = rand.nextInt(4);

            int incorrectAnswer;
            for (int i = 0; i < 4; i++) {
                if (i == locationOfAnswer) {
                    answers[i] = celebNames.get(ChosenCeleb);
                } else {
                    incorrectAnswer = rand.nextInt(celebURL.size());

                    while (incorrectAnswer == ChosenCeleb) {
                        incorrectAnswer = rand.nextInt(celebURL.size());
                    }

                    answers[i] = celebNames.get(incorrectAnswer);
                }
            }
            button1.setText(answers[0]);
            button2.setText(answers[1]);
            button3.setText(answers[2]);
            button4.setText(answers[3]);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1=findViewById(R.id.button0);
        button2=findViewById(R.id.button1);
        button3=findViewById(R.id.button2);
        button4=findViewById(R.id.button3);
        imageView=findViewById(R.id.imageView);
        score=findViewById(R.id.textView);


        downloadTask task = new downloadTask();
        String result = null;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticle\">");

            Pattern pattern = Pattern.compile("img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);

            while (matcher.find()){
                celebURL.add(matcher.group(1));
            }

            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);

            while (matcher.find()){
                celebNames.add(matcher.group(1));
            }
            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
