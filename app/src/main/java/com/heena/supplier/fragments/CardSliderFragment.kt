package com.heena.supplier.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heena.supplier.R
import com.heena.supplier.models.Cards
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import kotlinx.android.synthetic.main.fragment_card_slider.view.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CardSliderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CardSliderFragment(private var cards : Cards) : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var interval = 4
    var separator = ' '

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_card_slider, container, false)
        Utility.changeLanguage(
            requireContext(),
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )

        if (SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
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
        fun newInstance(param1: String, param2: String, cards: Cards) =
            CardSliderFragment(cards).apply {
                this.cards = cards
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        @SuppressLint("StaticFieldLeak")
        private var mView : View?=null

    }
}