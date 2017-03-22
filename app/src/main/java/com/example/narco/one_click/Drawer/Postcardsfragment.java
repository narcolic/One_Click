package com.example.narco.one_click.Drawer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.narco.one_click.R;
import com.squareup.picasso.Picasso;

import java.io.File;


public class Postcardsfragment extends Fragment {

    private File[] listFile;
    File file;

    public static Postcardsfragment newInstance() {
        return new Postcardsfragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_postcatds, parent, false);
        getActivity().setTitle(R.string.postcards_menu_title);
        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.main_layout);

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getActivity(), "Error! No SDCARD Found!", Toast.LENGTH_LONG)
                    .show();
        } else {
            // Locate the image folder in your SD Card
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + File.separator + "One_Click_Pics");
        }

        if (file.isDirectory()) {
            listFile = file.listFiles();
            // Create a String array for FilePathStrings
            String[] filePathStrings;
            filePathStrings = new String[listFile.length];
            // Create a String array for FileNameStrings
            String[] fileNameStrings;
            fileNameStrings = new String[listFile.length];

            for (int i = 0; i < listFile.length; i++) {
                // Get the path of the image file
                filePathStrings[i] = listFile[i].getAbsolutePath();
                // Get the name image file
                fileNameStrings[i] = listFile[i].getName();
            }
        }

        for (final File file : listFile) {
            ImageView imageView = new ImageView(getActivity());
            LinearLayout layout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 100, 0, 100);
            Picasso.with(getActivity())
                    .load(file)
                    .resize(400, 750)
                    .centerCrop()
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Intent i = new Intent(getActivity(), FullScreenImageActivity.class);
                    i.putExtra("FILE", file);
                    startActivity(i);

                }
            });

            layout.addView(imageView, layoutParams);
            linearLayout.addView(layout);
        }
        return v;
    }


}