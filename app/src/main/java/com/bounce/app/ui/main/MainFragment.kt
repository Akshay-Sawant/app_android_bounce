package com.bounce.app.ui.main

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bounce.app.R
import com.bounce.app.utils.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import kotlinx.android.synthetic.main.fragment_main.*
import kotlin.math.sqrt

class MainFragment : Fragment(R.layout.fragment_main), SeekBar.OnSeekBarChangeListener {

    private lateinit var mLineChartMain: LineChart

    private lateinit var mSeekBarMain: SeekBar
    private lateinit var mTextViewMainSeekCount: TextView

    private lateinit var mTextViewMainBounceCount: TextView

    private lateinit var mTextViewMainBounceTime: TextView

    //    private var mArrayListOfTime: ArrayList<Double>? = null
    private var mArrayListOfTime: ArrayList<Double>? = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLineChartMain = view.findViewById(R.id.lineChartMain)

        mSeekBarMain = view.findViewById(R.id.seekBarMain)
        mSeekBarMain.setOnSeekBarChangeListener(this)

        mTextViewMainSeekCount = view.findViewById(R.id.textViewMainSeekCount)

        mTextViewMainBounceCount = view.findViewById(R.id.textViewMainBounceCount)

        mTextViewMainBounceTime = view.findViewById(R.id.textViewMainBounceTime)
    }

    override fun onResume() {
        super.onResume()
        onConfigureLineChart()
        onConfigureSeekBar()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        textViewMainSeekCount.text = mSeekBarMain.progress.toString()
        onConfigSeekBarData(mSeekBarMain.progress)
        mLineChartMain.invalidate()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private fun onConfigureLineChart() {
        mLineChartMain.setViewPortOffsets(OFFSETS, OFFSETS, OFFSETS, OFFSETS)
        mLineChartMain.setBackgroundColor(
            resources.getColor(
                R.color.colorPrimaryDark,
                Resources.getSystem().newTheme()
            )
        )
        mLineChartMain.description.isEnabled = false
        mLineChartMain.setTouchEnabled(true)
        mLineChartMain.isDragEnabled = true
        mLineChartMain.setScaleEnabled(true)
        mLineChartMain.setPinchZoom(false)
        mLineChartMain.setDrawGridBackground(false)
        mLineChartMain.maxHighlightDistance = MAX_HIGHLIGHT_DISTANCE

        val x: XAxis = mLineChartMain.xAxis
        x.textColor = Color.BLACK
        x.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        x.setDrawGridLines(false)
        x.axisLineColor = Color.WHITE

        val y: YAxis = mLineChartMain.axisLeft
        y.setLabelCount(LABEL_COUNT, false)
        y.textColor = Color.BLUE
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        y.setDrawGridLines(false)
        y.axisLineColor = Color.WHITE
        mLineChartMain.axisRight.isEnabled = false

        mLineChartMain.legend.isEnabled = false
        mLineChartMain.animateXY(DURATION_X, DURATION_Y)
    }

    private fun onConfigureSeekBar() {
        mSeekBarMain.max = SEEKBAR_LIMIT
        mSeekBarMain.progress = SEEKBAR_PROGRESS
    }

    private fun onConfigSeekBarData(height: Int) {
        val mLineDataSet: LineDataSet
        val mEntryArrayList: ArrayList<Entry> = arrayListOf()
        val mHeightDouble = height.toDouble()
        var mHeightMain = mHeightDouble
        var mHeightMax = mHeightDouble
        var mFreeFall = true
        var mVertical = 0.0
        var mTime = 0.0

        mArrayListOfTime = arrayListOf()

        var mBounceInitial = 0
        var mMaximum = sqrt((2 * mHeightMax * mGrand))
        var mLastTimeCalculation = -sqrt(2 * mHeightDouble / mGrand)

        while (mHeightMax > mStopHeightAt) {
            when {
                mFreeFall -> {
                    val mCalculateHeight: Double =
                        mHeightMain + mVertical * mDataTime - 0.5 * mGrand * mDataTime * mDataTime
                    when {
                        mCalculateHeight < 0 -> {
                            mBounceInitial++
                            mTime = mLastTimeCalculation + 2 * sqrt((2 * mHeightMax / mGrand))
                            mFreeFall = false
                            mLastTimeCalculation = mTime + mTarget
                            mHeightMain = 0.0
                        }
                        else -> {
                            mTime += mDataTime
                            mVertical = (mVertical - mGrand * mDataTime)
                            mHeightMain = mCalculateHeight
                        }
                    }
                }
                else -> {
                    mTime += mTarget
                    mMaximum *= mRightHand
                    mVertical = mMaximum
                    mFreeFall = true
                    mHeightMain = 0.0
                    mHeightMax = 0.5 * mMaximum * mMaximum / mGrand
                }
            }
            mEntryArrayList.add(Entry(mTime.toFloat(), mHeightMain.toFloat()))
            mArrayListOfTime!!.add(mTime)
        }

        when {
            mLineChartMain.data != null &&
                    mLineChartMain.data.dataSetCount > 0 -> {
                mLineDataSet = mLineChartMain.data.getDataSetByIndex(0) as LineDataSet
                mLineDataSet.values = mEntryArrayList
                mTextViewMainBounceCount.text = mBounceInitial.toString()
                mTextViewMainBounceTime.text = mArrayListOfTime?.let { (it.size - 1).toString() }
                mLineChartMain.data.notifyDataChanged()
                mLineChartMain.animateXY(DURATION_X, DURATION_Y)
                mLineChartMain.notifyDataSetChanged()
            }
            else -> {
                mLineDataSet = LineDataSet(mEntryArrayList, getString(R.string.app_name))
                mLineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                mLineDataSet.cubicIntensity = 0.3f
                mLineDataSet.setDrawFilled(true)
                mLineDataSet.setDrawCircles(false)
                mLineDataSet.lineWidth = 2f
                mLineDataSet.circleRadius = 3f
                mLineDataSet.setCircleColor(Color.WHITE)
                mLineDataSet.highLightColor = Color.rgb(244, 117, 117)
                mLineDataSet.color = Color.BLACK
                mLineDataSet.fillColor = Color.WHITE
                mLineDataSet.fillAlpha = 100
                mLineDataSet.setDrawHorizontalHighlightIndicator(false)
                mLineDataSet.fillFormatter =
                    IFillFormatter { _, _ -> mLineChartMain.axisLeft.axisMinimum }

                val mLineData = LineData(mLineDataSet)
                mLineData.setValueTextSize(9f)
                mLineData.setDrawValues(false)
                mTextViewMainBounceCount.text = mBounceInitial.toString()
                mTextViewMainBounceTime.text = mArrayListOfTime?.let { (it.size - 1).toString() }
                mLineChartMain.data = mLineData
                mLineChartMain.invalidate()
            }
        }
    }
}