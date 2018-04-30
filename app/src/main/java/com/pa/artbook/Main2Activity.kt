package com.pa.artbook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream

class Main2Activity : AppCompatActivity() {


    //// selected image variable is null until the user selects a photo
    var selectedImage : Bitmap? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val intent = parentActivityIntent
        val info = intent.getStringExtra("info")

        if (info.equals("new")) {
            //// TODO: different drawable file
            val background = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.abc_btn_check_material)
            imageView.setImageBitmap(background)
            button.visibility = View.VISIBLE
            editText.setText ("")
        } else {
            val name = intent.getStringExtra("name")
            editText.setText(name)

            val chosen = Globals.Chosen
            val bitmap = chosen.returnImage()

            imageView.setImageBitmap(bitmap)

            button.visibility = View.INVISIBLE
        }

    }



    //// time to select the photo



    fun select (view: View) {

        //// if we do not have permission for storage, ask for it
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),2)
        } else {

            //// opens the media store where they can pick their photos
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == 2) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //// opens the media store where they can pick their photos
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 1)
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            //// this is the image the user has chosen from media store
            val image = data.data

            try {

                //// convert image to bitmap or something
                val selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, image)

                //// put the bitmap into the imageview
                imageView.setImageBitmap(selectedImage)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }



    //// now to save the text and photo


    fun save(view: View) {

        //// art name is whatever user puts into the edittext
        val artName = editText.text.toString()

        //// converting the image into bytes (data) to save it
        val outPutStream = ByteArrayOutputStream()

        //// compressing the image
        selectedImage?.compress(Bitmap.CompressFormat.PNG, 50,outPutStream)

        //// output stream to byte array
        val byteArray = outPutStream.toByteArray()

        try {

            //// open or create the database named "Arts" with a mode of private, meaning only our app can access it
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)

            //// create the database table named "arts" if it does not exist and allow image saving
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR, image BLOB)")

            //// idk what this is really doing
            val sqlString = "INSERT INTO arts (name, image) VALUES (?, ?)"
            val statement = database.compileStatement(sqlString)
            statement.bindString (1,artName)
            statement.bindBlob (2,byteArray)
            statement.execute()



        } catch (e: Exception) {
            e.printStackTrace()
        }

        //// go back to main activity after they have added their art
        val intent = Intent(applicationContext, MainActivity::class.java)



    }

}
