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
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.content
import com.heena.supplier.utils.Utility.helpCategory
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_help_page2.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HelpPageFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class HelpPageFragment2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.faq_card_2.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putString("title", requireContext().getString(R.string.frequently_asked_questions))
            findNavController().navigate(R.id.CMSFragment, bundle)
        }

        mView!!.admin_chat_card_2.setSafeOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("helpCategory", helpCategory)
            bundle.putSerializable("subhelpCategory", Utility.helpSubCategory)
            val user_id = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]
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
        contentSolutionsAdapter!!.notifyDataSetChanged()

        if(content?.size==0){
            mView!!.solutions_content_rv.visibility = View.GONE
            mView!!.text_no_solutions_found.visibility = View.VISIBLE
        }else{
            mView!!.solutions_content_rv.visibility = View.VISIBLE
            mView!!.text_no_solutions_found.visibility = View.GONE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HelpPageFragment2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HelpPageFragment2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}