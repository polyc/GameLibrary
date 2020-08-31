package com.example.gamelibrary.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.example.gamelibrary.R

class AboutActivity : MaterialAboutActivity() {
    override fun getMaterialAboutList(context: Context): MaterialAboutList {

        val infoCardBuilder = MaterialAboutCard.Builder()
        infoCardBuilder.title("Info")
        infoCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Source Code")
            .icon(R.drawable.ic_round_code_24)
            .setOnClickAction {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/polyc/GameLibrary"))
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
            .build())
        infoCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Version")
            .subText("1.0.0").icon(R.drawable.ic_baseline_update_24)
            .build())

        val authorCardBuilder = MaterialAboutCard.Builder()
        authorCardBuilder.title("Author")
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Gabriele Cervelli")
            .setOnClickAction {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/polyc"))
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
            .icon(R.drawable.ic_outline_person_24)
            .build())
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Email")
            .icon(R.drawable.ic_outline_email_24)
            .subText("ichigo69600@gmail.com")
            .setOnClickAction {
                composeEmail(arrayOf("ichigo69600@gmail.com"), "")
            }
            .build())


        return MaterialAboutList.Builder()
            .addCard(infoCardBuilder.build())
            .addCard(authorCardBuilder.build())
            .build()
    }

    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.app_name)
    }

    fun composeEmail(addresses: Array<String>, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }


}