package net.formula97.andorid.car_kei_bo.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import net.formula97.andorid.car_kei_bo.R
import net.formula97.andorid.car_kei_bo.data.CarMaster

class CarListAdapter(context: Context, private val lineResId: Int, private val carMasterList: List<CarMaster>) : ArrayAdapter<CarMaster>(context, lineResId, carMasterList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val vh: CarListViewHolder

        if (convertView == null) {
            convertView = View.inflate(context, lineResId, null)

            val carName = convertView!!.findViewById<View>(R.id.tv_element_CarName) as TextView
            val fuelMileage = convertView.findViewById<View>(R.id.tv_value_FuelMileage) as TextView
            val fuelMileageUnit = convertView.findViewById<View>(R.id.tv_unit_fuelMileage) as TextView
            val runningCost = convertView.findViewById<View>(R.id.tv_value_RunningCosts) as TextView
            val runningCostUnit = convertView.findViewById<View>(R.id.tv_unit_runningCosts) as TextView

            vh = CarListViewHolder()
            vh.carName = carName
            vh.fuelMileage = fuelMileage
            vh.fuelMileageUnit = fuelMileageUnit
            vh.runningCost = runningCost
            vh.runningCostUnit = runningCostUnit

            convertView.tag = vh
        } else {
            vh = convertView.tag as CarListViewHolder
        }

        val (_, carName, _, currentFuelMileage, currentRunningCost, _, _, _, fuelMileageLabel, runningCostLabel) = carMasterList[position]

        vh.carName!!.text = carName
        vh.fuelMileage!!.text = currentFuelMileage.toString()
        vh.fuelMileageUnit!!.text = fuelMileageLabel
        vh.runningCost!!.text = currentRunningCost.toString()
        vh.runningCostUnit!!.text = runningCostLabel

        return convertView
    }

    internal class CarListViewHolder {
        var carName: TextView? = null
        var fuelMileage: TextView? = null
        var fuelMileageUnit: TextView? = null
        var runningCost: TextView? = null
        var runningCostUnit: TextView? = null
    }
}
