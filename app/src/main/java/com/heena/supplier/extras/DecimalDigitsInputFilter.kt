package com.heena.supplier.extras

import android.text.Html

import android.text.Spanned

import android.text.InputFilter
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern


class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) :
    InputFilter {
    var mPattern: Pattern
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): String? {
        val s = Html.toHtml(dest).replace("\\<.*?>".toRegex(), "").replace("\n".toRegex(), "")
        val matcher: Matcher = mPattern.matcher(dest)
        if (!matcher.matches()) return ""
        try {
            return if (s.toDouble() < 9999.99 && s.contains(".")) {
                null
            } else if (s.toDouble() < 1000 && !s.contains(".") || source == ".") {
                null
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    init {
        mPattern =
            Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?")
    }
}