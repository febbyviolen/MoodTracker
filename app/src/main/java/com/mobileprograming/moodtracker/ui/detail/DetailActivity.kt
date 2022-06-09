package com.mobileprograming.moodtracker.ui.detail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mobileprograming.moodtracker.R
import com.mobileprograming.moodtracker.data.MyDBHelper
import com.mobileprograming.moodtracker.databinding.ActivityDetailBinding
import com.mobileprograming.moodtracker.util.IntentKey
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init()
    {
        val intent=intent
        val date=intent.getLongExtra(IntentKey.DIARY_KEY,0)

        val db=MyDBHelper(this)
        var diary=db.getDiary(date)
        binding.apply {
            if(diary.size!=0){
                val imageNum=diary[0].mood
                var emotionDraw: Int?=null
                when(imageNum){
                    //0608추가
                    0 -> emotionDraw = R.drawable.sohappy_0
                    //0608추가
                    1 -> emotionDraw = R.drawable.happy_1
                    2 -> emotionDraw=R.drawable.ok_2
                    3 -> emotionDraw=R.drawable.sad_4
                    4 -> emotionDraw=R.drawable.angry_3

                }
                if(emotionDraw!=null){
                    emotionImage.setImageResource(emotionDraw)
                }
                val dateString= Date(diary[0].date)
                val format = SimpleDateFormat("yyyy년 MM월 dd일")
                val str: String = format.format(dateString)
                diaryDate.text=str
                val imagebyte=diary[0].image
                if (imagebyte != null) {
                    diaryImage.setImageBitmap(
                        BitmapFactory.decodeByteArray(
                            imagebyte,
                            0,
                            imagebyte.size
                        )
                    )
                    diaryImage.visibility= View.VISIBLE
                    diaryImage.clipToOutline=true
                    diaryImage.setAdjustViewBounds(true)
                }
                diaryContent.text=diary[0].content
            }else{
                diaryDate.text="일기가 존재하지 않습니다."
            }
        }


        binding.closeButton.setOnClickListener {
            finish()
        }
    }
}