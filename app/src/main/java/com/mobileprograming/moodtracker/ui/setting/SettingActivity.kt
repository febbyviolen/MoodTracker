package com.mobileprograming.moodtracker.ui.setting

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobileprograming.moodtracker.R
import com.mobileprograming.moodtracker.databinding.ActivitySettingBinding
import java.util.*

class SettingActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initOnOffButton()
        initChangeTimeButton()
        initBackButton()

        val model = fetchDataFromSharedPreferences()
        renderModel(model)
    }

    private fun initBackButton() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.backTextView.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initChangeTimeButton(){
        val selectTimeButton = binding.selectTimeButton
        selectTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()

            TimePickerDialog(this, { picker, hour, minute->
                val model = saveAlarm(hour, minute, binding.switchButton.isChecked)
                renderModel(model)

                if(model.onOff){
                    val c = Calendar.getInstance()
                    c.apply {
                        set(Calendar.HOUR_OF_DAY, model.hour)
                        set(Calendar.MINUTE, model.minute)
                        // 현재 시각보다 이전의 시간이면 다음날로 설정
                        if(before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1)
                        }
                    }
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE,intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,    // 시간 방식
                        calendar.timeInMillis,      // 원하는 시간
                        AlarmManager.INTERVAL_DAY,  // 하루 한번
                        pendingIntent               // pending event 등록
                    )
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true).show()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun renderModel(model: AlarmDisplayModel){
        binding.timeTextView.apply{
            text = model.timeText
        }
        binding.switchButton.apply{
            isChecked = model.onOff
            // 현재 model을 전역변수로 설정하지 않았다 따라서 버튼에 태그를 달아서 오브젝트를 서정, 불러오기 가능
            tag = model
            if(isChecked){
                binding.timeTextView.setTextColor(ContextCompat.getColor(this@SettingActivity,R.color.black))
                binding.timeText.setTextColor(ContextCompat.getColor(this@SettingActivity,R.color.black))
            }else{
                binding.timeTextView.setTextColor(ContextCompat.getColor(this@SettingActivity,R.color.graycc))
                binding.timeText.setTextColor(ContextCompat.getColor(this@SettingActivity,R.color.graycc))
            }
        }
    }

    private fun saveAlarm(hour:Int, minute:Int, onOff:Boolean): AlarmDisplayModel{
        val model = AlarmDisplayModel(
            hour=hour,
            minute=minute,
            onOff=onOff
        )

        // SharedPreferences 사용, Data store은 이번에 사용하지 않음
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        // Scope Function with() 실행, sharedPreferences.edit() 의 함수들을 스코프 내에서 실행
        with(sharedPreferences.edit()){
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ON_OFF_KEY, model.onOff)
            commit()
        }

        return model
    }

    private fun fetchDataFromSharedPreferences(): AlarmDisplayModel {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        // nullable 반환이라 null safe 필요
        val timeValue = sharedPreferences.getString(ALARM_KEY,"09:30") ?: "09:30"
        val onOffValue = sharedPreferences.getBoolean(ON_OFF_KEY, false)
        val alarmData = timeValue.split(':')

        val model = AlarmDisplayModel(alarmData[0].toInt(), alarmData[1].toInt(),onOffValue)

        // 예외처리
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)
        if(pendingIntent == null && model.onOff){
            // pending event는 없는데 알람이 켜져있는 경우 알람 끄기
            model.onOff = false
        }
        else if( pendingIntent != null && model.onOff.not()){
            // pending event는 있는데 알람이 꺼저있는 경우 pending event 끄기
            pendingIntent.cancel()
        }
        return model
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun initOnOffButton() {
        val onOffButton = binding.switchButton
        onOffButton.setOnClickListener {
            val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
            val newModel = saveAlarm(model.hour, model.minute, binding.switchButton.isChecked)
            renderModel(newModel)
            if(newModel.onOff){
                // 알람이 켜진 경우 -> 알람을 등록
                binding.timeTextView.setTextColor(ContextCompat.getColor(this,R.color.black))
                binding.timeText.setTextColor(ContextCompat.getColor(this,R.color.black))
                val calendar = Calendar.getInstance().apply{
                    set(Calendar.HOUR_OF_DAY, newModel.hour)
                    set(Calendar.MINUTE, newModel.minute)
                    // 현재 시각보다 이전의 시간이면 다음날로 설정
                    if(before(Calendar.getInstance())){
                        add(Calendar.DATE, 1)
                    }
                }
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE,intent, PendingIntent.FLAG_UPDATE_CURRENT)

                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,    // 시간 방식
                    calendar.timeInMillis,      // 원하는 시간
                    AlarmManager.INTERVAL_DAY,  // 하루 한번
                    pendingIntent               // pending event 등록
                )
            }else{
                // 알람이 꺼진 경우 -> 알람 삭제
                cancelAlarm()
                binding.timeTextView.setTextColor(ContextCompat.getColor(this,R.color.graycc))
                binding.timeText.setTextColor(ContextCompat.getColor(this,R.color.graycc))
            }
        }
    }

    private fun cancelAlarm(){
        val pendingIntent = PendingIntent.getBroadcast(this,ALARM_REQUEST_CODE, Intent(this, AlarmReceiver::class.java),
        PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.cancel()
    }

    companion object{
        private const val SHARED_PREFERENCES_NAME = "time"
        private const val ALARM_KEY = "alarm"
        private const val ON_OFF_KEY = "onOff"
        private const val ALARM_REQUEST_CODE = 1000
    }
}