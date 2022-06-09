package com.mobileprograming.moodtracker.ui.diarylist

import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.mobileprograming.moodtracker.R
import com.mobileprograming.moodtracker.data.Diary
import com.mobileprograming.moodtracker.data.MyDBHelper
import com.mobileprograming.moodtracker.databinding.DiarylistRowBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class MyAdapter (val diaryList:MutableList<Diary>)
    :RecyclerView.Adapter<MyAdapter.ViewHolder>(){
        inner class ViewHolder(val binding:DiarylistRowBinding):RecyclerView.ViewHolder(binding.root)
        {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding=DiarylistRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content=diaryList[position].content
        holder.binding.diaryContent.text = content
        val imageNum = diaryList[position].mood
        var emotionDraw: Int? = null
        when (imageNum) {
            //0608추가
            0 -> emotionDraw = R.drawable.sohappy_0
            //0608추가
            1 -> emotionDraw = R.drawable.happy_1
            2 -> emotionDraw = R.drawable.ok_2
            3 -> emotionDraw = R.drawable.sad_4
            4 -> emotionDraw = R.drawable.angry_3
        }
        holder.binding.emotionImage.setImageResource(emotionDraw!!)
        val dateStr= Date(diaryList[position].date)
        val format = SimpleDateFormat("yyyy년 MM월 dd일")
        val str: String = format.format(dateStr)
        holder.binding.diaryDate.text = str
        val imagebyte = diaryList[position].image
        if (imagebyte != null) {
            holder.binding.diaryImage.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    imagebyte,
                    0,
                    imagebyte.size
                )
            )
            holder.binding.diaryImage.visibility = View.VISIBLE
            holder.binding.diaryImage.clipToOutline=true
            holder.binding.diaryImage.setAdjustViewBounds(true);
        }else{
            holder.binding.diaryImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return diaryList.size
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(database:MyDBHelper)
    {
        val localDate = LocalDate.now()
        val y = localDate.year
        val m = localDate.monthValue
        val d = localDate.dayOfMonth
        val sdf = SimpleDateFormat("yyyy.MM.dd")
        val formatStr = y.toString() + "." + m.toString() + "." + d.toString()
        val date = sdf.parse(formatStr)
        val ldate = date.time
        when(diaryList[0].date){
            ldate -> {
                diaryList[0]=database.getDiary(ldate)[0]
                if(diaryList[0].content!=""||diaryList[0].image!=null){
                    notifyItemChanged(0)
                }else{
                    diaryList.removeAt(0)
                    notifyItemRemoved(0)
                }
            }
            else -> {
                val checkDiary=database.getDiary(ldate)[0]
                if(checkDiary.content!=""||checkDiary.image!=null) {
                    diaryList.add(0,checkDiary)
                    notifyItemInserted(0)
                }
            }
        }

    }
}