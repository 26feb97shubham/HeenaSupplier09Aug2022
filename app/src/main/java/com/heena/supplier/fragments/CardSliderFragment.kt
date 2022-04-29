package com.heena.supplier.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.Cards
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.fragment_card_slider.view.*
import java.util.*

class CardSliderFragment(private var cards : Cards) : Fragment() {
    var interval = 4
    var separator = ' '

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_card_slider, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )

        if (sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
                .equals("en")
        ){
            mView!!.ivCardImage.setImageResource(R.drawable.card_bg)
        } else{
            mView!!.ivCardImage.setImageResource(R.drawable.arabic_card)
        }

        addDetails(cards)

        return mView
    }

    private fun addDetails(cards: Cards) {
        mView!!.tv_expiry_date_card.text = cards.expiry_date
        val card_number = cards.first_six+"******"+cards.number
        val sb: StringBuilder = StringBuilder(card_number)

        for (i in 0 until card_number.length / interval) {
            sb.insert((i + 1) * interval + i, separator)
        }

        val withDashes = sb.toString()
        Log.e("cardNumber", withDashes)
        mView!!.tv_card_number_card.text = withDashes
        mView!!.tv_account_holdee_name_card.text = cards.name
    }

    companion object {
        @JvmStatic
        fun newInstance(cards: Cards) =
            CardSliderFragment(cards).apply {
                this.cards = cards
            }

        @SuppressLint("StaticFieldLeak")
        private var mView : View?=null

    }
}