package com.example.doctorq.admin

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.doctorq.BuildConfig
import com.example.doctorq.R
import com.example.doctorq.databinding.FragmentRegisterBinding
import com.example.doctorq.databinding.FragmentRegisterDoctorBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.*
private const val MAX_IMAGE_DIMENSION = 800
class RegisterDoctorFragment : Fragment() ,  TimePickerDialog.OnTimeSetListener{
    private lateinit var binding: FragmentRegisterDoctorBinding
    var date: String? = null
    var time: String? = null
    var startTime: String? = null
    var endtime: String? = null
    var schedule: String? = null
    private var outputFile: File? = null

    var myHour: Int = 0
    var myMinute: Int = 0
    var hour: Int = 0
    var minute: Int = 0
    var endHour: Int = 0
    var endMinute: Int = 0
    var isStartTimeClicked = false
    var isStartTimeSet = false
    private val pictureCode = 101
    lateinit var doctorImage: Uri
    var encodedstring:String ="nil"
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private val requestCameraPermissionsContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->    if (isGranted) {
        val file = createTempImageFile(requireContext())
        val uri = FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", file)
        outputFile = file
        Log.d(TAG, "Camera permission granted. Output file = ${file.absolutePath} URI = $uri")
        captureImageContract.launch(uri)
    } else {
        Log.e(TAG, "Camera permission denied.")
    }
    }
    private val requestGalleryPermissionsContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->    if (isGranted) {
        Log.d(TAG, "Gallery permission granted")
        galleryPickContract.launch("image/*")
    } else {
        Log.e(TAG, "Gallery permission denied")
    }
    }
    private val captureImageContract = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccessfullyTookPicture ->    if (isSuccessfullyTookPicture) {
        val imageFile = outputFile
        if (imageFile != null) {
            Log.d(TAG, "Successfully took picture. Saved to ${imageFile.absolutePath}")
            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    val stream = imageFile.inputStream()
                    val src = BitmapFactory.decodeStream(stream)
                    stream.close()
                    val scaledImage = createScaledBitmap(src, MAX_IMAGE_DIMENSION)
                    src.recycle()
                    val resizedFile = createTempImageFile(requireContext())
                    val outputStream = resizedFile.outputStream()
                    scaledImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.close()
                    scaledImage.recycle()
                    outputFile = imageFile
                    Glide.with(binding.image).load(outputFile).into(binding.image)
                   // updateLocalProfilePicture(imageFile)
                }
            }
        } else {
            Log.e(TAG, "Failed to take picture.")
        }
    } else {
        Log.e(TAG, "Failed to capture photo")
    }
    }
    private val galleryPickContract = registerForActivityResult(ActivityResultContracts.GetContent()) { contentUri ->    Log.d(TAG, "Content URI from gallery -> $contentUri")
        if (contentUri != null) {
            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    val file = copyContentsToFile(requireContext(), contentUri)
                    if (file != null) {
                        Log.d(TAG, "Successfully written to file ${file.absolutePath}")
                        outputFile = file
                        Glide.with(binding.image).load(file).into(binding.image)
                      //  updateLocalProfilePicture(file)
                    } else {
                        Log.e(TAG, "Failed to copy to file")
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentRegisterDoctorBinding.inflate(layoutInflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//Taking Date of birth
        binding.etDocDob.setOnClickListener() {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->

                    val selectedDate =
                        (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    date = selectedDate
                    binding.etDocDob.setTextColor(Color.BLACK)
                    binding.etDocDob.setText(selectedDate)
                },
                year, month, day
            )

            datePickerDialog.show()
        }

        //pick time slots
        binding.Time.setOnClickListener() {
            isStartTimeClicked = true
            val timePickerDialog = TimePickerDialog(
                requireContext(), this, hour, minute, false
            )
            timePickerDialog.show()
        }

        binding.endTime.setOnClickListener() {
            if (isStartTimeSet) {
                val timePickerDialog = TimePickerDialog(
                    requireContext(),this, endHour, endMinute, false
                )
                timePickerDialog.show()
            }
        }

        binding.CameraContainer.setOnClickListener(){
            imageOptionsDialog()
            //selectImage()
        }

    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        time = "${hourOfDay}:${minute}"

        Log.d("time", "onTimeSet: hour-${hourOfDay}-Minute ${minute}")

        var formattedTime: String = when {
            hourOfDay == 0 -> {
                if (minute < 10) {
                    "${hourOfDay + 12}:0${minute} am"
                } else {
                    "${hourOfDay + 12}:${minute} am"
                }
            }
            hourOfDay > 12 -> {
                if (minute < 10) {
                    "${hourOfDay - 12}:0${minute} pm"
                } else {
                    "${hourOfDay - 12}:${minute} pm"
                }
            }
            hourOfDay == 12 -> {
                if (minute < 10) {
                    "${hourOfDay}:0${minute} pm"
                } else {
                    "${hourOfDay}:${minute} pm"
                }
            }
            else -> {
                if (minute < 10) {
                    "${hourOfDay}:${minute} am"
                } else {
                    "${hourOfDay}:${minute} am"
                }
            }
        }
        time = formattedTime
        // binding.Time.setText(time)
        if (isStartTimeClicked) {
            isStartTimeSet = true
            isStartTimeClicked = false
            binding.Time.setText(time)
            binding.Time.setTextColor(Color.BLACK)
            startTime = time
            Log.d("timecheck", "on->: ${startTime},${time}")
        } else {
            endtime = formattedTime
            Log.d("timeEnd", "on->: ${endtime},${formattedTime}")
            binding.endTime.setText(endtime)

        }
    }

    //Photo Upload
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), pictureCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pictureCode && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                 var bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, filePath)
                Glide.with(binding.image).load(bitmap).into(binding.image)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storageReference?.child("doctor_Images/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)
            Log.d("url", "uploadImage:${filePath} || ${ref} ->${uploadTask} ")
            encodedstring= ref.toString()

        }else{
            Toast.makeText(requireContext(), "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    //photo via alerts
    private fun imageOptionsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Upload Photo")
       builder.setPositiveButton("Choose from Gallery",DialogInterface.OnClickListener { dialog, which ->
           val permission = if (Build.VERSION.SDK_INT >= 33) {
               Manifest.permission.READ_EXTERNAL_STORAGE
           } else {
               Manifest.permission.READ_EXTERNAL_STORAGE
           }
           requestGalleryPermissionsContract.launch(permission)
       })
        builder.setNegativeButton("choose from camera",DialogInterface.OnClickListener { dialog, which ->
            requestCameraPermissionsContract.launch(Manifest.permission.CAMERA)
        })
        builder.show()


    }
    private fun createTempImageFile(context: Context): File {
        val imagesDir = File(context.cacheDir, "ProfilePictures")
        if (!imagesDir.exists()) {
            imagesDir.mkdir()
        }
        val cal = Calendar.getInstance(Locale.US)
        val fileName = String.format(
            Locale.US,
            "IMG_%04d%02d%02d_%02d%02d%02d_%02d.jpg",
            cal[Calendar.YEAR],
            cal[Calendar.MONTH] + 1,
            cal[Calendar.DAY_OF_MONTH],
            cal[Calendar.HOUR_OF_DAY],
            cal[Calendar.MINUTE],
            cal[Calendar.SECOND],
            cal[Calendar.MILLISECOND],
        )
        val file = File(imagesDir, fileName)
        file.createNewFile()
        return file
    }

    private fun copyContentsToFile(context: Context, uri: Uri): File? {
        return when (val stream = context.contentResolver.openInputStream(uri)) {
            null -> {
                null
            }
            else -> {
                val bitmap = BitmapFactory.decodeStream(stream)
                stream.close()
                val scaledBitmap = createScaledBitmap(bitmap, MAX_IMAGE_DIMENSION)
                bitmap.recycle()
                val file = createTempImageFile(context)
                val outputStream = file.outputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
                file
            }
        }

    }

    private fun createScaledBitmap(src: Bitmap, maxSize: Int): Bitmap {
        var targetWidth = 0
        var targetHeight = 0
        if (src.width >= src.height) {
            if (src.width >= maxSize) {
                val scale = src.width.toFloat() / maxSize.toFloat()
                targetWidth = (src.width.toFloat() / scale).toInt()
                targetHeight = (src.height.toFloat() / scale).toInt()
            } else {
                targetWidth = src.width
                targetHeight = src.height
            }
        } else {
            if (src.height >= maxSize) {
                val scale = src.height.toFloat() / maxSize.toFloat()
                targetWidth = (src.width.toFloat() / scale).toInt()
                targetHeight = (src.height.toFloat() / scale).toInt()
            } else {
                targetWidth = src.width
                targetHeight = src.height
            }
        }
        return Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    }
}