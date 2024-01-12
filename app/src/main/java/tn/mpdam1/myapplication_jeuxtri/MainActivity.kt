package tn.mpdam1.myapplication_jeuxtri

import android.content.ClipData
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView

import kotlin.random.Random





class MainActivity : AppCompatActivity() {

    private val numbers = mutableListOf<Int>()
    private var startTime: Long = 0
    private var lastClickedNumber: TextView? = null
    private var initialPosition: Int = 0
    private var initialText: String = ""
    private var isValiderButtonEnabled: Boolean = false
    private val draggedNumbers = mutableListOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Generate 5 random numbers
        for (i in 1..5) {
            numbers.add(Random.nextInt(1, 100))
        }

        // Display welcome message and random numbers
        val welcomeText: TextView = findViewById(R.id.welcomeText)
        val number1: TextView = findViewById(R.id.number1)
        val number2: TextView = findViewById(R.id.number2)
        val number3: TextView = findViewById(R.id.number3)
        val number4: TextView = findViewById(R.id.number4)
        val number5: TextView = findViewById(R.id.number5)
        val placeholder1: TextView = findViewById(R.id.placeholder1)
        val placeholder2: TextView = findViewById(R.id.placeholder2)
        val placeholder3: TextView = findViewById(R.id.placeholder3)
        val placeholder4: TextView = findViewById(R.id.placeholder4)
        val placeholder5: TextView = findViewById(R.id.placeholder5)
        val validerButton: Button = findViewById(R.id.validerButton)

        welcomeText.text = "Welcome to the App!"
        number1.text = numbers[0].toString()
        number2.text = numbers[1].toString()
        number3.text = numbers[2].toString()
        number4.text = numbers[3].toString()
        number5.text = numbers[4].toString()

        // Set initial text for placeholders
        placeholder1.text = ""
        placeholder2.text = ""
        placeholder3.text = ""
        placeholder4.text = ""
        placeholder5.text = ""

        // Start the timer
        startTimer()

        // Set up double click listeners for numbers
        setDoubleClickListenerForNumber(number1)
        setDoubleClickListenerForNumber(number2)
        setDoubleClickListenerForNumber(number3)
        setDoubleClickListenerForNumber(number4)
        setDoubleClickListenerForNumber(number5)

        // Set up click listeners for placeholders
        setClickListenerForPlaceholder(placeholder1, validerButton)
        setClickListenerForPlaceholder(placeholder2, validerButton)
        setClickListenerForPlaceholder(placeholder3, validerButton)
        setClickListenerForPlaceholder(placeholder4, validerButton)
        setClickListenerForPlaceholder(placeholder5, validerButton)



        // Set click listener for the Valider button
        validerButton.setOnClickListener {
            if (isValiderButtonEnabled) {
                if (checkOrder()) {
                    // If the order is correct, launch GoodJobActivity
                    val intent = Intent(this@MainActivity, GoodJobActivity::class.java)
                    startActivity(intent)
                } else {
                    // If the order is incorrect, launch GameOverActivity
                    val intent = Intent(this@MainActivity, GameOverActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }


    private fun setDoubleClickListenerForNumber(number: TextView) {
        number.setOnClickListener {
            if (lastClickedNumber == number && SystemClock.elapsedRealtime() - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                // Double-click action
                initialPosition = numbers.indexOf(number.text.toString().toInt())
                initialText = number.text.toString()

                number.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val dragData = ClipData.newPlainText("", "")
                            val shadowBuilder = View.DragShadowBuilder(number)

                            // Set a tag to identify the dragged view
                            number.tag = "NUMBER"

                            number.startDrag(dragData, shadowBuilder, number, 0)
                            number.visibility = View.INVISIBLE

                            true
                        }
                        else -> false
                    }
                }
            } else {
                // Single-click action
                lastClickTime = SystemClock.elapsedRealtime()
                lastClickedNumber = number
            }
        }
    }

