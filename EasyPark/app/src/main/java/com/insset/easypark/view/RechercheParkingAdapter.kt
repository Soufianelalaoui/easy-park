package com.insset.easypark.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.insset.easypark.databinding.ItemHeaderBinding
import com.insset.easypark.databinding.ItemNoResultBinding
import com.insset.easypark.databinding.ItemParkingBinding
import com.insset.easypark.model.Parking
import com.insset.easypark.model.ParkingHeader
import com.insset.easypark.model.ParkingNoResult
import com.insset.easypark.model.ParkingRecyclerView

enum class MyItemType(val type: Int) {
    PARKING(1),
    HEADER(2),
    NO_RESULT(3),
    PARKING_PRIVE(4)
}


val diffUtils = object : DiffUtil.ItemCallback<ParkingRecyclerView>() {
    override fun areItemsTheSame(oldItem: ParkingRecyclerView, newItem: ParkingRecyclerView): Boolean {
        return oldItem == newItem
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ParkingRecyclerView, newItem: ParkingRecyclerView): Boolean {
        return oldItem == newItem
    }
}

class ParkingViewHolder(
    val binding: ItemParkingBinding,
    onNavigationClick : ( recyclerViewObject: ParkingRecyclerView) -> Unit

) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var parking: Parking
    init {

        binding.floatingActionButton3.setOnClickListener{
            onNavigationClick(parking)
        }
    }
    @SuppressLint("SetTextI18n")
    fun bind(parking: Parking) {
        this.parking = parking
        binding.distanceDestination.text = "${parking.distanceDestination?.toInt().toString()} mètres"
        binding.nomParking.text = parking.nom
        binding.placeDispo.text = parking.placeDispo.toString()
        binding.placeTotal.text = parking.placeTotal.toString()
        binding.bailles.text = "${parking.bailles.toString()} %"
        binding.ville.text = parking.ville

    }
}



class HeaderViewHolder(
    val binding: ItemHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var ui: ParkingHeader

    fun bind(header: ParkingHeader) {
        ui = header
        binding.itemRecyclerViewHeader.text = header.libelle
    }
}

class NoResultViewHolder(
    val binding: ItemNoResultBinding
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var ui: ParkingNoResult

    fun bind(header: ParkingNoResult) {
        ui = header
    }
}

class RechercheParkingAdapter(
    private val onNavigationClick: (parkingRecyclerView: ParkingRecyclerView) -> Unit) : ListAdapter<ParkingRecyclerView, RecyclerView.ViewHolder>(diffUtils) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {


            MyItemType.PARKING.type -> {
                ParkingViewHolder(
                    ItemParkingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), onNavigationClick
                )
            }

            MyItemType.HEADER.type -> {
                HeaderViewHolder(
                    ItemHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                NoResultViewHolder(
                    ItemNoResultBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }






    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder.itemViewType) {
            MyItemType.PARKING.type -> (holder as ParkingViewHolder).bind(getItem(position) as Parking)
            MyItemType.NO_RESULT.type -> (holder as NoResultViewHolder).bind(getItem(position) as ParkingNoResult)
            else -> (holder as HeaderViewHolder).bind(getItem(position) as ParkingHeader)

        }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Parking -> MyItemType.PARKING.type
            is ParkingNoResult -> MyItemType.NO_RESULT.type
            else -> MyItemType.HEADER.type
        }
    }
}


