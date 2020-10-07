package com.ilnur.Fragments

import android.Manifest.permission.*
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.ilnur.FirstPoint
import com.ilnur.R
import com.ilnur.utils.Masker.setMasked
import com.ilnur.utils.MultipartUtility
import com.ilnur.utils.TaskImage
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class AnswerFragment1 : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    internal lateinit var answer: String
    internal var type: Int = 0
    internal var questionNumber: Int = 0
    internal var task: Int = 0
    internal var category: Int = 0
    internal var id_task = ""
    internal var NEXT_QUESTION: Byte = 1
    internal var SHOW_COMMENT: Byte = 2
    internal var SHOW_CRIT: Byte = 3
    internal var show_comment: Boolean = false
    internal lateinit var pointObj: FirstPoint
    private lateinit var permissionsToRequest: ArrayList<String>
    private var permissionsRejected: ArrayList<String> = ArrayList()
    private var permissions: ArrayList<String> = ArrayList()
    private val ALL_PERMISSIONS_RESULT = 107
    var images = HashMap<Int, TaskImage>()
    internal var b = "11100110111011111100"
    //internal var `setMasked()` = ""
    internal lateinit var subj_pref: String
    internal var token = ""
    //internal var current_img: TaskImage? = TaskImage()

    //internal  var bar: Array<Int> = Array(b.length, init = {i: Int -> 0})
    val maxPoints: Int
        get() {
            val max: Int
            max = pointObj.maxPoints()
            return max
        }

    fun setToken(token: String, subj_pref: String) {
        this.token = token
        this.subj_pref = subj_pref
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_answer, null)

        retainInstance = true
        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions)!!
        initializeButtons(v)
        initializeSettings()
        initializePoints()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (permissionsToRequest!!.size > 0)
                requestPermissions(permissionsToRequest.toTypedArray(), ALL_PERMISSIONS_RESULT);
        }

        return v
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }

    }

    // atoken eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1ODkwMTU5MjQsInVzZXJfaWQiOjg1NzkxN30.FP605_xiA1pVpkR8O3sJGqnR7PlFGUK5JWYKwZTSWLQ
    // rtoken eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo4NTc5MTcsImlkIjo0MjQwMzk4fQ.eJDtGXn2WToYPjWvyfvEIK8XGU9gu14kOtNu9u87Aow
    //__cfduid da4dacd6756aa341c90943ce41ba9a0651589012324
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //this.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("onRes", "fr")
        val LINE_FEED = "\r\n"
        if (resultCode == RESULT_OK) {
            Log.d("onREs", File(currentPhotoPath).path)
            Log.d("onREs", currentPhotoPath)
            doAsync(data, requestCode, resultCode).execute()
        }

        Log.d("Req & Res", "" + requestCode + " " + resultCode)
    }
    //var path: File? = null
    inner class doAsync(internal var data: Intent?, internal var requestCode: Int, internal var resultCode: Int) : AsyncTask<Void, Void, MultipartUtility>() {
        override fun doInBackground(vararg params: Void?): MultipartUtility {
            val link = "https://"+subj_pref+"-ege.sdamgia.ru/sol_upload?a=upload_txt&prob=" + setMasked(id_task)
            val url = URL(link)



            //images.put(questionNumber,TaskImage(id_task, setMasked(id_task), null, currentPhotoPath))
            //val options =
            Log.d("num, or, mask", questionNumber.toString() + "  " + id_task + " " + setMasked(id_task))
            val tk = "rtoken=" + token

            val builder = MultipartUtility(link, "UTF-8", tk)

            return builder
        }

        override fun onPreExecute() {
            super.onPreExecute()

        }

        override fun onPostExecute(builder: MultipartUtility) {
            super.onPostExecute(builder)
            if (requestCode == 221) {
                if (resultCode == RESULT_OK) {
                    Log.d("Req& Res", "" + requestCode + " " + resultCode)


                    val f = File(currentPhotoPath)

                    //val img = File(getImageFilePath(data))
                    builder.addHeaderField("Connection", "Keep-Alive")
                    builder.addHeaderField("ENCTYPE", "multipart/form-data")
                    builder.addHeaderField("Content-Type", "multipart/form-data;boundary=-----")
                    builder.addFormField("prob", setMasked(id_task))
                    builder.addFormField("a", "upload")
                   // val ur = data!!.data
                    //Log.d("uri", Uri.fromFile(path).toString())
                    //Log.d("path", path!!.path)
                    builder.addFilePart("solution", f)



                    val response: List<String> = builder.finish()
                    Log.d("Resp", response.toString())
                    setButtonsVisibility(type)
                } else if (resultCode == RESULT_CANCELED) {
                    setButtonsVisibility(type)
                } else {
                    setButtonsVisibility(type)
                }
            }
            if (token != "" ) {
                if (images.get(questionNumber) != null && images.get(questionNumber)!!.bitmap!=  null) {
                    val lay = view!!.findViewById<LinearLayout>(R.id.image_lay)
                    lay.visibility = View.VISIBLE
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 4
                    val s = images.get(questionNumber)
                    s!!.bitmap = BitmapFactory.decodeFile(currentPhotoPath, options)
                    val img_view = view!!.findViewById<View>(R.id.preload) as ImageView
                    img_view.visibility = View.VISIBLE
                    img_view.setImageBitmap(s!!.bitmap)
                    images.put(questionNumber, s!!)
                } else {
                    val lay = view!!.findViewById<LinearLayout>(R.id.image_lay)
                    lay.visibility = View.VISIBLE
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 4
                    val s = TaskImage(id_task, setMasked(id_task), File(currentPhotoPath).name, currentPhotoPath)
                    s!!.bitmap = BitmapFactory.decodeFile(currentPhotoPath, options)
                    val img_view = view!!.findViewById<View>(R.id.preload) as ImageView
                    img_view.visibility = View.VISIBLE
                    img_view.setImageBitmap(s!!.bitmap)
                    images.put(questionNumber, s!!)
                }
            }
        }
    }


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(answer: Boolean)
        fun onFragmentInteraction(yourAnswer: String, point: String, pluses: String, color: String, taskImage: TaskImage?)
        fun onFragmentInteraction(command: Byte)
    }

    fun sendResult(answer: Boolean, yourAnswer: String, point: String, pluses: String, color: String) {
        mListener!!.onFragmentInteraction(answer)
        mListener!!.onFragmentInteraction(yourAnswer, point, pluses, color, images.get(questionNumber))
    }

    fun setAnswer(answer: String, type: Int, questionNumber: Int, category: Int, id_task: String) {
        Log.d("setAns", "ANS")

        this.answer = answer
        this.type = type
        this.category = category
        this.questionNumber = questionNumber
        this.id_task = id_task
        //32373063
        if (type == 3) {
            setMasked(id_task)
            Log.d("or & Mas", id_task + " " + setMasked(id_task))
            if (images.get(questionNumber) != null && images.get(questionNumber)!!.bitmap != null) {
                requireView().findViewById<LinearLayout>(R.id.image_lay).visibility = View.VISIBLE

                val img_view = requireView().findViewById<View>(R.id.preload) as ImageView
                img_view.visibility = View.VISIBLE
                img_view.setImageBitmap(images.get(questionNumber)!!.bitmap)
            } else {
                requireView().findViewById<LinearLayout>(R.id.image_lay).visibility = View.GONE

            }
        }
        val answerText = requireView().findViewById<View>(R.id.answerText) as EditText
        answerText.setText("")
        answerText.isEnabled = true
        val answerButton = requireActivity().findViewById<View>(R.id.answerButton) as Button
        val nextButton = requireActivity().findViewById<View>(R.id.nextButton) as Button
        val commentButton = requireActivity().findViewById<View>(R.id.commentButton) as Button
        answerButton.visibility = View.VISIBLE
        nextButton.visibility = View.GONE
        commentButton.visibility = View.GONE
    }

    fun setYourAnswer(yourAnswer: String) {
        val answerText = requireView().findViewById<View>(R.id.answerText) as EditText
        answerText.setText("Ваш ответ: $yourAnswer")
        answerText.isEnabled = false
        val answerButton = requireActivity().findViewById<View>(R.id.answerButton) as Button
        val nextButton = requireActivity().findViewById<View>(R.id.nextButton) as Button
        val commentButton = requireActivity().findViewById<View>(R.id.commentButton) as Button
        val pointSpinner = requireView().findViewById<View>(R.id.pointSpinner) as Spinner
        val pointsButton = requireView().findViewById<View>(R.id.pointsButton) as Button
        answerButton.visibility = View.GONE
        nextButton.visibility = View.VISIBLE
        pointSpinner.visibility = View.GONE
        pointsButton.visibility = View.GONE
        if (show_comment)
            commentButton.visibility = View.VISIBLE
        else
            commentButton.visibility = View.GONE
    }

    private fun checkAnswer(answer: String, type: Int, category: Int) {
        var answer = answer
        val answerText = requireView().findViewById<View>(R.id.answerText) as EditText
        var yourAnswer = answerText.text.toString()

        //answer = answer.toLowerCase();
        if (answerText.inputType == InputType.TYPE_CLASS_PHONE) {
            answer = answer.replace(".", "")
        }
        answer = answer.replace(" ", "")
        answer = answer.replace(".", ",")
        //yourAnswer = yourAnswer.toLowerCase();
        yourAnswer = yourAnswer.replace(" ", "")
        yourAnswer = yourAnswer.replace(".", ",")
        if (yourAnswer.contentEquals("")) yourAnswer = "Не решено"



        if (type != 3) {
            val point = pointObj.getFirtsPoint(yourAnswer, answer, questionNumber, category)
            val pluses = pointObj.pluses
            var color = pointObj.color
            if (yourAnswer.contentEquals("Не решено")) color = "white"
            sendResult(pointObj.checkAnswer(), yourAnswer, point, pluses, color!!)
        } else {
            val pointSpinner = requireActivity().findViewById<View>(R.id.pointSpinner) as Spinner
            val point = pointSpinner.selectedItemPosition
            if (images.get(questionNumber) != null) {
                if (questionNumber < pointObj.task_count()) {
                    if (Integer.parseInt(pointObj.get_max(questionNumber)) == point) {
                        sendResult(true, "Часть С", Integer.toString(point), "+", "green")
                    } else if (Integer.parseInt(pointObj.get_max(questionNumber)) == 0)
                        sendResult(false, "Часть С", Integer.toString(point), "-", "red")
                    else
                        sendResult(false, "Часть С", Integer.toString(point), "-", "yellow")
                } else
                    sendResult(false, "Часть С", Integer.toString(point), "-", "yellow")
            } else {
                if (questionNumber < pointObj.task_count()) {
                    if (Integer.parseInt(pointObj.get_max(questionNumber)) == point) {
                        sendResult(true, "Часть С", Integer.toString(point), "+", "green")
                    } else if (Integer.parseInt(pointObj.get_max(questionNumber)) == 0)
                        sendResult(false, "Часть С", Integer.toString(point), "-", "red")
                    else
                        sendResult(false, "Часть С", Integer.toString(point), "-", "yellow")
                } else
                    sendResult(false, "Часть С", Integer.toString(point), "-", "yellow")
            }
        }
        answerText.setText("")
    }

    fun nextQuestion() {
        mListener!!.onFragmentInteraction(NEXT_QUESTION)
    }

    fun showComment() {
        mListener!!.onFragmentInteraction(SHOW_COMMENT)
    }

    fun setButtonsVisibility(type: Int) {
        Log.d("setButtVis", "ANS")
        val load_img = requireActivity().findViewById<View>(R.id.load_img) as Button
        val answerButton = requireActivity().findViewById<View>(R.id.answerButton) as Button
        val nextButton = requireActivity().findViewById<View>(R.id.nextButton) as Button
        val commentButton = requireActivity().findViewById<View>(R.id.commentButton) as Button
        val answerText = requireView().findViewById<View>(R.id.answerText) as EditText
        val pointSpinner = requireView().findViewById<View>(R.id.pointSpinner) as Spinner
        val pointsButton = requireView().findViewById<View>(R.id.pointsButton) as Button

        if (type == 3) {
            if (images.get(questionNumber) != null && images.get(questionNumber)!!.bitmap != null && token != "") {
                requireView().findViewById<LinearLayout>(R.id.image_lay).visibility = View.VISIBLE

                val img_view = requireView().findViewById<View>(R.id.preload) as ImageView
                img_view.setImageBitmap(images.get(questionNumber)!!.bitmap)
            } else {
                requireView().findViewById<LinearLayout>(R.id.image_lay).visibility = View.GONE
                //view!!.findViewById<View>(R.id.preload).visibility = View.GONE
            }

            nextButton.visibility = View.VISIBLE
            if (show_comment)
                commentButton.visibility = View.VISIBLE
            else
                commentButton.visibility = View.GONE

            val pointsArray = ArrayList<String>()
            var count = 0
            Log.d("QUnum", "" + questionNumber)
            if (questionNumber < pointObj.task_count()) {
                count = Integer.parseInt(pointObj.get_max(questionNumber + 1))
                Log.d("POINTS", "" + count)
            }
            for (i in 0..count) {
                pointsArray.add(Integer.toString(i))
            }

            val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, pointsArray)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            pointSpinner.adapter = adapter
            if (token != "")
                load_img.visibility = View.VISIBLE
            pointSpinner.visibility = View.VISIBLE
            pointsButton.visibility = View.VISIBLE

            answerButton.visibility = View.GONE
            answerText.visibility = View.GONE
            answerText.isEnabled = false
        } else {
            pointSpinner.visibility = View.GONE
            pointsButton.visibility = View.GONE
            answerText.visibility = View.VISIBLE
            load_img.visibility = View.GONE
            requireView().findViewById<LinearLayout>(R.id.image_lay).visibility = View.GONE
        }


    }




    internal fun initializeButtons(v: View) {
        val load_img = v.findViewById<View>(R.id.load_img) as Button
        load_img.setOnClickListener { v ->
            /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, )
            val f =  File(Environment.getDataDirectory(), "datadir.jpg")
            Log.d("ExternalStorageDir",Environment.getExternalStorageDirectory().toString())
            Log.d("DataDirectory", Environment.getDataDirectory().toString())

            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
            startActivityForResult(intent, 221)*/
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile(id_task)
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        ex.printStackTrace()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                                this.requireContext(),
                                "com.reshuege.android.fileprovider",
                                it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                            takePictureIntent.clipData = ClipData.newRawUri("", photoURI)
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivityForResult(takePictureIntent, 221)
                    }
                }
            }
            //startActivityForResult(getPickImageChooserIntent(id_task), 221)
        }

        val answerButton = v.findViewById<View>(R.id.answerButton) as Button
        answerButton.setOnClickListener { v ->
            checkAnswer(answer, type, category)
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
        }

        val nextButton = v.findViewById<View>(R.id.nextButton) as Button
        nextButton.setOnClickListener { nextQuestion() }

        val commentButton = v.findViewById<View>(R.id.commentButton) as Button
        commentButton.setOnClickListener { showComment() }

        val pointsButton = v.findViewById<View>(R.id.pointsButton) as Button
        pointsButton.setOnClickListener { checkAnswer(answer, type, category) }
    }
    fun getPhotoFileUri(fileName: String): File? {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "APP_TAG")

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d("APP_TAG", "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }
    lateinit var currentPhotoPath: String

    //@Throws(IOException::class)
    private fun createImageFile(name: String): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    internal fun initializeSettings() {
        val settingsPref = PreferenceManager.getDefaultSharedPreferences(activity)

        val section = requireActivity().intent.getStringExtra("section")

        if (section != null) {
            if (section.contentEquals("Варианты") || section.contentEquals("Каталог заданий"))
                show_comment = settingsPref.getBoolean("show_comment", false)
            else if (section.contentEquals("Режим экзамена")) show_comment = false
        }
    }

    internal fun initializePoints() {
        val subject_prefix = requireActivity().intent.getStringExtra("subject_prefix")
        //points = getResources().getStringArray(getResources().getIdentifier(subject_prefix + "_points", "array", getActivity().getPackageName()));
        pointObj = FirstPoint(subject_prefix.toString())
    }

    private fun findUnAskedPermissions(wanted: ArrayList<String>): ArrayList<String>? {
        val result = ArrayList<String>()
        for (perm in wanted) {
            if (!hasPermission(perm)) {
                result.add(perm)
            }
        }
        return result
    }

    private fun hasPermission(permission: String): Boolean {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return ActivityCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return true
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    private fun canMakeSmores(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }


    internal fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>?, grantResults: IntArray?) {
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                for (perms in permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms)
                    }
                }
                if (permissionsRejected.size > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    object : DialogInterface.OnClickListener {

                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toTypedArray(), ALL_PERMISSIONS_RESULT)
                                            }
                                        }
                                    })
                            return
                        }
                    }
                }
            }
        }
    }


}
