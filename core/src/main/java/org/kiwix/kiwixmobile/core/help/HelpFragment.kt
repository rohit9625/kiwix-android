/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.kiwix.kiwixmobile.core.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.base.BaseActivity
import org.kiwix.kiwixmobile.core.base.BaseFragment
import org.kiwix.kiwixmobile.core.compat.CompatHelper.Companion.queryIntentActivitiesCompat
import org.kiwix.kiwixmobile.core.compat.ResolveInfoFlagsCompat
import org.kiwix.kiwixmobile.core.databinding.FragmentHelpBinding
import org.kiwix.kiwixmobile.core.error.DiagnosticReportActivity
import org.kiwix.kiwixmobile.core.extensions.ActivityExtensions.start
import org.kiwix.kiwixmobile.core.extensions.toast
import org.kiwix.kiwixmobile.core.main.CoreMainActivity
import org.kiwix.kiwixmobile.core.utils.CONTACT_EMAIL_ADDRESS
import org.kiwix.kiwixmobile.core.utils.LanguageUtils.Companion.getCurrentLocale
import org.kiwix.kiwixmobile.core.utils.SharedPreferenceUtil
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class HelpFragment : BaseFragment() {
  @Inject
  lateinit var sharedPreferenceUtil: SharedPreferenceUtil
  private var fragmentHelpBinding: FragmentHelpBinding? = null
  protected open fun rawTitleDescriptionMap(): List<Pair<Int, Int>> = emptyList()
  override val fragmentToolbar: Toolbar? by lazy {
    fragmentHelpBinding?.root?.findViewById(R.id.toolbar)
  }
  override val fragmentTitle: String? by lazy { getString(R.string.menu_help) }

  private val titleDescriptionMap by lazy {
    rawTitleDescriptionMap().associate { (title, description) ->
      getString(title) to resources.getStringArray(description)
        .joinToString(separator = "\n")
    }
  }

  override fun inject(baseActivity: BaseActivity) {
    (baseActivity as CoreMainActivity).cachedComponent.inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val activity = requireActivity() as AppCompatActivity
    fragmentHelpBinding?.activityHelpFeedbackTextView?.setOnClickListener { sendFeedback() }
    fragmentHelpBinding?.activityHelpFeedbackImageView?.setOnClickListener { sendFeedback() }
    fragmentHelpBinding?.diagnosticClickableArea?.setOnClickListener { sendDiagnosticReport() }
    fragmentHelpBinding?.activityHelpRecyclerView?.addItemDecoration(
      DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
    )
    fragmentHelpBinding?.activityHelpRecyclerView?.adapter = HelpAdapter(titleDescriptionMap)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    fragmentHelpBinding =
      FragmentHelpBinding.inflate(inflater, container, false)
    return fragmentHelpBinding?.root
  }

  private fun sendDiagnosticReport() {
    requireActivity().start<DiagnosticReportActivity>()
  }

  private fun sendFeedback() {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
      data = (
        "mailto:${Uri.encode(CONTACT_EMAIL_ADDRESS)}" +
          "?subject=" +
          Uri.encode("Feedback in ${getCurrentLocale(requireActivity()).displayLanguage}")
        ).toUri()
    }
    val packageManager = requireActivity().packageManager
    val activities =
      packageManager.queryIntentActivitiesCompat(emailIntent, ResolveInfoFlagsCompat.EMPTY)
    if (activities.isNotEmpty()) {
      startActivity(Intent.createChooser(emailIntent, "Send Feedback via Email"))
    } else {
      activity.toast(getString(R.string.no_email_application_installed, CONTACT_EMAIL_ADDRESS))
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    fragmentHelpBinding = null
  }
}
