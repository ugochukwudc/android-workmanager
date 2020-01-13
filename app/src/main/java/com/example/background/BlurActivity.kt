/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_blur.*


class BlurActivity : AppCompatActivity() {

    private lateinit var viewModel: BlurViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blur)

        // Get the ViewModel
        viewModel = ViewModelProviders.of(this).get(BlurViewModel::class.java)

        // Image uri should be stored in the ViewModel; put it there then display
        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(image_view)
        }
        viewModel.outputWorkInfos.observe(this, Observer { workInfos ->
            workInfos.firstOrNull()?.let { saveWorkInfo ->
                if (saveWorkInfo.state.isFinished) {
                    showWorkFinished()
                    viewModel.setOutputUri(saveWorkInfo.outputData.getString(KEY_IMAGE_URI))
                    viewModel.outputUri?.let {
                        see_file_button.visibility = View.VISIBLE
                    }
                } else {
                    showWorkInProgress()
                }
            }

        })
        cancel_button.setOnClickListener{ viewModel.cancelWork() }
        go_button.setOnClickListener { viewModel.applyBlur(blurLevel) }
        see_file_button.setOnClickListener {
            viewModel.outputUri?.let { outputImageUri ->
                val viewImageIntent = Intent(Intent.ACTION_VIEW, outputImageUri)
                viewImageIntent.resolveActivity(packageManager)?.run {
                    startActivity(viewImageIntent)
                }
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        progress_bar.visibility = View.VISIBLE
        cancel_button.visibility = View.VISIBLE
        go_button.visibility = View.GONE
        see_file_button.visibility = View.GONE
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        progress_bar.visibility = View.GONE
        cancel_button.visibility = View.GONE
        go_button.visibility = View.VISIBLE
    }

    private val blurLevel: Int
        get() = when (radio_blur_group.checkedRadioButtonId) {
            R.id.radio_blur_lv_1 -> 1
            R.id.radio_blur_lv_2 -> 2
            R.id.radio_blur_lv_3 -> 3
            else -> 1
        }
}