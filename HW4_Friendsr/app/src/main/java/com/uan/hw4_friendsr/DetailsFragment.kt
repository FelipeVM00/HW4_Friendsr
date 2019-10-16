package com.uan.hw4_friendsr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class DetailsFragment : Fragment() {

    private var mRatingBar: RatingBar? = null
    private var mDetailsImageView: ImageView? = null
    private var mDetailsTextView: TextView? = null

    private var mMediaPlayer: MediaPlayer? = null

    private var mTakePhotoButton: Button? = null
    private var mChooseFromAlbumButton: Button? = null

    private var mName: String? = null

    private var mImageUri: Uri? = null

    private var mLinearLayout: LinearLayout? = null


    internal fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle
    ): View {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    internal fun Bundle.onActivityCreated() {
        super.onActivityCreated(this)

        mRatingBar = getActivity()?.findViewById(R.id.ratingBar)
        mDetailsImageView = getActivity()?.findViewById(R.id.detailsImage)
        mDetailsTextView = getActivity()?.findViewById(R.id.details)

        val intent = getActivity()?.getIntent()

        mName = intent?.getStringExtra(this@DetailsFragment.mName)

        setName(mName)

        mRatingBar!!.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_LANDSCAPE) {
                    refreshMainFragment()
                } else {
                    val intent = Intent()
                    intent.putExtra(MainFragment.NAME, mName)
                    intent.putExtra(MainFragment.STAR, rating)
                    getActivity()?.setResult(Activity.RESULT_OK, intent)
                }

                val editor = getActivity()?.getSharedPreferences(
                    MainFragment.RATING_PREF,
                    Context.MODE_PRIVATE
                )?.edit()

                editor!!.putFloat(mName, rating).commit()

                if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_PORTRAIT) {
                    getActivity()?.finish()
                }
            }

        getContext()?.let {
            mDetailsImageView!!.setOnTouchListener(object : OnSwipeTouchListener(
                it
            ) {

                override fun onSwipeLeft(dx: Float) {
                    super.onSwipeLeft(dx)
                    mRatingBar!!.rating = 1f
                }

                override fun onSwipeRight(dx: Float) {
                    super.onSwipeRight(dx)
                    mRatingBar!!.rating = mRatingBar!!.numStars.toFloat()
                }
            })
        }

        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.friends_theme)

        mTakePhotoButton = getActivity()?.findViewById(R.id.take_photo)
        mChooseFromAlbumButton = getActivity()?.findViewById(R.id.choose_from_album)

        mTakePhotoButton!!.setOnClickListener {
            val outputImage = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ), mName!! + ".jpg"
            )

            try {
                if (outputImage.exists()) {
                    outputImage.delete()
                }

                outputImage.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mImageUri = Uri.fromFile(outputImage)

            val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            i.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(
                i,
                TAKE_PHOTO
            )
        }

        mChooseFromAlbumButton!!.setOnClickListener {
            val outputImage = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ), mName!! + ".jpg"
            )

            try {
                if (outputImage.exists()) {
                    outputImage.delete()
                }

                outputImage.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mImageUri = Uri.fromFile(outputImage)

            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(
                i,
                PICK_PHOTO
            )
        }

        if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_LANDSCAPE) {

            mLinearLayout = getActivity()?.findViewById(R.id.details_linear_layout)

            mLinearLayout!!.scaleX = MainFragment.SCALE
            mLinearLayout!!.scaleY = MainFragment.SCALE
        }
    }

    override fun onResume() {
        super.onResume()

        if (mMediaPlayer !=
            null && getActivity()?.getSupportFragmentManager()?.findFragmentById(R.id.main_fragment) == null
        ) {
            mMediaPlayer!!.isLooping = true

            val sharedPreferences = getActivity()?.getSharedPreferences(
                MainFragment.RATING_PREF,
                Context.MODE_PRIVATE
            )

            if (sharedPreferences != null) {
                mMediaPlayer!!.seekTo(sharedPreferences.getInt(MainFragment.POS, 0))
            }

            mMediaPlayer!!.setOnSeekCompleteListener { mp -> mp.start() }

        }
    }

    override fun onPause() {

        if (mMediaPlayer != null && getActivity()?.getSupportFragmentManager()?.findFragmentById(R.id.main_fragment) == null) {

            mMediaPlayer!!.pause()
            val pos = mMediaPlayer!!.currentPosition

            val editor = getActivity()?.getSharedPreferences(
                MainFragment.RATING_PREF,
                Context.MODE_PRIVATE
            )?.edit()

            editor?.putInt(MainFragment.POS, pos)?.commit()

        }
        super.onPause()
    }

    override fun onDestroy() {

        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
        }

        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(mImageUri, "image/*")
                intent.putExtra("scale", true)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)

                startActivityForResult(intent, CROP_PHOTO)
            }
            CROP_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                try {
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 8

                    val bitmap = BitmapFactory.decodeStream(
                        mImageUri?.let { getActivity()?.getContentResolver()?.openInputStream(it) }, null, options
                    )
                    mDetailsImageView!!.setImageBitmap(bitmap)

                    val editor = getActivity()?.getSharedPreferences(
                        MainFragment.RATING_PREF,
                        Context.MODE_PRIVATE
                    )?.edit()

                    editor?.putString(mName!! + " image", mImageUri!!.toString())?.commit()

                    if (getResources().getConfiguration().orientation === Configuration.ORIENTATION_LANDSCAPE) {
                        refreshMainFragment()
                    }

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
            PICK_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                var fromImageUri: Uri? = null
                if (data != null) {
                    fromImageUri = data.data
                }

                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(fromImageUri, "image/*")
                intent.putExtra("scale", true)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)

                startActivityForResult(intent, CROP_PHOTO)
            }
            else -> {
            }
        }
    }

    fun setName(name: String?) {
        mName = name

        val friendNames = getResources().getStringArray(R.array.friend_names)
        val friendDetails = getResources().getStringArray(R.array.friend_details)

        for (i in friendNames.indices) {
            if (friendNames[i] == mName) {
                mDetailsImageView!!.setImageResource(
                    getResources().getIdentifier(
                        mName!!.toLowerCase(), "mipmap", getActivity()?.getPackageName()
                    )
                )
                mDetailsTextView!!.setText(friendDetails[i])

                val sharedPreferences = getActivity()?.getSharedPreferences(
                    MainFragment.RATING_PREF, Context.MODE_PRIVATE
                )

                mRatingBar!!.rating = sharedPreferences?.getFloat(friendNames[i], 0f)!!

                val image = sharedPreferences?.getString(friendNames[i] + " image", "")

                if ("" != image) {
                    try {
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 8

                        val bitmap = BitmapFactory.decodeStream(
                            getActivity()?.getContentResolver()?.openInputStream(Uri.parse(image)),
                            null,
                            options
                        )
                        mDetailsImageView!!.setImageBitmap(bitmap)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }

                }

                break
            }
        }
    }

    private fun refreshMainFragment() {
        val main_fragment =
            getActivity()?.getSupportFragmentManager()?.findFragmentById(R.id.main_fragment)
        val fragmentTransaction = getActivity()?.getSupportFragmentManager()?.beginTransaction()

        if (main_fragment != null) {
            fragmentTransaction?.detach(main_fragment)?.attach(main_fragment)?.commit()
        }
    }

    companion object {

        val TAKE_PHOTO = 0
        val CROP_PHOTO = 1
        val PICK_PHOTO = 2
    }
}

private fun ImageView.setOnTouchListener(any: Any) {

}
