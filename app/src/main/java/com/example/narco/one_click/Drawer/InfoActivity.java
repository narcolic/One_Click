package com.example.narco.one_click.Drawer;

import android.os.Bundle;

import com.example.narco.one_click.R;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.aboutlibraries.ui.LibsActivity;

import java.io.Serializable;
import java.util.Comparator;


public class InfoActivity extends LibsActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        LibsBuilder builder = new LibsBuilder()
                .withLibraries("crouton, actionbarsherlock", "showcaseview")
                .withActivityTheme(R.style.CustomTheme)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutAppName("One Click")
                .withAboutDescription("One click is all you need explore the world!")
                .withLibraryComparator(new LibraryComparator());

        setIntent(builder.intent(this));
        super.onCreate(savedInstanceState);
    }


    private static class LibraryComparator implements Comparator<Library>, Serializable {

        @Override
        public int compare(Library lhs, Library rhs) {
            // Just to show you can sort however you might want to...
            int result = lhs.getAuthor().compareTo(rhs.getAuthor());
            if (result == 0) {
                // Backwards sort by lib name.
                result = rhs.getLibraryName().compareTo(lhs.getLibraryName());
            }
            return result;
        }
    }
}