package com.georgv.sporttrackerapp.customHandlers

class CalorieCounter {
    fun countCalories(
        totalDistance: Float,
        userWeight: Double
    ): Double {
        val force = userWeight * 9.8
        val energy = force * totalDistance
        // If an 80 kg person walks 1 km,
        // according to the internet the calories burnt is about 76 Kcal.
        // I adjusted it to a fixed 83 Kcal (In case user is jogging, but checking that is not implemented)
        // It's not perfect, but it's something.
        return String.format(null,"%.3f", energy * 0.24 / 1000 / 2.25).toDouble()
    }
}