package variables.products

class MechanicumAWS {
    companion object {
        const val MECHANICUM_BUCKET = "mechanicum"
        const val MECHANICUM_REGION = "eu-west-1"

        /**
         * File containing all courses
         */
        const val MECHANICUM_COURSES_FILE_FULL_PATH = "MechanicumBooks/Courses.json"

        /**
         * Directory with course jsons (id.json)
         */
        const val MECHANICUM_COURSE_DIR = "MechanicumBooks/Courses/"

        /**
         * .json
         */
        const val JSON_EXTENSION = ".json"
    }
}