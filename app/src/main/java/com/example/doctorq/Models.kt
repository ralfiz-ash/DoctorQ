package com.example.doctorq

data class DoctorModel(
    var id: String? = null,
    var name: String? = null,
    var age: String? = null,
    var dob: String? = null,
    var department: String? = null,
    var specializatin: String? = null,
    var experience: String? = null,
    var Time: String? = null,
    var mobile: String? = null,
    var available: String? = null,
    var username: String? = null,
    var password: String? = null,
    var image: String? = null
)

data class PatientModel(
    var Id: String? = null,
    var name: String? = null,
    var age: String? = null,
    var address: String? = null,
    var mobile: String? = null,
    var username: String? = null,
    var password: String? = null,
    var image: String? = null,
    var adminFlag: Boolean? = null
)

data class FeedbackModel(
    var complaintId: String? = null,
    var userName: String? = null,
    var complaint: String? = null,
    var ratingValue: String? = null
)

data class Bookings(
    var bookId:String?=null,
    var drName:String?=null,
    var patientName:String?=null,
    var date:String?=null,
    var time:String?=null,
    var type:String?=null,
    var token:String?=null,
    var drImage:String?=null,


)