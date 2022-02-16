package com.heena.supplier.`interface`


import com.heena.supplier.models.CategoryItem
import com.heena.supplier.models.Content
import java.util.*

interface ClickInterface {
    interface OnRecyclerItemClick{
        fun OnClickAction(position: Int)
    }

    interface OnGalleryItemClick{
        fun OnClickAction(position: Int)
        fun onShowImage(position: Int)
    }

    interface OnCategoryItemClick{
        fun OnClickAction(position: Int, category : CategoryItem)
    }

    interface OnAddressItemClick{
        fun editAdddress(position: Int)
        fun deleteAddress(position: Int)
    }

    interface onServicesItemClick{
        fun onServicClick(position: Int)
        fun onServiceDele(position: Int)
        fun onServiceEdit(position: Int)
        fun onServiceView(position: Int)
    }

    interface onOffersItemClick{
        fun onOfferDelete(position: Int)
        fun onOfferEdit(position: Int)
        fun onOfferView(position: Int)
    }


    interface OnBookServiceClick{
        fun OnBookService()
    }


    interface OnServiceClick{
        fun OnAddService(position: Int, service: String)
    }

    interface OnCancelServiceClick{
        fun OnCancelService(rsn_for_cancellation: String?)
    }

    interface mainhelpCategoryClicked{
        fun mainHelpCategory(position: Int)
    }

    interface subhelpCategoryClicked{
        fun subHelpCategory(position: Int, content: ArrayList<Content>)
    }
}