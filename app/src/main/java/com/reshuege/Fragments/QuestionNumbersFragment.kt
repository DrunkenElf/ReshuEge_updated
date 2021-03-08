package com.reshuege.Fragments

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.reshuege.DataBase.DataPreferences

import com.reshuege.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionNumbersFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    internal var lastClicked = 0
    internal var lastClickedColor = 0
    internal var ClickedColor = 0
    internal lateinit var containerLayout: LinearLayout
    internal var questionCount = 1
    internal var color_answer: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_question_numbers, null)

        val questionNumbersLayout = v.findViewById<View>(R.id.questionNumberLayout) as LinearLayout

        containerLayout = v.findViewById<View>(R.id.questionNumbers) as LinearLayout
        val numberButton = v.findViewById<View>(R.id.numberButton) as Button
        val colorText = v.findViewById<View>(R.id.colorText) as TextView

        val subject_prefix = requireActivity().intent.getStringExtra("subject_prefix")
        val section = requireActivity().intent.getStringExtra("section")


        val countPref = DataPreferences(requireContext())

        val settingsPref = PreferenceManager.getDefaultSharedPreferences(activity)
        CoroutineScope(Dispatchers.IO).launch {
            if (section != null) {
                if (section.contentEquals("Варианты") ?: section.contentEquals("Режим экзамена")) {
                    questionCount = countPref.questionsCount(subject_prefix.toString())
                    if (questionCount == -1) questionCount = 0
                } else if (section?.contentEquals("Каталог заданий")) {
                    val themeNumber = requireActivity().intent.getIntExtra("theme_number", 1)
                    questionCount = countPref.questionsThemeCount(
                        subject_prefix.toString(),
                        themeNumber.toString()
                    )
                } else if (section.contentEquals("Поиск")) {
                    questionCount = requireActivity().intent.getIntExtra("count", 0)
                }

                if (section.contentEquals("Варианты") || section.contentEquals("Каталог заданий") || section.contentEquals(
                        "Поиск"
                    )
                ) {
                    color_answer = settingsPref.getBoolean("color_answer", false)
                } else {
                    color_answer = false
                }
            }
        }

        for (i in 1..questionCount) {
            val questNumber = Button(activity)
            val buttonColor = TextView(activity)
            questNumber.setBackgroundResource(R.drawable.button_back)
            questNumber.layoutParams = numberButton.layoutParams
            questNumber.text = Integer.toString(i)
            //questNumber.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            questNumber.id = Integer.valueOf(i)

            buttonColor.text = "none"
            buttonColor.setTextColor(resources.getColor(R.color.black))
            buttonColor.id = Integer.valueOf(i + 1000)


            questNumber.setOnClickListener { v ->
                sendQuestionNumber(v.id)
                if (lastClicked != 0 && lastClickedColor != 0) {
                    val color = requireActivity().findViewById<View>(lastClickedColor) as TextView
                    if (color.text.toString().contentEquals("none"))
                        requireActivity().findViewById<View>(lastClicked)
                            .setBackgroundResource(R.drawable.button_back)
                }
                if (ClickedColor != v.id + 1000)
                //v.setBackgroundResource(R.drawable.question_this);
                    v.setBackgroundResource(R.color.colorAccent)
                lastClicked = v.id
                lastClickedColor = lastClicked + 1000
                Log.d("myLogs", "Последняя нажатая: $lastClicked")

                val scroll =
                    requireActivity().findViewById<View>(R.id.buttonScroll) as HorizontalScrollView
                val displaymetrics = resources.displayMetrics
                scroll.scrollTo(v.left + v.width / 2 - displaymetrics.widthPixels / 2, v.top)
                Log.d("myLogs", Integer.toString(v.left + v.width))
            }

            val displaymetrics = resources.displayMetrics

            if (questNumber.width * questionCount < displaymetrics.widthPixels)
                questNumber.width = displaymetrics.widthPixels / questionCount

            questNumber.visibility = View.VISIBLE
            buttonColor.visibility = View.GONE
            containerLayout.addView(questNumber)
            containerLayout.addView(buttonColor)
        }

        return questionNumbersLayout
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mListener = activity as OnFragmentInteractionListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity должен реализовывать интерфейс OnFragmentInteractionListener")
        }

    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(link: Int)
    }

    fun sendQuestionNumber(questionNumber: Int) {
        mListener!!.onFragmentInteraction(questionNumber)
    }

    fun doClick(position: Int) {
        val button = containerLayout.findViewById<View>(position) as Button
        button.performClick()

    }

    fun setClickedWithColor(questionNumber: Int) {
        ClickedColor = questionNumber + 1001
    }

    fun setColorAnswer(color_answer: Boolean) {
        this.color_answer = color_answer
    }

    fun setButtonColor(answer: Boolean) {
        if (color_answer)
            if (answer)
                requireActivity().findViewById<View>(lastClicked)
                    .setBackgroundResource(R.drawable.button_right)
            else
                requireActivity().findViewById<View>(lastClicked)
                    .setBackgroundResource(R.drawable.button_wrong)
        else
            requireActivity().findViewById<View>(lastClicked)
                .setBackgroundResource(R.drawable.button_back)
        lastClickedColor = lastClicked + 1000
        val color = requireActivity().findViewById<View>(lastClickedColor) as TextView
        color.text = "color"
    }


}
