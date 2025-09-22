package com.example.ferfume


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class GeneralAccountFaqActivity : AppCompatActivity() {

    data class FaqItem(val question: String, val answer: String)

    private val faqList = listOf(
        FaqItem("What is Ferfume?", "Ferfume is a high-quality fragrance app that lets you explore and purchase premium perfumes easily."),
        FaqItem("How do I create an account?", "You can create an account by clicking the Sign Up button on the main screen and following the instructions."),
        FaqItem("How do I reset my password?", "Tap on 'Forgot Password' on the login screen and enter your registered email to receive reset instructions."),
        FaqItem("Can I change my email address?", "Yes, you can change your email address from the account settings after logging in."),
        FaqItem("Is my personal information secure?", "Absolutely. We use industry-standard encryption to protect your data and never share it without your consent.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_account_faq)

        val faqContainer = findViewById<LinearLayout>(R.id.faq_container)

        val inflater = LayoutInflater.from(this)

        faqList.forEach { faqItem ->
            val card = inflater.inflate(R.layout.item_faq_card, faqContainer, false) as MaterialCardView

            val questionText = card.findViewById<TextView>(R.id.tv_question)
            val answerText = card.findViewById<TextView>(R.id.tv_answer)

            questionText.text = faqItem.question
            answerText.text = faqItem.answer

            answerText.visibility = View.GONE

            questionText.setOnClickListener {
                answerText.visibility = if (answerText.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            faqContainer.addView(card)
        }
    }
}
