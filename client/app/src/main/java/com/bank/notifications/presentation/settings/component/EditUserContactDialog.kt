package com.bank.notifications.presentation.settings.component

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bank.notifications.R
import com.bank.notifications.common.ext.ime
import com.bank.notifications.databinding.DialogEditUserContactBinding
import com.bank.notifications.di.DI

class EditUserContactDialog : DialogFragment() {
    private var _binding: DialogEditUserContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(
            this,
            EditUserViewModel.Factory(DI.validateUserData)
        )[EditUserViewModel::class.java]
    }
    
    private var listener: EditListener = EditListener.NOOP

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = DialogEditUserContactBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val errorString: String
            val editType = enumValueOf<EditType>(it.getString(EXTRA_TYPE)!!)
            when (editType) {
                EditType.PHONE -> {
                    binding.userContactInputLayout.helperText =
                        getString(R.string.hint_user_phone_number)
                    binding.userContactInput.inputType = InputType.TYPE_CLASS_PHONE
                    errorString = getString(R.string.invalid_phone_number)
                }

                EditType.EMAIL -> {
                    binding.userContactInputLayout.helperText = getString(R.string.hint_user_email)
                    binding.userContactInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    errorString = getString(R.string.invalid_e_mail)
                }
            }

            with(binding.userContactInput) {
                setText(it.getString(EXTRA_VALUE))
                doOnTextChanged { text, _, _, _ ->
                    if (!viewModel.validate(editType, text.toString())) {
                        binding.userContactInputLayout.error = errorString
                        binding.buttonSave.isEnabled = false
                    } else {
                        binding.userContactInputLayout.error = null
                        binding.buttonSave.isEnabled = true
                    }
                }
                ime(show = true)
            }

            binding.buttonSave.setOnClickListener {
                listener.onUserContactEdited(editType, binding.userContactInput.text.toString())
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (parentFragment != null) {
            if (parentFragment is EditListener) {
                parentFragment as EditListener
            } else {
                throw RuntimeException("$parentFragment must implement EditUserContactDialog.EditListener")
            }
        } else {
            if (context is EditListener) {
                context
            } else {
                throw RuntimeException("$context must implement EditUserContactDialog.EditListener")
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        listener = EditListener.NOOP
        super.onDetach()
    }

    interface EditListener {
        fun onUserContactEdited(editType: EditType, value: String)

        object NOOP : EditListener {
            override fun onUserContactEdited(editType: EditType, value: String) = Unit
        }
    }

    companion object {
        const val TAG = "edit_user_contact_dialog"
        private const val EXTRA_TYPE = "extra_type"
        private const val EXTRA_VALUE = "extra_value"

        fun create(type: EditType, value: String): EditUserContactDialog {
            return EditUserContactDialog().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TYPE, type.name)
                    putString(EXTRA_VALUE, value)
                }
            }
        }
    }
}