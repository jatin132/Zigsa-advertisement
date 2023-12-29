package com.zigsawaitlist.network


import com.google.gson.annotations.SerializedName

import kotlin.collections.ArrayList


data class TVCodeRequest(
    @SerializedName("endpoint_id")
    var endpoint_id: String,
)

data class TVCodeResponse(
    @SerializedName("code")
    var code: String,
)

data class User(

    @SerializedName("email")
    var email: String,

    @SerializedName("id")
    var id: Int,

    @SerializedName("default_location_id")
    var default_location_id: Int,

    @SerializedName("display_id")
    var display_id: String,

    @SerializedName("full_name")
    var full_name: String
)

data class Business(
    val id: Int,
    val user_id: Int,
    val name: String,
    val show_customer_synonym: Boolean,
    val logo: String,
    val website: String,
    val bg_color: String,
    val text_color: String,

    )

data class Location(
    @SerializedName("device_id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("is_active_waitlist")
    var is_active_waitlist: Boolean,

    @SerializedName("self_check_in")
    var self_check_in: Boolean,

    )

data class CustomerList(
    @SerializedName("link")
    var link: String,

    @SerializedName("uuid")
    var uuid: String,

    @SerializedName("customers")
    var customers: ArrayList<Customer>? = null
)

data class Customer(
    @SerializedName("id")
    var id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("last_updated_at")
    var last_updated_at: String,

    @SerializedName("rank")
    var rank: String
)

data class CustomersList(
    @SerializedName("uuid")
    var uuid: String,
)

data class PusherCustomerDetail(
    @SerializedName("id")
    var id: Int,

    @SerializedName("uuid")
    var uuid: String,

    @SerializedName("name")
    var name: String,

    @SerializedName("display_id")
    var display_id: String? = null,

    @SerializedName("business_name")
    var business_name: String,

    @SerializedName("bg_color")
    var bg_color: String,

    @SerializedName("text_color")
    var text_color: String,

    @SerializedName("status")
    var status: String,
)

