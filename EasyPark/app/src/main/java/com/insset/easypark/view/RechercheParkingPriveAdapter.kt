package com.insset.easypark.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.insset.easypark.databinding.ItemNoResultBinding
import com.insset.easypark.databinding.ItemParkingPriveBinding
import com.insset.easypark.model.*


class ParkingPriveViewHolder(
    val binding: ItemParkingPriveBinding,
    onNavigationClick : (recyclerViewObject: ParkingRecyclerView) -> Unit,
    onContacClick: (parkingRecyclerView: ParkingRecyclerView) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var parkingPrive: ParkingPrive
    init {

        binding.contactButton.setOnClickListener{
            onContacClick(parkingPrive)
        }

        binding.navigationButton.setOnClickListener{
            onNavigationClick(parkingPrive)
        }

    }
    @SuppressLint("SetTextI18n")
    fun bind(parkingPrive: ParkingPrive) {
        this.parkingPrive = parkingPrive
        binding.textViewAdresse.text = parkingPrive.adresse?.libelle
        binding.textViewDispo.text = if(parkingPrive.disponibilite)"oui" else "non"
        binding.textViewSecurise.text = if(parkingPrive.securise) "oui" else "non"
        binding.textViewInterieur.text = if(parkingPrive.interieur) "oui" else "non"
        binding.textViewTarif.text = parkingPrive.tarif.toString()

    }
}





class RechercheParkingPriveAdapter(
    private val onNavigationClick: (parkingRecyclerView: ParkingRecyclerView) -> Unit,
    private val onContacClick:(parkingRecyclerView: ParkingRecyclerView) -> Unit
) : ListAdapter<ParkingRecyclerView, RecyclerView.ViewHolder>(diffUtils) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            MyItemType.PARKING_PRIVE.type -> {
                ParkingPriveViewHolder(
                    ItemParkingPriveBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), onNavigationClick, onContacClick
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
            MyItemType.PARKING_PRIVE.type -> (holder as ParkingPriveViewHolder).bind(getItem(position) as ParkingPrive)
            else -> (holder as NoResultViewHolder).bind(getItem(position) as ParkingNoResult)
        }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ParkingPrive -> MyItemType.PARKING_PRIVE.type
            else -> MyItemType.NO_RESULT.type
        }
    }
}


