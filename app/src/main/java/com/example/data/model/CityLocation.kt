package com.example.data.model

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val isGps: Boolean = false
) {
    val formattedCoordinates: String
        get() {
            val latDir = if (latitude >= 0) "N" else "S"
            val lonDir = if (longitude >= 0) "E" else "W"
            return String.format("%.2f°%s, %.2f°%s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lonDir)
        }

    companion object {
        val DEFAULT_LOCATION = CityLocation(
            name = "Islamabad",
            country = "Pakistan",
            latitude = 33.6844,
            longitude = 73.0479
        )

        val PRESET_CITIES = listOf(
            // Pakistan
            CityLocation("Islamabad", "Pakistan", 33.6844, 73.0479),
            CityLocation("Lahore", "Pakistan", 31.5204, 74.3587),
            CityLocation("Karachi", "Pakistan", 24.8607, 67.0011),
            CityLocation("Rawalpindi", "Pakistan", 33.5651, 73.0169),
            CityLocation("Faisalabad", "Pakistan", 31.4504, 73.1350),
            CityLocation("Multan", "Pakistan", 30.1575, 71.5249),
            CityLocation("Peshawar", "Pakistan", 34.0151, 71.5249),
            CityLocation("Quetta", "Pakistan", 30.1798, 66.9750),
            CityLocation("Sialkot", "Pakistan", 32.4945, 74.5229),
            CityLocation("Gujranwala", "Pakistan", 32.1877, 74.1945),
            CityLocation("Hyderabad", "Pakistan", 25.3960, 68.3578),
            CityLocation("Gilgit", "Pakistan", 35.9221, 74.3087),
            CityLocation("Skardu", "Pakistan", 35.2971, 75.6333),
            CityLocation("Muzaffarabad", "Pakistan", 34.3700, 73.4711),
            // Middle East & International
            CityLocation("Dubai", "UAE", 25.2048, 55.2708),
            CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753),
            CityLocation("Jeddah", "Saudi Arabia", 21.4858, 39.1925),
            CityLocation("Doha", "Qatar", 25.2854, 51.5310),
            CityLocation("Kabul", "Afghanistan", 34.5553, 69.2075),
            CityLocation("Delhi", "India", 28.6139, 77.2090),
            CityLocation("Mumbai", "India", 19.0760, 72.8777),
            CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125),
            CityLocation("Istanbul", "Turkey", 41.0082, 28.9784),
            CityLocation("London", "UK", 51.5074, -0.1278),
            CityLocation("Frankfurt", "Germany", 50.1109, 8.6821),
            CityLocation("New York", "USA", 40.7128, -74.0060),
            CityLocation("Toronto", "Canada", 43.6532, -79.3832)
        )
    }
}
