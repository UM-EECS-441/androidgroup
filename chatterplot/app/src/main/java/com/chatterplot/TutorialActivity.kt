package com.chatterplot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment

class TutorialActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure you don't call setContentView!

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(
            AppIntroFragment.newInstance(
            title = "Welcome to Chatterplot!",
            description = "Here's a quick tour of the app"
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "The magic word is \"Listen!\"",
            description = "Use it to give Chatterplot some commands:"
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "-Make new Dataset Called X\n-Enter N into X",
            description = "TODO: flesh out intro tutorial"
        ))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        finish()
    }
}