    private fun setClickListenerForPlaceholder(placeholder: TextView, validerButton: Button) {
        var lastClickTimePlaceholder: Long = 0
        var lastClickedNumberForPlaceholder: TextView? = null

        placeholder.setOnClickListener {
            // Click action
            lastClickedNumberForPlaceholder?.let { number ->
                if (placeholder.text.isNotEmpty()) {
                    // Return the text to its initial position
                    val initialNumber = numbers[initialPosition].toString()
                    placeholder.text = initialNumber

                    // Clear the text of the original view (number)
                    number.text = ""

                    // Make the original view visible again
                    number.visibility = View.VISIBLE
                    draggedNumbers.remove(number.text.toString().toInt())
                    lastClickedNumberForPlaceholder = null
                } else {
                    // Set the text of the placeholder to the dropped text
                    placeholder.text = number.text

                    // Clear the text of the dropped view (number)
                    number.text = ""

                    // Make the dropped view visible again
                    number.visibility = View.VISIBLE
                    draggedNumbers.add(placeholder.text.toString().toInt())
                    lastClickedNumberForPlaceholder = null

                    // Check if all placeholders have a number
                    isValiderButtonEnabled = areAllPlaceholdersFilled()
                    validerButton.isEnabled = isValiderButtonEnabled
                }
            }
        }

        // Double click on a placeholder to return the text to its initial position in the number
        placeholder.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.eventTime - event.downTime < DOUBLE_CLICK_TIME_DELTA) {
                        // Double-click action
                        if (lastClickedNumberForPlaceholder == null || lastClickedNumberForPlaceholder == lastClickedNumber) {
                            lastClickedNumberForPlaceholder = lastClickedNumber
                            lastClickTimePlaceholder = SystemClock.elapsedRealtime()
                        }
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (event.eventTime - event.downTime < DOUBLE_CLICK_TIME_DELTA) {
                        // Double-click action
                        if (lastClickedNumberForPlaceholder == lastClickedNumber && SystemClock.elapsedRealtime() - lastClickTimePlaceholder < DOUBLE_CLICK_TIME_DELTA) {
                            lastClickedNumberForPlaceholder?.let { number ->
                                // Return the text to its initial position in the number
                                val initialNumber = numbers[initialPosition].toString()
                                placeholder.text = initialNumber

                                // Clear the text of the original view (number)
                                number.text = ""

                                // Make the original view visible again
                                number.visibility = View.VISIBLE

                                lastClickedNumberForPlaceholder = null
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }
    private fun areAllPlaceholdersFilled(): Boolean {
        val placeholder1: TextView = findViewById(R.id.placeholder1)
        val placeholder2: TextView = findViewById(R.id.placeholder2)
        val placeholder3: TextView = findViewById(R.id.placeholder3)
        val placeholder4: TextView = findViewById(R.id.placeholder4)
        val placeholder5: TextView = findViewById(R.id.placeholder5)

        return (
                placeholder1.text.isNotEmpty() &&
                        placeholder2.text.isNotEmpty() &&
                        placeholder3.text.isNotEmpty() &&
                        placeholder4.text.isNotEmpty() &&
                        placeholder5.text.isNotEmpty()
                )
    }

    private fun checkOrder(): Boolean {
        val sortedNumbers = draggedNumbers.sorted()

        // Check if the sorted numbers match the expected order
        return (
                sortedNumbers[0] == 1 &&
                        sortedNumbers[1] == 2 &&
                        sortedNumbers[2] == 3 &&
                        sortedNumbers[3] == 4 &&
                        sortedNumbers[4] == 5
                )
    }

    private fun startTimer() {
        // Start the chronometer
        val chronometer: Chronometer = findViewById(R.id.chronometer)
        startTime = SystemClock.elapsedRealtime()
        chronometer.base = startTime
        chronometer.start()
    }
    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 // milliseconds
        private var lastClickTime: Long = 0
    }
}