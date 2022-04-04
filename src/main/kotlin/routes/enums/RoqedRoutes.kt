package routes.enums

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.JsonClass

enum class RoqedRoutes: Routes {
    ROQED_COURSES,
    ROQED_SEARCH_NAME,
    FORWARD_ROQED_COURSES,
    FORWARD_ROQED_INPUT,
    BACKWARDS_ROQED_COURSES,
    BACKWARDS_ROQED_INPUT,
    CHOOSE_ROQED_COURSE_ID,
    CHOSEN_ROQED_COURSE_ID,
    START_ROQED_COURSE,
    ROQED_SEARCH_NAME_CANCEL,
    BEFORE_CHOOSEN_ROQED_COURSE_ID,
}