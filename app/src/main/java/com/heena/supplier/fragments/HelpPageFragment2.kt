package com.heena.supplier.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.adapters.ContentSolutionsAdapter
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.content
import com.heena.supplier.utils.Utility.helpCategory
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_help_page2.view.*

class HelpPageFragment2 : Fragment() {
    private var mView : View?=null
    var room: String = ""
    var admin_id = 0
    private var contentSolutionsAdapter : ContentSolutionsAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            admin_id = it.getInt("admin_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_help_page2, container, false)
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )


        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.faq_card_2.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putString("title", requireContext().getString(R.string.frequently_asked_questions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        mView!!.admin_chat_card_2.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("helpCategory", helpCategory)
            bundle.putSerializable("subhelpCategory", Utility.helpSubCategory)
            val user_id = sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]
            room = if (user_id > admin_id) {
                StringBuilder().append(admin_id).append('-')
                    .append(user_id).toString()
            } else {
                StringBuilder().append(user_id).append('-')
                    .append(admin_id).toString()
            }
            bundle.putString("room", room)
            bundle.putString("type", "direct")
            bundle.putString("admin_id", ""+admin_id)
            findNavController().navigate(R.id.chatWithAdminFragment, bundle)
        }

        mView!!.solutions_content_rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        contentSolutionsAdapter = content?.let { ContentSolutionsAdapter(requireContext(), it) }
        mView!!.solutions_content_rv.adapter = contentSolutionsAdapter

        if(content?.size==0){
            mView!!.solutions_content_rv.visibility = View.GONE
            mView!!.text_no_solutions_found.visibility = View.VISIBLE
        }else{
            mView!!.solutions_content_rv.visibility = View.VISIBLE
            mView!!.text_no_solutions_found.visibility = View.GONE
        }


        return mView
    }
}