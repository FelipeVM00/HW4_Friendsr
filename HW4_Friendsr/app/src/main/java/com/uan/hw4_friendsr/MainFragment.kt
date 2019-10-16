package com.uan.hw4_friendsr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment

import java.io.FileNotFoundException


class MainFragment : Fragment() {

    private var mImageViews: Array<ImageView>? = null
    private var mTextViews: Array<TextView>? = null
    private var mRatingBars: Array<RatingBar>? = null

    private var mMediaPlayer: MediaPlayer? = null

    private var mLinearLayout: LinearLayout? = null

    internal fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    internal fun onActivityCreated(savedInstanceState: Bundle) {
        super.onActivityCreated(savedInstanceState)

        mImageViews = null
        mTextViews = null
        mRatingBars = null

        mImageViews?.set(0, activity?.findViewById(R.id.chandlerImage) as ImageView)
        mImageViews?.set(1, activity?.findViewById(R.id.joeyImage) as ImageView)
        mImageViews?.set(2, activity?.findViewById(R.id.monicaImage) as ImageView)
        mImageViews?.set(3, activity?.findViewById(R.id.phoebeImage) as ImageView)
        mImageViews?.set(4, activity?.findViewById(R.id.rachelImage) as ImageView)
        mImageViews?.set(5, activity?.findViewById(R.id.rossImage) as ImageView)

        mTextViews?.set(0, activity?.findViewById(R.id.chandler) as TextView)
        mTextViews?.set(1, activity?.findViewById(R.id.joey) as TextView)
        mTextViews?.set(2, activity?.findViewById(R.id.monica) as TextView)
        mTextViews?.set(3, activity?.findViewById(R.id.phoebe) as TextView)
        mTextViews?.set(4, activity?.findViewById(R.id.rachel) as TextView)
        mTextViews?.set(5, activity?.findViewById(R.id.ross) as TextView)

        mRatingBars?.set(0, activity?.findViewById(R.id.chandlerRatingBar) as RatingBar)
        mRatingBars?.set(1, activity?.findViewById(R.id.joeyRatingBar) as RatingBar)
        mRatingBars?.set(2, activity?.findViewById(R.id.monicaRatingBar) as RatingBar)
        mRatingBars?.set(3, activity?.findViewById(R.id.phoebeRatingBar) as RatingBar)
        mRatingBars?.set(4, activity?.findViewById(R.id.rachelRatingBar) as RatingBar)
        mRatingBars?.set(5, activity?.findViewById(R.id.rossRatingBar) as RatingBar)


        val friendNames = resources.getStringArray(R.array.friend_names)

        val sharedPreferences = activity?.getSharedPreferences(
            RATING_PREF,
            Context.MODE_PRIVATE
        )

        for (i in mRatingBars!!.indices) {
            val rating = sharedPreferences?.getFloat(friendNames[i], 0f)
            if (rating != null) {
                mRatingBars!![i].rating = rating
            }
        }

        for (i in mImageViews!!.indices) {
            mImageViews!![i].setOnClickListener {
                if (resources.configuration.orientation === Configuration.ORIENTATION_LANDSCAPE) {

                    val detailsFragment = fragmentManager?.findFragmentById(
                        R.id.details_fragment
                    ) as DetailsFragment

                    detailsFragment.setName(friendNames[i])

                } else {
                    val intent = Intent(activity, DetailsActivity::class.java)
                    intent.putExtra(NAME, friendNames[i])
                    startActivityForResult(intent, REQUEST_CODE)
                }
            }

            val image = sharedPreferences?.getString(friendNames[i] + " image", "")

            if ("" != image) {
                try {
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 8

                    val bitmap = BitmapFactory.decodeStream(
                        activity?.contentResolver?.openInputStream(Uri.parse(image)),
                        null,
                        options
                    )
                    mImageViews!![i].setImageBitmap(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }

        mMediaPlayer = MediaPlayer.create(activity, R.raw.friends_theme)

        if (resources.configuration.orientation === Configuration.ORIENTATION_LANDSCAPE) {

            mLinearLayout = activity?.findViewById(R.id.main_linear_layout)

            mLinearLayout!!.scaleX = SCALE
            mLinearLayout!!.scaleY = SCALE
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        val friendNames = arrayOf(resources.getStringArray(R.array.friend_names))

        when (requestCode) {
            REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                val name = data.getStringExtra(NAME)
                for (i in friendNames.indices) {
                    if (friendNames[i] == name) {
                        mRatingBars!![i].rating = data.getFloatExtra(STAR, 0f)
                    }
                }
            }
        }

        val sharedPreferences = activity?.getSharedPreferences(
            RATING_PREF,
            Context.MODE_PRIVATE
        )

        for (i in mImageViews!!.indices) {

            val image = sharedPreferences?.getString(friendNames[i] + " image", "")

            if ("" != image) {
                try {
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 8

                    val bitmap = BitmapFactory.decodeStream(
                        activity?.contentResolver?.openInputStream(Uri.parse(image)),
                        null,
                        options
                    )
                    mImageViews!![i].setImageBitmap(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (mMediaPlayer != null) {
            mMediaPlayer!!.isLooping = true

            val sharedPreferences = activity?.getSharedPreferences(
                RATING_PREF,
                Context.MODE_PRIVATE
            )

            if (sharedPreferences != null) {
                mMediaPlayer!!.seekTo(sharedPreferences.getInt(POS, 0))
            }

            mMediaPlayer!!.setOnSeekCompleteListener { mp -> mp.start() }

        }

    }

    override fun onPause() {
        mMediaPlayer!!.pause()
        val pos = mMediaPlayer!!.currentPosition

        val editor = activity?.getSharedPreferences(
            RATING_PREF,
            Context.MODE_PRIVATE
        )?.edit()

        editor?.putInt(POS, pos)?.commit()

        super.onPause()
    }

    override fun onDestroy() {

        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
        }

        super.onDestroy()
    }

    companion object {

        val REQUEST_CODE = 0
        val NAME = "NAME"
        val STAR = "STAR"

        val RATING_PREF = "rating"

        val POS = "pos"

        val SCALE = 0.6f
    }
}
